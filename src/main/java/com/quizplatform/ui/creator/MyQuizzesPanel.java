package com.quizplatform.ui.creator;

import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizStatus;
import com.quizplatform.model.User;
import com.quizplatform.service.QuestionService;
import com.quizplatform.service.QuizService;
import com.quizplatform.util.SessionManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Panel for managing quizzes created by the logged-in creator.
 */
public class MyQuizzesPanel extends JPanel {

    private final QuizService quizService;
    private final QuestionService questionService;
    private final Runnable onDataChanged;
    private final Consumer<Quiz> onManageQuestionsRequested;

    private JTable quizTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private List<Quiz> currentQuizzes = new ArrayList<>();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MyQuizzesPanel(QuizService quizService, QuestionService questionService,
                          Runnable onDataChanged, Consumer<Quiz> onManageQuestionsRequested) {
        this.quizService = quizService;
        this.questionService = questionService;
        this.onDataChanged = onDataChanged;
        this.onManageQuestionsRequested = onManageQuestionsRequested;

        setLayout(new BorderLayout(0, 15));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initTopBar();
        initTable();
        initBottomBar();

        refreshQuizzes();
    }

    private void initTopBar() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);

        // Title and description
        JPanel headerTextPanel = new JPanel();
        headerTextPanel.setLayout(new javax.swing.BoxLayout(headerTextPanel, javax.swing.BoxLayout.Y_AXIS));
        headerTextPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("My Quizzes");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel subLabel = new JLabel("Create, manage, and submit quizzes for administrative review.");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(100, 116, 139));

        headerTextPanel.add(titleLabel);
        headerTextPanel.add(subLabel);
        topPanel.add(headerTextPanel, BorderLayout.WEST);

        // Filter and Search controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controlsPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        controlsPanel.add(searchLabel);

        searchField = new JTextField(12);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        controlsPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchButton.setBackground(new Color(241, 245, 249));
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.addActionListener(e -> applyFilter());
        controlsPanel.add(searchButton);

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        controlsPanel.add(statusLabel);

        statusFilter = new JComboBox<>(new String[]{"All Status", "DRAFT", "PENDING_APPROVAL", "APPROVED", "REJECTED"});
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusFilter.addActionListener(e -> applyFilter());
        controlsPanel.add(statusFilter);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshButton.setBackground(new Color(241, 245, 249));
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            refreshQuizzes();
        });
        controlsPanel.add(refreshButton);

        topPanel.add(controlsPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
    }

    private void initTable() {
        String[] columns = {"ID", "Title", "Duration", "Questions", "Status", "Created At", "Updated At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        quizTable = new JTable(tableModel);
        quizTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        quizTable.setRowHeight(36);
        quizTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        quizTable.setGridColor(new Color(241, 245, 249));
        quizTable.setShowHorizontalLines(true);
        quizTable.setShowVerticalLines(false);

        // Header styling
        JTableHeader header = quizTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        // Column widths
        quizTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        quizTable.getColumnModel().getColumn(1).setPreferredWidth(230);
        quizTable.getColumnModel().getColumn(2).setPreferredWidth(85);
        quizTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        quizTable.getColumnModel().getColumn(4).setPreferredWidth(125);
        quizTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        quizTable.getColumnModel().getColumn(6).setPreferredWidth(130);

        // Center align columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        quizTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        quizTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        quizTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        quizTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        quizTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        // Custom status badge renderer
        quizTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));

                if (!isSelected) {
                    String status = value != null ? value.toString() : "";
                    switch (status) {
                        case "APPROVED" -> {
                            label.setForeground(new Color(22, 101, 52));
                            label.setBackground(new Color(220, 252, 231));
                        }
                        case "PENDING_APPROVAL" -> {
                            label.setForeground(new Color(133, 77, 14));
                            label.setBackground(new Color(254, 240, 138));
                        }
                        case "REJECTED" -> {
                            label.setForeground(new Color(153, 27, 27));
                            label.setBackground(new Color(254, 226, 226));
                        }
                        default -> {
                            label.setForeground(new Color(71, 85, 105));
                            label.setBackground(new Color(241, 245, 249));
                        }
                    }
                    label.setOpaque(true);
                }
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(quizTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void initBottomBar() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.setOpaque(false);

        JButton createBtn = new JButton("+ Create Quiz");
        createBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        createBtn.setBackground(new Color(124, 58, 237));
        createBtn.setForeground(Color.WHITE);
        createBtn.setFocusPainted(false);
        createBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createBtn.addActionListener(e -> handleCreateQuiz());

        JButton editBtn = new JButton("Edit Quiz");
        editBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        editBtn.setBackground(new Color(241, 245, 249));
        editBtn.setFocusPainted(false);
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editBtn.addActionListener(e -> handleEditQuiz());

        JButton deleteBtn = new JButton("Delete Quiz");
        deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        deleteBtn.setBackground(new Color(254, 226, 226));
        deleteBtn.setForeground(new Color(185, 28, 28));
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> handleDeleteQuiz());

        JButton submitBtn = new JButton("Submit for Approval");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        submitBtn.setBackground(new Color(37, 99, 235));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.addActionListener(e -> handleSubmitForApproval());

        JButton manageQuestionsBtn = new JButton("Manage Questions →");
        manageQuestionsBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        manageQuestionsBtn.setBackground(new Color(16, 185, 129));
        manageQuestionsBtn.setForeground(Color.WHITE);
        manageQuestionsBtn.setFocusPainted(false);
        manageQuestionsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        manageQuestionsBtn.addActionListener(e -> handleManageQuestions());

        bottomPanel.add(createBtn);
        bottomPanel.add(editBtn);
        bottomPanel.add(deleteBtn);
        bottomPanel.add(submitBtn);
        bottomPanel.add(manageQuestionsBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void refreshQuizzes() {
        User creator = SessionManager.getCurrentUser();
        if (creator == null) return;

        try {
            currentQuizzes = quizService.getQuizzesByCreator(creator.getId());
            applyFilter();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unable to load quizzes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilter() {
        String search = searchField.getText().trim().toLowerCase();
        String selectedStatus = (String) statusFilter.getSelectedItem();

        tableModel.setRowCount(0);

        for (Quiz quiz : currentQuizzes) {
            boolean matchesSearch = search.isEmpty() ||
                    quiz.getTitle().toLowerCase().contains(search) ||
                    String.valueOf(quiz.getId()).contains(search);

            boolean matchesStatus = "All Status".equals(selectedStatus) ||
                    quiz.getStatus().name().equalsIgnoreCase(selectedStatus);

            if (matchesSearch && matchesStatus) {
                int questionCount = questionService.getQuestionCountByQuizId(quiz.getId());
                tableModel.addRow(new Object[]{
                        quiz.getId(),
                        quiz.getTitle(),
                        quiz.getDurationMinutes() + " mins",
                        questionCount,
                        quiz.getStatus().name(),
                        quiz.getCreatedAt() != null ? quiz.getCreatedAt().format(DATE_FORMATTER) : "N/A",
                        quiz.getUpdatedAt() != null ? quiz.getUpdatedAt().format(DATE_FORMATTER) : "N/A"
                });
            }
        }
    }

    private Quiz getSelectedQuiz() {
        int row = quizTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a quiz from the table first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int quizId = (int) tableModel.getValueAt(row, 0);
        return quizService.getQuizById(quizId);
    }

    private void handleCreateQuiz() {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        QuizFormDialog dialog = new QuizFormDialog(parent, quizService, null);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            refreshQuizzes();
            if (onDataChanged != null) onDataChanged.run();
        }
    }

    private void handleEditQuiz() {
        Quiz quiz = getSelectedQuiz();
        if (quiz == null) return;

        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        QuizFormDialog dialog = new QuizFormDialog(parent, quizService, quiz);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            refreshQuizzes();
            if (onDataChanged != null) onDataChanged.run();
        }
    }

    private void handleDeleteQuiz() {
        Quiz quiz = getSelectedQuiz();
        if (quiz == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete quiz: \"" + quiz.getTitle() + "\"?\nThis will also delete all associated questions and options.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = quizService.deleteQuiz(quiz.getId());
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Quiz deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshQuizzes();
                    if (onDataChanged != null) onDataChanged.run();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete quiz.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Notice", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void handleSubmitForApproval() {
        Quiz quiz = getSelectedQuiz();
        if (quiz == null) return;

        if (quiz.getStatus() == QuizStatus.PENDING_APPROVAL) {
            JOptionPane.showMessageDialog(this, "This quiz has already been submitted and is currently pending approval.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (quiz.getStatus() == QuizStatus.APPROVED) {
            JOptionPane.showMessageDialog(this, "This quiz is already approved.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int qCount = questionService.getQuestionCountByQuizId(quiz.getId());
        if (qCount == 0) {
            JOptionPane.showMessageDialog(this, "A quiz must have at least one question before it can be submitted for approval.", "Validation Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Submit quiz \"" + quiz.getTitle() + "\" with " + qCount + " questions for Administrator approval?",
                "Confirm Submission",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                quizService.submitQuizForApproval(quiz.getId());
                JOptionPane.showMessageDialog(this, "Quiz submitted successfully! It is now pending administrator approval.", "Submitted", JOptionPane.INFORMATION_MESSAGE);
                refreshQuizzes();
                if (onDataChanged != null) onDataChanged.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to submit quiz: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleManageQuestions() {
        Quiz quiz = getSelectedQuiz();
        if (quiz == null) return;

        if (onManageQuestionsRequested != null) {
            onManageQuestionsRequested.accept(quiz);
        }
    }
}
