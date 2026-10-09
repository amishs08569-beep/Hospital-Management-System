package com.hospital.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MedicalRecord {
    private Integer recordId;
    private Integer patientId;
    private String patientName;
    private String patientCode;
    private Integer doctorId;
    private String doctorName;
    private LocalDateTime visitDate;
    private String symptoms;
    private String clinicalObservations;
    private String diagnosis;
    private String treatmentPlan;
    private LocalDate followUpDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MedicalRecord() {}

    public Integer getRecordId() { return recordId; }
    public void setRecordId(Integer recordId) { this.recordId = recordId; }

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientCode() { return patientCode; }
    public void setPatientCode(String patientCode) { this.patientCode = patientCode; }

    public Integer getDoctorId() { return doctorId; }
    public void setDoctorId(Integer doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public LocalDateTime getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDateTime visitDate) { this.visitDate = visitDate; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getClinicalObservations() { return clinicalObservations; }
    public void setClinicalObservations(String clinicalObservations) { this.clinicalObservations = clinicalObservations; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getTreatmentPlan() { return treatmentPlan; }
    public void setTreatmentPlan(String treatmentPlan) { this.treatmentPlan = treatmentPlan; }

    public LocalDate getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(LocalDate followUpDate) { this.followUpDate = followUpDate; }

    public String getRecordNumber() {
        return recordId != null ? "MR-" + String.format("%04d", recordId) : "MR-NEW";
    }

    public LocalDate getRecordDate() {
        return visitDate != null ? visitDate.toLocalDate() : (createdAt != null ? createdAt.toLocalDate() : LocalDate.now());
    }

    public void setRecordDate(LocalDate date) {
        this.visitDate = date != null ? date.atStartOfDay() : LocalDateTime.now();
    }

    public String getPhysicalExamination() {
        return clinicalObservations;
    }

    public void setPhysicalExamination(String obs) {
        this.clinicalObservations = obs;
    }

    private String followUpInstructions = "";
    public String getFollowUpInstructions() {
        return followUpInstructions;
    }

    public void setFollowUpInstructions(String instructions) {
        this.followUpInstructions = instructions;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Record #" + recordId + " - " + diagnosis + " (" + visitDate.toLocalDate() + ")";
    }
}
