package com.hospital.dao;

import com.hospital.model.MedicalRecord;
import java.util.List;
import java.util.Optional;

public interface MedicalRecordDao {
    Optional<MedicalRecord> findById(int recordId);
    List<MedicalRecord> findByPatientId(int patientId);
    int insert(MedicalRecord record);
    boolean update(MedicalRecord record);
}
