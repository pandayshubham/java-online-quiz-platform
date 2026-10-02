package com.quizplatform.model;

/**
 * Model representing an option for a question (A, B, C, or D).
 */
public class Option {

    private int id;
    private int questionId;
    private String optionText;
    private String optionLabel;
    private boolean isCorrect;

    public Option() {
    }

    public Option(int questionId, String optionText, String optionLabel, boolean isCorrect) {
        this.questionId = questionId;
        this.optionText = optionText;
        this.optionLabel = optionLabel;
        this.isCorrect = isCorrect;
    }

    public Option(int id, int questionId, String optionText, String optionLabel, boolean isCorrect) {
        this.id = id;
        this.questionId = questionId;
        this.optionText = optionText;
        this.optionLabel = optionLabel;
        this.isCorrect = isCorrect;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public String getOptionLabel() {
        return optionLabel;
    }

    public void setOptionLabel(String optionLabel) {
        this.optionLabel = optionLabel;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    @Override
    public String toString() {
        return "Option{" +
                "id=" + id +
                ", questionId=" + questionId +
                ", optionLabel='" + optionLabel + '\'' +
                ", optionText='" + optionText + '\'' +
                ", isCorrect=" + isCorrect +
                '}';
    }
}
