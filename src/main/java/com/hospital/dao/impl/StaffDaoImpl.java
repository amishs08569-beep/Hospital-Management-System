package com.hospital.dao.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.StaffDao;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Staff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StaffDaoImpl implements StaffDao {

    private static final Logger logger = LoggerFactory.getLogger(StaffDaoImpl.class);

    @Override
    public Optional<Staff> findById(int staffId) {
        String sql = "SELECT s.*, dept.name AS department_name FROM staff s " +
                "LEFT JOIN departments dept ON s.department_id = dept.department_id WHERE s.staff_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStaff(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding staff by ID {}: {}", staffId, e.getMessage());
            throw new DatabaseException("Failed to find staff: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Staff> findByUserId(int userId) {
        String sql = "SELECT s.*, dept.name AS department_name FROM staff s " +
                "LEFT JOIN departments dept ON s.department_id = dept.department_id WHERE s.user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStaff(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding staff by user ID {}: {}", userId, e.getMessage());
            throw new DatabaseException("Failed to find staff: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Staff> findAll(String search, Integer departmentId) {
        List<Staff> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT s.*, dept.name AS department_name FROM staff s " +
                "LEFT JOIN departments dept ON s.department_id = dept.department_id WHERE 1=1 ");

        if (departmentId != null) {
            sql.append("AND s.department_id = ? ");
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (s.full_name LIKE ? OR s.role_title LIKE ? OR s.employee_id LIKE ?) ");
        }
        sql.append("ORDER BY s.full_name ASC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIdx = 1;
            if (departmentId != null) {
                ps.setInt(paramIdx++, departmentId);
            }
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim() + "%";
                ps.setString(paramIdx++, pattern);
                ps.setString(paramIdx++, pattern);
                ps.setString(paramIdx++, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStaff(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving staff: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve staff: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int insert(Staff staff) {
        if (staff.getEmployeeId() == null || staff.getEmployeeId().trim().isEmpty()) {
            staff.setEmployeeId(generateNextEmployeeId());
        }
        String sql = "INSERT INTO staff (employee_id, user_id, full_name, department_id, role_title, " +
                "contact_number, email, status, hire_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, staff.getEmployeeId());
            if (staff.getUserId() != null) ps.setInt(2, staff.getUserId()); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, staff.getFullName());
            if (staff.getDepartmentId() != null) ps.setInt(4, staff.getDepartmentId()); else ps.setNull(4, Types.INTEGER);
            ps.setString(5, staff.getRoleTitle());
            ps.setString(6, staff.getContactNumber());
            ps.setString(7, staff.getEmail());
            ps.setString(8, staff.getStatus());
            ps.setDate(9, staff.getHireDate() != null ? Date.valueOf(staff.getHireDate()) : Date.valueOf(java.time.LocalDate.now()));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    staff.setStaffId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting staff: {}", e.getMessage());
            throw new DatabaseException("Failed to insert staff: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean update(Staff staff) {
        String sql = "UPDATE staff SET full_name = ?, department_id = ?, role_title = ?, " +
                "contact_number = ?, email = ?, status = ?, hire_date = ? WHERE staff_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staff.getFullName());
            if (staff.getDepartmentId() != null) ps.setInt(2, staff.getDepartmentId()); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, staff.getRoleTitle());
            ps.setString(4, staff.getContactNumber());
            ps.setString(5, staff.getEmail());
            ps.setString(6, staff.getStatus());
            ps.setDate(7, staff.getHireDate() != null ? Date.valueOf(staff.getHireDate()) : Date.valueOf(java.time.LocalDate.now()));
            ps.setInt(8, staff.getStaffId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating staff {}: {}", staff.getStaffId(), e.getMessage());
            throw new DatabaseException("Failed to update staff: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean setStatus(int staffId, String status) {
        String sql = "UPDATE staff SET status = ? WHERE staff_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, staffId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating staff status {}: {}", staffId, e.getMessage());
            throw new DatabaseException("Failed to update staff status: " + e.getMessage(), e);
        }
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM staff WHERE status = 'ACTIVE'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting staff: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public String generateNextEmployeeId() {
        String sql = "SELECT COALESCE(MAX(staff_id), 0) + 1 FROM staff";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("STF-%03d", rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Error generating staff ID: {}", e.getMessage());
        }
        return "STF-" + (System.currentTimeMillis() % 1000);
    }

    private Staff mapResultSetToStaff(ResultSet rs) throws SQLException {
        Staff s = new Staff();
        s.setStaffId(rs.getInt("staff_id"));
        s.setEmployeeId(rs.getString("employee_id"));
        int uid = rs.getInt("user_id");
        if (!rs.wasNull()) s.setUserId(uid);
        s.setFullName(rs.getString("full_name"));
        int did = rs.getInt("department_id");
        if (!rs.wasNull()) s.setDepartmentId(did);
        s.setDepartmentName(rs.getString("department_name"));
        s.setRoleTitle(rs.getString("role_title"));
        s.setContactNumber(rs.getString("contact_number"));
        s.setEmail(rs.getString("email"));
        s.setStatus(rs.getString("status"));
        Date hd = rs.getDate("hire_date");
        if (hd != null) s.setHireDate(hd.toLocalDate());
        return s;
    }
}
