package com.hospital.dao;

import com.hospital.model.InventoryTransaction;
import com.hospital.model.Medicine;
import com.hospital.model.MedicineBatch;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface MedicineDao {
    Optional<Medicine> findById(int medicineId);
    Optional<Medicine> findByCode(String code);
    List<Medicine> findAll(String search, String category, boolean lowStockOnly);
    int insert(Medicine medicine);
    boolean update(Medicine medicine);
    boolean setActiveStatus(int medicineId, boolean active);

    // Batches
    List<MedicineBatch> findBatchesByMedicineId(int medicineId);
    Optional<MedicineBatch> findBatchById(int batchId, Connection conn);
    int insertBatch(MedicineBatch batch, Connection conn);
    boolean updateBatchQuantity(int batchId, int newQuantity, Connection conn);
    List<MedicineBatch> findExpiringBatches(int daysThreshold);

    // Transactions
    int insertTransaction(InventoryTransaction transaction, Connection conn);
    List<InventoryTransaction> findRecentTransactions(int limit);

    int countLowStock();
    int countNearExpiry(int daysThreshold);
    String generateNextMedicineCode();
}
