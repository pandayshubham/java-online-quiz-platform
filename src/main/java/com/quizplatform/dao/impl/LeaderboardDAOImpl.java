package com.quizplatform.dao.impl;

import com.quizplatform.config.DatabaseConnection;
import com.quizplatform.dao.LeaderboardDAO;
import com.quizplatform.exception.DatabaseException;
import com.quizplatform.model.LeaderboardEntry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JDBC implementation of LeaderboardDAO.
 */
public class LeaderboardDAOImpl implements LeaderboardDAO {

    @Override
    public List<LeaderboardEntry> getQuizLeaderboard(int quizId) {
        String sql = "SELECT a.participant_id, u.name AS participant_name, u.email AS participant_email, " +
                     "a.quiz_id, q.title AS quiz_title, a.score, a.total_questions, a.percentage, a.completed_at " +
                     "FROM quiz_attempts a " +
                     "JOIN users u ON a.participant_id = u.id " +
                     "JOIN quizzes q ON a.quiz_id = q.id " +
                     "WHERE a.quiz_id = ? AND a.status IN ('COMPLETED', 'TIME_EXPIRED') " +
                     "ORDER BY a.score DESC, a.percentage DESC, a.completed_at ASC";

        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        Set<Integer> seenParticipants = new HashSet<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    int participantId = rs.getInt("participant_id");
                    if (seenParticipants.add(participantId)) {
                        Timestamp completedTs = rs.getTimestamp("completed_at");
                        LeaderboardEntry entry = new LeaderboardEntry(
                                rank++,
                                participantId,
                                rs.getString("participant_name"),
                                rs.getString("participant_email"),
                                rs.getInt("quiz_id"),
                                rs.getString("quiz_title"),
                                rs.getInt("score"),
                                rs.getInt("total_questions"),
                                rs.getDouble("percentage"),
                                completedTs != null ? completedTs.toLocalDateTime() : null
                        );
                        leaderboard.add(entry);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving quiz leaderboard for quiz ID: " + quizId, e);
        }
        return leaderboard;
    }

    @Override
    public List<LeaderboardEntry> getGlobalLeaderboard() {
        String sql = "SELECT u.id AS participant_id, u.name AS participant_name, u.email AS participant_email, " +
                     "COUNT(DISTINCT a.quiz_id) AS quizzes_completed, " +
                     "SUM(a.score) AS total_score, " +
                     "ROUND(AVG(a.percentage), 2) AS avg_percentage, " +
                     "MAX(a.completed_at) AS last_active " +
                     "FROM users u " +
                     "JOIN quiz_attempts a ON u.id = a.participant_id " +
                     "WHERE u.role = 'PARTICIPANT' AND a.status IN ('COMPLETED', 'TIME_EXPIRED') " +
                     "GROUP BY u.id, u.name, u.email " +
                     "ORDER BY total_score DESC, avg_percentage DESC, quizzes_completed DESC, u.name ASC";

        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int rank = 1;
            while (rs.next()) {
                Timestamp lastActiveTs = rs.getTimestamp("last_active");
                LeaderboardEntry entry = new LeaderboardEntry(
                        rank++,
                        rs.getInt("participant_id"),
                        rs.getString("participant_name"),
                        rs.getString("participant_email"),
                        rs.getInt("quizzes_completed"),
                        rs.getInt("total_score"),
                        rs.getDouble("avg_percentage"),
                        lastActiveTs != null ? lastActiveTs.toLocalDateTime() : null
                );
                leaderboard.add(entry);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving global platform leaderboard", e);
        }
        return leaderboard;
    }
}
