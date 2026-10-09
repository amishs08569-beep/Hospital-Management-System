package com.hospital.dao.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.LabDao;
import com.hospital.exception.DatabaseException;
import com.hospital.model.LabOrder;
import com.hospital.model.LabResult;
import com.hospital.model.LabTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LabDaoImpl implements LabDao {

    private static final Logger logger = LoggerFactory.getLogger(LabDaoImpl.class);

    @Override
    public List<LabTest> findAllTests(boolean activeOnly) {
        List<LabTest> list = new ArrayList<>();
        String sql = "SELECT * FROM lab_tests " + (activeOnly ? "WHERE is_active = TRUE " : "") + "ORDER BY category, test_name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToTest(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving lab tests: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve lab tests: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Optional<LabTest> findTestById(int testId) {
        String sql = "SELECT * FROM lab_tests WHERE test_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, testId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTest(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding test {}: {}", testId, e.getMessage());
            throw new DatabaseException("Failed to find test: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public int insertTest(LabTest test) {
        String sql = "INSERT INTO lab_tests (test_code, test_name, category, price, normal_range, unit, description, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, test.getTestCode());
            ps.setString(2, test.getTestName());
            ps.setString(3, test.getCategory());
            ps.setBigDecimal(4, test.getPrice());
            ps.setString(5, test.getNormalRange());
            ps.setString(6, test.getUnit());
            ps.setString(7, test.getDescription());
            ps.setBoolean(8, test.isActive());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    test.setTestId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting test: {}", e.getMessage());
            throw new DatabaseException("Failed to save lab test: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean updateTest(LabTest test) {
        String sql = "UPDATE lab_tests SET test_name = ?, category = ?, price = ?, normal_range = ?, unit = ?, " +
                "description = ?, is_active = ? WHERE test_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, test.getTestName());
            ps.setString(2, test.getCategory());
            ps.setBigDecimal(3, test.getPrice());
            ps.setString(4, test.getNormalRange());
            ps.setString(5, test.getUnit());
            ps.setString(6, test.getDescription());
            ps.setBoolean(7, test.isActive());
            ps.setInt(8, test.getTestId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating test {}: {}", test.getTestId(), e.getMessage());
            throw new DatabaseException("Failed to update test: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LabOrder> findAllOrders(String status, Integer patientId) {
        List<LabOrder> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT o.*, p.first_name, p.last_name, p.patient_code, d.full_name AS doctor_name " +
                "FROM lab_orders o " +
                "JOIN patients p ON o.patient_id = p.patient_id " +
                "JOIN doctors d ON o.doctor_id = d.doctor_id WHERE 1=1 ");

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND o.status = ? ");
        }
        if (patientId != null) {
            sql.append("AND o.patient_id = ? ");
        }
        sql.append("ORDER BY o.order_id DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
                ps.setString(idx++, status.trim().toUpperCase());
            }
            if (patientId != null) {
                ps.setInt(idx++, patientId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LabOrder order = mapResultSetToOrder(rs);
                    order.setResults(findResultsByOrderId(order.getOrderId()));
                    list.add(order);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding lab orders: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve lab orders: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Optional<LabOrder> findOrderById(int orderId) {
        String sql = "SELECT o.*, p.first_name, p.last_name, p.patient_code, d.full_name AS doctor_name " +
                "FROM lab_orders o " +
                "JOIN patients p ON o.patient_id = p.patient_id " +
                "JOIN doctors d ON o.doctor_id = d.doctor_id WHERE o.order_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LabOrder order = mapResultSetToOrder(rs);
                    order.setResults(findResultsByOrderId(orderId));
                    return Optional.of(order);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding lab order {}: {}", orderId, e.getMessage());
            throw new DatabaseException("Failed to find lab order: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public int insertOrder(LabOrder order, List<Integer> testIds, Connection conn) {
        if (order.getOrderNumber() == null || order.getOrderNumber().trim().isEmpty()) {
            order.setOrderNumber(generateNextOrderNumber());
        }
        String sql = "INSERT INTO lab_orders (order_number, patient_id, doctor_id, order_date, status, priority, clinical_notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, order.getOrderNumber());
                ps.setInt(2, order.getPatientId());
                ps.setInt(3, order.getDoctorId());
                ps.setTimestamp(4, Timestamp.valueOf(order.getOrderDate() != null ? order.getOrderDate() : LocalDateTime.now()));
                ps.setString(5, order.getStatus());
                ps.setString(6, order.getPriority());
                ps.setString(7, order.getClinicalNotes());

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int orderId = rs.getInt(1);
                        order.setOrderId(orderId);

                        // If test IDs were provided, pre-create empty result records ready for entry
                        if (testIds != null && !testIds.isEmpty()) {
                            String resSql = "INSERT INTO lab_results (order_id, test_id, result_value, reference_range, unit, is_abnormal, remarks, result_date) " +
                                    "SELECT ?, t.test_id, 'Pending', t.normal_range, t.unit, FALSE, '', NOW() FROM lab_tests t WHERE t.test_id = ?";
                            try (PreparedStatement resPs = conn.prepareStatement(resSql)) {
                                for (int tid : testIds) {
                                    resPs.setInt(1, orderId);
                                    resPs.setInt(2, tid);
                                    resPs.addBatch();
                                }
                                resPs.executeBatch();
                            }
                        }
                        return orderId;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating lab order: {}", e.getMessage());
            throw new DatabaseException("Failed to save lab order: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return 0;
    }

    @Override
    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE lab_orders SET status = ? WHERE order_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating lab order {} status: {}", orderId, e.getMessage());
            throw new DatabaseException("Failed to update lab order status: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LabResult> findResultsByOrderId(int orderId) {
        List<LabResult> list = new ArrayList<>();
        String sql = "SELECT r.*, t.test_name, t.test_code, u.username AS performed_by_username " +
                "FROM lab_results r " +
                "JOIN lab_tests t ON r.test_id = t.test_id " +
                "LEFT JOIN users u ON r.performed_by_user_id = u.user_id " +
                "WHERE r.order_id = ? ORDER BY r.result_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LabResult r = new LabResult();
                    r.setResultId(rs.getInt("result_id"));
                    r.setOrderId(rs.getInt("order_id"));
                    r.setTestId(rs.getInt("test_id"));
                    r.setTestName(rs.getString("test_name"));
                    r.setTestCode(rs.getString("test_code"));
                    r.setResultValue(rs.getString("result_value"));
                    r.setReferenceRange(rs.getString("reference_range"));
                    r.setUnit(rs.getString("unit"));
                    r.setAbnormal(rs.getBoolean("is_abnormal"));
                    r.setRemarks(rs.getString("remarks"));
                    int uid = rs.getInt("performed_by_user_id");
                    if (!rs.wasNull()) r.setPerformedByUserId(uid);
                    r.setPerformedByUsername(rs.getString("performed_by_username"));
                    Timestamp rd = rs.getTimestamp("result_date");
                    if (rd != null) r.setResultDate(rd.toLocalDateTime());
                    Timestamp cat = rs.getTimestamp("created_at");
                    if (cat != null) r.setCreatedAt(cat.toLocalDateTime());
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding results for order {}: {}", orderId, e.getMessage());
            throw new DatabaseException("Failed to find lab results: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int insertResult(LabResult result, Connection conn) {
        String sql = "INSERT INTO lab_results (order_id, test_id, result_value, reference_range, unit, is_abnormal, remarks, performed_by_user_id, result_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, result.getOrderId());
                ps.setInt(2, result.getTestId());
                ps.setString(3, result.getResultValue());
                ps.setString(4, result.getReferenceRange());
                ps.setString(5, result.getUnit());
                ps.setBoolean(6, result.isAbnormal());
                ps.setString(7, result.getRemarks());
                if (result.getPerformedByUserId() != null) ps.setInt(8, result.getPerformedByUserId()); else ps.setNull(8, Types.INTEGER);
                ps.setTimestamp(9, Timestamp.valueOf(result.getResultDate() != null ? result.getResultDate() : LocalDateTime.now()));

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        result.setResultId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting lab result: {}", e.getMessage());
            throw new DatabaseException("Failed to record lab result: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return 0;
    }

    @Override
    public boolean updateResult(LabResult result, Connection conn) {
        String sql = "UPDATE lab_results SET result_value = ?, reference_range = ?, unit = ?, is_abnormal = ?, " +
                "remarks = ?, performed_by_user_id = ?, result_date = ? WHERE result_id = ?";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, result.getResultValue());
                ps.setString(2, result.getReferenceRange());
                ps.setString(3, result.getUnit());
                ps.setBoolean(4, result.isAbnormal());
                ps.setString(5, result.getRemarks());
                if (result.getPerformedByUserId() != null) ps.setInt(6, result.getPerformedByUserId()); else ps.setNull(6, Types.INTEGER);
                ps.setTimestamp(7, Timestamp.valueOf(result.getResultDate() != null ? result.getResultDate() : LocalDateTime.now()));
                ps.setInt(8, result.getResultId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            logger.error("Error updating lab result {}: {}", result.getResultId(), e.getMessage());
            throw new DatabaseException("Failed to update lab result: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public int countPendingOrders() {
        String sql = "SELECT COUNT(*) FROM lab_orders WHERE status IN ('REQUESTED', 'SAMPLE_COLLECTED', 'IN_PROGRESS')";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting pending orders: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public String generateNextOrderNumber() {
        String sql = "SELECT COALESCE(MAX(order_id), 0) + 1 FROM lab_orders";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("LBO-%d-%03d", LocalDateTime.now().getYear(), rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Error generating order number: {}", e.getMessage());
        }
        return "LBO-" + System.currentTimeMillis() % 10000;
    }

    private LabTest mapResultSetToTest(ResultSet rs) throws SQLException {
        LabTest t = new LabTest();
        t.setTestId(rs.getInt("test_id"));
        t.setTestCode(rs.getString("test_code"));
        t.setTestName(rs.getString("test_name"));
        t.setCategory(rs.getString("category"));
        t.setPrice(rs.getBigDecimal("price"));
        t.setNormalRange(rs.getString("normal_range"));
        t.setUnit(rs.getString("unit"));
        t.setDescription(rs.getString("description"));
        t.setActive(rs.getBoolean("is_active"));
        return t;
    }

    private LabOrder mapResultSetToOrder(ResultSet rs) throws SQLException {
        LabOrder o = new LabOrder();
        o.setOrderId(rs.getInt("order_id"));
        o.setOrderNumber(rs.getString("order_number"));
        o.setPatientId(rs.getInt("patient_id"));
        o.setPatientName(rs.getString("first_name") + " " + rs.getString("last_name"));
        o.setPatientCode(rs.getString("patient_code"));
        o.setDoctorId(rs.getInt("doctor_id"));
        o.setDoctorName(rs.getString("doctor_name"));
        Timestamp od = rs.getTimestamp("order_date");
        if (od != null) o.setOrderDate(od.toLocalDateTime());
        o.setStatus(rs.getString("status"));
        o.setPriority(rs.getString("priority"));
        o.setClinicalNotes(rs.getString("clinical_notes"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) o.setCreatedAt(cat.toLocalDateTime());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) o.setUpdatedAt(uat.toLocalDateTime());
        return o;
    }
}
