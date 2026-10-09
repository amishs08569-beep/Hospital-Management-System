package com.hospital.model;

import java.time.LocalDateTime;

public class AuditLog {
    private Long logId;
    private Integer userId;
    private String username;
    private String action; // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, DISPENSE, DISCHARGE
    private String entityType; // PATIENT, APPOINTMENT, ADMISSION, MEDICINE, INVOICE, etc.
    private String entityId;
    private String details;
    private String ipAddress;
    private LocalDateTime createdAt;

    public AuditLog() {}

    public AuditLog(Integer userId, String username, String action, String entityType, String entityId, String details, String ipAddress) {
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.ipAddress = ipAddress;
        this.createdAt = LocalDateTime.now();
    }

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getTimestamp() { return createdAt; }

    @Override
    public String toString() {
        return "[" + createdAt + "] " + username + " - " + action + " " + entityType + " (" + entityId + "): " + details;
    }
}
