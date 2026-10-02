package com.quizplatform.model.report;

/**
 * Question result detail for an attempt, showing question, selected answer,
 * correct answer, explanation, and result status (CORRECT, INCORRECT, UNANSWERED).
 */
public class QuestionResultDetail {

    private int questionId;
    private int questionOrder;
    private String questionText;
    private String explanation;
    private String selectedOptionLabel;
    private String selectedOptionText;
    private String correctOptionLabel;
    private String correctOptionText;
    private String result; // CORRECT, INCORRECT, UNANSWERED

    public QuestionResultDetail() {
    }

    public QuestionResultDetail(int questionId, int questionOrder, String questionText, String explanation,
                                String selectedOptionLabel, String selectedOptionText,
                                String correctOptionLabel, String correctOptionText, String result) {
        this.questionId = questionId;
        this.questionOrder = questionOrder;
        this.questionText = questionText;
        this.explanation = explanation;
        this.selectedOptionLabel = selectedOptionLabel;
        this.selectedOptionText = selectedOptionText;
        this.correctOptionLabel = correctOptionLabel;
        this.correctOptionText = correctOptionText;
        this.result = result;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(int questionOrder) {
        this.questionOrder = questionOrder;
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

    public String getSelectedOptionLabel() {
        return selectedOptionLabel;
    }

    public void setSelectedOptionLabel(String selectedOptionLabel) {
        this.selectedOptionLabel = selectedOptionLabel;
    }

    public String getSelectedOptionText() {
        return selectedOptionText;
    }

    public void setSelectedOptionText(String selectedOptionText) {
        this.selectedOptionText = selectedOptionText;
    }

    public String getCorrectOptionLabel() {
        return correctOptionLabel;
    }

    public void setCorrectOptionLabel(String correctOptionLabel) {
        this.correctOptionLabel = correctOptionLabel;
    }

    public String getCorrectOptionText() {
        return correctOptionText;
    }

    public void setCorrectOptionText(String correctOptionText) {
        this.correctOptionText = correctOptionText;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
