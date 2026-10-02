package com.quizplatform.service.impl;

import com.quizplatform.dao.UserDAO;
import com.quizplatform.dao.impl.UserDAOImpl;
import com.quizplatform.exception.AuthenticationException;
import com.quizplatform.model.User;
import com.quizplatform.service.AuthenticationService;
import com.quizplatform.util.ValidationUtil;

/**
 * Implementation of AuthenticationService.
 * Validates inputs, fetches credentials via UserDAO, and safely evaluates user state.
 */
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserDAO userDAO;

    public AuthenticationServiceImpl() {
        this(new UserDAOImpl());
    }

    public AuthenticationServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User authenticate(String email, String password) throws AuthenticationException {
        // 1. Validate email input
        if (ValidationUtil.isBlank(email)) {
            throw new AuthenticationException("Email cannot be empty.");
        }

        String trimmedEmail = email.trim();
        if (!ValidationUtil.isValidEmail(trimmedEmail)) {
            throw new AuthenticationException("Invalid email format.");
        }

        // 2. Validate password input
        if (ValidationUtil.isBlank(password)) {
            throw new AuthenticationException("Password cannot be empty.");
        }

        // 3. Find user in database
        User user = userDAO.findByEmail(trimmedEmail);

        // 4. Check user existence (generic message to prevent email enumeration)
        if (user == null) {
            throw new AuthenticationException("Invalid email or password.");
        }

        // 5. Check if user account is active
        if (!user.isActive()) {
            throw new AuthenticationException("Your account has been deactivated. Please contact the administrator.");
        }

        // 6. Verify password (isolated comparison logic)
        if (!verifyPassword(password, user.getPassword())) {
            throw new AuthenticationException("Invalid email or password.");
        }

        // 7. Return authenticated user with password sanitized
        user.setPassword(null);
        return user;
    }

    /**
     * Isolated password verification method.
     * Complies with project rule: standard Java only, no BCrypt/external libraries.
     */
    private boolean verifyPassword(String inputPassword, String storedPassword) {
        if (inputPassword == null || storedPassword == null) {
            return false;
        }
        return inputPassword.equals(storedPassword);
    }
}
