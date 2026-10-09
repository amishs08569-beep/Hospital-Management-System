package com.hospital.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LabOrder {
    private Integer orderId;
    private String orderNumber;
    private Integer patientId;
    private String patientName;
    private String patientCode;
    private Integer doctorId;
    private String doctorName;
    private LocalDateTime orderDate;
    private String status = "REQUESTED"; // REQUESTED, SAMPLE_COLLECTED, IN_PROGRESS, COMPLETED, CANCELLED
    private String priority = "ROUTINE"; // ROUTINE, URGENT, STAT
    private String clinicalNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LabResult> results = new ArrayList<>();

    public LabOrder() {}

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

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

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getClinicalNotes() { return clinicalNotes; }
    public void setClinicalNotes(String clinicalNotes) { this.clinicalNotes = clinicalNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<LabResult> getResults() { return results; }
    public void setResults(List<LabResult> results) { this.results = results; }

    @Override
    public String toString() {
        return orderNumber + " (" + status + ") - " + patientName;
    }
}
