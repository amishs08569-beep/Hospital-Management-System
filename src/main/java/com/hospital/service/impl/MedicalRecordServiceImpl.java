package com.hospital.service.impl;

import com.hospital.dao.DoctorDao;
import com.hospital.dao.MedicalRecordDao;
import com.hospital.dao.PatientDao;
import com.hospital.dao.impl.DoctorDaoImpl;
import com.hospital.dao.impl.MedicalRecordDaoImpl;
import com.hospital.dao.impl.PatientDaoImpl;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.exception.ValidationException;
import com.hospital.model.MedicalRecord;
import com.hospital.security.RolePermission;
import com.hospital.security.SecurityContext;
import com.hospital.service.AuditService;
import com.hospital.service.MedicalRecordService;
import com.hospital.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MedicalRecordServiceImpl implements MedicalRecordService {

    private static final Logger logger = LoggerFactory.getLogger(MedicalRecordServiceImpl.class);

    private final MedicalRecordDao medicalRecordDao;
    private final PatientDao patientDao;
    private final DoctorDao doctorDao;
    private final AuditService auditService;

    public MedicalRecordServiceImpl() {
        this.medicalRecordDao = new MedicalRecordDaoImpl();
        this.patientDao = new PatientDaoImpl();
        this.doctorDao = new DoctorDaoImpl();
        this.auditService = new AuditServiceImpl();
    }

    public MedicalRecordServiceImpl(MedicalRecordDao medicalRecordDao, PatientDao patientDao,
                                    DoctorDao doctorDao, AuditService auditService) {
        this.medicalRecordDao = medicalRecordDao;
        this.patientDao = patientDao;
        this.doctorDao = doctorDao;
        this.auditService = auditService;
    }

    @Override
    public MedicalRecord createMedicalRecord(MedicalRecord record) {
        SecurityContext.requirePermission(RolePermission.Permission.CREATE_MEDICAL_RECORD);

        if (record.getPatientId() == null) throw new ValidationException("Patient is required.");
        if (record.getDoctorId() == null) throw new ValidationException("Doctor is required.");
        ValidationUtil.requireNonBlank(record.getDiagnosis(), "Diagnosis");
        ValidationUtil.requireNonBlank(record.getTreatmentPlan(), "Treatment Plan");

        if (record.getVisitDate() == null) {
            record.setVisitDate(LocalDateTime.now());
        }

        patientDao.findById(record.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        doctorDao.findById(record.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        int id = medicalRecordDao.insert(record);
        record.setRecordId(id);

        auditService.log("CREATE", "MEDICAL_RECORD", String.valueOf(id),
                "Recorded clinical diagnosis for Patient ID " + record.getPatientId() + ": " + record.getDiagnosis());
        logger.info("Saved medical record #{} for patient {}", id, record.getPatientId());
        return record;
    }

    @Override
    public boolean updateMedicalRecord(MedicalRecord record) {
        SecurityContext.requirePermission(RolePermission.Permission.CREATE_MEDICAL_RECORD);
        ValidationUtil.requireNonBlank(record.getDiagnosis(), "Diagnosis");
        ValidationUtil.requireNonBlank(record.getTreatmentPlan(), "Treatment Plan");

        medicalRecordDao.findById(record.getRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));

        boolean updated = medicalRecordDao.update(record);
        if (updated) {
            auditService.log("UPDATE", "MEDICAL_RECORD", String.valueOf(record.getRecordId()),
                    "Updated medical record diagnosis/treatment");
        }
        return updated;
    }

    @Override
    public Optional<MedicalRecord> getRecordById(int recordId) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_MEDICAL_RECORD);
        return medicalRecordDao.findById(recordId);
    }

    @Override
    public List<MedicalRecord> getPatientHistory(int patientId) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_MEDICAL_RECORD);
        return medicalRecordDao.findByPatientId(patientId);
    }
}
