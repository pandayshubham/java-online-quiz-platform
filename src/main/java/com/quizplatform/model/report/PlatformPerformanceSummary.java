package com.quizplatform.model.report;

/**
 * System-wide performance overview for administrators.
 */
public class PlatformPerformanceSummary {

    private int totalQuizAttempts;
    private int completedAttempts;
    private int timeExpiredAttempts;
    private double averagePlatformScore;
    private int totalQuizQuestions;
    private int totalAnswers;

    public PlatformPerformanceSummary() {
    }

    public PlatformPerformanceSummary(int totalQuizAttempts, int completedAttempts, int timeExpiredAttempts,
                                    double averagePlatformScore, int totalQuizQuestions, int totalAnswers) {
        this.totalQuizAttempts = totalQuizAttempts;
        this.completedAttempts = completedAttempts;
        this.timeExpiredAttempts = timeExpiredAttempts;
        this.averagePlatformScore = averagePlatformScore;
        this.totalQuizQuestions = totalQuizQuestions;
        this.totalAnswers = totalAnswers;
    }

    public int getTotalQuizAttempts() {
        return totalQuizAttempts;
    }

    public void setTotalQuizAttempts(int totalQuizAttempts) {
        this.totalQuizAttempts = totalQuizAttempts;
    }

    public int getCompletedAttempts() {
        return completedAttempts;
    }

    public void setCompletedAttempts(int completedAttempts) {
        this.completedAttempts = completedAttempts;
    }

    public int getTimeExpiredAttempts() {
        return timeExpiredAttempts;
    }

    public void setTimeExpiredAttempts(int timeExpiredAttempts) {
        this.timeExpiredAttempts = timeExpiredAttempts;
    }

    public double getAveragePlatformScore() {
        return averagePlatformScore;
    }

    public void setAveragePlatformScore(double averagePlatformScore) {
        this.averagePlatformScore = averagePlatformScore;
    }

    public int getTotalQuizQuestions() {
        return totalQuizQuestions;
    }

    public void setTotalQuizQuestions(int totalQuizQuestions) {
        this.totalQuizQuestions = totalQuizQuestions;
    }

    public int getTotalAnswers() {
        return totalAnswers;
    }

    public void setTotalAnswers(int totalAnswers) {
        this.totalAnswers = totalAnswers;
    }
}
