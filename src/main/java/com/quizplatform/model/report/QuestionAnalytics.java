package com.quizplatform.model.report;

/**
 * Question-level performance analytics for quiz creators.
 */
public class QuestionAnalytics {

    private int questionId;
    private int questionOrder;
    private String questionText;
    private int timesAnswered;
    private int correctCount;
    private int incorrectCount;
    private int unansweredCount;
    private double accuracyPercentage;

    public QuestionAnalytics() {
    }

    public QuestionAnalytics(int questionId, int questionOrder, String questionText,
                             int timesAnswered, int correctCount, int incorrectCount,
                             int unansweredCount, double accuracyPercentage) {
        this.questionId = questionId;
        this.questionOrder = questionOrder;
        this.questionText = questionText;
        this.timesAnswered = timesAnswered;
        this.correctCount = correctCount;
        this.incorrectCount = incorrectCount;
        this.unansweredCount = unansweredCount;
        this.accuracyPercentage = accuracyPercentage;
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

    public int getTimesAnswered() {
        return timesAnswered;
    }

    public void setTimesAnswered(int timesAnswered) {
        this.timesAnswered = timesAnswered;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public int getIncorrectCount() {
        return incorrectCount;
    }

    public void setIncorrectCount(int incorrectCount) {
        this.incorrectCount = incorrectCount;
    }

    public int getUnansweredCount() {
        return unansweredCount;
    }

    public void setUnansweredCount(int unansweredCount) {
        this.unansweredCount = unansweredCount;
    }

    public double getAccuracyPercentage() {
        return accuracyPercentage;
    }

    public void setAccuracyPercentage(double accuracyPercentage) {
        this.accuracyPercentage = accuracyPercentage;
    }
}
