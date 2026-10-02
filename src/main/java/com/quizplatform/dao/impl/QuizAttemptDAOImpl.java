package com.quizplatform.dao.impl;

import com.quizplatform.config.DatabaseConnection;
import com.quizplatform.dao.QuizAttemptDAO;
import com.quizplatform.exception.DatabaseException;
import com.quizplatform.model.AttemptStatus;
import com.quizplatform.model.QuizAttempt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of QuizAttemptDAO.
 */
public class QuizAttemptDAOImpl implements QuizAttemptDAO {

    @Override
    public int createAttempt(QuizAttempt attempt) {
        String sql = "INSERT INTO quiz_attempts (quiz_id, participant_id, status) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, attempt.getQuizId());
            ps.setInt(2, attempt.getParticipantId());
            ps.setString(3, attempt.getStatus().name());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating quiz attempt failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    attempt.setId(generatedId);
                    return generatedId;
                } else {
                    throw new DatabaseException("Creating quiz attempt failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating quiz attempt for participant: " + attempt.getParticipantId(), e);
        }
    }

    @Override
    public QuizAttempt findById(int id) {
        String sql = "SELECT * FROM quiz_attempts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttempt(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding quiz attempt by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<QuizAttempt> findByParticipantId(int participantId) {
        String sql = "SELECT * FROM quiz_attempts WHERE participant_id = ? ORDER BY id DESC";
        List<QuizAttempt> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, participantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttempt(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving attempts for participant: " + participantId, e);
        }
        return list;
    }

    @Override
    public List<QuizAttempt> findByQuizId(int quizId) {
        String sql = "SELECT * FROM quiz_attempts WHERE quiz_id = ? ORDER BY id DESC";
        List<QuizAttempt> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttempt(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving attempts for quiz: " + quizId, e);
        }
        return list;
    }

    @Override
    public List<QuizAttempt> findAll() {
        String sql = "SELECT * FROM quiz_attempts ORDER BY id DESC";
        List<QuizAttempt> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToAttempt(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving all quiz attempts", e);
        }
        return list;
    }

    @Override
    public boolean updateAttempt(QuizAttempt attempt) {
        String sql = "UPDATE quiz_attempts SET completed_at = ?, score = ?, total_questions = ?, " +
                     "correct_answers = ?, incorrect_answers = ?, unanswered_questions = ?, " +
                     "percentage = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, attempt.getCompletedAt() != null ? Timestamp.valueOf(attempt.getCompletedAt()) : null);
            ps.setInt(2, attempt.getScore());
            ps.setInt(3, attempt.getTotalQuestions());
            ps.setInt(4, attempt.getCorrectAnswers());
            ps.setInt(5, attempt.getIncorrectAnswers());
            ps.setInt(6, attempt.getUnansweredQuestions());
            ps.setDouble(7, attempt.getPercentage());
            ps.setString(8, attempt.getStatus().name());
            ps.setInt(9, attempt.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating quiz attempt id: " + attempt.getId(), e);
        }
    }

    @Override
    public double getAverageScoreByQuizId(int quizId) {
        String sql = "SELECT AVG(score) FROM quiz_attempts WHERE quiz_id = ? AND status IN ('COMPLETED', 'TIME_EXPIRED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting average score for quiz id: " + quizId, e);
        }
        return 0.0;
    }

    @Override
    public int getHighestScoreByQuizId(int quizId) {
        String sql = "SELECT MAX(score) FROM quiz_attempts WHERE quiz_id = ? AND status IN ('COMPLETED', 'TIME_EXPIRED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting highest score for quiz id: " + quizId, e);
        }
        return 0;
    }

    @Override
    public int getLowestScoreByQuizId(int quizId) {
        String sql = "SELECT MIN(score) FROM quiz_attempts WHERE quiz_id = ? AND status IN ('COMPLETED', 'TIME_EXPIRED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting lowest score for quiz id: " + quizId, e);
        }
        return 0;
    }

    @Override
    public int getTotalAttemptsCount() {
        String sql = "SELECT COUNT(*) FROM quiz_attempts";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting total attempts", e);
        }
        return 0;
    }

    @Override
    public int getCompletedAttemptsCount() {
        String sql = "SELECT COUNT(*) FROM quiz_attempts WHERE status IN ('COMPLETED', 'TIME_EXPIRED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting completed attempts", e);
        }
        return 0;
    }

    @Override
    public List<QuizAttempt> findByCreatorId(int creatorId) {
        String sql = "SELECT a.* FROM quiz_attempts a JOIN quizzes z ON a.quiz_id = z.id WHERE z.creator_id = ? ORDER BY a.id DESC";
        List<QuizAttempt> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttempt(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving attempts for creator: " + creatorId, e);
        }
        return list;
    }

    @Override
    public double getAverageScoreByCreatorId(int creatorId) {
        String sql = "SELECT AVG(a.score) FROM quiz_attempts a JOIN quizzes z ON a.quiz_id = z.id WHERE z.creator_id = ? AND a.status IN ('COMPLETED', 'TIME_EXPIRED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting average score for creator id: " + creatorId, e);
        }
        return 0.0;
    }

    @Override
    public int getHighestScoreByCreatorId(int creatorId) {
        String sql = "SELECT MAX(a.score) FROM quiz_attempts a JOIN quizzes z ON a.quiz_id = z.id WHERE z.creator_id = ? AND a.status IN ('COMPLETED', 'TIME_EXPIRED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting highest score for creator id: " + creatorId, e);
        }
        return 0;
    }

    @Override
    public int getLowestScoreByCreatorId(int creatorId) {
        String sql = "SELECT MIN(a.score) FROM quiz_attempts a JOIN quizzes z ON a.quiz_id = z.id WHERE z.creator_id = ? AND a.status IN ('COMPLETED', 'TIME_EXPIRED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting lowest score for creator id: " + creatorId, e);
        }
        return 0;
    }

    @Override
    public int getAttemptsCountByParticipant(int participantId) {
        String sql = "SELECT COUNT(*) FROM quiz_attempts WHERE participant_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting attempts for participant id: " + participantId, e);
        }
        return 0;
    }

    @Override
    public int getCompletedAttemptsCountByParticipant(int participantId) {
        String sql = "SELECT COUNT(*) FROM quiz_attempts WHERE participant_id = ? AND status IN ('COMPLETED', 'TIME_EXPIRED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting completed attempts for participant id: " + participantId, e);
        }
        return 0;
    }

    @Override
    public double getAveragePercentageByParticipant(int participantId) {
        String sql = "SELECT AVG(percentage) FROM quiz_attempts WHERE participant_id = ? AND status IN ('COMPLETED', 'TIME_EXPIRED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting average percentage for participant id: " + participantId, e);
        }
        return 0.0;
    }

    @Override
    public int getHighestScoreByParticipant(int participantId) {
        String sql = "SELECT MAX(score) FROM quiz_attempts WHERE participant_id = ? AND status IN ('COMPLETED', 'TIME_EXPIRED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting highest score for participant id: " + participantId, e);
        }
        return 0;
    }

    @Override
    public QuizAttempt findById(Connection conn, int id) {
        String sql = "SELECT * FROM quiz_attempts WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttempt(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding quiz attempt by id (transactional): " + id, e);
        }
        return null;
    }

    @Override
    public boolean updateAttempt(Connection conn, QuizAttempt attempt) {
        String sql = "UPDATE quiz_attempts SET completed_at = ?, score = ?, total_questions = ?, " +
                     "correct_answers = ?, incorrect_answers = ?, unanswered_questions = ?, " +
                     "percentage = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, attempt.getCompletedAt() != null ? Timestamp.valueOf(attempt.getCompletedAt()) : null);
            ps.setInt(2, attempt.getScore());
            ps.setInt(3, attempt.getTotalQuestions());
            ps.setInt(4, attempt.getCorrectAnswers());
            ps.setInt(5, attempt.getIncorrectAnswers());
            ps.setInt(6, attempt.getUnansweredQuestions());
            ps.setDouble(7, attempt.getPercentage());
            ps.setString(8, attempt.getStatus().name());
            ps.setInt(9, attempt.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating quiz attempt id (transactional): " + attempt.getId(), e);
        }
    }

    @Override
    public QuizAttempt findActiveAttemptByParticipantAndQuiz(int participantId, int quizId) {
        String sql = "SELECT * FROM quiz_attempts WHERE participant_id = ? AND quiz_id = ? AND status = 'IN_PROGRESS' ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            ps.setInt(2, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttempt(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding active attempt for participant: " + participantId + " and quiz: " + quizId, e);
        }
        return null;
    }

    @Override
    public int getTimeExpiredAttemptsCount() {
        String sql = "SELECT COUNT(*) FROM quiz_attempts WHERE status = 'TIME_EXPIRED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting time expired attempts", e);
        }
        return 0;
    }

    @Override
    public double getPlatformAverageScore() {
        String sql = "SELECT AVG(score) FROM quiz_attempts WHERE status IN ('COMPLETED', 'TIME_EXPIRED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating platform average score", e);
        }
        return 0.0;
    }

    @Override
    public List<QuizAttempt> findRecentAttempts(int limit) {
        String sql = "SELECT * FROM quiz_attempts ORDER BY id DESC LIMIT ?";
        List<QuizAttempt> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttempt(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving recent attempts", e);
        }
        return list;
    }

    @Override
    public double getAveragePercentageByCreatorId(int creatorId) {
        String sql = "SELECT AVG(a.percentage) FROM quiz_attempts a JOIN quizzes z ON a.quiz_id = z.id WHERE z.creator_id = ? AND a.status IN ('COMPLETED', 'TIME_EXPIRED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting average percentage for creator id: " + creatorId, e);
        }
        return 0.0;
    }

    @Override
    public double getHighestPercentageByCreatorId(int creatorId) {
        String sql = "SELECT MAX(a.percentage) FROM quiz_attempts a JOIN quizzes z ON a.quiz_id = z.id WHERE z.creator_id = ? AND a.status IN ('COMPLETED', 'TIME_EXPIRED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting highest percentage for creator id: " + creatorId, e);
        }
        return 0.0;
    }

    @Override
    public double getLowestPercentageByCreatorId(int creatorId) {
        String sql = "SELECT MIN(a.percentage) FROM quiz_attempts a JOIN quizzes z ON a.quiz_id = z.id WHERE z.creator_id = ? AND a.status IN ('COMPLETED', 'TIME_EXPIRED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting lowest percentage for creator id: " + creatorId, e);
        }
        return 0.0;
    }

    @Override
    public int getTimeExpiredAttemptsCountByCreator(int creatorId) {
        String sql = "SELECT COUNT(*) FROM quiz_attempts a JOIN quizzes z ON a.quiz_id = z.id WHERE z.creator_id = ? AND a.status = 'TIME_EXPIRED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting time expired attempts for creator: " + creatorId, e);
        }
        return 0;
    }

    @Override
    public int getTimeExpiredAttemptsCountByParticipant(int participantId) {
        String sql = "SELECT COUNT(*) FROM quiz_attempts WHERE participant_id = ? AND status = 'TIME_EXPIRED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting time expired attempts for participant: " + participantId, e);
        }
        return 0;
    }

    @Override
    public List<QuizAttempt> findByParticipantIdAndQuizId(int participantId, int quizId) {
        String sql = "SELECT * FROM quiz_attempts WHERE participant_id = ? AND quiz_id = ? ORDER BY id DESC";
        List<QuizAttempt> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, participantId);
            ps.setInt(2, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttempt(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving attempts for participant: " + participantId + " and quiz: " + quizId, e);
        }
        return list;
    }



    private QuizAttempt mapResultSetToAttempt(ResultSet rs) throws SQLException {
        Timestamp startedTs = rs.getTimestamp("started_at");
        Timestamp completedTs = rs.getTimestamp("completed_at");

        return new QuizAttempt(
                rs.getInt("id"),
                rs.getInt("quiz_id"),
                rs.getInt("participant_id"),
                startedTs != null ? startedTs.toLocalDateTime() : null,
                completedTs != null ? completedTs.toLocalDateTime() : null,
                rs.getInt("score"),
                rs.getInt("total_questions"),
                rs.getInt("correct_answers"),
                rs.getInt("incorrect_answers"),
                rs.getInt("unanswered_questions"),
                rs.getDouble("percentage"),
                AttemptStatus.valueOf(rs.getString("status"))
        );
    }
}
