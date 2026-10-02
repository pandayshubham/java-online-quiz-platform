package com.quizplatform.model.report;

/**
 * Summary metrics for a quiz creator across all their authored quizzes.
 */
public class CreatorPerformanceSummary {

    private int creatorId;
    private int totalAttempts;
    private int completedAttempts;
    private int timeExpiredAttempts;
    private double averagePercentage;
    private double highestPercentage;
    private double lowestPercentage;
    private double averageScore;
    private int highestScore;
    private int lowestScore;

    public CreatorPerformanceSummary() {
    }

    public CreatorPerformanceSummary(int creatorId, int totalAttempts, int completedAttempts,
                                    int timeExpiredAttempts, double averagePercentage,
                                    double highestPercentage, double lowestPercentage,
                                    double averageScore, int highestScore, int lowestScore) {
        this.creatorId = creatorId;
        this.totalAttempts = totalAttempts;
        this.completedAttempts = completedAttempts;
        this.timeExpiredAttempts = timeExpiredAttempts;
        this.averagePercentage = averagePercentage;
        this.highestPercentage = highestPercentage;
        this.lowestPercentage = lowestPercentage;
        this.averageScore = averageScore;
        this.highestScore = highestScore;
        this.lowestScore = lowestScore;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
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

    public double getAveragePercentage() {
        return averagePercentage;
    }

    public void setAveragePercentage(double averagePercentage) {
        this.averagePercentage = averagePercentage;
    }

    public double getHighestPercentage() {
        return highestPercentage;
    }

    public void setHighestPercentage(double highestPercentage) {
        this.highestPercentage = highestPercentage;
    }

    public double getLowestPercentage() {
        return lowestPercentage;
    }

    public void setLowestPercentage(double lowestPercentage) {
        this.lowestPercentage = lowestPercentage;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(int highestScore) {
        this.highestScore = highestScore;
    }

    public int getLowestScore() {
        return lowestScore;
    }

    public void setLowestScore(int lowestScore) {
        this.lowestScore = lowestScore;
    }
}
