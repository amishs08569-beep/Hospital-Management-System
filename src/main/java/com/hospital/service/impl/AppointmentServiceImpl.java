package com.hospital.service.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.AppointmentDao;
import com.hospital.dao.DoctorDao;
import com.hospital.dao.PatientDao;
import com.hospital.dao.impl.AppointmentDaoImpl;
import com.hospital.dao.impl.DoctorDaoImpl;
import com.hospital.dao.impl.PatientDaoImpl;
import com.hospital.exception.ConflictException;
import com.hospital.exception.DatabaseException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.exception.ValidationException;
import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.security.RolePermission;
import com.hospital.security.SecurityContext;
import com.hospital.service.AppointmentService;
import com.hospital.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final AppointmentDao appointmentDao;
    private final DoctorDao doctorDao;
    private final PatientDao patientDao;
    private final AuditService auditService;

    public AppointmentServiceImpl() {
        this.appointmentDao = new AppointmentDaoImpl();
        this.doctorDao = new DoctorDaoImpl();
        this.patientDao = new PatientDaoImpl();
        this.auditService = new AuditServiceImpl();
    }

    public AppointmentServiceImpl(AppointmentDao appointmentDao, DoctorDao doctorDao, PatientDao patientDao, AuditService auditService) {
        this.appointmentDao = appointmentDao;
        this.doctorDao = doctorDao;
        this.patientDao = patientDao;
        this.auditService = auditService;
    }

    @Override
    public Appointment bookAppointment(Appointment appointment) {
        SecurityContext.requirePermission(RolePermission.Permission.BOOK_APPOINTMENT);

        if (appointment.getPatientId() == null) throw new ValidationException("Patient selection is required.");
        if (appointment.getDoctorId() == null) throw new ValidationException("Doctor selection is required.");
        if (appointment.getAppointmentDate() == null) throw new ValidationException("Appointment date is required.");
        if (appointment.getAppointmentTime() == null) throw new ValidationException("Appointment time is required.");

        if (appointment.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Cannot schedule an appointment for a past date.");
        }

        if (appointment.getEndTime() == null) {
            appointment.setEndTime(appointment.getAppointmentTime().plusMinutes(20)); // default 20-min slot
        }

        if (!appointment.getAppointmentTime().isBefore(appointment.getEndTime())) {
            throw new ValidationException("Start time must be before end time.");
        }

        Patient patient = patientDao.findById(appointment.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        if (!patient.isActive()) {
            throw new ValidationException("Cannot book appointment for a deactivated patient record.");
        }

        Doctor doctor = doctorDao.findById(appointment.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        if (!"ACTIVE".equalsIgnoreCase(doctor.getStatus())) {
            throw new ValidationException("Doctor is not currently available for appointments (Status: " + doctor.getStatus() + ")");
        }

        // Perform transactional booking with conflict prevention
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                List<Appointment> conflicts = appointmentDao.findConflicting(
                        appointment.getDoctorId(),
                        appointment.getAppointmentDate(),
                        appointment.getAppointmentTime(),
                        appointment.getEndTime(),
                        null,
                        conn
                );

                if (!conflicts.isEmpty()) {
                    Appointment c = conflicts.get(0);
                    throw new ConflictException("Doctor " + doctor.getFullName() + " already has an appointment booked on " +
                            appointment.getAppointmentDate() + " from " + c.getAppointmentTime() + " to " + c.getEndTime() +
                            " (" + c.getAppointmentNumber() + ")");
                }

                int id = appointmentDao.insert(appointment, conn);
                appointment.setAppointmentId(id);
                conn.commit();

                auditService.log("BOOK", "APPOINTMENT", appointment.getAppointmentNumber(),
                        "Booked appointment for " + patient.getFullName() + " with " + doctor.getFullName() +
                                " on " + appointment.getAppointmentDate() + " " + appointment.getAppointmentTime());
                logger.info("Booked appointment {} successfully", appointment.getAppointmentNumber());
                return appointment;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Transaction failed during appointment booking: {}", e.getMessage());
            throw new DatabaseException("Failed to book appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean rescheduleAppointment(int appointmentId, LocalDate newDate, LocalTime newStartTime, LocalTime newEndTime) {
        SecurityContext.requirePermission(RolePermission.Permission.UPDATE_APPOINTMENT);

        if (newDate.isBefore(LocalDate.now())) {
            throw new ValidationException("Cannot reschedule to a past date.");
        }
        if (newEndTime == null) {
            newEndTime = newStartTime.plusMinutes(20);
        }
        if (!newStartTime.isBefore(newEndTime)) {
            throw new ValidationException("Start time must be before end time.");
        }

        Appointment appt = appointmentDao.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if ("COMPLETED".equalsIgnoreCase(appt.getStatus()) || "CANCELLED".equalsIgnoreCase(appt.getStatus())) {
            throw new ValidationException("Cannot reschedule an appointment with status: " + appt.getStatus());
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                List<Appointment> conflicts = appointmentDao.findConflicting(
                        appt.getDoctorId(), newDate, newStartTime, newEndTime, appointmentId, conn
                );

                if (!conflicts.isEmpty()) {
                    Appointment c = conflicts.get(0);
                    throw new ConflictException("Doctor already has an overlapping appointment on " + newDate +
                            " from " + c.getAppointmentTime() + " to " + c.getEndTime());
                }

                boolean success = appointmentDao.reschedule(appointmentId, newDate, newStartTime, newEndTime, conn);
                conn.commit();

                if (success) {
                    auditService.log("RESCHEDULE", "APPOINTMENT", appt.getAppointmentNumber(),
                            "Rescheduled to " + newDate + " " + newStartTime);
                }
                return success;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error rescheduling appointment {}: {}", appointmentId, e.getMessage());
            throw new DatabaseException("Failed to reschedule appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatus(int appointmentId, String newStatus) {
        SecurityContext.requirePermission(RolePermission.Permission.UPDATE_APPOINTMENT);

        Appointment appt = appointmentDao.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        boolean updated = appointmentDao.updateStatus(appointmentId, newStatus);
        if (updated) {
            auditService.log("STATUS_CHANGE", "APPOINTMENT", appt.getAppointmentNumber(),
                    "Updated appointment status from " + appt.getStatus() + " to " + newStatus);
        }
        return updated;
    }

    @Override
    public boolean cancelAppointment(int appointmentId, String reason) {
        SecurityContext.requirePermission(RolePermission.Permission.CANCEL_APPOINTMENT);

        Appointment appt = appointmentDao.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        boolean updated = appointmentDao.updateStatus(appointmentId, "CANCELLED");
        if (updated) {
            auditService.log("CANCEL", "APPOINTMENT", appt.getAppointmentNumber(),
                    "Cancelled appointment. Reason: " + (reason != null ? reason : "Not specified"));
        }
        return updated;
    }

    @Override
    public Optional<Appointment> getAppointmentById(int appointmentId) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_APPOINTMENTS);
        return appointmentDao.findById(appointmentId);
    }

    @Override
    public List<Appointment> listAppointments(LocalDate date, Integer doctorId, Integer patientId, String status) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_APPOINTMENTS);
        return appointmentDao.findAll(date, doctorId, patientId, status);
    }

    @Override
    public int getTodayAppointmentsCount() {
        return appointmentDao.countTodayAppointments();
    }
}
