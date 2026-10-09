package com.hospital.dao.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.AuditLogDao;
import com.hospital.model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDaoImpl implements AuditLogDao {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogDaoImpl.class);

    @Override
    public void insert(AuditLog log) {
        String sql = "INSERT INTO audit_logs (user_id, username, action, entity_type, entity_id, details, ip_address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (log.getUserId() != null) ps.setInt(1, log.getUserId()); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, log.getUsername());
            ps.setString(3, log.getAction());
            ps.setString(4, log.getEntityType());
            ps.setString(5, log.getEntityId());
            ps.setString(6, log.getDetails());
            ps.setString(7, log.getIpAddress());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error inserting audit log: {}", e.getMessage());
            // Do not throw to avoid failing user transaction over audit write
        }
    }

    @Override
    public List<AuditLog> findAll(String entityType, String username, int limit) {
        List<AuditLog> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_logs WHERE 1=1 ");
        if (entityType != null && !entityType.trim().isEmpty() && !"ALL".equalsIgnoreCase(entityType)) {
            sql.append("AND entity_type = ? ");
        }
        if (username != null && !username.trim().isEmpty()) {
            sql.append("AND username LIKE ? ");
        }
        sql.append("ORDER BY log_id DESC LIMIT ?");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (entityType != null && !entityType.trim().isEmpty() && !"ALL".equalsIgnoreCase(entityType)) {
                ps.setString(idx++, entityType.trim());
            }
            if (username != null && !username.trim().isEmpty()) {
                ps.setString(idx++, "%" + username.trim() + "%");
            }
            ps.setInt(idx, limit > 0 ? limit : 100);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog();
                    log.setLogId(rs.getLong("log_id"));
                    int uid = rs.getInt("user_id");
                    if (!rs.wasNull()) log.setUserId(uid);
                    log.setUsername(rs.getString("username"));
                    log.setAction(rs.getString("action"));
                    log.setEntityType(rs.getString("entity_type"));
                    log.setEntityId(rs.getString("entity_id"));
                    log.setDetails(rs.getString("details"));
                    log.setIpAddress(rs.getString("ip_address"));
                    Timestamp cat = rs.getTimestamp("created_at");
                    if (cat != null) log.setCreatedAt(cat.toLocalDateTime());
                    list.add(log);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving audit logs: {}", e.getMessage());
        }
        return list;
    }
}
