package com.hospital.model;

import java.time.LocalDate;

public class Staff {
    private Integer staffId;
    private String employeeId;
    private Integer userId;
    private String fullName;
    private Integer departmentId;
    private String departmentName;
    private String roleTitle;
    private String contactNumber;
    private String email;
    private String status = "ACTIVE"; // ACTIVE, ON_LEAVE, TERMINATED
    private LocalDate hireDate;

    public Staff() {}

    public Staff(Integer staffId, String employeeId, String fullName, Integer departmentId,
                 String departmentName, String roleTitle, String contactNumber, String email,
                 String status, LocalDate hireDate) {
        this.staffId = staffId;
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.roleTitle = roleTitle;
        this.contactNumber = contactNumber;
        this.email = email;
        this.status = status;
        this.hireDate = hireDate;
    }

    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }

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

    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStaffCode() { return employeeId; }
    public void setStaffCode(String code) { this.employeeId = code; }

    public String getEmploymentStatus() { return status; }
    public void setEmploymentStatus(String status) { this.status = status; }

    private String qualification = "";
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    @Override
    public String toString() {
        return fullName + " (" + roleTitle + ")";
    }
}
