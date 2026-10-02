package com.quizplatform.ui.participant;

import com.quizplatform.dao.QuizDAO;
import com.quizplatform.dao.impl.QuizDAOImpl;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizReminder;
import com.quizplatform.model.ReminderStatus;
import com.quizplatform.model.User;
import com.quizplatform.service.QuizReminderService;
import com.quizplatform.service.impl.QuizReminderServiceImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Panel displaying scheduled reminders for the participant with options to cancel.
 */
public class MyRemindersPanel extends JPanel {

    private final User currentUser;
    private final QuizReminderService reminderService;
    private final QuizDAO quizDAO;

    private JTable remindersTable;
    private DefaultTableModel tableModel;
    private JButton btnCancelReminder;
    private JButton btnRefresh;
    private JLabel lblEmptyReminders;

    private List<QuizReminder> currentReminders = new ArrayList<>();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MyRemindersPanel(User currentUser) {
        this(currentUser, new QuizReminderServiceImpl(), new QuizDAOImpl());
    }

    public MyRemindersPanel(User currentUser, QuizReminderService reminderService, QuizDAO quizDAO) {
        this.currentUser = currentUser;
        this.reminderService = reminderService;
        this.quizDAO = quizDAO;

        initUI();
        loadReminders();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header Row
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("⏰ My Scheduled Quiz Reminders");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(15, 23, 42));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setBackground(Color.WHITE);
        btnRefresh.setForeground(new Color(51, 65, 85));
        btnRefresh.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadReminders());
        actionPanel.add(btnRefresh);

        btnCancelReminder = new JButton("Cancel Selected Reminder");
        btnCancelReminder.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancelReminder.setBackground(new Color(239, 68, 68));
        btnCancelReminder.setForeground(Color.WHITE);
        btnCancelReminder.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btnCancelReminder.setFocusPainted(false);
        btnCancelReminder.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelReminder.setEnabled(false);
        btnCancelReminder.addActionListener(e -> handleCancelReminder());
        actionPanel.add(btnCancelReminder);

        headerPanel.add(actionPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new Object[]{"ID", "Quiz Title", "Reminder Time", "Status", "Scheduled On"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        remindersTable = new JTable(tableModel);
        remindersTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        remindersTable.setRowHeight(38);
        remindersTable.setShowGrid(false);
        remindersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        remindersTable.setSelectionBackground(new Color(241, 245, 249));
        remindersTable.setSelectionForeground(new Color(15, 23, 42));

        JTableHeader tableHeader = remindersTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHeader.setBackground(new Color(241, 245, 249));
        tableHeader.setForeground(new Color(71, 85, 105));
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        remindersTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = remindersTable.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < currentReminders.size()) {
                QuizReminder r = currentReminders.get(selectedRow);
                btnCancelReminder.setEnabled(r.getReminderStatus() == ReminderStatus.PENDING);
            } else {
                btnCancelReminder.setEnabled(false);
            }
        });

        // Hide ID column
        remindersTable.getColumnModel().getColumn(0).setMinWidth(0);
        remindersTable.getColumnModel().getColumn(0).setMaxWidth(0);
        remindersTable.getColumnModel().getColumn(0).setWidth(0);

        // Status renderer
        remindersTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                if ("PENDING".equals(value)) {
                    setForeground(new Color(217, 119, 6)); // Amber
                } else if ("SENT".equals(value)) {
                    setForeground(new Color(22, 101, 52)); // Green
                } else {
                    setForeground(new Color(148, 163, 184)); // Gray
                }
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(remindersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        lblEmptyReminders = new JLabel("No reminders scheduled.", SwingConstants.CENTER);
        lblEmptyReminders.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblEmptyReminders.setForeground(new Color(100, 116, 139));
        lblEmptyReminders.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        lblEmptyReminders.setVisible(false);
        tableContainer.add(lblEmptyReminders, BorderLayout.SOUTH);

        add(tableContainer, BorderLayout.CENTER);
    }

    public void loadReminders() {
        tableModel.setRowCount(0);
        currentReminders = reminderService.getRemindersForParticipant(currentUser.getId());
        btnCancelReminder.setEnabled(false);

        if (currentReminders == null || currentReminders.isEmpty()) {
            lblEmptyReminders.setVisible(true);
            return;
        }

        lblEmptyReminders.setVisible(false);
        for (QuizReminder r : currentReminders) {
            Quiz quiz = quizDAO.findById(r.getQuizId());
            String quizTitle = (quiz != null && quiz.getTitle() != null) ? quiz.getTitle() : "Quiz #" + r.getQuizId();
            String timeStr = r.getReminderTime() != null ? r.getReminderTime().format(DATE_FORMATTER) : "";
            String createdStr = r.getCreatedAt() != null ? r.getCreatedAt().format(DATE_FORMATTER) : "";

            tableModel.addRow(new Object[]{
                    r.getId(),
                    quizTitle,
                    timeStr,
                    r.getReminderStatus().name(),
                    createdStr
            });
        }
    }

    private void handleCancelReminder() {
        int selectedRow = remindersTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentReminders.size()) return;

        QuizReminder reminder = currentReminders.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this quiz reminder?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = reminderService.cancelReminder(reminder.getId(), currentUser.getId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Reminder cancelled successfully.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                loadReminders();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel reminder.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
