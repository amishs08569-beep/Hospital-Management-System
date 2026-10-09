package com.hospital.dao.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.AdmissionDao;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Admission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdmissionDaoImpl implements AdmissionDao {

    private static final Logger logger = LoggerFactory.getLogger(AdmissionDaoImpl.class);

    @Override
    public Optional<Admission> findById(int admissionId) {
        String sql = "SELECT a.*, p.first_name, p.last_name, p.patient_code, " +
                "b.bed_number, w.ward_name, d.full_name AS doctor_name FROM admissions a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN beds b ON a.bed_id = b.bed_id " +
                "JOIN wards w ON b.ward_id = w.ward_id " +
                "JOIN doctors d ON a.attending_doctor_id = d.doctor_id WHERE a.admission_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, admissionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAdmission(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding admission {}: {}", admissionId, e.getMessage());
            throw new DatabaseException("Failed to find admission: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Admission> findActiveByBedId(int bedId, Connection conn) {
        String sql = "SELECT a.*, p.first_name, p.last_name, p.patient_code, " +
                "b.bed_number, w.ward_name, d.full_name AS doctor_name FROM admissions a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN beds b ON a.bed_id = b.bed_id " +
                "JOIN wards w ON b.ward_id = w.ward_id " +
                "JOIN doctors d ON a.attending_doctor_id = d.doctor_id " +
                "WHERE a.bed_id = ? AND a.status = 'ADMITTED'";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, bedId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToAdmission(rs));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding active admission for bed {}: {}", bedId, e.getMessage());
            throw new DatabaseException("Failed to find active admission: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Admission> findAll(String status, String search) {
        List<Admission> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT a.*, p.first_name, p.last_name, p.patient_code, " +
                "b.bed_number, w.ward_name, d.full_name AS doctor_name FROM admissions a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN beds b ON a.bed_id = b.bed_id " +
                "JOIN wards w ON b.ward_id = w.ward_id " +
                "JOIN doctors d ON a.attending_doctor_id = d.doctor_id WHERE 1=1 ");

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND a.status = ? ");
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (a.admission_number LIKE ? OR p.first_name LIKE ? OR p.last_name LIKE ? OR p.patient_code LIKE ?) ");
        }
        sql.append("ORDER BY a.admission_id DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
                ps.setString(idx++, status.trim().toUpperCase());
            }
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim() + "%";
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAdmission(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving admissions: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve admissions: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int insert(Admission admission, Connection conn) {
        if (admission.getAdmissionNumber() == null || admission.getAdmissionNumber().trim().isEmpty()) {
            admission.setAdmissionNumber(generateNextAdmissionNumber());
        }
        String sql = "INSERT INTO admissions (admission_number, patient_id, bed_id, attending_doctor_id, " +
                "admission_date, diagnosis, status, daily_rate) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, admission.getAdmissionNumber());
                ps.setInt(2, admission.getPatientId());
                ps.setInt(3, admission.getBedId());
                ps.setInt(4, admission.getAttendingDoctorId());
                ps.setTimestamp(5, Timestamp.valueOf(admission.getAdmissionDate()));
                ps.setString(6, admission.getDiagnosis());
                ps.setString(7, admission.getStatus());
                ps.setBigDecimal(8, admission.getDailyRate());

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        admission.setAdmissionId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting admission: {}", e.getMessage());
            throw new DatabaseException("Failed to save admission: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return 0;
    }

    @Override
    public boolean discharge(int admissionId, LocalDateTime dischargeDate, String summary, Connection conn) {
        String sql = "UPDATE admissions SET status = 'DISCHARGED', discharge_date = ?, discharge_summary = ? WHERE admission_id = ?";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(dischargeDate));
                ps.setString(2, summary);
                ps.setInt(3, admissionId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            logger.error("Error discharging admission {}: {}", admissionId, e.getMessage());
            throw new DatabaseException("Failed to discharge patient: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public int countActiveAdmissions() {
        String sql = "SELECT COUNT(*) FROM admissions WHERE status = 'ADMITTED'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting active admissions: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public String generateNextAdmissionNumber() {
        String sql = "SELECT COALESCE(MAX(admission_id), 0) + 1 FROM admissions";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("ADM-%d-%03d", LocalDateTime.now().getYear(), rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Error generating admission number: {}", e.getMessage());
        }
        return "ADM-" + System.currentTimeMillis() % 10000;
    }

    private Admission mapResultSetToAdmission(ResultSet rs) throws SQLException {
        Admission a = new Admission();
        a.setAdmissionId(rs.getInt("admission_id"));
        a.setAdmissionNumber(rs.getString("admission_number"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setPatientName(rs.getString("first_name") + " " + rs.getString("last_name"));
        a.setPatientCode(rs.getString("patient_code"));
        a.setBedId(rs.getInt("bed_id"));
        a.setBedNumber(rs.getString("bed_number"));
        a.setWardName(rs.getString("ward_name"));
        a.setAttendingDoctorId(rs.getInt("attending_doctor_id"));
        a.setAttendingDoctorName(rs.getString("doctor_name"));
        Timestamp ad = rs.getTimestamp("admission_date");
        if (ad != null) a.setAdmissionDate(ad.toLocalDateTime());
        Timestamp dd = rs.getTimestamp("discharge_date");
        if (dd != null) a.setDischargeDate(dd.toLocalDateTime());
        a.setDiagnosis(rs.getString("diagnosis"));
        a.setDischargeSummary(rs.getString("discharge_summary"));
        a.setStatus(rs.getString("status"));
        a.setDailyRate(rs.getBigDecimal("daily_rate"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) a.setCreatedAt(cat.toLocalDateTime());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) a.setUpdatedAt(uat.toLocalDateTime());
        return a;
    }
}
