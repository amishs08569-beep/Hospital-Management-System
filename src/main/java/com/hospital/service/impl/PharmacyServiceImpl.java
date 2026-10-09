package com.hospital.service.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.MedicineDao;
import com.hospital.dao.impl.MedicineDaoImpl;
import com.hospital.exception.ConflictException;
import com.hospital.exception.DatabaseException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.exception.ValidationException;
import com.hospital.model.InventoryTransaction;
import com.hospital.model.Medicine;
import com.hospital.model.MedicineBatch;
import com.hospital.security.RolePermission;
import com.hospital.security.SecurityContext;
import com.hospital.service.AuditService;
import com.hospital.service.PharmacyService;
import com.hospital.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PharmacyServiceImpl implements PharmacyService {

    private static final Logger logger = LoggerFactory.getLogger(PharmacyServiceImpl.class);

    private final MedicineDao medicineDao;
    private final AuditService auditService;

    public PharmacyServiceImpl() {
        this.medicineDao = new MedicineDaoImpl();
        this.auditService = new AuditServiceImpl();
    }

    public PharmacyServiceImpl(MedicineDao medicineDao, AuditService auditService) {
        this.medicineDao = medicineDao;
        this.auditService = auditService;
    }

    @Override
    public Medicine addMedicine(Medicine medicine) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_MEDICINES);

        ValidationUtil.requireNonBlank(medicine.getName(), "Medicine Name");
        ValidationUtil.requireNonBlank(medicine.getCategory(), "Category");
        ValidationUtil.requireNonBlank(medicine.getUnit(), "Unit");
        ValidationUtil.requirePositive(medicine.getSellingPrice(), "Selling Price");
        ValidationUtil.requireNonNegative(medicine.getPurchasePrice(), "Purchase Price");

        if (medicine.getCode() != null && !medicine.getCode().trim().isEmpty()) {
            Optional<Medicine> existing = medicineDao.findByCode(medicine.getCode().trim());
            if (existing.isPresent()) {
                throw new ConflictException("Medicine with code " + medicine.getCode() + " already exists.");
            }
        }

        int id = medicineDao.insert(medicine);
        medicine.setMedicineId(id);
        auditService.log("CREATE", "MEDICINE", medicine.getCode(), "Added medicine " + medicine.getName());
        logger.info("Added medicine {} [{}]", medicine.getName(), medicine.getCode());
        return medicine;
    }

    @Override
    public boolean updateMedicine(Medicine medicine) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_MEDICINES);

        ValidationUtil.requireNonBlank(medicine.getName(), "Medicine Name");
        ValidationUtil.requireNonBlank(medicine.getCategory(), "Category");
        ValidationUtil.requirePositive(medicine.getSellingPrice(), "Selling Price");

        medicineDao.findById(medicine.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found"));

        boolean updated = medicineDao.update(medicine);
        if (updated) {
            auditService.log("UPDATE", "MEDICINE", medicine.getCode(), "Updated medicine details for " + medicine.getName());
        }
        return updated;
    }

    @Override
    public boolean setMedicineActive(int medicineId, boolean active) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_MEDICINES);
        Medicine med = medicineDao.findById(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found"));

        boolean updated = medicineDao.setActiveStatus(medicineId, active);
        if (updated) {
            auditService.log("STATUS_CHANGE", "MEDICINE", med.getCode(), "Set active status to " + active);
        }
        return updated;
    }

    @Override
    public Optional<Medicine> getMedicineById(int medicineId) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_PHARMACY);
        return medicineDao.findById(medicineId);
    }

    @Override
    public List<Medicine> listMedicines(String search, String category, boolean lowStockOnly) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_PHARMACY);
        return medicineDao.findAll(search, category, lowStockOnly);
    }

    @Override
    public MedicineBatch addBatch(MedicineBatch batch) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_INVENTORY);

        if (batch.getMedicineId() == null) throw new ValidationException("Medicine selection is required.");
        ValidationUtil.requireNonBlank(batch.getBatchNumber(), "Batch Number");
        if (batch.getExpiryDate() == null) throw new ValidationException("Expiry date is required.");
        if (batch.getExpiryDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Cannot add an already expired medicine batch.");
        }
        if (batch.getQuantity() <= 0) {
            throw new ValidationException("Batch initial quantity must be greater than zero.");
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int batchId = medicineDao.insertBatch(batch, conn);
                batch.setBatchId(batchId);

                // Record inventory receipt transaction
                InventoryTransaction txn = new InventoryTransaction();
                txn.setBatchId(batchId);
                txn.setMedicineId(batch.getMedicineId());
                txn.setTransactionType("PURCHASE");
                txn.setQuantity(batch.getQuantity());
                txn.setUnitPrice(batch.getPurchasePrice());
                txn.setTotalAmount(batch.getPurchasePrice().multiply(BigDecimal.valueOf(batch.getQuantity())));
                txn.setReferenceNumber("BATCH-INIT-" + batch.getBatchNumber());
                txn.setNotes("Initial stock intake for batch " + batch.getBatchNumber());
                txn.setPerformedByUserId(SecurityContext.getCurrentUserId());

                medicineDao.insertTransaction(txn, conn);
                conn.commit();

                auditService.log("ADD_BATCH", "MEDICINE_BATCH", batch.getBatchNumber(),
                        "Added batch with " + batch.getQuantity() + " units, expiry: " + batch.getExpiryDate());
                logger.info("Added batch {} with qty {}", batch.getBatchNumber(), batch.getQuantity());
                return batch;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error adding medicine batch: {}", e.getMessage());
            throw new DatabaseException("Failed to add medicine batch: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MedicineBatch> getBatchesForMedicine(int medicineId) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_PHARMACY);
        return medicineDao.findBatchesByMedicineId(medicineId);
    }

    @Override
    public List<MedicineBatch> getExpiringBatches(int daysThreshold) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_PHARMACY);
        return medicineDao.findExpiringBatches(daysThreshold);
    }

    @Override
    public void dispenseStock(int batchId, int quantity, String referenceNumber, String notes) {
        SecurityContext.requirePermission(RolePermission.Permission.DISPENSE_MEDICINE);

        if (quantity <= 0) {
            throw new ValidationException("Dispense quantity must be positive.");
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                MedicineBatch batch = medicineDao.findBatchById(batchId, conn)
                        .orElseThrow(() -> new ResourceNotFoundException("Medicine batch not found"));

                // Critical rule: prevent dispensing expired medicine
                if (batch.isExpired()) {
                    throw new ConflictException("Safety violation: Cannot dispense expired batch " +
                            batch.getBatchNumber() + " (Expired on " + batch.getExpiryDate() + ")");
                }

                // Critical rule: prevent dispensing more than available stock
                if (quantity > batch.getQuantity()) {
                    throw new ConflictException("Insufficient stock in batch " + batch.getBatchNumber() +
                            ". Available: " + batch.getQuantity() + ", Requested: " + quantity);
                }

                int newQuantity = batch.getQuantity() - quantity;
                medicineDao.updateBatchQuantity(batchId, newQuantity, conn);

                InventoryTransaction txn = new InventoryTransaction();
                txn.setBatchId(batchId);
                txn.setMedicineId(batch.getMedicineId());
                txn.setTransactionType("DISPENSED");
                txn.setQuantity(quantity);
                txn.setUnitPrice(batch.getSellingPrice());
                txn.setTotalAmount(batch.getSellingPrice().multiply(BigDecimal.valueOf(quantity)));
                txn.setReferenceNumber(referenceNumber != null ? referenceNumber : "DISP-" + System.currentTimeMillis() % 10000);
                txn.setNotes(notes != null ? notes : "Dispensed to patient");
                txn.setPerformedByUserId(SecurityContext.getCurrentUserId());

                medicineDao.insertTransaction(txn, conn);
                conn.commit();

                auditService.log("DISPENSE", "INVENTORY", batch.getBatchNumber(),
                        "Dispensed " + quantity + " units of " + batch.getMedicineName() + " (Remaining: " + newQuantity + ")");
                logger.info("Dispensed {} units from batch {}", quantity, batch.getBatchNumber());
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error dispensing stock: {}", e.getMessage());
            throw new DatabaseException("Failed to dispense stock: " + e.getMessage(), e);
        }
    }

    @Override
    public void receiveStock(int batchId, int quantity, String referenceNumber, String notes) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_INVENTORY);

        if (quantity <= 0) {
            throw new ValidationException("Received quantity must be positive.");
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                MedicineBatch batch = medicineDao.findBatchById(batchId, conn)
                        .orElseThrow(() -> new ResourceNotFoundException("Medicine batch not found"));

                int newQuantity = batch.getQuantity() + quantity;
                medicineDao.updateBatchQuantity(batchId, newQuantity, conn);

                InventoryTransaction txn = new InventoryTransaction();
                txn.setBatchId(batchId);
                txn.setMedicineId(batch.getMedicineId());
                txn.setTransactionType("PURCHASE");
                txn.setQuantity(quantity);
                txn.setUnitPrice(batch.getPurchasePrice());
                txn.setTotalAmount(batch.getPurchasePrice().multiply(BigDecimal.valueOf(quantity)));
                txn.setReferenceNumber(referenceNumber != null ? referenceNumber : "RCV-" + System.currentTimeMillis() % 10000);
                txn.setNotes(notes != null ? notes : "Stock received");
                txn.setPerformedByUserId(SecurityContext.getCurrentUserId());

                medicineDao.insertTransaction(txn, conn);
                conn.commit();

                auditService.log("RECEIVE_STOCK", "INVENTORY", batch.getBatchNumber(),
                        "Received " + quantity + " units of " + batch.getMedicineName() + " (Total: " + newQuantity + ")");
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error receiving stock: {}", e.getMessage());
            throw new DatabaseException("Failed to receive stock: " + e.getMessage(), e);
        }
    }

    @Override
    public List<InventoryTransaction> getRecentTransactions(int limit) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_PHARMACY);
        return medicineDao.findRecentTransactions(limit);
    }

    @Override
    public int getLowStockCount() {
        return medicineDao.countLowStock();
    }

    @Override
    public int getNearExpiryCount(int daysThreshold) {
        return medicineDao.countNearExpiry(daysThreshold);
    }
}
