package com.hospital.service.impl;

import com.hospital.dao.AuditLogDao;
import com.hospital.dao.impl.AuditLogDaoImpl;
import com.hospital.model.AuditLog;
import com.hospital.security.SecurityContext;
import com.hospital.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AuditServiceImpl implements AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);
    private final AuditLogDao auditLogDao;

    public AuditServiceImpl() {
        this.auditLogDao = new AuditLogDaoImpl();
    }

    public AuditServiceImpl(AuditLogDao auditLogDao) {
        this.auditLogDao = auditLogDao;
    }

    @Override
    public void log(String action, String entityType, String entityId, String details) {
        try {
            Integer userId = SecurityContext.getCurrentUserId();
            String username = SecurityContext.getCurrentUsername();
            AuditLog log = new AuditLog(userId, username, action, entityType, entityId, details, "127.0.0.1");
            auditLogDao.insert(log);
            logger.info("AUDIT: [{}] {} {} ({}) - {}", username, action, entityType, entityId, details);
        } catch (Exception e) {
            logger.error("Failed to persist audit log: {}", e.getMessage());
        }
    }

    @Override
    public List<AuditLog> getRecentLogs(String entityType, String username, int limit) {
        return auditLogDao.findAll(entityType, username, limit);
    }
}
