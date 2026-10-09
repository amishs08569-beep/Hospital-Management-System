package com.hospital.dao;

import com.hospital.model.Staff;
import java.util.List;
import java.util.Optional;

public interface StaffDao {
    Optional<Staff> findById(int staffId);
    Optional<Staff> findByUserId(int userId);
    List<Staff> findAll(String search, Integer departmentId);
    int insert(Staff staff);
    boolean update(Staff staff);
    boolean setStatus(int staffId, String status);
    int countAll();
    String generateNextEmployeeId();
}
