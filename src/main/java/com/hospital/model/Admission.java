package com.hospital.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Admission {

    private Integer admissionId;
    private String admissionNumber;
    private Integer patientId;
    private String patientName;
    private String patientCode;
    private Integer bedId;
    private String bedNumber;
    private String wardName;
    private Integer attendingDoctorId;
    private String attendingDoctorName;
    private LocalDateTime admissionDate;
    private LocalDateTime dischargeDate;
    private String diagnosis;
    private String dischargeSummary;
    private String status = "ADMITTED";
    private BigDecimal dailyRate = BigDecimal.ZERO;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public Admission() {
    }

    // Parameterized constructor with LocalDate (backward compatibility)
    public Admission(Integer admissionId, Integer patientId,
            Integer doctorId, LocalDate admissionDate,
            LocalDate dischargeDate, String reasonForAdmission,
            String status) {
        this.admissionId = admissionId;
        this.patientId = patientId;
        this.attendingDoctorId = doctorId;
        if (admissionDate != null) {
            this.admissionDate = admissionDate.atStartOfDay();
        }
        if (dischargeDate != null) {
            this.dischargeDate = dischargeDate.atStartOfDay();
        }
        this.diagnosis = reasonForAdmission;
        this.status = status;
    }

    // Parameterized constructor with LocalDateTime
    public Admission(Integer admissionId, Integer patientId,
            Integer doctorId, LocalDateTime admissionDate,
            LocalDateTime dischargeDate, String reasonForAdmission,
            String status) {
        this.admissionId = admissionId;
        this.patientId = patientId;
        this.attendingDoctorId = doctorId;
        this.admissionDate = admissionDate;
        this.dischargeDate = dischargeDate;
        this.diagnosis = reasonForAdmission;
        this.status = status;
    }

    // Full constructor
    public Admission(Integer admissionId, String admissionNumber, Integer patientId,
            String patientName, String patientCode, Integer bedId,
            String bedNumber, String wardName, Integer attendingDoctorId,
            String attendingDoctorName, LocalDateTime admissionDate,
            LocalDateTime dischargeDate, String diagnosis, String dischargeSummary,
            String status, BigDecimal dailyRate) {
        this.admissionId = admissionId;
        this.admissionNumber = admissionNumber;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientCode = patientCode;
        this.bedId = bedId;
        this.bedNumber = bedNumber;
        this.wardName = wardName;
        this.attendingDoctorId = attendingDoctorId;
        this.attendingDoctorName = attendingDoctorName;
        this.admissionDate = admissionDate;
        this.dischargeDate = dischargeDate;
        this.diagnosis = diagnosis;
        this.dischargeSummary = dischargeSummary;
        this.status = status;
        this.dailyRate = dailyRate != null ? dailyRate : BigDecimal.ZERO;
    }

    // Getters and setters
    public Integer getAdmissionId() {
        return admissionId;
    }

    public void setAdmissionId(Integer admissionId) {
        this.admissionId = admissionId;
    }

    public String getAdmissionNumber() {
        return admissionNumber;
    }

    public void setAdmissionNumber(String admissionNumber) {
        this.admissionNumber = admissionNumber;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public Integer getBedId() {
        return bedId;
    }

    public void setBedId(Integer bedId) {
        this.bedId = bedId;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    // Doctor ID and Attending Doctor ID aliases
    public Integer getAttendingDoctorId() {
        return attendingDoctorId;
    }

    public void setAttendingDoctorId(Integer attendingDoctorId) {
        this.attendingDoctorId = attendingDoctorId;
    }

    public Integer getDoctorId() {
        return attendingDoctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.attendingDoctorId = doctorId;
    }

    // Doctor Name and Attending Doctor Name aliases
    public String getAttendingDoctorName() {
        return attendingDoctorName;
    }

    public void setAttendingDoctorName(String attendingDoctorName) {
        this.attendingDoctorName = attendingDoctorName;
    }

    public String getDoctorName() {
        return attendingDoctorName;
    }

    public void setDoctorName(String doctorName) {
        this.attendingDoctorName = doctorName;
    }

    public LocalDateTime getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDateTime admissionDate) {
        this.admissionDate = admissionDate;
    }

    public void setAdmissionDate(LocalDate admissionDate) {
        this.admissionDate = (admissionDate != null) ? admissionDate.atStartOfDay() : null;
    }

    public LocalDateTime getDischargeDate() {
        return dischargeDate;
    }

    public void setDischargeDate(LocalDateTime dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public void setDischargeDate(LocalDate dischargeDate) {
        this.dischargeDate = (dischargeDate != null) ? dischargeDate.atStartOfDay() : null;
    }

    // Diagnosis and Reason for Admission aliases
    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getReasonForAdmission() {
        return diagnosis;
    }

    public void setReasonForAdmission(String reasonForAdmission) {
        this.diagnosis = reasonForAdmission;
    }

    public String getDischargeSummary() {
        return dischargeSummary;
    }

    public void setDischargeSummary(String dischargeSummary) {
        this.dischargeSummary = dischargeSummary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Admission{" +
                "admissionNumber='" + admissionNumber + '\'' +
                ", patientName='" + patientName + '\'' +
                ", bedNumber='" + bedNumber + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
