package com.hospital.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Patient {
    private Integer patientId;
    private String patientCode;
    private Integer userId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender; // Male, Female, Other
    private String contactNumber;
    private String email;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String bloodGroup; // A+, A-, B+, B-, O+, O-, AB+, AB-
    private String allergies;
    private String medicalHistory;
    private boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Patient() {}

    public Patient(Integer patientId, String patientCode, String firstName, String lastName,
                   LocalDate dateOfBirth, String gender, String contactNumber, String email) {
        this.patientId = patientId;
        this.patientCode = patientCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.email = email;
    }

    public String getFullName() {
        return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            this.firstName = "";
            this.lastName = "";
            return;
        }
        String[] parts = fullName.trim().split("\\s+", 2);
        this.firstName = parts[0];
        this.lastName = parts.length > 1 ? parts[1] : "";
    }

    public String getEmergencyContact() {
        return (emergencyContactName != null ? emergencyContactName : "") +
                (emergencyContactPhone != null && !emergencyContactPhone.isEmpty() ? " - " + emergencyContactPhone : "");
    }

    public void setEmergencyContact(String contact) {
        if (contact != null && contact.contains("-")) {
            String[] parts = contact.split("-", 2);
            this.emergencyContactName = parts[0].trim();
            this.emergencyContactPhone = parts[1].trim();
        } else {
            this.emergencyContactName = contact != null ? contact.trim() : "";
            this.emergencyContactPhone = "";
        }
    }

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getPatientCode() { return patientCode; }
    public void setPatientCode(String patientCode) { this.patientCode = patientCode; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return getFullName() + " [" + (patientCode != null ? patientCode : "ID:" + patientId) + "]";
    }
}
