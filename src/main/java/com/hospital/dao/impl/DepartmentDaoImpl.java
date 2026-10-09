package com.hospital.dao.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.DepartmentDao;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Department;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DepartmentDaoImpl implements DepartmentDao {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentDaoImpl.class);

    @Override
    public List<Department> findAll(boolean activeOnly) {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT * FROM departments " + (activeOnly ? "WHERE is_active = TRUE " : "") + "ORDER BY name ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Department(rs.getInt("department_id"), rs.getString("name"), rs.getString("description"), rs.getBoolean("is_active")));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving departments: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve departments: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Optional<Department> findById(int departmentId) {
        String sql = "SELECT * FROM departments WHERE department_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Department(rs.getInt("department_id"), rs.getString("name"), rs.getString("description"), rs.getBoolean("is_active")));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving department {}: {}", departmentId, e.getMessage());
            throw new DatabaseException("Failed to retrieve department: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public int insert(Department dept) {
        String sql = "INSERT INTO departments (name, description, is_active) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dept.getName());
            ps.setString(2, dept.getDescription());
            ps.setBoolean(3, dept.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    dept.setDepartmentId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting department: {}", e.getMessage());
            throw new DatabaseException("Failed to insert department: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean update(Department dept) {
        String sql = "UPDATE departments SET name = ?, description = ?, is_active = ? WHERE department_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dept.getName());
            ps.setString(2, dept.getDescription());
            ps.setBoolean(3, dept.isActive());
            ps.setInt(4, dept.getDepartmentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating department: {}", e.getMessage());
            throw new DatabaseException("Failed to update department: " + e.getMessage(), e);
        }
    }
}
