package com.hospital.service;

import com.hospital.model.MedicalRecord;
import java.util.List;
import java.util.Optional;

public interface MedicalRecordService {
    MedicalRecord createMedicalRecord(MedicalRecord record);
    boolean updateMedicalRecord(MedicalRecord record);
    Optional<MedicalRecord> getRecordById(int recordId);
    List<MedicalRecord> getPatientHistory(int patientId);
}
