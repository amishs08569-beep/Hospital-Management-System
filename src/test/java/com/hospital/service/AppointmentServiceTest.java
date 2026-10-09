package com.hospital.service;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.ConflictException;
import com.hospital.model.Appointment;
import com.hospital.service.impl.AppointmentServiceImpl;
import com.hospital.util.DbInitializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AppointmentServiceTest {

    private static AppointmentService appointmentService;

    @BeforeAll
    public static void setup() {
        DatabaseConfig.initializeH2();
        DbInitializer.initializeDatabase();
        com.hospital.model.User admin = new com.hospital.model.User();
        admin.setUserId(1);
        admin.setUsername("admin");
        admin.setRoleName("ADMIN");
        com.hospital.security.SecurityContext.setCurrentSession(new com.hospital.security.UserSession(admin));
        appointmentService = new AppointmentServiceImpl();
    }

    @Test
    public void testBookAppointmentAndConflictPrevention() {
        LocalDate testDate = LocalDate.now().plusDays(10);
        LocalTime startTime = LocalTime.of(14, 0);
        LocalTime endTime = LocalTime.of(14, 30);

        Appointment a1 = new Appointment();
        a1.setPatientId(1);
        a1.setDoctorId(1);
        a1.setAppointmentDate(testDate);
        a1.setAppointmentTime(startTime);
        a1.setEndTime(endTime);
        a1.setReason("Chest tightness consultation");
        a1.setStatus("SCHEDULED");

        Appointment booked1 = appointmentService.bookAppointment(a1);
        assertNotNull(booked1.getAppointmentId());
        assertNotNull(booked1.getAppointmentNumber());

        // Attempting to book an overlapping appointment for the same doctor must fail with ConflictException
        Appointment a2 = new Appointment();
        a2.setPatientId(2);
        a2.setDoctorId(1); // Same doctor
        a2.setAppointmentDate(testDate); // Same date
        a2.setAppointmentTime(LocalTime.of(14, 15)); // Overlaps 14:00 - 14:30
        a2.setEndTime(LocalTime.of(14, 45));
        a2.setReason("Second patient conflicting consultation");
        a2.setStatus("SCHEDULED");

        assertThrows(ConflictException.class, () -> {
            appointmentService.bookAppointment(a2);
        });

        // Updating appointment status
        boolean checkedIn = appointmentService.updateStatus(booked1.getAppointmentId(), "CHECKED_IN");
        assertTrue(checkedIn);

        boolean completed = appointmentService.updateStatus(booked1.getAppointmentId(), "COMPLETED");
        assertTrue(completed);
    }
}
