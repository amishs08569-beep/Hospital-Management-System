package com.hospital.dao;

import com.hospital.model.Appointment;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentDao {
    Optional<Appointment> findById(int appointmentId);
    List<Appointment> findAll(LocalDate date, Integer doctorId, Integer patientId, String status);
    List<Appointment> findConflicting(int doctorId, LocalDate date, LocalTime startTime, LocalTime endTime, Integer excludeAppointmentId, Connection conn);
    int insert(Appointment appointment, Connection conn);
    boolean updateStatus(int appointmentId, String status);
    boolean reschedule(int appointmentId, LocalDate date, LocalTime startTime, LocalTime endTime, Connection conn);
    int countTodayAppointments();
    String generateNextAppointmentNumber();
}
