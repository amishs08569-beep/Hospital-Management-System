package com.hospital.dao.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.MedicalRecordDao;
import com.hospital.exception.DatabaseException;
import com.hospital.model.MedicalRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MedicalRecordDaoImpl implements MedicalRecordDao {

    private static final Logger logger = LoggerFactory.getLogger(MedicalRecordDaoImpl.class);

    @Override
    public Optional<MedicalRecord> findById(int recordId) {
        String sql = "SELECT m.*, p.first_name, p.last_name, p.patient_code, d.full_name AS doctor_name " +
                "FROM medical_records m " +
                "JOIN patients p ON m.patient_id = p.patient_id " +
                "JOIN doctors d ON m.doctor_id = d.doctor_id WHERE m.record_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRecord(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding medical record {}: {}", recordId, e.getMessage());
            throw new DatabaseException("Failed to find medical record: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<MedicalRecord> findByPatientId(int patientId) {
        List<MedicalRecord> list = new ArrayList<>();
        String sql = "SELECT m.*, p.first_name, p.last_name, p.patient_code, d.full_name AS doctor_name " +
                "FROM medical_records m " +
                "JOIN patients p ON m.patient_id = p.patient_id " +
                "JOIN doctors d ON m.doctor_id = d.doctor_id WHERE m.patient_id = ? ORDER BY m.visit_date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRecord(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving medical records for patient {}: {}", patientId, e.getMessage());
            throw new DatabaseException("Failed to retrieve medical records: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int insert(MedicalRecord record) {
        String sql = "INSERT INTO medical_records (patient_id, doctor_id, visit_date, symptoms, clinical_observations, " +
                "diagnosis, treatment_plan, follow_up_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, record.getPatientId());
            ps.setInt(2, record.getDoctorId());
            ps.setTimestamp(3, Timestamp.valueOf(record.getVisitDate()));
            ps.setString(4, record.getSymptoms());
            ps.setString(5, record.getClinicalObservations());
            ps.setString(6, record.getDiagnosis());
            ps.setString(7, record.getTreatmentPlan());
            if (record.getFollowUpDate() != null) ps.setDate(8, Date.valueOf(record.getFollowUpDate())); else ps.setNull(8, Types.DATE);

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    record.setRecordId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting medical record: {}", e.getMessage());
            throw new DatabaseException("Failed to save medical record: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean update(MedicalRecord record) {
        String sql = "UPDATE medical_records SET symptoms = ?, clinical_observations = ?, diagnosis = ?, " +
                "treatment_plan = ?, follow_up_date = ? WHERE record_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, record.getSymptoms());
            ps.setString(2, record.getClinicalObservations());
            ps.setString(3, record.getDiagnosis());
            ps.setString(4, record.getTreatmentPlan());
            if (record.getFollowUpDate() != null) ps.setDate(5, Date.valueOf(record.getFollowUpDate())); else ps.setNull(5, Types.DATE);
            ps.setInt(6, record.getRecordId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating medical record {}: {}", record.getRecordId(), e.getMessage());
            throw new DatabaseException("Failed to update medical record: " + e.getMessage(), e);
        }
    }

    private MedicalRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        MedicalRecord m = new MedicalRecord();
        m.setRecordId(rs.getInt("record_id"));
        m.setPatientId(rs.getInt("patient_id"));
        m.setPatientName(rs.getString("first_name") + " " + rs.getString("last_name"));
        m.setPatientCode(rs.getString("patient_code"));
        m.setDoctorId(rs.getInt("doctor_id"));
        m.setDoctorName(rs.getString("doctor_name"));
        Timestamp vd = rs.getTimestamp("visit_date");
        if (vd != null) m.setVisitDate(vd.toLocalDateTime());
        m.setSymptoms(rs.getString("symptoms"));
        m.setClinicalObservations(rs.getString("clinical_observations"));
        m.setDiagnosis(rs.getString("diagnosis"));
        m.setTreatmentPlan(rs.getString("treatment_plan"));
        Date fud = rs.getDate("follow_up_date");
        if (fud != null) m.setFollowUpDate(fud.toLocalDate());
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) m.setCreatedAt(cat.toLocalDateTime());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) m.setUpdatedAt(uat.toLocalDateTime());
        return m;
    }
}
