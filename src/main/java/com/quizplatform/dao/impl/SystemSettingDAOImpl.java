package com.quizplatform.dao.impl;

import com.quizplatform.config.DatabaseConnection;
import com.quizplatform.dao.SystemSettingDAO;
import com.quizplatform.exception.DatabaseException;
import com.quizplatform.model.SystemSetting;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of SystemSettingDAO.
 */
public class SystemSettingDAOImpl implements SystemSettingDAO {

    @Override
    public SystemSetting findByKey(String key) {
        String sql = "SELECT * FROM system_settings WHERE setting_key = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSetting(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding system setting by key: " + key, e);
        }
        return null;
    }

    @Override
    public List<SystemSetting> findAll() {
        String sql = "SELECT * FROM system_settings ORDER BY setting_key ASC";
        List<SystemSetting> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToSetting(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving all system settings", e);
        }
        return list;
    }

    @Override
    public boolean updateSetting(SystemSetting setting) {
        String sql = "UPDATE system_settings SET setting_value = ?, description = ? WHERE setting_key = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, setting.getSettingValue());
            ps.setString(2, setting.getDescription());
            ps.setString(3, setting.getSettingKey());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating setting: " + setting.getSettingKey(), e);
        }
    }

    @Override
    public boolean existsByKey(String key) {
        String sql = "SELECT 1 FROM system_settings WHERE setting_key = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking setting existence: " + key, e);
        }
    }

    @Override
    public int createSetting(SystemSetting setting) {
        String sql = "INSERT INTO system_settings (setting_key, setting_value, description) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, setting.getSettingKey());
            ps.setString(2, setting.getSettingValue());
            ps.setString(3, setting.getDescription());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating setting failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    setting.setId(generatedId);
                    return generatedId;
                } else {
                    return 1;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating setting: " + setting.getSettingKey(), e);
        }
    }

    @Override
    public boolean saveOrUpdate(SystemSetting setting) {
        if (setting == null || setting.getSettingKey() == null) {
            return false;
        }
        if (existsByKey(setting.getSettingKey())) {
            return updateSetting(setting);
        } else {
            return createSetting(setting) > 0;
        }
    }

    private SystemSetting mapResultSetToSetting(ResultSet rs) throws SQLException {
        Timestamp updatedTs = rs.getTimestamp("updated_at");

        return new SystemSetting(
                rs.getInt("id"),
                rs.getString("setting_key"),
                rs.getString("setting_value"),
                rs.getString("description"),
                updatedTs != null ? updatedTs.toLocalDateTime() : null
        );
    }
}
