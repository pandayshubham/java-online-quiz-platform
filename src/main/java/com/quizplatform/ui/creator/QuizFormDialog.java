package com.quizplatform.ui.creator;

import com.quizplatform.exception.ValidationException;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizStatus;
import com.quizplatform.service.QuizService;
import com.quizplatform.util.SessionManager;
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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Modal dialog for creating and editing quizzes.
 */
public class QuizFormDialog extends JDialog {

    private final QuizService quizService;
    private final Quiz quizToEdit;
    private boolean saved = false;

    private JTextField titleField;
    private JTextArea descriptionArea;
    private JSpinner durationSpinner;

    public QuizFormDialog(Frame parent, QuizService quizService, Quiz quizToEdit) {
        super(parent, quizToEdit == null ? "Create New Quiz" : "Edit Quiz", true);
        this.quizService = quizService;
        this.quizToEdit = quizToEdit;

        initUI();
    }

    private void initUI() {
        setSize(480, 420);
        setResizable(false);
        setLocationRelativeTo(getParent());

        JPanel rootPanel = new JPanel(new BorderLayout(0, 15));
        rootPanel.setBackground(Color.WHITE);
        rootPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Header Title
        JLabel titleLabel = new JLabel(quizToEdit == null ? "Create Quiz" : "Edit Quiz Details");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(15, 23, 42));
        rootPanel.add(titleLabel, BorderLayout.NORTH);

        // Form Fields
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel lblTitle = new JLabel("Quiz Title *:");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblTitle, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        titleField = new JTextField(20);
        titleField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        formPanel.add(titleField, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblDesc = new JLabel("Description:");
        lblDesc.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblDesc, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        formPanel.add(descScrollPane, gbc);

        // Duration
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel lblDuration = new JLabel("Duration (mins) *:");
        lblDuration.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblDuration, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        durationSpinner = new JSpinner(new SpinnerNumberModel(15, 1, 300, 1));
        durationSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(durationSpinner, gbc);

        rootPanel.add(formPanel, BorderLayout.CENTER);

        // Pre-fill fields if editing
        if (quizToEdit != null) {
            titleField.setText(quizToEdit.getTitle());
            descriptionArea.setText(quizToEdit.getDescription() != null ? quizToEdit.getDescription() : "");
            durationSpinner.setValue(Math.max(1, quizToEdit.getDurationMinutes()));
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

        JButton saveButton = new JButton(quizToEdit == null ? "Create" : "Update");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveButton.setPreferredSize(new Dimension(95, 36));
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

    private void handleSave() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        int duration = (int) durationSpinner.getValue();

        if (ValidationUtil.isNullOrBlank(title)) {
            JOptionPane.showMessageDialog(this, "Quiz title cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            titleField.requestFocus();
            return;
        }

        if (duration <= 0) {
            JOptionPane.showMessageDialog(this, "Quiz duration must be greater than 0 minutes.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (quizToEdit == null) {
                int creatorId = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : 1;
                Quiz newQuiz = new Quiz();
                newQuiz.setCreatorId(creatorId);
                newQuiz.setTitle(title);
                newQuiz.setDescription(description);
                newQuiz.setDurationMinutes(duration);
                newQuiz.setStatus(QuizStatus.DRAFT);

                quizService.createQuiz(newQuiz);
                saved = true;
                JOptionPane.showMessageDialog(this, "Quiz created successfully as DRAFT.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                quizToEdit.setTitle(title);
                quizToEdit.setDescription(description);
                quizToEdit.setDurationMinutes(duration);

                quizService.updateQuiz(quizToEdit);
                saved = true;
                JOptionPane.showMessageDialog(this, "Quiz updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            dispose();
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to save quiz: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
