package com.hospital.dao;

import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionItem;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface PrescriptionDao {
    Optional<Prescription> findById(int prescriptionId);
    List<Prescription> findByPatientId(int patientId);
    List<Prescription> findAll(boolean pendingDispenseOnly);
    int insert(Prescription prescription, Connection conn);
    int insertItem(PrescriptionItem item, Connection conn);
    List<PrescriptionItem> findItemsByPrescriptionId(int prescriptionId);
    boolean markDispensed(int prescriptionId, Connection conn);
    String generateNextPrescriptionNumber();
}
