package com.quizplatform.ui.creator;

import com.quizplatform.exception.ValidationException;
import com.quizplatform.model.Option;
import com.quizplatform.model.Question;
import com.quizplatform.service.QuestionService;
import com.quizplatform.util.ValidationUtil;
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
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Modal dialog for creating and editing questions and their 4 options (A, B, C, D).
 */
public class QuestionFormDialog extends JDialog {

    private final QuestionService questionService;
    private final int quizId;
    private final Question questionToEdit;
    private boolean saved = false;

    private JTextArea questionTextArea;
    private JTextArea explanationArea;
    private JSpinner orderSpinner;

    private JTextField optionAField;
    private JTextField optionBField;
    private JTextField optionCField;
    private JTextField optionDField;

    private JRadioButton radioA;
    private JRadioButton radioB;
    private JRadioButton radioC;
    private JRadioButton radioD;

    public QuestionFormDialog(Frame parent, QuestionService questionService, int quizId, Question questionToEdit) {
        super(parent, questionToEdit == null ? "Add Question" : "Edit Question", true);
        this.questionService = questionService;
        this.quizId = quizId;
        this.questionToEdit = questionToEdit;

        initUI();
    }

    private void initUI() {
        setSize(580, 620);
        setResizable(false);
        setLocationRelativeTo(getParent());

        JPanel rootPanel = new JPanel(new BorderLayout(0, 15));
        rootPanel.setBackground(Color.WHITE);
        rootPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Header
        JLabel titleLabel = new JLabel(questionToEdit == null ? "Add New Question" : "Edit Question Details");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(15, 23, 42));
        rootPanel.add(titleLabel, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Order
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.25;
        JLabel lblOrder = new JLabel("Question Order:");
        lblOrder.setFont(new Font("Segoe UI", Font.BOLD, 12));
        contentPanel.add(lblOrder, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        int nextOrder = questionToEdit != null ? questionToEdit.getQuestionOrder() : questionService.getNextQuestionOrder(quizId);
        orderSpinner = new JSpinner(new SpinnerNumberModel(Math.max(1, nextOrder), 1, 999, 1));
        orderSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentPanel.add(orderSpinner, gbc);

        // Question Text
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblText = new JLabel("Question Text *:");
        lblText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        contentPanel.add(lblText, gbc);

        gbc.gridx = 1;
        questionTextArea = new JTextArea(3, 20);
        questionTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        JScrollPane qScrollPane = new JScrollPane(questionTextArea);
        qScrollPane.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        contentPanel.add(qScrollPane, gbc);

        // Options Section Header
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lblOptionsHeader = new JLabel("Options & Correct Answer (Select the radio button for the correct option):");
        lblOptionsHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblOptionsHeader.setForeground(new Color(79, 70, 229));
        contentPanel.add(lblOptionsHeader, gbc);
        gbc.gridwidth = 1;

        // Radio group for correct answer
        ButtonGroup correctGroup = new ButtonGroup();
        radioA = new JRadioButton("Option A *");
        radioB = new JRadioButton("Option B *");
        radioC = new JRadioButton("Option C *");
        radioD = new JRadioButton("Option D *");

        radioA.setOpaque(false);
        radioB.setOpaque(false);
        radioC.setOpaque(false);
        radioD.setOpaque(false);

        radioA.setFont(new Font("Segoe UI", Font.BOLD, 12));
        radioB.setFont(new Font("Segoe UI", Font.BOLD, 12));
        radioC.setFont(new Font("Segoe UI", Font.BOLD, 12));
        radioD.setFont(new Font("Segoe UI", Font.BOLD, 12));

        correctGroup.add(radioA);
        correctGroup.add(radioB);
        correctGroup.add(radioC);
        correctGroup.add(radioD);
        radioA.setSelected(true); // default A is selected

        optionAField = createOptionTextField();
        optionBField = createOptionTextField();
        optionCField = createOptionTextField();
        optionDField = createOptionTextField();

        // Option A row
        gbc.gridy = 3;
        gbc.gridx = 0;
        contentPanel.add(radioA, gbc);
        gbc.gridx = 1;
        contentPanel.add(optionAField, gbc);

        // Option B row
        gbc.gridy = 4;
        gbc.gridx = 0;
        contentPanel.add(radioB, gbc);
        gbc.gridx = 1;
        contentPanel.add(optionBField, gbc);

        // Option C row
        gbc.gridy = 5;
        gbc.gridx = 0;
        contentPanel.add(radioC, gbc);
        gbc.gridx = 1;
        contentPanel.add(optionCField, gbc);

        // Option D row
        gbc.gridy = 6;
        gbc.gridx = 0;
        contentPanel.add(radioD, gbc);
        gbc.gridx = 1;
        contentPanel.add(optionDField, gbc);

        // Explanation
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblExplanation = new JLabel("Explanation:");
        lblExplanation.setFont(new Font("Segoe UI", Font.BOLD, 12));
        contentPanel.add(lblExplanation, gbc);

        gbc.gridx = 1;
        explanationArea = new JTextArea(2, 20);
        explanationArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        JScrollPane expScrollPane = new JScrollPane(explanationArea);
        expScrollPane.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        contentPanel.add(expScrollPane, gbc);

        rootPanel.add(contentPanel, BorderLayout.CENTER);

        // Pre-fill fields if editing
        if (questionToEdit != null) {
            questionTextArea.setText(questionToEdit.getQuestionText());
            explanationArea.setText(questionToEdit.getExplanation() != null ? questionToEdit.getExplanation() : "");
            orderSpinner.setValue(Math.max(1, questionToEdit.getQuestionOrder()));

            List<Option> existingOptions = questionService.getOptionsByQuestionId(questionToEdit.getId());
            for (Option opt : existingOptions) {
                if ("A".equalsIgnoreCase(opt.getOptionLabel())) {
                    optionAField.setText(opt.getOptionText());
                    if (opt.isCorrect()) radioA.setSelected(true);
                } else if ("B".equalsIgnoreCase(opt.getOptionLabel())) {
                    optionBField.setText(opt.getOptionText());
                    if (opt.isCorrect()) radioB.setSelected(true);
                } else if ("C".equalsIgnoreCase(opt.getOptionLabel())) {
                    optionCField.setText(opt.getOptionText());
                    if (opt.isCorrect()) radioC.setSelected(true);
                } else if ("D".equalsIgnoreCase(opt.getOptionLabel())) {
                    optionDField.setText(opt.getOptionText());
                    if (opt.isCorrect()) radioD.setSelected(true);
                }
            }
        }

        // Bottom buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelButton.setPreferredSize(new Dimension(95, 36));
        cancelButton.setFocusPainted(false);
        cancelButton.setBackground(new Color(241, 245, 249));
        cancelButton.setForeground(new Color(51, 65, 85));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = new JButton(questionToEdit == null ? "Save Question" : "Update Question");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveButton.setPreferredSize(new Dimension(135, 36));
        saveButton.setFocusPainted(false);
        saveButton.setBackground(new Color(124, 58, 237));
        saveButton.setForeground(Color.WHITE);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> handleSave());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        rootPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    private JTextField createOptionTextField() {
        JTextField tf = new JTextField(20);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)
        ));
        return tf;
    }

    private void handleSave() {
        String questionText = questionTextArea.getText().trim();
        String explanation = explanationArea.getText().trim();
        int order = (int) orderSpinner.getValue();

        String textA = optionAField.getText().trim();
        String textB = optionBField.getText().trim();
        String textC = optionCField.getText().trim();
        String textD = optionDField.getText().trim();

        if (ValidationUtil.isNullOrBlank(questionText)) {
            JOptionPane.showMessageDialog(this, "Question text cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            questionTextArea.requestFocus();
            return;
        }

        if (ValidationUtil.isNullOrBlank(textA) || ValidationUtil.isNullOrBlank(textB) ||
                ValidationUtil.isNullOrBlank(textC) || ValidationUtil.isNullOrBlank(textD)) {
            JOptionPane.showMessageDialog(this, "All four options (A, B, C, and D) must have text.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Option> options = new ArrayList<>();
        options.add(new Option(0, 0, textA, "A", radioA.isSelected()));
        options.add(new Option(0, 0, textB, "B", radioB.isSelected()));
        options.add(new Option(0, 0, textC, "C", radioC.isSelected()));
        options.add(new Option(0, 0, textD, "D", radioD.isSelected()));

        try {
            if (questionToEdit == null) {
                Question question = new Question();
                question.setQuizId(quizId);
                question.setQuestionText(questionText);
                question.setExplanation(explanation);
                question.setQuestionOrder(order);

                questionService.createQuestion(question, options);
                saved = true;
                JOptionPane.showMessageDialog(this, "Question added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                questionToEdit.setQuestionText(questionText);
                questionToEdit.setExplanation(explanation);
                questionToEdit.setQuestionOrder(order);

                questionService.updateQuestion(questionToEdit, options);
                saved = true;
                JOptionPane.showMessageDialog(this, "Question updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            dispose();
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to save question: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
