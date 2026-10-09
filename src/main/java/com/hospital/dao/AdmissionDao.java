package com.hospital.dao;

import com.hospital.model.Admission;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AdmissionDao {
    Optional<Admission> findById(int admissionId);
    Optional<Admission> findActiveByBedId(int bedId, Connection conn);
    List<Admission> findAll(String status, String search);
    int insert(Admission admission, Connection conn);
    boolean discharge(int admissionId, LocalDateTime dischargeDate, String summary, Connection conn);
    int countActiveAdmissions();
    String generateNextAdmissionNumber();
}
