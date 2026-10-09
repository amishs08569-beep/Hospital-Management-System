package com.hospital.service;

import com.hospital.model.Admission;
import com.hospital.model.Bed;
import com.hospital.model.Ward;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AdmissionService {
    Admission admitPatient(Admission admission);
    boolean dischargePatient(int admissionId, LocalDateTime dischargeDate, String dischargeSummary);
    Optional<Admission> getAdmissionById(int admissionId);
    List<Admission> listAdmissions(String status, String search);
    List<Ward> listWards();
    List<Bed> listAvailableBeds();
    List<Bed> listBedsByWard(int wardId);
    int getActiveAdmissionsCount();
}
