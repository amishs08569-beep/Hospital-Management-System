package com.hospital.dao.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.RoleDao;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoleDaoImpl implements RoleDao {

    private static final Logger logger = LoggerFactory.getLogger(RoleDaoImpl.class);

    @Override
    public List<Role> findAll() {
        List<Role> list = new ArrayList<>();
        String sql = "SELECT * FROM roles ORDER BY role_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Role(rs.getInt("role_id"), rs.getString("role_name"), rs.getString("description")));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving roles: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve roles: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Optional<Role> findById(int roleId) {
        String sql = "SELECT * FROM roles WHERE role_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Role(rs.getInt("role_id"), rs.getString("role_name"), rs.getString("description")));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving role by ID {}: {}", roleId, e.getMessage());
            throw new DatabaseException("Failed to retrieve role: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Role> findByName(String roleName) {
        String sql = "SELECT * FROM roles WHERE UPPER(role_name) = UPPER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Role(rs.getInt("role_id"), rs.getString("role_name"), rs.getString("description")));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving role by name {}: {}", roleName, e.getMessage());
            throw new DatabaseException("Failed to retrieve role: " + e.getMessage(), e);
        }
        return Optional.empty();
    }
}
