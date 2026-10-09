package com.hospital.service;

import com.hospital.model.Prescription;
import java.util.List;
import java.util.Optional;

public interface PrescriptionService {
    Prescription createPrescription(Prescription prescription);
    boolean markPrescriptionDispensed(int prescriptionId);
    Optional<Prescription> getPrescriptionById(int prescriptionId);
    List<Prescription> listPrescriptions(boolean pendingDispenseOnly);
    List<Prescription> getPatientPrescriptions(int patientId);
}
