package com.hospital.dao;

import com.hospital.model.Doctor;
import java.util.List;
import java.util.Optional;

public interface DoctorDao {
    Optional<Doctor> findById(int doctorId);
    Optional<Doctor> findByUserId(int userId);
    Optional<Doctor> findByEmployeeId(String employeeId);
    List<Doctor> findAll(String search, Integer departmentId, boolean activeOnly);
    int insert(Doctor doctor);
    boolean update(Doctor doctor);
    boolean setStatus(int doctorId, String status);
    int countAll();
    String generateNextEmployeeId();
}
