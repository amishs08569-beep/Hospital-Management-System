package com.hospital.service.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.DoctorDao;
import com.hospital.dao.PatientDao;
import com.hospital.dao.PrescriptionDao;
import com.hospital.dao.impl.DoctorDaoImpl;
import com.hospital.dao.impl.PatientDaoImpl;
import com.hospital.dao.impl.PrescriptionDaoImpl;
import com.hospital.exception.DatabaseException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.exception.ValidationException;
import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionItem;
import com.hospital.security.RolePermission;
import com.hospital.security.SecurityContext;
import com.hospital.service.AuditService;
import com.hospital.service.PrescriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PrescriptionServiceImpl implements PrescriptionService {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptionServiceImpl.class);

    private final PrescriptionDao prescriptionDao;
    private final PatientDao patientDao;
    private final DoctorDao doctorDao;
    private final AuditService auditService;

    public PrescriptionServiceImpl() {
        this.prescriptionDao = new PrescriptionDaoImpl();
        this.patientDao = new PatientDaoImpl();
        this.doctorDao = new DoctorDaoImpl();
        this.auditService = new AuditServiceImpl();
    }

    public PrescriptionServiceImpl(PrescriptionDao prescriptionDao, PatientDao patientDao, DoctorDao doctorDao, AuditService auditService) {
        this.prescriptionDao = prescriptionDao;
        this.patientDao = patientDao;
        this.doctorDao = doctorDao;
        this.auditService = auditService;
    }

    @Override
    public Prescription createPrescription(Prescription prescription) {
        SecurityContext.requirePermission(RolePermission.Permission.CREATE_PRESCRIPTION);

        if (prescription.getPatientId() == null) throw new ValidationException("Patient is required.");
        if (prescription.getDoctorId() == null) throw new ValidationException("Prescribing doctor is required.");
        if (prescription.getItems() == null || prescription.getItems().isEmpty()) {
            throw new ValidationException("Prescription must contain at least one prescribed medicine.");
        }

        if (prescription.getPrescriptionDate() == null) {
            prescription.setPrescriptionDate(LocalDate.now());
        }

        patientDao.findById(prescription.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        doctorDao.findById(prescription.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        // Save prescription and items in a single transaction
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int rxId = prescriptionDao.insert(prescription, conn);
                prescription.setPrescriptionId(rxId);

                for (PrescriptionItem it : prescription.getItems()) {
                    it.setPrescriptionId(rxId);
                    prescriptionDao.insertItem(it, conn);
                }

                conn.commit();
                auditService.log("CREATE", "PRESCRIPTION", prescription.getPrescriptionNumber(),
                        "Created prescription with " + prescription.getItems().size() + " items for patient ID " + prescription.getPatientId());
                logger.info("Created prescription {}", prescription.getPrescriptionNumber());
                return prescription;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error creating prescription: {}", e.getMessage());
            throw new DatabaseException("Failed to save prescription: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean markPrescriptionDispensed(int prescriptionId) {
        SecurityContext.requirePermission(RolePermission.Permission.DISPENSE_MEDICINE);

        Prescription rx = prescriptionDao.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));

        if (rx.isDispensed()) {
            throw new ValidationException("Prescription has already been marked dispensed.");
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            boolean updated = prescriptionDao.markDispensed(prescriptionId, conn);
            if (updated) {
                auditService.log("DISPENSE", "PRESCRIPTION", rx.getPrescriptionNumber(),
                        "Prescription dispensed by pharmacy");
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Error marking prescription dispensed: {}", e.getMessage());
            throw new DatabaseException("Failed to update prescription status: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Prescription> getPrescriptionById(int prescriptionId) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_PRESCRIPTION);
        return prescriptionDao.findById(prescriptionId);
    }

    @Override
    public List<Prescription> listPrescriptions(boolean pendingDispenseOnly) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_PRESCRIPTION);
        return prescriptionDao.findAll(pendingDispenseOnly);
    }

    @Override
    public List<Prescription> getPatientPrescriptions(int patientId) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_PRESCRIPTION);
        return prescriptionDao.findByPatientId(patientId);
    }
}
