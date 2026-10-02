package com.quizplatform.model;

import java.time.LocalDateTime;

/**
 * Model representing a participant's reminder for an upcoming quiz.
 */
public class QuizReminder {

    private int id;
    private int participantId;
    private int quizId;
    private LocalDateTime reminderTime;
    private ReminderStatus reminderStatus;
    private LocalDateTime createdAt;

    public QuizReminder() {
        this.reminderStatus = ReminderStatus.PENDING;
    }

    public QuizReminder(int participantId, int quizId, LocalDateTime reminderTime) {
        this.participantId = participantId;
        this.quizId = quizId;
        this.reminderTime = reminderTime;
        this.reminderStatus = ReminderStatus.PENDING;
    }

    public QuizReminder(int id, int participantId, int quizId, LocalDateTime reminderTime, ReminderStatus reminderStatus, LocalDateTime createdAt) {
        this.id = id;
        this.participantId = participantId;
        this.quizId = quizId;
        this.reminderTime = reminderTime;
        this.reminderStatus = reminderStatus;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public LocalDateTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalDateTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public ReminderStatus getReminderStatus() {
        return reminderStatus;
    }

    public void setReminderStatus(ReminderStatus reminderStatus) {
        this.reminderStatus = reminderStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "QuizReminder{" +
                "id=" + id +
                ", participantId=" + participantId +
                ", quizId=" + quizId +
                ", reminderTime=" + reminderTime +
                ", reminderStatus=" + reminderStatus +
                ", createdAt=" + createdAt +
                '}';
    }
}
