package com.quizplatform.exception;

/**
 * Exception thrown for quiz attempt state or lifecycle errors.
 * Demonstrates multi-level inheritance in the custom exception hierarchy.
 */
public class AttemptException extends QuizException {

    public AttemptException(String message) {
        super(message);
    }

    public AttemptException(String message, Throwable cause) {
        super(message, cause);
    }
}
