package com.quizplatform.service;

import com.quizplatform.exception.AuthenticationException;
import com.quizplatform.model.User;

/**
 * Service interface for user authentication and credential verification.
 */
public interface AuthenticationService {

    /**
     * Authenticates a user with email and password.
     * 
     * @param email user email
     * @param password user password (plain-text for academic demo)
     * @return authenticated User object
     * @throws AuthenticationException if validation fails, account is inactive, or credentials do not match
     */
    User authenticate(String email, String password) throws AuthenticationException;
}
