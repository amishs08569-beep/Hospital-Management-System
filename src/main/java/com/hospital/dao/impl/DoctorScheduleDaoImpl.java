package com.hospital.dao.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.DoctorScheduleDao;
import com.hospital.exception.DatabaseException;
import com.hospital.model.DoctorSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorScheduleDaoImpl implements DoctorScheduleDao {

    private static final Logger logger = LoggerFactory.getLogger(DoctorScheduleDaoImpl.class);

    @Override
    public List<DoctorSchedule> findByDoctorId(int doctorId) {
        List<DoctorSchedule> list = new ArrayList<>();
        String sql = "SELECT s.*, d.full_name AS doctor_name FROM doctor_schedules s " +
                "JOIN doctors d ON s.doctor_id = d.doctor_id WHERE s.doctor_id = ? AND s.is_active = TRUE ORDER BY s.schedule_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DoctorSchedule ds = new DoctorSchedule();
                    ds.setScheduleId(rs.getInt("schedule_id"));
                    ds.setDoctorId(rs.getInt("doctor_id"));
                    ds.setDoctorName(rs.getString("doctor_name"));
                    ds.setDayOfWeek(rs.getString("day_of_week"));
                    Time st = rs.getTime("start_time");
                    if (st != null) ds.setStartTime(st.toLocalTime());
                    Time et = rs.getTime("end_time");
                    if (et != null) ds.setEndTime(et.toLocalTime());
                    ds.setSlotDurationMinutes(rs.getInt("slot_duration_minutes"));
                    ds.setMaxPatients(rs.getInt("max_patients"));
                    ds.setActive(rs.getBoolean("is_active"));
                    list.add(ds);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving schedules for doctor {}: {}", doctorId, e.getMessage());
            throw new DatabaseException("Failed to retrieve schedules: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int insert(DoctorSchedule schedule) {
        String sql = "INSERT INTO doctor_schedules (doctor_id, day_of_week, start_time, end_time, slot_duration_minutes, max_patients, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, schedule.getDoctorId());
            ps.setString(2, schedule.getDayOfWeek().toUpperCase());
            ps.setTime(3, Time.valueOf(schedule.getStartTime()));
            ps.setTime(4, Time.valueOf(schedule.getEndTime()));
            ps.setInt(5, schedule.getSlotDurationMinutes());
            ps.setInt(6, schedule.getMaxPatients());
            ps.setBoolean(7, schedule.isActive());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    schedule.setScheduleId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting schedule: {}", e.getMessage());
            throw new DatabaseException("Failed to insert schedule: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean delete(int scheduleId) {
        String sql = "DELETE FROM doctor_schedules WHERE schedule_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, scheduleId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting schedule {}: {}", scheduleId, e.getMessage());
            throw new DatabaseException("Failed to delete schedule: " + e.getMessage(), e);
        }
    }
}
