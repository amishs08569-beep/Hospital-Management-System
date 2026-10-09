package com.hospital.dao;

import com.hospital.model.Department;
import java.util.List;
import java.util.Optional;

public interface DepartmentDao {
    List<Department> findAll(boolean activeOnly);
    Optional<Department> findById(int departmentId);
    int insert(Department dept);
    boolean update(Department dept);
}
