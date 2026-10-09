package com.hospital.model;

import java.time.LocalDateTime;

public class LabResult {

    private Integer resultId;
    private Integer orderId;
    private Integer testId;
    private String testName;
    private String testCode;
    private Integer patientId;
    private Integer technicianId;
    private String resultValue;
    private String units;
    private String referenceRange;
    private boolean abnormal = false;
    private String abnormalFlag;
    private String remarks;
    private String technicianNotes;
    private Integer performedByUserId;
    private String performedByUsername;
    private LocalDateTime resultDate;
    private LocalDateTime createdAt;

    // Default constructor
    public LabResult() {
    }

    // Parameterized constructor (user version)
    public LabResult(
            Integer resultId,
            Integer testId,
            Integer patientId,
            Integer technicianId,
            String resultValue,
            String units,
            String referenceRange,
            String abnormalFlag,
            String technicianNotes,
            LocalDateTime resultDate) {

        this.resultId = resultId;
        this.testId = testId;
        this.patientId = patientId;
        this.technicianId = technicianId;
        this.resultValue = resultValue;
        this.units = units;
        this.referenceRange = referenceRange;
        this.abnormalFlag = abnormalFlag;
        this.abnormal = "Y".equalsIgnoreCase(abnormalFlag) || "ABNORMAL".equalsIgnoreCase(abnormalFlag);
        this.technicianNotes = technicianNotes;
        this.remarks = technicianNotes;
        this.resultDate = resultDate;
    }

    // Full constructor
    public LabResult(
            Integer resultId, Integer orderId, Integer testId,
            String testName, String testCode, String resultValue,
            String referenceRange, String unit, boolean abnormal,
            String remarks, Integer performedByUserId,
            String performedByUsername, LocalDateTime resultDate,
            LocalDateTime createdAt) {
        this.resultId = resultId;
        this.orderId = orderId;
        this.testId = testId;
        this.testName = testName;
        this.testCode = testCode;
        this.resultValue = resultValue;
        this.referenceRange = referenceRange;
        this.units = unit;
        this.abnormal = abnormal;
        this.abnormalFlag = abnormal ? "ABNORMAL" : "NORMAL";
        this.remarks = remarks;
        this.technicianNotes = remarks;
        this.performedByUserId = performedByUserId;
        this.performedByUsername = performedByUsername;
        this.resultDate = resultDate;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public Integer getResultId() {
        return resultId;
    }

    public void setResultId(Integer resultId) {
        this.resultId = resultId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getTestId() {
        return testId;
    }

    public void setTestId(Integer testId) {
        this.testId = testId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestCode() {
        return testCode;
    }

    public void setTestCode(String testCode) {
        this.testCode = testCode;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getTechnicianId() {
        return technicianId != null ? technicianId : performedByUserId;
    }

    public void setTechnicianId(Integer technicianId) {
        this.technicianId = technicianId;
        this.performedByUserId = technicianId;
    }

    public Integer getPerformedByUserId() {
        return performedByUserId != null ? performedByUserId : technicianId;
    }

    public void setPerformedByUserId(Integer performedByUserId) {
        this.performedByUserId = performedByUserId;
        this.technicianId = performedByUserId;
    }

    public String getPerformedByUsername() {
        return performedByUsername;
    }

    public void setPerformedByUsername(String performedByUsername) {
        this.performedByUsername = performedByUsername;
    }

    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    // Units / Unit alias
    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getUnit() {
        return units;
    }

    public void setUnit(String unit) {
        this.units = unit;
    }

    public String getReferenceRange() {
        return referenceRange;
    }

    public void setReferenceRange(String referenceRange) {
        this.referenceRange = referenceRange;
    }

    // Abnormal / AbnormalFlag alias
    public boolean isAbnormal() {
        return abnormal;
    }

    public void setAbnormal(boolean abnormal) {
        this.abnormal = abnormal;
        this.abnormalFlag = abnormal ? "ABNORMAL" : "NORMAL";
    }

    public String getAbnormalFlag() {
        return abnormalFlag;
    }

    public void setAbnormalFlag(String abnormalFlag) {
        this.abnormalFlag = abnormalFlag;
        this.abnormal = "Y".equalsIgnoreCase(abnormalFlag) || "ABNORMAL".equalsIgnoreCase(abnormalFlag);
    }

    // Remarks / TechnicianNotes alias
    public String getRemarks() {
        return remarks != null ? remarks : technicianNotes;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
        this.technicianNotes = remarks;
    }

    public String getTechnicianNotes() {
        return technicianNotes != null ? technicianNotes : remarks;
    }

    public void setTechnicianNotes(String technicianNotes) {
        this.technicianNotes = technicianNotes;
        this.remarks = technicianNotes;
    }

    public LocalDateTime getResultDate() {
        return resultDate;
    }

    public void setResultDate(LocalDateTime resultDate) {
        this.resultDate = resultDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return (testName != null ? testName : "Test " + testId) + ": " + resultValue + " " + (units != null ? units : "") + (abnormal ? " [ABNORMAL]" : "");
    }
}
