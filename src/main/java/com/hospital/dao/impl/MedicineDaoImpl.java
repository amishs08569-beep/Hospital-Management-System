package com.hospital.dao.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.MedicineDao;
import com.hospital.exception.DatabaseException;
import com.hospital.model.InventoryTransaction;
import com.hospital.model.Medicine;
import com.hospital.model.MedicineBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MedicineDaoImpl implements MedicineDao {

    private static final Logger logger = LoggerFactory.getLogger(MedicineDaoImpl.class);

    @Override
    public Optional<Medicine> findById(int medicineId) {
        String sql = "SELECT m.*, COALESCE((SELECT SUM(quantity) FROM medicine_batches b WHERE b.medicine_id = m.medicine_id), 0) AS total_stock " +
                "FROM medicines m WHERE m.medicine_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMedicine(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding medicine {}: {}", medicineId, e.getMessage());
            throw new DatabaseException("Failed to find medicine: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Medicine> findByCode(String code) {
        String sql = "SELECT m.*, COALESCE((SELECT SUM(quantity) FROM medicine_batches b WHERE b.medicine_id = m.medicine_id), 0) AS total_stock " +
                "FROM medicines m WHERE UPPER(m.code) = UPPER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMedicine(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding medicine by code {}: {}", code, e.getMessage());
            throw new DatabaseException("Failed to find medicine by code: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Medicine> findAll(String search, String category, boolean lowStockOnly) {
        List<Medicine> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT m.*, " +
                "COALESCE((SELECT SUM(quantity) FROM medicine_batches b WHERE b.medicine_id = m.medicine_id), 0) AS total_stock " +
                "FROM medicines m WHERE m.is_active = TRUE ");

        if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
            sql.append("AND m.category = ? ");
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (m.name LIKE ? OR m.generic_name LIKE ? OR m.code LIKE ?) ");
        }
        if (lowStockOnly) {
            sql.append("HAVING total_stock <= m.reorder_level ");
        }
        sql.append("ORDER BY m.name ASC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
                ps.setString(idx++, category.trim());
            }
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim() + "%";
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMedicine(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving medicines: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve medicines: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int insert(Medicine medicine) {
        if (medicine.getCode() == null || medicine.getCode().trim().isEmpty()) {
            medicine.setCode(generateNextMedicineCode());
        }
        String sql = "INSERT INTO medicines (code, name, generic_name, category, manufacturer, unit, purchase_price, selling_price, reorder_level, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, medicine.getCode());
            ps.setString(2, medicine.getName());
            ps.setString(3, medicine.getGenericName());
            ps.setString(4, medicine.getCategory());
            ps.setString(5, medicine.getManufacturer());
            ps.setString(6, medicine.getUnit());
            ps.setBigDecimal(7, medicine.getPurchasePrice());
            ps.setBigDecimal(8, medicine.getSellingPrice());
            ps.setInt(9, medicine.getReorderLevel());
            ps.setBoolean(10, medicine.isActive());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    medicine.setMedicineId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting medicine: {}", e.getMessage());
            throw new DatabaseException("Failed to save medicine: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean update(Medicine medicine) {
        String sql = "UPDATE medicines SET name = ?, generic_name = ?, category = ?, manufacturer = ?, unit = ?, " +
                "purchase_price = ?, selling_price = ?, reorder_level = ?, is_active = ? WHERE medicine_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, medicine.getName());
            ps.setString(2, medicine.getGenericName());
            ps.setString(3, medicine.getCategory());
            ps.setString(4, medicine.getManufacturer());
            ps.setString(5, medicine.getUnit());
            ps.setBigDecimal(6, medicine.getPurchasePrice());
            ps.setBigDecimal(7, medicine.getSellingPrice());
            ps.setInt(8, medicine.getReorderLevel());
            ps.setBoolean(9, medicine.isActive());
            ps.setInt(10, medicine.getMedicineId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating medicine {}: {}", medicine.getMedicineId(), e.getMessage());
            throw new DatabaseException("Failed to update medicine: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean setActiveStatus(int medicineId, boolean active) {
        String sql = "UPDATE medicines SET is_active = ? WHERE medicine_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, medicineId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating medicine status {}: {}", medicineId, e.getMessage());
            throw new DatabaseException("Failed to update medicine status: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MedicineBatch> findBatchesByMedicineId(int medicineId) {
        List<MedicineBatch> list = new ArrayList<>();
        String sql = "SELECT b.*, m.name AS medicine_name, m.code AS medicine_code " +
                "FROM medicine_batches b JOIN medicines m ON b.medicine_id = m.medicine_id " +
                "WHERE b.medicine_id = ? ORDER BY b.expiry_date ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBatch(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding batches for medicine {}: {}", medicineId, e.getMessage());
            throw new DatabaseException("Failed to find medicine batches: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Optional<MedicineBatch> findBatchById(int batchId, Connection conn) {
        String sql = "SELECT b.*, m.name AS medicine_name, m.code AS medicine_code " +
                "FROM medicine_batches b JOIN medicines m ON b.medicine_id = m.medicine_id " +
                "WHERE b.batch_id = ?";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, batchId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToBatch(rs));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding batch {}: {}", batchId, e.getMessage());
            throw new DatabaseException("Failed to find batch: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return Optional.empty();
    }

    @Override
    public int insertBatch(MedicineBatch batch, Connection conn) {
        String sql = "INSERT INTO medicine_batches (medicine_id, batch_number, expiry_date, quantity, purchase_price, selling_price) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, batch.getMedicineId());
                ps.setString(2, batch.getBatchNumber());
                ps.setDate(3, Date.valueOf(batch.getExpiryDate()));
                ps.setInt(4, batch.getQuantity());
                ps.setBigDecimal(5, batch.getPurchasePrice());
                ps.setBigDecimal(6, batch.getSellingPrice());

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        batch.setBatchId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting batch: {}", e.getMessage());
            throw new DatabaseException("Failed to add batch: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return 0;
    }

    @Override
    public boolean updateBatchQuantity(int batchId, int newQuantity, Connection conn) {
        String sql = "UPDATE medicine_batches SET quantity = ? WHERE batch_id = ?";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, newQuantity);
                ps.setInt(2, batchId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            logger.error("Error updating batch {} quantity: {}", batchId, e.getMessage());
            throw new DatabaseException("Failed to update batch quantity: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public List<MedicineBatch> findExpiringBatches(int daysThreshold) {
        List<MedicineBatch> list = new ArrayList<>();
        String sql = "SELECT b.*, m.name AS medicine_name, m.code AS medicine_code " +
                "FROM medicine_batches b JOIN medicines m ON b.medicine_id = m.medicine_id " +
                "WHERE b.quantity > 0 AND b.expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                "ORDER BY b.expiry_date ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, daysThreshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBatch(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding expiring batches: {}", e.getMessage());
            throw new DatabaseException("Failed to find expiring batches: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int insertTransaction(InventoryTransaction transaction, Connection conn) {
        String sql = "INSERT INTO inventory_transactions (batch_id, medicine_id, transaction_type, quantity, unit_price, total_amount, reference_number, notes, performed_by_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, transaction.getBatchId());
                ps.setInt(2, transaction.getMedicineId());
                ps.setString(3, transaction.getTransactionType());
                ps.setInt(4, transaction.getQuantity());
                ps.setBigDecimal(5, transaction.getUnitPrice());
                ps.setBigDecimal(6, transaction.getTotalAmount());
                ps.setString(7, transaction.getReferenceNumber());
                ps.setString(8, transaction.getNotes());
                if (transaction.getPerformedByUserId() != null) ps.setInt(9, transaction.getPerformedByUserId()); else ps.setNull(9, Types.INTEGER);

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        transaction.setTransactionId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting inventory transaction: {}", e.getMessage());
            throw new DatabaseException("Failed to save inventory transaction: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return 0;
    }

    @Override
    public List<InventoryTransaction> findRecentTransactions(int limit) {
        List<InventoryTransaction> list = new ArrayList<>();
        String sql = "SELECT t.*, m.name AS medicine_name, b.batch_number, u.username AS performed_by_username " +
                "FROM inventory_transactions t " +
                "JOIN medicines m ON t.medicine_id = m.medicine_id " +
                "JOIN medicine_batches b ON t.batch_id = b.batch_id " +
                "LEFT JOIN users u ON t.performed_by_user_id = u.user_id " +
                "ORDER BY t.transaction_id DESC LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InventoryTransaction it = new InventoryTransaction();
                    it.setTransactionId(rs.getInt("transaction_id"));
                    it.setBatchId(rs.getInt("batch_id"));
                    it.setMedicineId(rs.getInt("medicine_id"));
                    it.setMedicineName(rs.getString("medicine_name"));
                    it.setBatchNumber(rs.getString("batch_number"));
                    it.setTransactionType(rs.getString("transaction_type"));
                    it.setQuantity(rs.getInt("quantity"));
                    it.setUnitPrice(rs.getBigDecimal("unit_price"));
                    it.setTotalAmount(rs.getBigDecimal("total_amount"));
                    it.setReferenceNumber(rs.getString("reference_number"));
                    it.setNotes(rs.getString("notes"));
                    int uid = rs.getInt("performed_by_user_id");
                    if (!rs.wasNull()) it.setPerformedByUserId(uid);
                    it.setPerformedByUsername(rs.getString("performed_by_username"));
                    Timestamp cat = rs.getTimestamp("created_at");
                    if (cat != null) it.setCreatedAt(cat.toLocalDateTime());
                    list.add(it);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving recent transactions: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve inventory transactions: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int countLowStock() {
        String sql = "SELECT COUNT(*) FROM medicines m " +
                "WHERE m.is_active = TRUE AND " +
                "COALESCE((SELECT SUM(quantity) FROM medicine_batches b WHERE b.medicine_id = m.medicine_id), 0) <= m.reorder_level";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting low stock medicines: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public int countNearExpiry(int daysThreshold) {
        String sql = "SELECT COUNT(*) FROM medicine_batches WHERE quantity > 0 AND expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, daysThreshold);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting near expiry batches: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public String generateNextMedicineCode() {
        String sql = "SELECT COALESCE(MAX(medicine_id), 0) + 1 FROM medicines";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("MED-%03d", rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Error generating medicine code: {}", e.getMessage());
        }
        return "MED-" + System.currentTimeMillis() % 1000;
    }

    private Medicine mapResultSetToMedicine(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setMedicineId(rs.getInt("medicine_id"));
        m.setCode(rs.getString("code"));
        m.setName(rs.getString("name"));
        m.setGenericName(rs.getString("generic_name"));
        m.setCategory(rs.getString("category"));
        m.setManufacturer(rs.getString("manufacturer"));
        m.setUnit(rs.getString("unit"));
        m.setPurchasePrice(rs.getBigDecimal("purchase_price"));
        m.setSellingPrice(rs.getBigDecimal("selling_price"));
        m.setReorderLevel(rs.getInt("reorder_level"));
        m.setTotalStock(rs.getInt("total_stock"));
        m.setActive(rs.getBoolean("is_active"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) m.setCreatedAt(cat.toLocalDateTime());
        return m;
    }

    private MedicineBatch mapResultSetToBatch(ResultSet rs) throws SQLException {
        MedicineBatch b = new MedicineBatch();
        b.setBatchId(rs.getInt("batch_id"));
        b.setMedicineId(rs.getInt("medicine_id"));
        b.setMedicineName(rs.getString("medicine_name"));
        b.setMedicineCode(rs.getString("medicine_code"));
        b.setBatchNumber(rs.getString("batch_number"));
        Date ed = rs.getDate("expiry_date");
        if (ed != null) b.setExpiryDate(ed.toLocalDate());
        b.setQuantity(rs.getInt("quantity"));
        b.setPurchasePrice(rs.getBigDecimal("purchase_price"));
        b.setSellingPrice(rs.getBigDecimal("selling_price"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) b.setCreatedAt(cat.toLocalDateTime());
        return b;
    }
}
