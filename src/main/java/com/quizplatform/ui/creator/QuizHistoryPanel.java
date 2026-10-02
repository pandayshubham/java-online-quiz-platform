package com.quizplatform.ui.creator;

import com.quizplatform.model.Quiz;
import com.quizplatform.model.User;
import com.quizplatform.service.QuestionService;
import com.quizplatform.service.QuizAttemptService;
import com.quizplatform.service.QuizService;
import com.quizplatform.util.SessionManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Panel showing the status history and lifecycle of all quizzes created by the creator.
 */
public class QuizHistoryPanel extends JPanel {

    private final QuizService quizService;
    private final QuestionService questionService;
    private final QuizAttemptService attemptService;

    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilter;
    private JLabel summaryLabel;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public QuizHistoryPanel(QuizService quizService, QuestionService questionService, QuizAttemptService attemptService) {
        this.quizService = quizService;
        this.questionService = questionService;
        this.attemptService = attemptService;

        setLayout(new BorderLayout(0, 15));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initTopBar();
        initTable();
        refreshHistory();
    }

    private void initTopBar() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new javax.swing.BoxLayout(textPanel, javax.swing.BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Quiz History & Lifecycle");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel subLabel = new JLabel("Audit trail of all your created quizzes, approval states, and participant activities.");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(100, 116, 139));

        textPanel.add(titleLabel);
        textPanel.add(subLabel);
        topPanel.add(textPanel, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);

        summaryLabel = new JLabel("Loading history...");
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        summaryLabel.setForeground(new Color(79, 70, 229));
        filterPanel.add(summaryLabel);

        JLabel filterLbl = new JLabel("Status Filter:");
        filterLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterPanel.add(filterLbl);

        statusFilter = new JComboBox<>(new String[]{"All Status", "DRAFT", "PENDING_APPROVAL", "APPROVED", "REJECTED"});
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusFilter.addActionListener(e -> refreshHistory());
        filterPanel.add(statusFilter);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setBackground(new Color(241, 245, 249));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshHistory());
        filterPanel.add(refreshBtn);

        topPanel.add(filterPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
    }

    private void initTable() {
        String[] columns = {"Quiz ID", "Title", "Status", "Questions", "Attempts", "Duration", "Created At", "Last Updated"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setRowHeight(36);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        historyTable.setGridColor(new Color(241, 245, 249));
        historyTable.setShowHorizontalLines(true);
        historyTable.setShowVerticalLines(false);

        JTableHeader header = historyTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        historyTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        historyTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
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

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshHistory() {
        User creator = SessionManager.getCurrentUser();
        if (creator == null) return;

        tableModel.setRowCount(0);
        String selectedFilter = (String) statusFilter.getSelectedItem();

        List<Quiz> quizzes = quizService.getQuizzesByCreator(creator.getId());
        int total = quizzes.size();
        int approved = 0, pending = 0, draft = 0, rejected = 0;

        for (Quiz q : quizzes) {
            switch (q.getStatus()) {
                case APPROVED -> approved++;
                case PENDING_APPROVAL -> pending++;
                case DRAFT -> draft++;
                case REJECTED -> rejected++;
            }

            boolean matches = "All Status".equals(selectedFilter) || q.getStatus().name().equalsIgnoreCase(selectedFilter);
            if (matches) {
                int qCount = questionService.getQuestionCountByQuizId(q.getId());
                int attemptsCount = attemptService.getAttemptsByQuiz(q.getId()).size();

                tableModel.addRow(new Object[]{
                        "#" + q.getId(),
                        q.getTitle(),
                        q.getStatus().name(),
                        qCount,
                        attemptsCount,
                        q.getDurationMinutes() + " mins",
                        q.getCreatedAt() != null ? q.getCreatedAt().format(DATE_FORMATTER) : "N/A",
                        q.getUpdatedAt() != null ? q.getUpdatedAt().format(DATE_FORMATTER) : "N/A"
                });
            }
        }

        summaryLabel.setText("Total: " + total + " | Approved: " + approved + " | Pending: " + pending + " | Draft: " + draft + " | Rejected: " + rejected);
    }
}
