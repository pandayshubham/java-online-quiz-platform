package com.quizplatform.dao.impl;

import com.quizplatform.config.DatabaseConnection;
import com.quizplatform.dao.UserDAO;
import com.quizplatform.exception.DatabaseException;
import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of UserDAO.
 */
public class UserDAOImpl implements UserDAO {

    // Demonstrates JDBC CRUD - CREATE: PreparedStatement, RETURN_GENERATED_KEYS, try-with-resources
    @Override
    public int createUser(User user) {
        String sql = "INSERT INTO users (name, email, password, role, is_active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole().name());
            ps.setBoolean(5, user.isActive());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    user.setId(generatedId);
                    return generatedId;
                } else {
                    throw new DatabaseException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating user with email " + user.getEmail(), e);
        }
    }

    // Demonstrates JDBC CRUD - READ: PreparedStatement, ResultSet query execution and mapping
    @Override
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by id: " + id, e);
        }
        return null;
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by email: " + email, e);
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id ASC";
        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving all users", e);
        }
        return list;
    }

    @Override
    public List<User> findByRole(UserRole role) {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY id ASC";
        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving users by role: " + role, e);
        }
        return list;
    }

    // Demonstrates JDBC CRUD - UPDATE: PreparedStatement with executeUpdate
    @Override
    public boolean updateUser(User user) {
        boolean updatePassword = user.getPassword() != null && !user.getPassword().trim().isEmpty();
        String sql = updatePassword
                ? "UPDATE users SET name = ?, email = ?, password = ?, role = ?, is_active = ? WHERE id = ?"
                : "UPDATE users SET name = ?, email = ?, role = ?, is_active = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            ps.setString(paramIndex++, user.getName());
            ps.setString(paramIndex++, user.getEmail());
            if (updatePassword) {
                ps.setString(paramIndex++, user.getPassword());
            }
            ps.setString(paramIndex++, user.getRole().name());
            ps.setBoolean(paramIndex++, user.isActive());
            ps.setInt(paramIndex, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating user with id: " + user.getId(), e);
        }
    }

    // Demonstrates JDBC CRUD - DELETE: PreparedStatement with executeUpdate
    @Override
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // MySQL error code 1451 indicates foreign key constraint failure
            if (e.getErrorCode() == 1451 || (e.getMessage() != null && e.getMessage().contains("foreign key constraint fails"))) {
                throw new DatabaseException("This user cannot be deleted because related records exist. You can deactivate the account instead.", e);
            }
            throw new DatabaseException("Error deleting user with id: " + id, e);
        }
    }

    @Override
    public boolean updateActiveStatus(int id, boolean active) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating active status for user id: " + id, e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking user existence by email: " + email, e);
        }
    }

    @Override
    public List<User> searchAndFilter(String query, UserRole role, Boolean active) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(email) LIKE ?)");
            String pattern = "%" + query.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        if (role != null) {
            sql.append(" AND role = ?");
            params.add(role.name());
        }

        if (active != null) {
            sql.append(" AND is_active = ?");
            params.add(active);
        }

        sql.append(" ORDER BY id ASC");

        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String s) {
                    ps.setString(i + 1, s);
                } else if (p instanceof Boolean b) {
                    ps.setBoolean(i + 1, b);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching and filtering users", e);
        }
        return list;
    }

    @Override
    public List<User> searchByNameOrEmail(String query) {
        return searchAndFilter(query, null, null);
    }

    @Override
    public List<User> findByActiveStatus(boolean active) {
        return searchAndFilter(null, null, active);
    }

    @Override
    public int getTotalUsersCount() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting total users", e);
        }
        return 0;
    }

    @Override
    public int getActiveUsersCount(boolean active) {
        String sql = "SELECT COUNT(*) FROM users WHERE is_active = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting users by active status: " + active, e);
        }
        return 0;
    }

    @Override
    public int getUserCountByRole(UserRole role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting users by role: " + role, e);
        }
        return 0;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");

        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                UserRole.valueOf(rs.getString("role")),
                rs.getBoolean("is_active"),
                createdTs != null ? createdTs.toLocalDateTime() : null,
                updatedTs != null ? updatedTs.toLocalDateTime() : null
        );
    }
}
