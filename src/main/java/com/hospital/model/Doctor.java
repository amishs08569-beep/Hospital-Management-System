package com.hospital.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Doctor {
    private Integer doctorId;
    private String employeeId;
    private Integer userId;
    private String fullName;
    private Integer departmentId;
    private String departmentName;
    private String specialization;
    private String qualification;
    private String contactNumber;
    private String email;
    private BigDecimal consultationFee = new BigDecimal("50.00");
    private String status = "ACTIVE"; // ACTIVE, ON_LEAVE, INACTIVE
    private LocalDateTime createdAt;

    public Doctor() {}

    public Doctor(Integer doctorId, String employeeId, String fullName, Integer departmentId,
                  String departmentName, String specialization, String qualification,
                  String contactNumber, String email, BigDecimal consultationFee, String status) {
        this.doctorId = doctorId;
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.specialization = specialization;
        this.qualification = qualification;
        this.contactNumber = contactNumber;
        this.email = email;
        this.consultationFee = consultationFee;
        this.status = status;
    }

    public Integer getDoctorId() { return doctorId; }
    public void setDoctorId(Integer doctorId) { this.doctorId = doctorId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDoctorCode() { return employeeId; }
    public void setDoctorCode(String code) { this.employeeId = code; }

    public String getEmploymentStatus() { return status; }
    public void setEmploymentStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return fullName + (specialization != null ? " (" + specialization + ")" : "");
    }
}
