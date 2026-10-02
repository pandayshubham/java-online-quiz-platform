package com.quizplatform.util;

import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;

/**
 * Global in-memory session manager for the currently authenticated user.
 * Thread-safe singleton/static utility for the desktop application.
 * Note: Never retains raw passwords in memory.
 */
public class SessionManager {

    private static User currentUser = null;

    private SessionManager() {
    }

    /**
     * Initializes the session with the authenticated user.
     * Sanitizes the user object to ensure passwords are removed from memory.
     */
    public static synchronized void login(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null during session login.");
        }
        // Clone user details without password for memory security
        currentUser = new User(
                user.getId(),
                user.getName(),
                user.getEmail(),
                null, // Password cleared from session
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    /**
     * Returns the currently authenticated user, or null if no active session exists.
     */
    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks if a user is currently logged into the session.
     */
    public static synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Terminates the current session by clearing stored user details.
     */
    public static synchronized void logout() {
        currentUser = null;
    }

    /**
     * Checks if the active user possesses the specified role.
     */
    public static synchronized boolean hasRole(UserRole role) {
        return currentUser != null && currentUser.getRole() == role;
    }
}
