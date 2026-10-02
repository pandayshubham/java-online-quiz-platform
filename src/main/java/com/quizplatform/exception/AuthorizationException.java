package com.quizplatform.exception;

/**
 * Exception thrown when a user attempts an unauthorized operation.
 * Part of the OOP custom exception hierarchy.
 */
public class AuthorizationException extends QuizPlatformException {

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
