package com.hospital.dao.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.WardBedDao;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Bed;
import com.hospital.model.Ward;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WardBedDaoImpl implements WardBedDao {

    private static final Logger logger = LoggerFactory.getLogger(WardBedDaoImpl.class);

    @Override
    public List<Ward> findAllWards() {
        List<Ward> list = new ArrayList<>();
        String sql = "SELECT w.*, " +
                "(SELECT COUNT(*) FROM beds b WHERE b.ward_id = w.ward_id AND b.is_occupied = FALSE AND b.is_active = TRUE) AS available_beds " +
                "FROM wards w ORDER BY w.ward_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Ward w = new Ward();
                w.setWardId(rs.getInt("ward_id"));
                w.setWardName(rs.getString("ward_name"));
                w.setWardType(rs.getString("ward_type"));
                w.setTotalBeds(rs.getInt("total_beds"));
                w.setAvailableBeds(rs.getInt("available_beds"));
                w.setDailyRate(rs.getBigDecimal("daily_rate"));
                w.setDescription(rs.getString("description"));
                list.add(w);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving wards: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve wards: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Optional<Ward> findWardById(int wardId) {
        String sql = "SELECT w.*, " +
                "(SELECT COUNT(*) FROM beds b WHERE b.ward_id = w.ward_id AND b.is_occupied = FALSE AND b.is_active = TRUE) AS available_beds " +
                "FROM wards w WHERE w.ward_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, wardId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ward w = new Ward();
                    w.setWardId(rs.getInt("ward_id"));
                    w.setWardName(rs.getString("ward_name"));
                    w.setWardType(rs.getString("ward_type"));
                    w.setTotalBeds(rs.getInt("total_beds"));
                    w.setAvailableBeds(rs.getInt("available_beds"));
                    w.setDailyRate(rs.getBigDecimal("daily_rate"));
                    w.setDescription(rs.getString("description"));
                    return Optional.of(w);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving ward {}: {}", wardId, e.getMessage());
            throw new DatabaseException("Failed to retrieve ward: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Bed> findBedsByWardId(int wardId) {
        List<Bed> list = new ArrayList<>();
        String sql = "SELECT b.*, w.ward_name FROM beds b JOIN wards w ON b.ward_id = w.ward_id WHERE b.ward_id = ? AND b.is_active = TRUE ORDER BY b.bed_number ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, wardId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBed(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving beds for ward {}: {}", wardId, e.getMessage());
            throw new DatabaseException("Failed to retrieve beds: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Bed> findAvailableBeds() {
        List<Bed> list = new ArrayList<>();
        String sql = "SELECT b.*, w.ward_name FROM beds b JOIN wards w ON b.ward_id = w.ward_id WHERE b.is_occupied = FALSE AND b.is_active = TRUE ORDER BY w.ward_name, b.bed_number";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToBed(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving available beds: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve available beds: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Optional<Bed> findBedById(int bedId) {
        String sql = "SELECT b.*, w.ward_name FROM beds b JOIN wards w ON b.ward_id = w.ward_id WHERE b.bed_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bedId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBed(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving bed {}: {}", bedId, e.getMessage());
            throw new DatabaseException("Failed to retrieve bed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public boolean setBedOccupied(int bedId, boolean occupied, Connection conn) {
        String sql = "UPDATE beds SET is_occupied = ? WHERE bed_id = ?";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, occupied);
                ps.setInt(2, bedId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            logger.error("Error setting bed {} occupation status to {}: {}", bedId, occupied, e.getMessage());
            throw new DatabaseException("Failed to update bed occupation: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public int insertWard(Ward ward) {
        String sql = "INSERT INTO wards (ward_name, ward_type, total_beds, daily_rate, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ward.getWardName());
            ps.setString(2, ward.getWardType());
            ps.setInt(3, ward.getTotalBeds());
            ps.setBigDecimal(4, ward.getDailyRate());
            ps.setString(5, ward.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    ward.setWardId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting ward: {}", e.getMessage());
            throw new DatabaseException("Failed to insert ward: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int insertBed(Bed bed) {
        String sql = "INSERT INTO beds (ward_id, bed_number, is_occupied, is_active) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bed.getWardId());
            ps.setString(2, bed.getBedNumber());
            ps.setBoolean(3, bed.isOccupied());
            ps.setBoolean(4, bed.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    bed.setBedId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting bed: {}", e.getMessage());
            throw new DatabaseException("Failed to insert bed: " + e.getMessage(), e);
        }
        return 0;
    }

    private Bed mapResultSetToBed(ResultSet rs) throws SQLException {
        Bed b = new Bed();
        b.setBedId(rs.getInt("bed_id"));
        b.setWardId(rs.getInt("ward_id"));
        b.setWardName(rs.getString("ward_name"));
        b.setBedNumber(rs.getString("bed_number"));
        b.setOccupied(rs.getBoolean("is_occupied"));
        b.setActive(rs.getBoolean("is_active"));
        return b;
    }
}
