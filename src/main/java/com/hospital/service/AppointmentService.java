package com.hospital.service;

import com.hospital.model.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentService {
    Appointment bookAppointment(Appointment appointment);
    boolean rescheduleAppointment(int appointmentId, LocalDate newDate, LocalTime newStartTime, LocalTime newEndTime);
    boolean updateStatus(int appointmentId, String newStatus);
    boolean cancelAppointment(int appointmentId, String reason);
    Optional<Appointment> getAppointmentById(int appointmentId);
    List<Appointment> listAppointments(LocalDate date, Integer doctorId, Integer patientId, String status);
    int getTodayAppointmentsCount();
}
