package com.quizplatform.exception;

/**
 * Custom runtime exception to wrap database and SQL errors in DAO operations.
 * Demonstrates inheritance in custom exception hierarchy.
 */
public class DatabaseException extends QuizPlatformException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
