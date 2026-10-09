package com.hospital.service;

import com.hospital.model.Department;
import com.hospital.model.Doctor;
import com.hospital.model.DoctorSchedule;
import com.hospital.model.Staff;

import java.util.List;
import java.util.Optional;

public interface DoctorStaffService {
    // Doctors
    Doctor registerDoctor(Doctor doctor);
    boolean updateDoctor(Doctor doctor);
    boolean setDoctorStatus(int doctorId, String status);
    Optional<Doctor> getDoctorById(int doctorId);
    List<Doctor> listDoctors(String search, Integer departmentId, boolean activeOnly);
    int getTotalDoctorCount();

    // Schedules
    List<DoctorSchedule> getDoctorSchedules(int doctorId);
    DoctorSchedule addDoctorSchedule(DoctorSchedule schedule);
    boolean removeDoctorSchedule(int scheduleId);

    // Staff
    Staff registerStaff(Staff staff);
    boolean updateStaff(Staff staff);
    boolean setStaffStatus(int staffId, String status);
    Optional<Staff> getStaffById(int staffId);
    List<Staff> listStaff(String search, Integer departmentId);
    int getTotalStaffCount();

    // Departments
    List<Department> listDepartments(boolean activeOnly);
    Department createDepartment(Department department);
    boolean updateDepartment(Department department);
}
