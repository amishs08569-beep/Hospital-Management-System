package com.hospital.dao.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.PatientDao;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientDaoImpl implements PatientDao {

    private static final Logger logger = LoggerFactory.getLogger(PatientDaoImpl.class);

    @Override
    public Optional<Patient> findById(int patientId) {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding patient by ID {}: {}", patientId, e.getMessage());
            throw new DatabaseException("Failed to find patient: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Patient> findByUserId(int userId) {
        String sql = "SELECT * FROM patients WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding patient by user ID {}: {}", userId, e.getMessage());
            throw new DatabaseException("Failed to find patient by user ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Patient> findByCode(String code) {
        String sql = "SELECT * FROM patients WHERE UPPER(patient_code) = UPPER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding patient by code {}: {}", code, e.getMessage());
            throw new DatabaseException("Failed to find patient by code: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Patient> findByPhone(String phone) {
        String sql = "SELECT * FROM patients WHERE contact_number = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding patient by phone {}: {}", phone, e.getMessage());
            throw new DatabaseException("Failed to find patient by phone: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Patient> findAll(String search, boolean activeOnly) {
        List<Patient> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM patients WHERE 1=1 ");
        if (activeOnly) {
            sql.append("AND is_active = TRUE ");
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (patient_code LIKE ? OR first_name LIKE ? OR last_name LIKE ? OR contact_number LIKE ?) ");
        }
        sql.append("ORDER BY patient_id DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim() + "%";
                ps.setString(1, pattern);
                ps.setString(2, pattern);
                ps.setString(3, pattern);
                ps.setString(4, pattern);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding patients: {}", e.getMessage());
            throw new DatabaseException("Failed to find patients: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int insert(Patient patient) {
        if (patient.getPatientCode() == null || patient.getPatientCode().trim().isEmpty()) {
            patient.setPatientCode(generateNextCode());
        }
        String sql = "INSERT INTO patients (patient_code, first_name, last_name, date_of_birth, gender, " +
                "contact_number, email, address, emergency_contact_name, emergency_contact_phone, blood_group, " +
                "allergies, medical_history, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, patient.getPatientCode());
            ps.setString(2, patient.getFirstName());
            ps.setString(3, patient.getLastName());
            ps.setDate(4, Date.valueOf(patient.getDateOfBirth()));
            ps.setString(5, patient.getGender());
            ps.setString(6, patient.getContactNumber());
            ps.setString(7, patient.getEmail());
            ps.setString(8, patient.getAddress());
            ps.setString(9, patient.getEmergencyContactName());
            ps.setString(10, patient.getEmergencyContactPhone());
            ps.setString(11, patient.getBloodGroup());
            ps.setString(12, patient.getAllergies());
            ps.setString(13, patient.getMedicalHistory());
            ps.setBoolean(14, patient.isActive());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    patient.setPatientId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting patient: {}", e.getMessage());
            throw new DatabaseException("Failed to register patient: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean update(Patient patient) {
        String sql = "UPDATE patients SET first_name = ?, last_name = ?, date_of_birth = ?, gender = ?, " +
                "contact_number = ?, email = ?, address = ?, emergency_contact_name = ?, emergency_contact_phone = ?, " +
                "blood_group = ?, allergies = ?, medical_history = ?, is_active = ? WHERE patient_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patient.getFirstName());
            ps.setString(2, patient.getLastName());
            ps.setDate(3, Date.valueOf(patient.getDateOfBirth()));
            ps.setString(4, patient.getGender());
            ps.setString(5, patient.getContactNumber());
            ps.setString(6, patient.getEmail());
            ps.setString(7, patient.getAddress());
            ps.setString(8, patient.getEmergencyContactName());
            ps.setString(9, patient.getEmergencyContactPhone());
            ps.setString(10, patient.getBloodGroup());
            ps.setString(11, patient.getAllergies());
            ps.setString(12, patient.getMedicalHistory());
            ps.setBoolean(13, patient.isActive());
            ps.setInt(14, patient.getPatientId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating patient {}: {}", patient.getPatientId(), e.getMessage());
            throw new DatabaseException("Failed to update patient: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean setActiveStatus(int patientId, boolean active) {
        String sql = "UPDATE patients SET is_active = ? WHERE patient_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, patientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating patient status {}: {}", patientId, e.getMessage());
            throw new DatabaseException("Failed to update patient status: " + e.getMessage(), e);
        }
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM patients WHERE is_active = TRUE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting patients: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public String generateNextCode() {
        String sql = "SELECT COALESCE(MAX(patient_id), 0) + 1 FROM patients";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("PAT-%04d", rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Error generating patient code: {}", e.getMessage());
        }
        return "PAT-" + System.currentTimeMillis() % 10000;
    }

    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("patient_id"));
        p.setPatientCode(rs.getString("patient_code"));
        p.setFirstName(rs.getString("first_name"));
        p.setLastName(rs.getString("last_name"));
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) p.setDateOfBirth(dob.toLocalDate());
        p.setGender(rs.getString("gender"));
        p.setContactNumber(rs.getString("contact_number"));
        p.setEmail(rs.getString("email"));
        p.setAddress(rs.getString("address"));
        p.setEmergencyContactName(rs.getString("emergency_contact_name"));
        p.setEmergencyContactPhone(rs.getString("emergency_contact_phone"));
        p.setBloodGroup(rs.getString("blood_group"));
        p.setAllergies(rs.getString("allergies"));
        p.setMedicalHistory(rs.getString("medical_history"));
        p.setActive(rs.getBoolean("is_active"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) p.setCreatedAt(cat.toLocalDateTime());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) p.setUpdatedAt(uat.toLocalDateTime());
        return p;
    }
}
