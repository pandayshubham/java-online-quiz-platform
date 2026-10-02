package com.quizplatform.model;

import java.time.LocalDateTime;

/**
 * Model representing an entry in a quiz-specific or global platform leaderboard.
 */
public class LeaderboardEntry {

    private int rank;
    private int participantId;
    private String participantName;
    private String participantEmail;
    private int quizId;
    private String quizTitle;
    private int score;
    private int totalQuestions;
    private double percentage;
    private LocalDateTime completedAt;

    // Additional aggregated fields for global platform leaderboard
    private int quizzesCompleted;
    private int totalScore;
    private double averagePercentage;

    public LeaderboardEntry() {
    }

    /**
     * Constructor for Quiz-specific leaderboard entry.
     */
    public LeaderboardEntry(int rank, int participantId, String participantName, String participantEmail,
                            int quizId, String quizTitle, int score, int totalQuestions,
                            double percentage, LocalDateTime completedAt) {
        this.rank = rank;
        this.participantId = participantId;
        this.participantName = participantName;
        this.participantEmail = participantEmail;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.percentage = percentage;
        this.completedAt = completedAt;
    }

    /**
     * Constructor for Global Platform Leaderboard entry.
     */
    public LeaderboardEntry(int rank, int participantId, String participantName, String participantEmail,
                            int quizzesCompleted, int totalScore, double averagePercentage, LocalDateTime completedAt) {
        this.rank = rank;
        this.participantId = participantId;
        this.participantName = participantName;
        this.participantEmail = participantEmail;
        this.quizzesCompleted = quizzesCompleted;
        this.totalScore = totalScore;
        this.averagePercentage = averagePercentage;
        this.completedAt = completedAt;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getParticipantEmail() {
        return participantEmail;
    }

    public void setParticipantEmail(String participantEmail) {
        this.participantEmail = participantEmail;
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

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public int getQuizzesCompleted() {
        return quizzesCompleted;
    }

    public void setQuizzesCompleted(int quizzesCompleted) {
        this.quizzesCompleted = quizzesCompleted;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public double getAveragePercentage() {
        return averagePercentage;
    }

    public void setAveragePercentage(double averagePercentage) {
        this.averagePercentage = averagePercentage;
    }

    @Override
    public String toString() {
        return "LeaderboardEntry{" +
                "rank=" + rank +
                ", participantName='" + participantName + '\'' +
                ", quizTitle='" + quizTitle + '\'' +
                ", score=" + score +
                ", percentage=" + percentage +
                ", totalScore=" + totalScore +
                ", avgPercentage=" + averagePercentage +
                '}';
    }
}
