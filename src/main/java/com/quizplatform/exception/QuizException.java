package com.quizplatform.exception;

/**
 * Exception thrown for domain-level quiz rule violations.
 * Part of the OOP custom exception hierarchy.
 */
public class QuizException extends QuizPlatformException {

    public QuizException(String message) {
        super(message);
    }

    public QuizException(String message, Throwable cause) {
        super(message, cause);
    }
}
