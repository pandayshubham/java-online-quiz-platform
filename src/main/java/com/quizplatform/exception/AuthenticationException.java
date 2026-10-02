package com.quizplatform.exception;

/**
 * Exception thrown when authentication fails due to invalid credentials,
 * validation errors, or inactive account status.
 * Demonstrates inheritance in custom exception hierarchy.
 */
public class AuthenticationException extends QuizPlatformException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
