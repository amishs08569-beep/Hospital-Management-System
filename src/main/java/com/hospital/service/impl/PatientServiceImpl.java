package com.hospital.service.impl;

import com.hospital.dao.PatientDao;
import com.hospital.dao.impl.PatientDaoImpl;
import com.hospital.exception.ConflictException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.model.Patient;
import com.hospital.security.RolePermission;
import com.hospital.security.SecurityContext;
import com.hospital.service.AuditService;
import com.hospital.service.PatientService;
import com.hospital.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PatientServiceImpl implements PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientServiceImpl.class);
    private final PatientDao patientDao;
    private final AuditService auditService;

    public PatientServiceImpl() {
        this.patientDao = new PatientDaoImpl();
        this.auditService = new AuditServiceImpl();
    }

    public PatientServiceImpl(PatientDao patientDao, AuditService auditService) {
        this.patientDao = patientDao;
        this.auditService = auditService;
    }

    @Override
    public Patient registerPatient(Patient patient) {
        SecurityContext.requirePermission(RolePermission.Permission.REGISTER_PATIENT);

        ValidationUtil.requireNonBlank(patient.getFirstName(), "First Name");
        ValidationUtil.requireNonBlank(patient.getLastName(), "Last Name");
        ValidationUtil.requireNonBlank(patient.getGender(), "Gender");
        ValidationUtil.requireNonBlank(patient.getContactNumber(), "Contact Number");
        ValidationUtil.validatePhone(patient.getContactNumber(), "Contact Number");
        ValidationUtil.validateEmail(patient.getEmail(), "Email");

        if (patient.getDateOfBirth() == null) {
            patient.setDateOfBirth(LocalDate.now().minusYears(30)); // Default if unspecified
        } else if (patient.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future.");
        }

        // Check for accidental duplicate phone number among active patients
        Optional<Patient> existing = patientDao.findByPhone(patient.getContactNumber().trim());
        if (existing.isPresent() && existing.get().isActive()) {
            throw new ConflictException("A patient with contact number " + patient.getContactNumber() + " is already registered: " + existing.get().getFullName());
        }

        int id = patientDao.insert(patient);
        patient.setPatientId(id);
        auditService.log("REGISTER", "PATIENT", patient.getPatientCode(), "Registered patient " + patient.getFullName());
        logger.info("Successfully registered patient {} [{}]", patient.getFullName(), patient.getPatientCode());
        return patient;
    }

    @Override
    public boolean updatePatient(Patient patient) {
        SecurityContext.requirePermission(RolePermission.Permission.UPDATE_PATIENT);

        ValidationUtil.requireNonBlank(patient.getFirstName(), "First Name");
        ValidationUtil.requireNonBlank(patient.getLastName(), "Last Name");
        ValidationUtil.requireNonBlank(patient.getGender(), "Gender");
        ValidationUtil.requireNonBlank(patient.getContactNumber(), "Contact Number");
        ValidationUtil.validatePhone(patient.getContactNumber(), "Contact Number");
        ValidationUtil.validateEmail(patient.getEmail(), "Email");

        patientDao.findById(patient.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID " + patient.getPatientId()));

        boolean updated = patientDao.update(patient);
        if (updated) {
            auditService.log("UPDATE", "PATIENT", patient.getPatientCode(), "Updated details for " + patient.getFullName());
        }
        return updated;
    }

    @Override
    public boolean deactivatePatient(int patientId) {
        SecurityContext.requirePermission(RolePermission.Permission.DEACTIVATE_PATIENT);
        Patient p = patientDao.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID " + patientId));

        boolean updated = patientDao.setActiveStatus(patientId, false);
        if (updated) {
            auditService.log("DEACTIVATE", "PATIENT", p.getPatientCode(), "Deactivated patient " + p.getFullName());
        }
        return updated;
    }

    @Override
    public boolean activatePatient(int patientId) {
        SecurityContext.requirePermission(RolePermission.Permission.UPDATE_PATIENT);
        Patient p = patientDao.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID " + patientId));

        boolean updated = patientDao.setActiveStatus(patientId, true);
        if (updated) {
            auditService.log("ACTIVATE", "PATIENT", p.getPatientCode(), "Reactivated patient " + p.getFullName());
        }
        return updated;
    }

    @Override
    public Optional<Patient> getPatientById(int patientId) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_PATIENT);
        return patientDao.findById(patientId);
    }

    @Override
    public Optional<Patient> getPatientByCode(String code) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_PATIENT);
        return patientDao.findByCode(code);
    }

    @Override
    public List<Patient> searchPatients(String query, boolean activeOnly) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_PATIENT);
        return patientDao.findAll(query, activeOnly);
    }

    @Override
    public int getTotalPatientCount() {
        return patientDao.countAll();
    }
}
