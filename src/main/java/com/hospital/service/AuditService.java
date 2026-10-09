package com.hospital.service;

import com.hospital.model.AuditLog;
import java.util.List;

public interface AuditService {
    void log(String action, String entityType, String entityId, String details);
    List<AuditLog> getRecentLogs(String entityType, String username, int limit);
}
