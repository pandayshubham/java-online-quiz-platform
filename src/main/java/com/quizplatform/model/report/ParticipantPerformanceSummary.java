package com.quizplatform.model.report;

/**
 * Summary metrics for a participant across all their quiz attempts.
 */
public class ParticipantPerformanceSummary {

    private int participantId;
    private int totalAttempts;
    private int completedAttempts;
    private int timeExpiredAttempts;
    private double averageScore;
    private int bestScore;
    private int totalQuestionsAnswered;
    private int totalCorrectAnswers;
    private int totalIncorrectAnswers;
    private int totalUnansweredQuestions;
    private double averagePercentage;

    public ParticipantPerformanceSummary() {
    }

    public ParticipantPerformanceSummary(int participantId, int totalAttempts, int completedAttempts,
                                         int timeExpiredAttempts, double averageScore, int bestScore,
                                         int totalQuestionsAnswered, int totalCorrectAnswers,
                                         int totalIncorrectAnswers, int totalUnansweredQuestions,
                                         double averagePercentage) {
        this.participantId = participantId;
        this.totalAttempts = totalAttempts;
        this.completedAttempts = completedAttempts;
        this.timeExpiredAttempts = timeExpiredAttempts;
        this.averageScore = averageScore;
        this.bestScore = bestScore;
        this.totalQuestionsAnswered = totalQuestionsAnswered;
        this.totalCorrectAnswers = totalCorrectAnswers;
        this.totalIncorrectAnswers = totalIncorrectAnswers;
        this.totalUnansweredQuestions = totalUnansweredQuestions;
        this.averagePercentage = averagePercentage;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
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

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }

    public int getTotalQuestionsAnswered() {
        return totalQuestionsAnswered;
    }

    public void setTotalQuestionsAnswered(int totalQuestionsAnswered) {
        this.totalQuestionsAnswered = totalQuestionsAnswered;
    }

    public int getTotalCorrectAnswers() {
        return totalCorrectAnswers;
    }

    public void setTotalCorrectAnswers(int totalCorrectAnswers) {
        this.totalCorrectAnswers = totalCorrectAnswers;
    }

    public int getTotalIncorrectAnswers() {
        return totalIncorrectAnswers;
    }

    public void setTotalIncorrectAnswers(int totalIncorrectAnswers) {
        this.totalIncorrectAnswers = totalIncorrectAnswers;
    }

    public int getTotalUnansweredQuestions() {
        return totalUnansweredQuestions;
    }

    public void setTotalUnansweredQuestions(int totalUnansweredQuestions) {
        this.totalUnansweredQuestions = totalUnansweredQuestions;
    }

    public double getAveragePercentage() {
        return averagePercentage;
    }

    public void setAveragePercentage(double averagePercentage) {
        this.averagePercentage = averagePercentage;
    }
}
