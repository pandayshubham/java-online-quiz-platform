package com.quizplatform.exception;

/**
 * Root custom unchecked exception for the Quiz Platform.
 * Demonstrates an object-oriented custom exception hierarchy.
 */
public class QuizPlatformException extends RuntimeException {

    public QuizPlatformException(String message) {
        super(message);
    }

    public QuizPlatformException(String message, Throwable cause) {
        super(message, cause);
    }
}
