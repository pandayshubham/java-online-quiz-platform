package com.quizplatform.model;

import java.time.LocalDateTime;

/**
 * Model representing a quiz question.
 */
public class Question {

    private int id;
    private int quizId;
    private String questionText;
    private String explanation;
    private int questionOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Question() {
        this.questionOrder = 1;
    }

    public Question(int quizId, String questionText, String explanation, int questionOrder) {
        this.quizId = quizId;
        this.questionText = questionText;
        this.explanation = explanation;
        this.questionOrder = questionOrder;
    }

    public Question(int id, int quizId, String questionText, String explanation, int questionOrder, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.quizId = quizId;
        this.questionText = questionText;
        this.explanation = explanation;
        this.questionOrder = questionOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public int getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(int questionOrder) {
        this.questionOrder = questionOrder;
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
        return "Question{" +
                "id=" + id +
                ", quizId=" + quizId +
                ", questionOrder=" + questionOrder +
                ", questionText='" + questionText + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
