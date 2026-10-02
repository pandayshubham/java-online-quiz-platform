package com.quizplatform.dao.impl;

import com.quizplatform.config.DatabaseConnection;
import com.quizplatform.dao.MessageDAO;
import com.quizplatform.exception.DatabaseException;
import com.quizplatform.model.Message;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of MessageDAO.
 */
public class MessageDAOImpl implements MessageDAO {

    @Override
    public int createMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, message_text, is_read) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, message.getSenderId());
            ps.setInt(2, message.getReceiverId());
            ps.setString(3, message.getMessageText());
            ps.setBoolean(4, message.isRead());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating message failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    message.setId(generatedId);
                    return generatedId;
                } else {
                    throw new DatabaseException("Creating message failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating message from user " + message.getSenderId(), e);
        }
    }

    @Override
    public Message findById(int id) {
        String sql = "SELECT * FROM messages WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMessage(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding message by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<Message> findBySenderId(int senderId) {
        String sql = "SELECT * FROM messages WHERE sender_id = ? ORDER BY created_at DESC";
        List<Message> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, senderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMessage(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving messages sent by user: " + senderId, e);
        }
        return list;
    }

    @Override
    public List<Message> findByReceiverId(int receiverId) {
        String sql = "SELECT * FROM messages WHERE receiver_id = ? ORDER BY created_at DESC";
        List<Message> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, receiverId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMessage(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving messages received by user: " + receiverId, e);
        }
        return list;
    }

    @Override
    public List<Message> findConversation(int user1, int user2) {
        String sql = "SELECT * FROM messages WHERE (sender_id = ? AND receiver_id = ?) " +
                     "OR (sender_id = ? AND receiver_id = ?) ORDER BY created_at ASC";
        List<Message> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, user1);
            ps.setInt(2, user2);
            ps.setInt(3, user2);
            ps.setInt(4, user1);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMessage(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving conversation between " + user1 + " and " + user2, e);
        }
        return list;
    }

    @Override
    public boolean markAsRead(int messageId) {
        String sql = "UPDATE messages SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, messageId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error marking message as read for id: " + messageId, e);
        }
    }

    @Override
    public boolean deleteMessage(int id) {
        String sql = "DELETE FROM messages WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting message id: " + id, e);
        }
    }

    @Override
    public int getUnreadCount(int receiverId) {
        String sql = "SELECT COUNT(*) FROM messages WHERE receiver_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, receiverId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting unread messages for user: " + receiverId, e);
        }
        return 0;
    }

    @Override
    public boolean markConversationAsRead(int receiverId, int senderId) {
        String sql = "UPDATE messages SET is_read = TRUE WHERE receiver_id = ? AND sender_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, receiverId);
            ps.setInt(2, senderId);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error marking conversation as read between receiver " + receiverId + " and sender " + senderId, e);
        }
    }

    @Override
    public List<Integer> findChatPartnerIds(int userId) {
        String sql = "SELECT DISTINCT CASE WHEN sender_id = ? THEN receiver_id ELSE sender_id END AS partner_id " +
                     "FROM messages WHERE sender_id = ? OR receiver_id = ?";
        List<Integer> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int partnerId = rs.getInt("partner_id");
                    if (partnerId != userId && !list.contains(partnerId)) {
                        list.add(partnerId);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving chat partner IDs for user: " + userId, e);
        }
        return list;
    }

    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");

        return new Message(
                rs.getInt("id"),
                rs.getInt("sender_id"),
                rs.getInt("receiver_id"),
                rs.getString("message_text"),
                rs.getBoolean("is_read"),
                createdTs != null ? createdTs.toLocalDateTime() : null
        );
    }
}
