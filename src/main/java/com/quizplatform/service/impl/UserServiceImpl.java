package com.quizplatform.service.impl;

import com.quizplatform.dao.UserDAO;
import com.quizplatform.dao.impl.UserDAOImpl;
import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import com.quizplatform.service.UserService;
import com.quizplatform.util.SessionManager;
import java.util.List;

/**
 * Implementation of UserService.
 */
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;

    public UserServiceImpl() {
        this(new UserDAOImpl());
    }

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    private void checkAdminAccess() {
        if (SessionManager.isLoggedIn() && !SessionManager.hasRole(UserRole.ADMIN)) {
            throw new SecurityException("Access Denied: Only administrators can manage users.");
        }
    }

    @Override
    public User getUserById(int id) {
        return userDAO.findById(id);
    }

    @Override
    public User getUserByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    @Override
    public List<User> getUsersByRole(UserRole role) {
        return userDAO.findByRole(role);
    }

    @Override
    public boolean registerUser(User user) {
        if (user == null || userDAO.existsByEmail(user.getEmail())) {
            return false;
        }
        return userDAO.createUser(user) > 0;
    }

    @Override
    public User createUser(User user) {
        checkAdminAccess();
        if (user == null) {
            throw new IllegalArgumentException("User object cannot be null.");
        }
        if (userDAO.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        int id = userDAO.createUser(user);
        user.setId(id);
        return user;
    }

    @Override
    public boolean updateUser(User user) {
        checkAdminAccess();
        if (user == null) {
            return false;
        }
        return userDAO.updateUser(user);
    }

    @Override
    public boolean deleteUser(int id) {
        checkAdminAccess();
        return userDAO.deleteUser(id);
    }

    @Override
    public boolean deactivateUser(int id) {
        checkAdminAccess();
        return userDAO.updateActiveStatus(id, false);
    }

    @Override
    public boolean activateUser(int id) {
        checkAdminAccess();
        return userDAO.updateActiveStatus(id, true);
    }

    @Override
    public boolean setUserActiveStatus(int id, boolean active) {
        checkAdminAccess();
        return userDAO.updateActiveStatus(id, active);
    }

    @Override
    public boolean emailExists(String email) {
        return userDAO.existsByEmail(email);
    }

    @Override
    public List<User> searchUsers(String searchText) {
        return userDAO.searchByNameOrEmail(searchText);
    }

    @Override
    public List<User> filterUsers(UserRole role, Boolean active) {
        return userDAO.searchAndFilter(null, role, active);
    }

    @Override
    public List<User> searchAndFilterUsers(String searchText, UserRole role, Boolean active) {
        return userDAO.searchAndFilter(searchText, role, active);
    }

    @Override
    public int getTotalUsers() {
        return userDAO.getTotalUsersCount();
    }

    @Override
    public int getActiveUsers() {
        return userDAO.getActiveUsersCount(true);
    }

    @Override
    public int getInactiveUsers() {
        return userDAO.getActiveUsersCount(false);
    }

    @Override
    public int getUserCountByRole(UserRole role) {
        return userDAO.getUserCountByRole(role);
    }
}
