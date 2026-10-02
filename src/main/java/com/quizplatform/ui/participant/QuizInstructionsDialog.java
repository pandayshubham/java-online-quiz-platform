package com.quizplatform.ui.participant;

import com.quizplatform.model.Quiz;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Modal dialog showing quiz rules and instructions before a participant starts an exam.
 */
public class QuizInstructionsDialog extends JDialog {

    private final Quiz quiz;
    private final int totalQuestions;
    private boolean confirmed = false;

    public QuizInstructionsDialog(Frame parent, Quiz quiz, int totalQuestions) {
        super(parent, "Quiz Instructions - " + quiz.getTitle(), true);
        this.quiz = quiz;
        this.totalQuestions = totalQuestions;

        initUI();
    }

    private void initUI() {
        setSize(520, 480);
        setResizable(false);
        setLocationRelativeTo(getParent());

        JPanel rootPanel = new JPanel(new BorderLayout(0, 15));
        rootPanel.setBackground(Color.WHITE);
        rootPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new javax.swing.BoxLayout(headerPanel, javax.swing.BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(quiz.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel subLabel = new JLabel("Please read the following instructions carefully before starting.");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(100, 116, 139));

        headerPanel.add(titleLabel);
        headerPanel.add(javax.swing.Box.createRigidArea(new Dimension(0, 4)));
        headerPanel.add(subLabel);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Details & Instructions
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        // Details grid
        JPanel detailsGrid = new JPanel(new GridBagLayout());
        detailsGrid.setBackground(new Color(248, 250, 252));
        detailsGrid.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addDetailRow(detailsGrid, gbc, 0, "Total Questions:", totalQuestions + " Multiple Choice Questions");
        addDetailRow(detailsGrid, gbc, 1, "Time Limit:", quiz.getDurationMinutes() + " Minutes");
        addDetailRow(detailsGrid, gbc, 2, "Format:", "Single Correct Option (A, B, C, or D)");
        addDetailRow(detailsGrid, gbc, 3, "Passing Mark:", "60% or higher");

        centerPanel.add(detailsGrid, BorderLayout.NORTH);

        // Instructions text
        JTextArea instructionsText = new JTextArea();
        instructionsText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        instructionsText.setLineWrap(true);
        instructionsText.setWrapStyleWord(true);
        instructionsText.setEditable(false);
        instructionsText.setBackground(Color.WHITE);
        instructionsText.setText(
                "EXAM GUIDELINES:\n\n" +
                "1. Navigation: Use the 'Next' and 'Previous' buttons to move between questions.\n\n" +
                "2. Timer: A countdown timer is displayed at the top. When the time expires, your answers will be automatically submitted.\n\n" +
                "3. Selection: Choose one option per question. You may change your answer at any time before submitting.\n\n" +
                "4. Submission: Click 'Submit Quiz' when you have finished all questions. A detailed performance summary will be displayed immediately.\n\n" +
                "Good luck!"
        );
        instructionsText.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(instructionsText);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        rootPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.setPreferredSize(new Dimension(95, 36));
        cancelBtn.setBackground(new Color(241, 245, 249));
        cancelBtn.setForeground(new Color(71, 85, 105));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        JButton startBtn = new JButton("Start Quiz Now →");
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        startBtn.setPreferredSize(new Dimension(150, 36));
        startBtn.setBackground(new Color(16, 185, 129)); // Green
        startBtn.setForeground(Color.WHITE);
        startBtn.setFocusPainted(false);
        startBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startBtn.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(startBtn);
        rootPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.4;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(71, 85, 105));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        val.setForeground(new Color(15, 23, 42));
        panel.add(val, gbc);
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
