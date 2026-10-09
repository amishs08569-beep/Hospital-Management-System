package com.hospital.service;

import com.hospital.model.Patient;
import java.util.List;
import java.util.Optional;

public interface PatientService {
    Patient registerPatient(Patient patient);
    boolean updatePatient(Patient patient);
    boolean deactivatePatient(int patientId);
    boolean activatePatient(int patientId);
    Optional<Patient> getPatientById(int patientId);
    Optional<Patient> getPatientByCode(String code);
    List<Patient> searchPatients(String query, boolean activeOnly);
    int getTotalPatientCount();
}
