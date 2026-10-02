package com.quizplatform.dao.impl;

import com.quizplatform.config.DatabaseConnection;
import com.quizplatform.dao.QuizReminderDAO;
import com.quizplatform.exception.DatabaseException;
import com.quizplatform.model.QuizReminder;
import com.quizplatform.model.ReminderStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of QuizReminderDAO.
 */
public class QuizReminderDAOImpl implements QuizReminderDAO {

    @Override
    public int createReminder(QuizReminder reminder) {
        String sql = "INSERT INTO quiz_reminders (participant_id, quiz_id, reminder_time, reminder_status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, reminder.getParticipantId());
            ps.setInt(2, reminder.getQuizId());
            ps.setTimestamp(3, Timestamp.valueOf(reminder.getReminderTime()));
            ps.setString(4, reminder.getReminderStatus().name());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating quiz reminder failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    reminder.setId(generatedId);
                    return generatedId;
                } else {
                    throw new DatabaseException("Creating quiz reminder failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating reminder for participant: " + reminder.getParticipantId(), e);
        }
    }

    @Override
    public QuizReminder findById(int id) {
        String sql = "SELECT * FROM quiz_reminders WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReminder(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding reminder by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<QuizReminder> findByParticipantId(int participantId) {
        String sql = "SELECT * FROM quiz_reminders WHERE participant_id = ? ORDER BY reminder_time ASC";
        List<QuizReminder> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, participantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToReminder(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving reminders for participant: " + participantId, e);
        }
        return list;
    }

    @Override
    public List<QuizReminder> findPendingReminders() {
        String sql = "SELECT * FROM quiz_reminders WHERE reminder_status = 'PENDING' ORDER BY reminder_time ASC";
        List<QuizReminder> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToReminder(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving pending reminders", e);
        }
        return list;
    }

    @Override
    public List<QuizReminder> findDueReminders() {
        String sql = "SELECT * FROM quiz_reminders WHERE reminder_status = 'PENDING' AND reminder_time <= NOW() ORDER BY reminder_time ASC";
        List<QuizReminder> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToReminder(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving due reminders", e);
        }
        return list;
    }

    @Override
    public boolean updateReminderStatus(int id, ReminderStatus status) {
        String sql = "UPDATE quiz_reminders SET reminder_status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating reminder status for id: " + id, e);
        }
    }

    @Override
    public boolean deleteReminder(int id) {
        String sql = "DELETE FROM quiz_reminders WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting reminder id: " + id, e);
        }
    }

    private QuizReminder mapResultSetToReminder(ResultSet rs) throws SQLException {
        Timestamp reminderTs = rs.getTimestamp("reminder_time");
        Timestamp createdTs = rs.getTimestamp("created_at");

        return new QuizReminder(
                rs.getInt("id"),
                rs.getInt("participant_id"),
                rs.getInt("quiz_id"),
                reminderTs != null ? reminderTs.toLocalDateTime() : null,
                ReminderStatus.valueOf(rs.getString("reminder_status")),
                createdTs != null ? createdTs.toLocalDateTime() : null
        );
    }
}
