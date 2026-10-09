package com.hospital.service;

import com.hospital.config.DatabaseConfig;
import com.hospital.model.Patient;
import com.hospital.service.impl.PatientServiceImpl;
import com.hospital.util.DbInitializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PatientServiceTest {

    private static PatientService patientService;

    @BeforeAll
    public static void setup() {
        DatabaseConfig.initializeH2();
        DbInitializer.initializeDatabase();
        patientService = new PatientServiceImpl();
    }

    @Test
    public void testRegisterAndRetrievePatient() {
        Patient p = new Patient();
        p.setFullName("Test Patient JUnit");
        p.setDateOfBirth(LocalDate.of(1995, 3, 20));
        p.setGender("Female");
        p.setContactNumber("555-987-6543");
        p.setEmail("junit@test.com");
        p.setBloodGroup("A+");
        p.setAddress("123 Test Street");
        p.setAllergies("None");

        Patient registered = patientService.registerPatient(p);
        assertNotNull(registered.getPatientId());
        assertNotNull(registered.getPatientCode());
        assertTrue(registered.getPatientCode().startsWith("PAT-"));

        // Search for registered patient
        List<Patient> found = patientService.searchPatients("JUnit", true);
        assertFalse(found.isEmpty());
        assertEquals("Test Patient JUnit", found.get(0).getFullName());

        // Deactivate patient
        boolean deactivated = patientService.deactivatePatient(registered.getPatientId());
        assertTrue(deactivated);

        // Reactivate patient
        boolean reactivated = patientService.activatePatient(registered.getPatientId());
        assertTrue(reactivated);
    }
}
