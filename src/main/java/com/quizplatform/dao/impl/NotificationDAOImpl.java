package com.quizplatform.dao.impl;

import com.quizplatform.config.DatabaseConnection;
import com.quizplatform.dao.NotificationDAO;
import com.quizplatform.exception.DatabaseException;
import com.quizplatform.model.Notification;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of NotificationDAO.
 */
public class NotificationDAOImpl implements NotificationDAO {

    @Override
    public int createNotification(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, title, message, is_read) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, notification.getUserId());
            ps.setString(2, notification.getTitle());
            ps.setString(3, notification.getMessage());
            ps.setBoolean(4, notification.isRead());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating notification failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    notification.setId(generatedId);
                    return generatedId;
                } else {
                    throw new DatabaseException("Creating notification failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating notification for user: " + notification.getUserId(), e);
        }
    }

    @Override
    public Notification findById(int id) {
        String sql = "SELECT * FROM notifications WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToNotification(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding notification by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<Notification> findByUserId(int userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        List<Notification> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving notifications for user: " + userId, e);
        }
        return list;
    }

    @Override
    public List<Notification> findUnreadByUserId(int userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC";
        List<Notification> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving unread notifications for user: " + userId, e);
        }
        return list;
    }

    @Override
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, notificationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error marking notification as read for id: " + notificationId, e);
        }
    }

    @Override
    public boolean deleteNotification(int id) {
        String sql = "DELETE FROM notifications WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting notification id: " + id, e);
        }
    }

    @Override
    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting unread notifications for user: " + userId, e);
        }
        return 0;
    }

    @Override
    public boolean markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error marking all notifications as read for user: " + userId, e);
        }
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");

        return new Notification(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("title"),
                rs.getString("message"),
                rs.getBoolean("is_read"),
                createdTs != null ? createdTs.toLocalDateTime() : null
        );
    }
}
