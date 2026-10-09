package com.hospital.dao;

import com.hospital.model.Patient;
import java.util.List;
import java.util.Optional;

public interface PatientDao {
    Optional<Patient> findById(int patientId);
    Optional<Patient> findByCode(String code);
    Optional<Patient> findByPhone(String phone);
    List<Patient> findAll(String search, boolean activeOnly);
    int insert(Patient patient);
    boolean update(Patient patient);
    boolean setActiveStatus(int patientId, boolean active);
    int countAll();
    String generateNextCode();
}
