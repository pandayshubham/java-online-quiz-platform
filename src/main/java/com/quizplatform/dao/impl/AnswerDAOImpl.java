package com.quizplatform.dao.impl;

import com.quizplatform.config.DatabaseConnection;
import com.quizplatform.dao.AnswerDAO;
import com.quizplatform.exception.DatabaseException;
import com.quizplatform.model.Answer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of AnswerDAO.
 */
public class AnswerDAOImpl implements AnswerDAO {

    @Override
    public int createAnswer(Answer answer) {
        String sql = "INSERT INTO answers (attempt_id, question_id, selected_option_id, is_correct) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, answer.getAttemptId());
            ps.setInt(2, answer.getQuestionId());
            if (answer.getSelectedOptionId() != null) {
                ps.setInt(3, answer.getSelectedOptionId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setBoolean(4, answer.isCorrect());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating answer failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    answer.setId(generatedId);
                    return generatedId;
                } else {
                    throw new DatabaseException("Creating answer failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating answer for attempt id: " + answer.getAttemptId(), e);
        }
    }

    @Override
    public Answer findById(int id) {
        String sql = "SELECT * FROM answers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAnswer(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding answer by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<Answer> findByAttemptId(int attemptId) {
        String sql = "SELECT * FROM answers WHERE attempt_id = ? ORDER BY id ASC";
        List<Answer> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, attemptId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAnswer(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving answers for attempt id: " + attemptId, e);
        }
        return list;
    }

    @Override
    public boolean updateAnswer(Answer answer) {
        String sql = "UPDATE answers SET question_id = ?, selected_option_id = ?, is_correct = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, answer.getQuestionId());
            if (answer.getSelectedOptionId() != null) {
                ps.setInt(2, answer.getSelectedOptionId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setBoolean(3, answer.isCorrect());
            ps.setInt(4, answer.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating answer id: " + answer.getId(), e);
        }
    }

    @Override
    public boolean deleteAnswer(int id) {
        String sql = "DELETE FROM answers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting answer id: " + id, e);
        }
    }

    @Override
    public int createAnswer(Connection conn, Answer answer) {
        String sql = "INSERT INTO answers (attempt_id, question_id, selected_option_id, is_correct) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, answer.getAttemptId());
            ps.setInt(2, answer.getQuestionId());
            if (answer.getSelectedOptionId() != null) {
                ps.setInt(3, answer.getSelectedOptionId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setBoolean(4, answer.isCorrect());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating answer failed in transaction, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    answer.setId(generatedId);
                    return generatedId;
                } else {
                    throw new DatabaseException("Creating answer failed in transaction, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating answer in transaction for attempt id: " + answer.getAttemptId(), e);
        }
    }

    @Override
    public int getTotalAnswersCount() {
        String sql = "SELECT COUNT(*) FROM answers WHERE selected_option_id IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting total answers count", e);
        }
        return 0;
    }

    @Override
    public int getTotalAnswersCountByParticipant(int participantId) {
        String sql = "SELECT COUNT(*) FROM answers a JOIN quiz_attempts q ON a.attempt_id = q.id WHERE q.participant_id = ? AND a.selected_option_id IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting answered questions for participant: " + participantId, e);
        }
        return 0;
    }

    @Override
    public int getTotalCorrectAnswersByParticipant(int participantId) {
        String sql = "SELECT COUNT(*) FROM answers a JOIN quiz_attempts q ON a.attempt_id = q.id WHERE q.participant_id = ? AND a.is_correct = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting correct answers for participant: " + participantId, e);
        }
        return 0;
    }

    @Override
    public int getTotalIncorrectAnswersByParticipant(int participantId) {
        String sql = "SELECT COUNT(*) FROM answers a JOIN quiz_attempts q ON a.attempt_id = q.id WHERE q.participant_id = ? AND a.is_correct = FALSE AND a.selected_option_id IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting incorrect answers for participant: " + participantId, e);
        }
        return 0;
    }

    @Override
    public int getTotalUnansweredByParticipant(int participantId) {
        String sql = "SELECT COUNT(*) FROM answers a JOIN quiz_attempts q ON a.attempt_id = q.id WHERE q.participant_id = ? AND a.selected_option_id IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting unanswered questions for participant: " + participantId, e);
        }
        return 0;
    }

    @Override
    public int getTimesAnsweredCountByQuestionId(int questionId) {
        String sql = "SELECT COUNT(*) FROM answers WHERE question_id = ? AND selected_option_id IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting times answered for question: " + questionId, e);
        }
        return 0;
    }

    @Override
    public int getCorrectCountByQuestionId(int questionId) {
        String sql = "SELECT COUNT(*) FROM answers WHERE question_id = ? AND is_correct = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting correct answers for question: " + questionId, e);
        }
        return 0;
    }

    @Override
    public int getIncorrectCountByQuestionId(int questionId) {
        String sql = "SELECT COUNT(*) FROM answers WHERE question_id = ? AND is_correct = FALSE AND selected_option_id IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting incorrect answers for question: " + questionId, e);
        }
        return 0;
    }

    @Override
    public int getUnansweredCountByQuestionId(int questionId) {
        String sql = "SELECT COUNT(*) FROM answers WHERE question_id = ? AND selected_option_id IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting unanswered for question: " + questionId, e);
        }
        return 0;
    }

    private Answer mapResultSetToAnswer(ResultSet rs) throws SQLException {
        int selectedOption = rs.getInt("selected_option_id");
        Integer selectedOptionId = rs.wasNull() ? null : selectedOption;

        return new Answer(
                rs.getInt("id"),
                rs.getInt("attempt_id"),
                rs.getInt("question_id"),
                selectedOptionId,
                rs.getBoolean("is_correct")
        );
    }
}
