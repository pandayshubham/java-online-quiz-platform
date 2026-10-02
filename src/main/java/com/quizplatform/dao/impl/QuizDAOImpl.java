package com.quizplatform.dao.impl;

import com.quizplatform.config.DatabaseConnection;
import com.quizplatform.dao.QuizDAO;
import com.quizplatform.exception.DatabaseException;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of QuizDAO.
 */
public class QuizDAOImpl implements QuizDAO {

    @Override
    public int createQuiz(Quiz quiz) {
        String sql = "INSERT INTO quizzes (creator_id, title, description, duration_minutes, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, quiz.getCreatorId());
            ps.setString(2, quiz.getTitle());
            ps.setString(3, quiz.getDescription());
            ps.setInt(4, quiz.getDurationMinutes());
            ps.setString(5, quiz.getStatus().name());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating quiz failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    quiz.setId(generatedId);
                    return generatedId;
                } else {
                    throw new DatabaseException("Creating quiz failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating quiz: " + quiz.getTitle(), e);
        }
    }

    @Override
    public Quiz findById(int id) {
        String sql = "SELECT * FROM quizzes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToQuiz(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding quiz by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<Quiz> findAll() {
        String sql = "SELECT * FROM quizzes ORDER BY id DESC";
        List<Quiz> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToQuiz(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving all quizzes", e);
        }
        return list;
    }

    @Override
    public List<Quiz> findByCreatorId(int creatorId) {
        String sql = "SELECT * FROM quizzes WHERE creator_id = ? ORDER BY id DESC";
        List<Quiz> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToQuiz(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving quizzes by creator id: " + creatorId, e);
        }
        return list;
    }

    @Override
    public List<Quiz> findApprovedQuizzes() {
        return findByStatus(QuizStatus.APPROVED);
    }

    @Override
    public List<Quiz> findPendingApproval() {
        return findByStatus(QuizStatus.PENDING_APPROVAL);
    }

    @Override
    public List<Quiz> findByStatus(QuizStatus status) {
        String sql = "SELECT * FROM quizzes WHERE status = ? ORDER BY id DESC";
        List<Quiz> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToQuiz(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving quizzes by status: " + status, e);
        }
        return list;
    }

    @Override
    public boolean updateQuiz(Quiz quiz) {
        String sql = "UPDATE quizzes SET title = ?, description = ?, duration_minutes = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, quiz.getTitle());
            ps.setString(2, quiz.getDescription());
            ps.setInt(3, quiz.getDurationMinutes());
            ps.setString(4, quiz.getStatus().name());
            ps.setInt(5, quiz.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating quiz id: " + quiz.getId(), e);
        }
    }

    @Override
    public boolean deleteQuiz(int id) {
        String sql = "DELETE FROM quizzes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                throw new DatabaseException("This quiz cannot be deleted because participant attempts exist. It can be archived or kept.", e);
            }
            throw new DatabaseException("Error deleting quiz id: " + id, e);
        }
    }

    @Override
    public boolean updateStatus(int quizId, QuizStatus status) {
        String sql = "UPDATE quizzes SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, quizId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating status for quiz id: " + quizId, e);
        }
    }

    @Override
    public int getQuizCountByCreator(int creatorId) {
        String sql = "SELECT COUNT(*) FROM quizzes WHERE creator_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting quizzes for creator id: " + creatorId, e);
        }
        return 0;
    }

    @Override
    public int getQuizCountByCreatorAndStatus(int creatorId, QuizStatus status) {
        String sql = "SELECT COUNT(*) FROM quizzes WHERE creator_id = ? AND status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, creatorId);
            ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting quizzes by status for creator id: " + creatorId, e);
        }
        return 0;
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
    public int getTotalQuestionsCountByCreator(int creatorId) {
        String sql = "SELECT COUNT(*) FROM questions q JOIN quizzes z ON q.quiz_id = z.id WHERE z.creator_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting total questions for creator id: " + creatorId, e);
        }
        return 0;
    }

    @Override
    public int getTotalAttemptsCountByCreator(int creatorId) {
        String sql = "SELECT COUNT(*) FROM quiz_attempts a JOIN quizzes z ON a.quiz_id = z.id WHERE z.creator_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting total attempts for creator id: " + creatorId, e);
        }
        return 0;
    }

    private Quiz mapResultSetToQuiz(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");

        return new Quiz(
                rs.getInt("id"),
                rs.getInt("creator_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("duration_minutes"),
                QuizStatus.valueOf(rs.getString("status")),
                createdTs != null ? createdTs.toLocalDateTime() : null,
                updatedTs != null ? updatedTs.toLocalDateTime() : null
        );
    }
}
