package com.quizplatform.dao;

import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import java.util.List;

/**
 * Data Access Object interface for User entities.
 */
public interface UserDAO {

    int createUser(User user);

    User findById(int id);

    User findByEmail(String email);

    List<User> findAll();

    List<User> findByRole(UserRole role);

    boolean updateUser(User user);

    boolean deleteUser(int id);

    boolean updateActiveStatus(int id, boolean active);

    boolean existsByEmail(String email);

    List<User> searchAndFilter(String query, UserRole role, Boolean active);

    List<User> searchByNameOrEmail(String query);

    List<User> findByActiveStatus(boolean active);

    int getTotalUsersCount();

    int getActiveUsersCount(boolean active);

    int getUserCountByRole(UserRole role);
}
