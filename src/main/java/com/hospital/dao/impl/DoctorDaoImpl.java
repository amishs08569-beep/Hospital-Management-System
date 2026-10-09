package com.hospital.dao.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.DoctorDao;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Doctor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DoctorDaoImpl implements DoctorDao {

    private static final Logger logger = LoggerFactory.getLogger(DoctorDaoImpl.class);

    @Override
    public Optional<Doctor> findById(int doctorId) {
        String sql = "SELECT d.*, dept.name AS department_name FROM doctors d " +
                "JOIN departments dept ON d.department_id = dept.department_id WHERE d.doctor_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding doctor by ID {}: {}", doctorId, e.getMessage());
            throw new DatabaseException("Failed to find doctor: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Doctor> findByUserId(int userId) {
        String sql = "SELECT d.*, dept.name AS department_name FROM doctors d " +
                "JOIN departments dept ON d.department_id = dept.department_id WHERE d.user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding doctor by user ID {}: {}", userId, e.getMessage());
            throw new DatabaseException("Failed to find doctor: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Doctor> findByEmployeeId(String employeeId) {
        String sql = "SELECT d.*, dept.name AS department_name FROM doctors d " +
                "JOIN departments dept ON d.department_id = dept.department_id WHERE d.employee_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding doctor by employee ID {}: {}", employeeId, e.getMessage());
            throw new DatabaseException("Failed to find doctor: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Doctor> findAll(String search, Integer departmentId, boolean activeOnly) {
        List<Doctor> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT d.*, dept.name AS department_name FROM doctors d " +
                "JOIN departments dept ON d.department_id = dept.department_id WHERE 1=1 ");

        if (activeOnly) {
            sql.append("AND d.status = 'ACTIVE' ");
        }
        if (departmentId != null) {
            sql.append("AND d.department_id = ? ");
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (d.full_name LIKE ? OR d.specialization LIKE ? OR d.employee_id LIKE ?) ");
        }
        sql.append("ORDER BY d.full_name ASC");

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
                    list.add(mapResultSetToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving doctors: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve doctors: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int insert(Doctor doctor) {
        if (doctor.getEmployeeId() == null || doctor.getEmployeeId().trim().isEmpty()) {
            doctor.setEmployeeId(generateNextEmployeeId());
        }
        String sql = "INSERT INTO doctors (employee_id, user_id, full_name, department_id, specialization, " +
                "qualification, contact_number, email, consultation_fee, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, doctor.getEmployeeId());
            if (doctor.getUserId() != null) ps.setInt(2, doctor.getUserId()); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, doctor.getFullName());
            ps.setInt(4, doctor.getDepartmentId());
            ps.setString(5, doctor.getSpecialization());
            ps.setString(6, doctor.getQualification());
            ps.setString(7, doctor.getContactNumber());
            ps.setString(8, doctor.getEmail());
            ps.setBigDecimal(9, doctor.getConsultationFee());
            ps.setString(10, doctor.getStatus());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    doctor.setDoctorId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting doctor: {}", e.getMessage());
            throw new DatabaseException("Failed to insert doctor: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean update(Doctor doctor) {
        String sql = "UPDATE doctors SET full_name = ?, department_id = ?, specialization = ?, " +
                "qualification = ?, contact_number = ?, email = ?, consultation_fee = ?, status = ? WHERE doctor_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctor.getFullName());
            ps.setInt(2, doctor.getDepartmentId());
            ps.setString(3, doctor.getSpecialization());
            ps.setString(4, doctor.getQualification());
            ps.setString(5, doctor.getContactNumber());
            ps.setString(6, doctor.getEmail());
            ps.setBigDecimal(7, doctor.getConsultationFee());
            ps.setString(8, doctor.getStatus());
            ps.setInt(9, doctor.getDoctorId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating doctor {}: {}", doctor.getDoctorId(), e.getMessage());
            throw new DatabaseException("Failed to update doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean setStatus(int doctorId, String status) {
        String sql = "UPDATE doctors SET status = ? WHERE doctor_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, doctorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating doctor status {}: {}", doctorId, e.getMessage());
            throw new DatabaseException("Failed to update doctor status: " + e.getMessage(), e);
        }
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM doctors WHERE status = 'ACTIVE'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting doctors: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public String generateNextEmployeeId() {
        String sql = "SELECT COALESCE(MAX(doctor_id), 0) + 1 FROM doctors";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("DOC-%03d", rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Error generating doctor ID: {}", e.getMessage());
        }
        return "DOC-" + (System.currentTimeMillis() % 1000);
    }

    private Doctor mapResultSetToDoctor(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setDoctorId(rs.getInt("doctor_id"));
        d.setEmployeeId(rs.getString("employee_id"));
        int uid = rs.getInt("user_id");
        if (!rs.wasNull()) d.setUserId(uid);
        d.setFullName(rs.getString("full_name"));
        d.setDepartmentId(rs.getInt("department_id"));
        d.setDepartmentName(rs.getString("department_name"));
        d.setSpecialization(rs.getString("specialization"));
        d.setQualification(rs.getString("qualification"));
        d.setContactNumber(rs.getString("contact_number"));
        d.setEmail(rs.getString("email"));
        d.setConsultationFee(rs.getBigDecimal("consultation_fee"));
        d.setStatus(rs.getString("status"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) d.setCreatedAt(cat.toLocalDateTime());
        return d;
    }
}
