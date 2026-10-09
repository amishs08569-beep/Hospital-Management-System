package com.hospital.service.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.AdmissionDao;
import com.hospital.dao.DoctorDao;
import com.hospital.dao.PatientDao;
import com.hospital.dao.WardBedDao;
import com.hospital.dao.impl.AdmissionDaoImpl;
import com.hospital.dao.impl.DoctorDaoImpl;
import com.hospital.dao.impl.PatientDaoImpl;
import com.hospital.dao.impl.WardBedDaoImpl;
import com.hospital.exception.ConflictException;
import com.hospital.exception.DatabaseException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.exception.ValidationException;
import com.hospital.model.Admission;
import com.hospital.model.Bed;
import com.hospital.model.Ward;
import com.hospital.security.RolePermission;
import com.hospital.security.SecurityContext;
import com.hospital.service.AdmissionService;
import com.hospital.service.AuditService;
import com.hospital.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AdmissionServiceImpl implements AdmissionService {

    private static final Logger logger = LoggerFactory.getLogger(AdmissionServiceImpl.class);

    private final AdmissionDao admissionDao;
    private final WardBedDao wardBedDao;
    private final PatientDao patientDao;
    private final DoctorDao doctorDao;
    private final AuditService auditService;

    public AdmissionServiceImpl() {
        this.admissionDao = new AdmissionDaoImpl();
        this.wardBedDao = new WardBedDaoImpl();
        this.patientDao = new PatientDaoImpl();
        this.doctorDao = new DoctorDaoImpl();
        this.auditService = new AuditServiceImpl();
    }

    public AdmissionServiceImpl(AdmissionDao admissionDao, WardBedDao wardBedDao, PatientDao patientDao,
                                DoctorDao doctorDao, AuditService auditService) {
        this.admissionDao = admissionDao;
        this.wardBedDao = wardBedDao;
        this.patientDao = patientDao;
        this.doctorDao = doctorDao;
        this.auditService = auditService;
    }

    @Override
    public Admission admitPatient(Admission admission) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_ADMISSIONS);

        if (admission.getPatientId() == null) throw new ValidationException("Patient is required.");
        if (admission.getBedId() == null) throw new ValidationException("Bed allocation is required.");
        if (admission.getAttendingDoctorId() == null) throw new ValidationException("Attending doctor is required.");
        ValidationUtil.requireNonBlank(admission.getDiagnosis(), "Provisional Diagnosis");

        if (admission.getAdmissionDate() == null) {
            admission.setAdmissionDate(LocalDateTime.now());
        }

        patientDao.findById(admission.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        doctorDao.findById(admission.getAttendingDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        // Transactional bed check and allocation
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Bed bed = wardBedDao.findBedById(admission.getBedId())
                        .orElseThrow(() -> new ResourceNotFoundException("Selected bed not found"));

                if (bed.isOccupied()) {
                    throw new ConflictException("Bed " + bed.getBedNumber() + " is currently occupied and cannot be allocated.");
                }

                Optional<Admission> activeOnBed = admissionDao.findActiveByBedId(admission.getBedId(), conn);
                if (activeOnBed.isPresent()) {
                    throw new ConflictException("Bed " + bed.getBedNumber() + " already has an active patient admission: " + activeOnBed.get().getAdmissionNumber());
                }

                // Look up ward daily rate if dailyRate is zero
                if (admission.getDailyRate() == null || admission.getDailyRate().doubleValue() == 0.0) {
                    wardBedDao.findWardById(bed.getWardId()).ifPresent(w -> admission.setDailyRate(w.getDailyRate()));
                }

                int admId = admissionDao.insert(admission, conn);
                admission.setAdmissionId(admId);

                // Set bed status to occupied
                wardBedDao.setBedOccupied(admission.getBedId(), true, conn);

                conn.commit();

                auditService.log("ADMISSION", "ADMISSION", admission.getAdmissionNumber(),
                        "Admitted patient ID " + admission.getPatientId() + " to Bed " + bed.getBedNumber());
                logger.info("Successfully registered admission {}", admission.getAdmissionNumber());
                return admission;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Database error during admission: {}", e.getMessage());
            throw new DatabaseException("Failed to register admission: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean dischargePatient(int admissionId, LocalDateTime dischargeDate, String dischargeSummary) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_ADMISSIONS);

        ValidationUtil.requireNonBlank(dischargeSummary, "Discharge Summary");
        if (dischargeDate == null) dischargeDate = LocalDateTime.now();

        Admission adm = admissionDao.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission record not found"));

        if (!"ADMITTED".equalsIgnoreCase(adm.getStatus())) {
            throw new ValidationException("Patient is already discharged.");
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean discharged = admissionDao.discharge(admissionId, dischargeDate, dischargeSummary, conn);

                // Free up bed
                wardBedDao.setBedOccupied(adm.getBedId(), false, conn);

                conn.commit();

                if (discharged) {
                    auditService.log("DISCHARGE", "ADMISSION", adm.getAdmissionNumber(),
                            "Discharged patient " + adm.getPatientName() + " from Bed " + adm.getBedNumber());
                }
                return discharged;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Database error during discharge: {}", e.getMessage());
            throw new DatabaseException("Failed to discharge patient: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Admission> getAdmissionById(int admissionId) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_ADMISSIONS);
        return admissionDao.findById(admissionId);
    }

    @Override
    public List<Admission> listAdmissions(String status, String search) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_ADMISSIONS);
        return admissionDao.findAll(status, search);
    }

    @Override
    public List<Ward> listWards() {
        return wardBedDao.findAllWards();
    }

    @Override
    public List<Bed> listAvailableBeds() {
        return wardBedDao.findAvailableBeds();
    }

    @Override
    public List<Bed> listBedsByWard(int wardId) {
        return wardBedDao.findBedsByWardId(wardId);
    }

    @Override
    public int getActiveAdmissionsCount() {
        return admissionDao.countActiveAdmissions();
    }
}
