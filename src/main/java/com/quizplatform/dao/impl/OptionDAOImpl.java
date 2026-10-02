package com.quizplatform.dao.impl;

import com.quizplatform.config.DatabaseConnection;
import com.quizplatform.dao.OptionDAO;
import com.quizplatform.exception.DatabaseException;
import com.quizplatform.model.Option;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of OptionDAO.
 */
public class OptionDAOImpl implements OptionDAO {

    @Override
    public int createOption(Option option) {
        String sql = "INSERT INTO options (question_id, option_text, option_label, is_correct) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, option.getQuestionId());
            ps.setString(2, option.getOptionText());
            ps.setString(3, option.getOptionLabel());
            ps.setBoolean(4, option.isCorrect());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating option failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    option.setId(generatedId);
                    return generatedId;
                } else {
                    throw new DatabaseException("Creating option failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating option for question id: " + option.getQuestionId(), e);
        }
    }

    @Override
    public Option findById(int id) {
        String sql = "SELECT * FROM options WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOption(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding option by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<Option> findByQuestionId(int questionId) {
        String sql = "SELECT * FROM options WHERE question_id = ? ORDER BY option_label ASC";
        List<Option> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOption(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving options for question id: " + questionId, e);
        }
        return list;
    }

    @Override
    public Option findCorrectOptionByQuestionId(int questionId) {
        String sql = "SELECT * FROM options WHERE question_id = ? AND is_correct = TRUE LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOption(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding correct option for question id: " + questionId, e);
        }
        return null;
    }

    @Override
    public Option findCorrectOptionByQuestionId(Connection conn, int questionId) {
        String sql = "SELECT * FROM options WHERE question_id = ? AND is_correct = TRUE LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOption(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding correct option in transaction for question id: " + questionId, e);
        }
        return null;
    }

    @Override
    public boolean updateOption(Option option) {
        String sql = "UPDATE options SET option_text = ?, option_label = ?, is_correct = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, option.getOptionText());
            ps.setString(2, option.getOptionLabel());
            ps.setBoolean(3, option.isCorrect());
            ps.setInt(4, option.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating option id: " + option.getId(), e);
        }
    }

    @Override
    public boolean deleteOption(int id) {
        String sql = "DELETE FROM options WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting option id: " + id, e);
        }
    }

    @Override
    public boolean deleteByQuestionId(int questionId) {
        String sql = "DELETE FROM options WHERE question_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, questionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting options for question id: " + questionId, e);
        }
    }


    private Option mapResultSetToOption(ResultSet rs) throws SQLException {
        return new Option(
                rs.getInt("id"),
                rs.getInt("question_id"),
                rs.getString("option_text"),
                rs.getString("option_label"),
                rs.getBoolean("is_correct")
        );
    }
}
