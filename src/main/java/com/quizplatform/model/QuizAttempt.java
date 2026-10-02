package com.quizplatform.model;

import java.time.LocalDateTime;

/**
 * Model representing a participant's attempt at taking a quiz.
 */
public class QuizAttempt {

    private int id;
    private int quizId;
    private int participantId;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private int score;
    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;
    private int unansweredQuestions;
    private double percentage;
    private AttemptStatus status;

    public QuizAttempt() {
        this.status = AttemptStatus.IN_PROGRESS;
    }

    public QuizAttempt(int quizId, int participantId) {
        this.quizId = quizId;
        this.participantId = participantId;
        this.status = AttemptStatus.IN_PROGRESS;
    }

    public QuizAttempt(int id, int quizId, int participantId, LocalDateTime startedAt, LocalDateTime completedAt,
                       int score, int totalQuestions, int correctAnswers, int incorrectAnswers,
                       int unansweredQuestions, double percentage, AttemptStatus status) {
        this.id = id;
        this.quizId = quizId;
        this.participantId = participantId;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.incorrectAnswers = incorrectAnswers;
        this.unansweredQuestions = unansweredQuestions;
        this.percentage = percentage;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void setIncorrectAnswers(int incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }

    public int getUnansweredQuestions() {
        return unansweredQuestions;
    }

    public void setUnansweredQuestions(int unansweredQuestions) {
        this.unansweredQuestions = unansweredQuestions;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "QuizAttempt{" +
                "id=" + id +
                ", quizId=" + quizId +
                ", participantId=" + participantId +
                ", startedAt=" + startedAt +
                ", completedAt=" + completedAt +
                ", score=" + score +
                ", totalQuestions=" + totalQuestions +
                ", correctAnswers=" + correctAnswers +
                ", percentage=" + percentage +
                ", status=" + status +
                '}';
    }
}
