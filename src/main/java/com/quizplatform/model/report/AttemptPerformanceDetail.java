package com.quizplatform.model.report;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Detailed attempt performance model with high-level metrics and
 * question-by-question breakdown.
 */
public class AttemptPerformanceDetail {

    private int attemptId;
    private int quizId;
    private String quizTitle;
    private LocalDateTime attemptDate;
    private String status;
    private int score;
    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;
    private int unansweredQuestions;
    private double percentage;
    private List<QuestionResultDetail> questionDetails = new ArrayList<>();

    public AttemptPerformanceDetail() {
    }

    public AttemptPerformanceDetail(int attemptId, int quizId, String quizTitle,
                                   LocalDateTime attemptDate, String status,
                                   int score, int totalQuestions, int correctAnswers,
                                   int incorrectAnswers, int unansweredQuestions,
                                   double percentage, List<QuestionResultDetail> questionDetails) {
        this.attemptId = attemptId;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.attemptDate = attemptDate;
        this.status = status;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.incorrectAnswers = incorrectAnswers;
        this.unansweredQuestions = unansweredQuestions;
        this.percentage = percentage;
        this.questionDetails = questionDetails != null ? questionDetails : new ArrayList<>();
    }

    public int getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(int attemptId) {
        this.attemptId = attemptId;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public LocalDateTime getAttemptDate() {
        return attemptDate;
    }

    public void setAttemptDate(LocalDateTime attemptDate) {
        this.attemptDate = attemptDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public List<QuestionResultDetail> getQuestionDetails() {
        return questionDetails;
    }

    public void setQuestionDetails(List<QuestionResultDetail> questionDetails) {
        this.questionDetails = questionDetails;
    }
}
