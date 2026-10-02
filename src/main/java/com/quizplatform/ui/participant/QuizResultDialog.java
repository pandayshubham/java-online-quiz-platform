package com.quizplatform.ui.participant;

import com.quizplatform.model.Answer;
import com.quizplatform.model.Option;
import com.quizplatform.model.Question;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizAttempt;
import com.quizplatform.service.QuestionService;
import com.quizplatform.service.QuizAttemptService;
import com.quizplatform.service.QuizService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * Modal dialog displaying the performance summary and review for a submitted quiz attempt.
 */
public class QuizResultDialog extends JDialog {

    private final QuizAttempt attempt;
    private final Quiz quiz;
    private final QuestionService questionService;
    private final QuizAttemptService attemptService;

    public QuizResultDialog(Frame parent, QuizAttempt attempt, Quiz quiz,
                            QuestionService questionService, QuizAttemptService attemptService) {
        super(parent, "Quiz Results - " + (quiz != null ? quiz.getTitle() : "Attempt #" + attempt.getId()), true);
        this.attempt = attempt;
        this.quiz = quiz;
        this.questionService = questionService;
        this.attemptService = attemptService;

        initUI();
    }

    private void initUI() {
        setSize(560, 520);
        setResizable(false);
        setLocationRelativeTo(getParent());

        JPanel rootPanel = new JPanel(new BorderLayout(0, 15));
        rootPanel.setBackground(Color.WHITE);
        rootPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Top Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        boolean passed = attempt.getPercentage() >= 60.0;
        boolean isTimeExpired = attempt.getStatus() == com.quizplatform.model.AttemptStatus.TIME_EXPIRED;

        String headerText;
        Color headerColor;
        if (isTimeExpired) {
            headerText = "Time Expired - Quiz Auto-Submitted";
            headerColor = new Color(217, 119, 6); // Amber
        } else if (passed) {
            headerText = "Congratulations! Quiz Completed";
            headerColor = new Color(22, 163, 74); // Green
        } else {
            headerText = "Quiz Finished";
            headerColor = new Color(220, 38, 38); // Red
        }

        JLabel statusLabel = new JLabel(headerText, SwingConstants.CENTER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        statusLabel.setForeground(headerColor);

        String quizTitle = quiz != null ? quiz.getTitle() : ("Quiz #" + attempt.getQuizId());
        JLabel titleLabel = new JLabel(quizTitle + "  •  Status: " + attempt.getStatus().name(), SwingConstants.CENTER);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(100, 116, 139));

        headerPanel.add(statusLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        headerPanel.add(titleLabel);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Center: Metrics Card
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        // Score Hero Card
        JPanel heroCard = new JPanel(new BorderLayout());
        heroCard.setBackground(new Color(248, 250, 252));
        heroCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel scoreVal = new JLabel(attempt.getScore() + " / " + attempt.getTotalQuestions(), SwingConstants.CENTER);
        scoreVal.setFont(new Font("Segoe UI", Font.BOLD, 42));
        scoreVal.setForeground(new Color(15, 23, 42));

        String resultSubtitle = String.format("%.1f%%  -  %s", attempt.getPercentage(), passed ? "PASSED" : "NEEDS IMPROVEMENT");
        if (isTimeExpired) {
            resultSubtitle += "  (TIME EXPIRED)";
        }

        JLabel percentageVal = new JLabel(resultSubtitle, SwingConstants.CENTER);
        percentageVal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        percentageVal.setForeground(passed ? new Color(22, 163, 74) : new Color(220, 38, 38));

        heroCard.add(scoreVal, BorderLayout.CENTER);
        heroCard.add(percentageVal, BorderLayout.SOUTH);
        centerPanel.add(heroCard, BorderLayout.NORTH);

        // Breakdown stats grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 12, 0));
        statsGrid.setOpaque(false);

        statsGrid.add(createMiniCard("CORRECT", String.valueOf(attempt.getCorrectAnswers()), new Color(22, 163, 74)));
        statsGrid.add(createMiniCard("INCORRECT", String.valueOf(attempt.getIncorrectAnswers()), new Color(220, 38, 38)));
        statsGrid.add(createMiniCard("UNANSWERED", String.valueOf(attempt.getUnansweredQuestions()), new Color(100, 116, 139)));

        centerPanel.add(statsGrid, BorderLayout.CENTER);
        rootPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton reviewBtn = new JButton("Review Questions & Answers");
        reviewBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reviewBtn.setPreferredSize(new Dimension(210, 38));
        reviewBtn.setBackground(new Color(241, 245, 249));
        reviewBtn.setForeground(new Color(71, 85, 105));
        reviewBtn.setFocusPainted(false);
        reviewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        reviewBtn.addActionListener(e -> showReviewDialog());

        JButton closeBtn = new JButton("Back to Dashboard");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setPreferredSize(new Dimension(160, 38));
        closeBtn.setBackground(new Color(79, 70, 229));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());

        buttonPanel.add(reviewBtn);
        buttonPanel.add(closeBtn);
        rootPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    private JPanel createMiniCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(new Color(100, 116, 139));

        JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLbl.setForeground(accent);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        return card;
    }

