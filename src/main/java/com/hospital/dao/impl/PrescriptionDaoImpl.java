package com.hospital.dao.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.PrescriptionDao;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PrescriptionDaoImpl implements PrescriptionDao {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptionDaoImpl.class);

    @Override
    public Optional<Prescription> findById(int prescriptionId) {
        String sql = "SELECT rx.*, p.first_name, p.last_name, p.patient_code, d.full_name AS doctor_name " +
                "FROM prescriptions rx " +
                "JOIN patients p ON rx.patient_id = p.patient_id " +
                "JOIN doctors d ON rx.doctor_id = d.doctor_id WHERE rx.prescription_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, prescriptionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Prescription p = mapResultSetToPrescription(rs);
                    p.setItems(findItemsByPrescriptionId(prescriptionId));
                    return Optional.of(p);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding prescription {}: {}", prescriptionId, e.getMessage());
            throw new DatabaseException("Failed to find prescription: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Prescription> findByPatientId(int patientId) {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT rx.*, p.first_name, p.last_name, p.patient_code, d.full_name AS doctor_name " +
                "FROM prescriptions rx " +
                "JOIN patients p ON rx.patient_id = p.patient_id " +
                "JOIN doctors d ON rx.doctor_id = d.doctor_id WHERE rx.patient_id = ? ORDER BY rx.prescription_date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Prescription p = mapResultSetToPrescription(rs);
                    p.setItems(findItemsByPrescriptionId(p.getPrescriptionId()));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving prescriptions for patient {}: {}", patientId, e.getMessage());
            throw new DatabaseException("Failed to retrieve prescriptions: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Prescription> findAll(boolean pendingDispenseOnly) {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT rx.*, p.first_name, p.last_name, p.patient_code, d.full_name AS doctor_name " +
                "FROM prescriptions rx " +
                "JOIN patients p ON rx.patient_id = p.patient_id " +
                "JOIN doctors d ON rx.doctor_id = d.doctor_id " +
                (pendingDispenseOnly ? "WHERE rx.is_dispensed = FALSE " : "") +
                "ORDER BY rx.prescription_id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Prescription p = mapResultSetToPrescription(rs);
                p.setItems(findItemsByPrescriptionId(p.getPrescriptionId()));
                list.add(p);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving prescriptions: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve prescriptions: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int insert(Prescription prescription, Connection conn) {
        if (prescription.getPrescriptionNumber() == null || prescription.getPrescriptionNumber().trim().isEmpty()) {
            prescription.setPrescriptionNumber(generateNextPrescriptionNumber());
        }
        String sql = "INSERT INTO prescriptions (prescription_number, patient_id, doctor_id, record_id, " +
                "prescription_date, diagnosis, notes, is_dispensed) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, prescription.getPrescriptionNumber());
                ps.setInt(2, prescription.getPatientId());
                ps.setInt(3, prescription.getDoctorId());
                if (prescription.getRecordId() != null) ps.setInt(4, prescription.getRecordId()); else ps.setNull(4, Types.INTEGER);
                ps.setDate(5, Date.valueOf(prescription.getPrescriptionDate()));
                ps.setString(6, prescription.getDiagnosis());
                ps.setString(7, prescription.getNotes());
                ps.setBoolean(8, prescription.isDispensed());

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        prescription.setPrescriptionId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting prescription: {}", e.getMessage());
            throw new DatabaseException("Failed to save prescription: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return 0;
    }

    @Override
    public int insertItem(PrescriptionItem item, Connection conn) {
        String sql = "INSERT INTO prescription_items (prescription_id, medicine_id, medicine_name, dosage, frequency, duration, quantity, instructions) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, item.getPrescriptionId());
                if (item.getMedicineId() != null) ps.setInt(2, item.getMedicineId()); else ps.setNull(2, Types.INTEGER);
                ps.setString(3, item.getMedicineName());
                ps.setString(4, item.getDosage());
                ps.setString(5, item.getFrequency());
                ps.setString(6, item.getDuration());
                ps.setInt(7, item.getQuantity());
                ps.setString(8, item.getInstructions());

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        item.setItemId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting prescription item: {}", e.getMessage());
            throw new DatabaseException("Failed to save prescription item: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return 0;
    }

    @Override
    public List<PrescriptionItem> findItemsByPrescriptionId(int prescriptionId) {
        List<PrescriptionItem> items = new ArrayList<>();
        String sql = "SELECT * FROM prescription_items WHERE prescription_id = ? ORDER BY item_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, prescriptionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PrescriptionItem item = new PrescriptionItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setPrescriptionId(rs.getInt("prescription_id"));
                    int mid = rs.getInt("medicine_id");
                    if (!rs.wasNull()) item.setMedicineId(mid);
                    item.setMedicineName(rs.getString("medicine_name"));
                    item.setDosage(rs.getString("dosage"));
                    item.setFrequency(rs.getString("frequency"));
                    item.setDuration(rs.getString("duration"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setInstructions(rs.getString("instructions"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding prescription items {}: {}", prescriptionId, e.getMessage());
            throw new DatabaseException("Failed to find prescription items: " + e.getMessage(), e);
        }
        return items;
    }

    @Override
    public boolean markDispensed(int prescriptionId, Connection conn) {
        String sql = "UPDATE prescriptions SET is_dispensed = TRUE WHERE prescription_id = ?";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, prescriptionId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            logger.error("Error marking prescription {} dispensed: {}", prescriptionId, e.getMessage());
            throw new DatabaseException("Failed to update prescription status: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public String generateNextPrescriptionNumber() {
        String sql = "SELECT COALESCE(MAX(prescription_id), 0) + 1 FROM prescriptions";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("RX-%d-%03d", LocalDate.now().getYear(), rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Error generating prescription number: {}", e.getMessage());
        }
        return "RX-" + System.currentTimeMillis() % 10000;
    }

    private Prescription mapResultSetToPrescription(ResultSet rs) throws SQLException {
        Prescription p = new Prescription();
        p.setPrescriptionId(rs.getInt("prescription_id"));
        p.setPrescriptionNumber(rs.getString("prescription_number"));
        p.setPatientId(rs.getInt("patient_id"));
        p.setPatientName(rs.getString("first_name") + " " + rs.getString("last_name"));
        p.setPatientCode(rs.getString("patient_code"));
        p.setDoctorId(rs.getInt("doctor_id"));
        p.setDoctorName(rs.getString("doctor_name"));
        int rid = rs.getInt("record_id");
        if (!rs.wasNull()) p.setRecordId(rid);
        Date pd = rs.getDate("prescription_date");
        if (pd != null) p.setPrescriptionDate(pd.toLocalDate());
        p.setDiagnosis(rs.getString("diagnosis"));
        p.setNotes(rs.getString("notes"));
        p.setDispensed(rs.getBoolean("is_dispensed"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) p.setCreatedAt(cat.toLocalDateTime());
        return p;
    }
}
