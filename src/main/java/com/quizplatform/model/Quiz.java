package com.quizplatform.model;

import java.time.LocalDateTime;

/**
 * Model representing a quiz created by a QUIZ_CREATOR.
 */
public class Quiz {

    private int id;
    private int creatorId;
    private String title;
    private String description;
    private int durationMinutes;
    private QuizStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Quiz() {
        this.status = QuizStatus.DRAFT;
        this.durationMinutes = 15;
    }

    public Quiz(int creatorId, String title, String description, int durationMinutes, QuizStatus status) {
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.status = status != null ? status : QuizStatus.DRAFT;
    }

    public Quiz(int id, int creatorId, String title, String description, int durationMinutes, QuizStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public QuizStatus getStatus() {
        return status;
    }

    public void setStatus(QuizStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Quiz{" +
                "id=" + id +
                ", creatorId=" + creatorId +
                ", title='" + title + '\'' +
                ", durationMinutes=" + durationMinutes +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
