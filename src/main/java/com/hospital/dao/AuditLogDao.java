package com.hospital.dao;

import com.hospital.model.AuditLog;
import java.util.List;

public interface AuditLogDao {
    void insert(AuditLog log);
    List<AuditLog> findAll(String entityType, String username, int limit);
}
