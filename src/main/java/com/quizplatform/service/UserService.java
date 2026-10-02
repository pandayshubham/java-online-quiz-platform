package com.quizplatform.service;

import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import java.util.List;

/**
 * Service interface for user operations.
 */
public interface UserService {

    User getUserById(int id);

    User getUserByEmail(String email);

    List<User> getAllUsers();

    List<User> getUsersByRole(UserRole role);

    boolean registerUser(User user);

    User createUser(User user);

    boolean updateUser(User user);

    boolean deleteUser(int id);

    boolean deactivateUser(int id);

    boolean activateUser(int id);

    boolean setUserActiveStatus(int id, boolean active);

    boolean emailExists(String email);

    List<User> searchUsers(String searchText);

    List<User> filterUsers(UserRole role, Boolean active);

    List<User> searchAndFilterUsers(String searchText, UserRole role, Boolean active);

    int getTotalUsers();

    int getActiveUsers();

    int getInactiveUsers();

    int getUserCountByRole(UserRole role);
}
