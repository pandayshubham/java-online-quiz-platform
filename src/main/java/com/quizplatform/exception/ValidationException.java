package com.quizplatform.exception;

/**
 * Exception thrown when input validation fails in the service or UI layer.
 * Demonstrates inheritance in custom exception hierarchy.
 */
public class ValidationException extends QuizPlatformException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
