package com.quizplatform.dao.impl;

import com.quizplatform.config.DatabaseConnection;
import com.quizplatform.dao.QuestionDAO;
import com.quizplatform.exception.DatabaseException;
import com.quizplatform.model.Question;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of QuestionDAO.
 */
public class QuestionDAOImpl implements QuestionDAO {

    @Override
    public int createQuestion(Question question) {
        String sql = "INSERT INTO questions (quiz_id, question_text, explanation, question_order) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, question.getQuizId());
            ps.setString(2, question.getQuestionText());
            ps.setString(3, question.getExplanation());
            ps.setInt(4, question.getQuestionOrder());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating question failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    question.setId(generatedId);
                    return generatedId;
                } else {
                    throw new DatabaseException("Creating question failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating question for quiz id: " + question.getQuizId(), e);
        }
    }

    @Override
    public Question findById(int id) {
        String sql = "SELECT * FROM questions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToQuestion(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding question by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<Question> findByQuizId(int quizId) {
        String sql = "SELECT * FROM questions WHERE quiz_id = ? ORDER BY question_order ASC, id ASC";
        List<Question> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToQuestion(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving questions for quiz id: " + quizId, e);
        }
        return list;
    }

    @Override
    public boolean updateQuestion(Question question) {
        String sql = "UPDATE questions SET question_text = ?, explanation = ?, question_order = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, question.getQuestionText());
            ps.setString(2, question.getExplanation());
            ps.setInt(3, question.getQuestionOrder());
            ps.setInt(4, question.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating question id: " + question.getId(), e);
        }
    }

    @Override
    public boolean deleteQuestion(int id) {
        String sql = "DELETE FROM questions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting question id: " + id, e);
        }
    }

    @Override
    public int getQuestionCountByQuizId(int quizId) {
        String sql = "SELECT COUNT(*) FROM questions WHERE quiz_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting questions for quiz id: " + quizId, e);
        }
        return 0;
    }

    @Override
    public int getMaxQuestionOrderByQuizId(int quizId) {
        String sql = "SELECT COALESCE(MAX(question_order), 0) FROM questions WHERE quiz_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding max question order for quiz id: " + quizId, e);
        }
        return 0;
    }

    @Override
    public List<Question> findByQuizId(Connection conn, int quizId) {
        String sql = "SELECT * FROM questions WHERE quiz_id = ? ORDER BY question_order ASC";
        List<Question> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToQuestion(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving questions in transaction for quiz id: " + quizId, e);
        }
        return list;
    }

    @Override
    public int getTotalQuestionsCount() {
        String sql = "SELECT COUNT(*) FROM questions";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting total questions", e);
        }
        return 0;
    }


    private Question mapResultSetToQuestion(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");

        return new Question(
                rs.getInt("id"),
                rs.getInt("quiz_id"),
                rs.getString("question_text"),
                rs.getString("explanation"),
                rs.getInt("question_order"),
                createdTs != null ? createdTs.toLocalDateTime() : null,
                updatedTs != null ? updatedTs.toLocalDateTime() : null
        );
    }
}