    private void showReviewDialog() {
        JDialog reviewDialog = new JDialog(this, "Question Review - " + (quiz != null ? quiz.getTitle() : "Attempt #" + attempt.getId()), true);
        reviewDialog.setSize(640, 560);
        reviewDialog.setLocationRelativeTo(this);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(248, 250, 252));
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        List<Question> questions = questionService.getQuestionsByQuizId(attempt.getQuizId());
        List<Answer> answers = attemptService.getAnswersByAttempt(attempt.getId());

        Map<Integer, Answer> answerMap = new HashMap<>();
        for (Answer a : answers) {
            answerMap.put(a.getQuestionId(), a);
        }

        int index = 1;
        for (Question q : questions) {
            Answer ans = answerMap.get(q.getId());
            List<Option> opts = questionService.getOptionsByQuestionId(q.getId());

            JPanel qCard = new JPanel(new BorderLayout(0, 8));
            qCard.setBackground(Color.WHITE);
            qCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240)),
                    BorderFactory.createEmptyBorder(12, 14, 12, 14)
            ));

            boolean correct = ans != null && ans.isCorrect();
            boolean answered = ans != null && ans.getSelectedOptionId() != null;

            String statusText = !answered ? "[UNANSWERED]" : (correct ? "[CORRECT]" : "[INCORRECT]");
            Color statusColor = !answered ? new Color(100, 116, 139) : (correct ? new Color(22, 163, 74) : new Color(220, 38, 38));

            JLabel qTitle = new JLabel("Question " + index + ": " + q.getQuestionText());
            qTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));

            JLabel qStatus = new JLabel(statusText);
            qStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
            qStatus.setForeground(statusColor);

            JPanel qHeader = new JPanel(new BorderLayout());
            qHeader.setOpaque(false);
            qHeader.add(qTitle, BorderLayout.WEST);
            qHeader.add(qStatus, BorderLayout.EAST);
            qCard.add(qHeader, BorderLayout.NORTH);

            JPanel optsPanel = new JPanel(new GridLayout(4, 1, 0, 4));
            optsPanel.setOpaque(false);

            for (Option opt : opts) {
                boolean isSelected = ans != null && ans.getSelectedOptionId() != null && ans.getSelectedOptionId() == opt.getId();
                boolean isCorrectOpt = opt.isCorrect();

                String optPrefix = "(" + opt.getOptionLabel() + ") " + opt.getOptionText();
                if (isSelected && isCorrectOpt) {
                    optPrefix += "  ✓ (Your Correct Choice)";
                } else if (isSelected && !isCorrectOpt) {
                    optPrefix += "  ✗ (Your Choice)";
                } else if (isCorrectOpt) {
                    optPrefix += "  ✓ (Correct Answer)";
                }

                JLabel optLbl = new JLabel(optPrefix);
                optLbl.setFont(new Font("Segoe UI", isSelected || isCorrectOpt ? Font.BOLD : Font.PLAIN, 12));
                if (isCorrectOpt) {
                    optLbl.setForeground(new Color(22, 163, 74));
                } else if (isSelected) {
                    optLbl.setForeground(new Color(220, 38, 38));
                } else {
                    optLbl.setForeground(new Color(71, 85, 105));
                }
                optsPanel.add(optLbl);
            }
            qCard.add(optsPanel, BorderLayout.CENTER);

            if (q.getExplanation() != null && !q.getExplanation().trim().isEmpty()) {
                JLabel expLbl = new JLabel("Explanation: " + q.getExplanation());
                expLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                expLbl.setForeground(new Color(100, 116, 139));
                qCard.add(expLbl, BorderLayout.SOUTH);
            }

            content.add(qCard);
            content.add(Box.createRigidArea(new Dimension(0, 10)));
            index++;
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);

        JPanel root = new JPanel(new BorderLayout());
        root.add(scroll, BorderLayout.CENTER);

        JButton doneBtn = new JButton("Close Review");
        doneBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        doneBtn.setBackground(new Color(241, 245, 249));
        doneBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        doneBtn.addActionListener(e -> reviewDialog.dispose());

        JPanel btm = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btm.setBackground(Color.WHITE);
        btm.add(doneBtn);
        root.add(btm, BorderLayout.SOUTH);

        reviewDialog.setContentPane(root);
        reviewDialog.setVisible(true);
    }
}
