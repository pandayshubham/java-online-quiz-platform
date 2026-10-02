package com.quizplatform.model.report;

import java.util.ArrayList;
import java.util.List;

/**
 * Performance metrics aggregated for a specific quiz (for participants or creators).
 */
public class QuizPerformanceSummary {

    private int quizId;
    private String quizTitle;

    // Participant-specific metrics
    private int attemptsCount;
    private int bestScore;
    private double averageScore;
    private int latestScore;
    private double highestPercentage;
    private String latestStatus;

    // Creator-specific metrics
    private int totalAttempts;
    private int completedAttempts;
    private int timeExpiredAttempts;
    private int highestScore;
    private int lowestScore;
    private double averagePercentage;
    private List<QuestionAnalytics> questionAnalytics = new ArrayList<>();

    public QuizPerformanceSummary() {
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

    public int getAttemptsCount() {
        return attemptsCount;
    }

    public void setAttemptsCount(int attemptsCount) {
        this.attemptsCount = attemptsCount;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public int getLatestScore() {
        return latestScore;
    }

    public void setLatestScore(int latestScore) {
        this.latestScore = latestScore;
    }

    public double getHighestPercentage() {
        return highestPercentage;
    }

    public void setHighestPercentage(double highestPercentage) {
        this.highestPercentage = highestPercentage;
    }

    public String getLatestStatus() {
        return latestStatus;
    }

    public void setLatestStatus(String latestStatus) {
        this.latestStatus = latestStatus;
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

    public double getAveragePercentage() {
        return averagePercentage;
    }

    public void setAveragePercentage(double averagePercentage) {
        this.averagePercentage = averagePercentage;
    }

    public List<QuestionAnalytics> getQuestionAnalytics() {
        return questionAnalytics;
    }

    public void setQuestionAnalytics(List<QuestionAnalytics> questionAnalytics) {
        this.questionAnalytics = questionAnalytics;
    }
}
