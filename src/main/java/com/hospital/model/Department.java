package com.hospital.model;

public class Department {

    private Integer departmentId;
    private String departmentName;
    private String description;
    private String status = "ACTIVE";
    private boolean active = true;

    // Default constructor
    public Department() {
    }

    // Parameterized constructor (String status)
    public Department(
            Integer departmentId,
            String departmentName,
            String description,
            String status) {

        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.description = description;
        this.status = status;
        this.active = "ACTIVE".equalsIgnoreCase(status);
    }

    // Parameterized constructor (boolean active)
    public Department(
            Integer departmentId,
            String name,
            String description,
            boolean active) {

        this.departmentId = departmentId;
        this.departmentName = name;
        this.description = description;
        this.active = active;
        this.status = active ? "ACTIVE" : "INACTIVE";
    }

    // Getters and setters
    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    // Alias for getName / setName
    public String getName() {
        return departmentName;
    }

    public void setName(String name) {
        this.departmentName = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.active = "ACTIVE".equalsIgnoreCase(status);
    }

    // Alias for isActive / setActive
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.status = active ? "ACTIVE" : "INACTIVE";
    }

    @Override
    public String toString() {
        return departmentName;
    }
}
