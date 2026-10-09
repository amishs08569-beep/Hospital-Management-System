package com.hospital.dao.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.AppointmentDao;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppointmentDaoImpl implements AppointmentDao {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentDaoImpl.class);

    @Override
    public Optional<Appointment> findById(int appointmentId) {
        String sql = "SELECT a.*, p.first_name, p.last_name, p.patient_code, p.contact_number AS patient_phone, " +
                "d.full_name AS doctor_name, dept.name AS department_name FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                "JOIN departments dept ON d.department_id = dept.department_id WHERE a.appointment_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding appointment {}: {}", appointmentId, e.getMessage());
            throw new DatabaseException("Failed to find appointment: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Appointment> findAll(LocalDate date, Integer doctorId, Integer patientId, String status) {
        List<Appointment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT a.*, p.first_name, p.last_name, p.patient_code, p.contact_number AS patient_phone, " +
                "d.full_name AS doctor_name, dept.name AS department_name FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                "JOIN departments dept ON d.department_id = dept.department_id WHERE 1=1 ");

        if (date != null) sql.append("AND a.appointment_date = ? ");
        if (doctorId != null) sql.append("AND a.doctor_id = ? ");
        if (patientId != null) sql.append("AND a.patient_id = ? ");
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND a.status = ? ");
        }
        sql.append("ORDER BY a.appointment_date DESC, a.appointment_time ASC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (date != null) ps.setDate(idx++, Date.valueOf(date));
            if (doctorId != null) ps.setInt(idx++, doctorId);
            if (patientId != null) ps.setInt(idx++, patientId);
            if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
                ps.setString(idx++, status.trim().toUpperCase());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding appointments: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve appointments: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Appointment> findConflicting(int doctorId, LocalDate date, LocalTime startTime, LocalTime endTime, Integer excludeAppointmentId, Connection conn) {
        List<Appointment> conflicts = new ArrayList<>();
        String sql = "SELECT a.*, p.first_name, p.last_name, p.patient_code, p.contact_number AS patient_phone, " +
                "d.full_name AS doctor_name, dept.name AS department_name FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                "JOIN departments dept ON d.department_id = dept.department_id " +
                "WHERE a.doctor_id = ? AND a.appointment_date = ? AND a.status != 'CANCELLED' " +
                "AND (a.appointment_time < ? AND a.end_time > ?) " +
                (excludeAppointmentId != null ? "AND a.appointment_id != ? " : "");

        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, doctorId);
                ps.setDate(2, Date.valueOf(date));
                ps.setTime(3, Time.valueOf(endTime));
                ps.setTime(4, Time.valueOf(startTime));
                if (excludeAppointmentId != null) {
                    ps.setInt(5, excludeAppointmentId);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        conflicts.add(mapResultSetToAppointment(rs));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking conflicting appointments: {}", e.getMessage());
            throw new DatabaseException("Failed to check conflicting appointments: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return conflicts;
    }

    @Override
    public int insert(Appointment appointment, Connection conn) {
        if (appointment.getAppointmentNumber() == null || appointment.getAppointmentNumber().trim().isEmpty()) {
            appointment.setAppointmentNumber(generateNextAppointmentNumber());
        }
        String sql = "INSERT INTO appointments (appointment_number, patient_id, doctor_id, appointment_date, " +
                "appointment_time, end_time, status, reason, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, appointment.getAppointmentNumber());
                ps.setInt(2, appointment.getPatientId());
                ps.setInt(3, appointment.getDoctorId());
                ps.setDate(4, Date.valueOf(appointment.getAppointmentDate()));
                ps.setTime(5, Time.valueOf(appointment.getAppointmentTime()));
                ps.setTime(6, Time.valueOf(appointment.getEndTime()));
                ps.setString(7, appointment.getStatus());
                ps.setString(8, appointment.getReason());
                ps.setString(9, appointment.getNotes());

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        appointment.setAppointmentId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting appointment: {}", e.getMessage());
            throw new DatabaseException("Failed to save appointment: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return 0;
    }

    @Override
    public boolean updateStatus(int appointmentId, String status) {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating appointment status {}: {}", appointmentId, e.getMessage());
            throw new DatabaseException("Failed to update appointment status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean reschedule(int appointmentId, LocalDate date, LocalTime startTime, LocalTime endTime, Connection conn) {
        String sql = "UPDATE appointments SET appointment_date = ?, appointment_time = ?, end_time = ?, status = 'SCHEDULED' " +
                "WHERE appointment_id = ?";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, Date.valueOf(date));
                ps.setTime(2, Time.valueOf(startTime));
                ps.setTime(3, Time.valueOf(endTime));
                ps.setInt(4, appointmentId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            logger.error("Error rescheduling appointment {}: {}", appointmentId, e.getMessage());
            throw new DatabaseException("Failed to reschedule appointment: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public int countTodayAppointments() {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = CURDATE() AND status != 'CANCELLED'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting today's appointments: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public String generateNextAppointmentNumber() {
        String sql = "SELECT COALESCE(MAX(appointment_id), 0) + 1 FROM appointments";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("APT-%d-%03d", LocalDate.now().getYear(), rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Error generating appointment number: {}", e.getMessage());
        }
        return "APT-" + System.currentTimeMillis() % 10000;
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setAppointmentId(rs.getInt("appointment_id"));
        a.setAppointmentNumber(rs.getString("appointment_number"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setPatientName(rs.getString("first_name") + " " + rs.getString("last_name"));
        a.setPatientCode(rs.getString("patient_code"));
        a.setPatientPhone(rs.getString("patient_phone"));
        a.setDoctorId(rs.getInt("doctor_id"));
        a.setDoctorName(rs.getString("doctor_name"));
        a.setDepartmentName(rs.getString("department_name"));
        Date ad = rs.getDate("appointment_date");
        if (ad != null) a.setAppointmentDate(ad.toLocalDate());
        Time at = rs.getTime("appointment_time");
        if (at != null) a.setAppointmentTime(at.toLocalTime());
        Time et = rs.getTime("end_time");
        if (et != null) a.setEndTime(et.toLocalTime());
        a.setStatus(rs.getString("status"));
        a.setReason(rs.getString("reason"));
        a.setNotes(rs.getString("notes"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) a.setCreatedAt(cat.toLocalDateTime());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) a.setUpdatedAt(uat.toLocalDateTime());
        return a;
    }
}
