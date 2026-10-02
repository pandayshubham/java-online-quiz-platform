package com.quizplatform.model;

/**
 * Model representing a participant's submitted answer for a question in a quiz attempt.
 */
public class Answer {

    private int id;
    private int attemptId;
    private int questionId;
    private Integer selectedOptionId;
    private boolean isCorrect;

    public Answer() {
    }

    public Answer(int attemptId, int questionId, Integer selectedOptionId, boolean isCorrect) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
        this.isCorrect = isCorrect;
    }

    public Answer(int id, int attemptId, int questionId, Integer selectedOptionId, boolean isCorrect) {
        this.id = id;
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
        this.isCorrect = isCorrect;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(int attemptId) {
        this.attemptId = attemptId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public Integer getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Integer selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", attemptId=" + attemptId +
                ", questionId=" + questionId +
                ", selectedOptionId=" + selectedOptionId +
                ", isCorrect=" + isCorrect +
                '}';
    }
}
