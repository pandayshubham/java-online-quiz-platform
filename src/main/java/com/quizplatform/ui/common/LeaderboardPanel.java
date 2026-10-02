package com.quizplatform.ui.common;

import com.quizplatform.model.LeaderboardEntry;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import com.quizplatform.service.LeaderboardService;
import com.quizplatform.service.QuizService;
import com.quizplatform.service.impl.LeaderboardServiceImpl;
import com.quizplatform.service.impl.QuizServiceImpl;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Modern Swing panel for browsing Quiz-specific and Global Platform Leaderboards.
 * Displays podium rankings, top performers, percentages, and handles administrator toggling.
 */
public class LeaderboardPanel extends JPanel {

    private final User currentUser;
    private final LeaderboardService leaderboardService;
    private final QuizService quizService;

    // View Modes
    private static final String MODE_QUIZ = "QUIZ_MODE";
    private static final String MODE_GLOBAL = "GLOBAL_MODE";
    private String currentMode = MODE_QUIZ;

    // UI Components
    private JComboBox<Quiz> quizComboBox;
    private JButton btnQuizMode;
    private JButton btnGlobalMode;
    private JButton btnRefresh;
    private JButton btnAdminToggle;
    private JPanel disabledBanner;
    private JPanel podiumPanel;
    private JLabel lblPodium1;
    private JLabel lblPodium2;
    private JLabel lblPodium3;

    // Table
    private JTable leaderboardTable;
    private DefaultTableModel tableModel;
    private JLabel lblEmptyLeaderboard;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public LeaderboardPanel(User currentUser) {
        this(currentUser, new LeaderboardServiceImpl(), new QuizServiceImpl());
    }

    public LeaderboardPanel(User currentUser, LeaderboardService leaderboardService, QuizService quizService) {
        this.currentUser = currentUser;
        this.leaderboardService = leaderboardService;
        this.quizService = quizService;

        initUI();
        loadQuizzes();
        refreshLeaderboard();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Top Section: Header & Mode Controls
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // Header Title Row
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel lblTitle = new JLabel("🏆 Leaderboards & Rankings");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(15, 23, 42));
        headerRow.add(lblTitle, BorderLayout.WEST);

        // Right controls: Admin toggle (if admin) + Refresh
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerActions.setOpaque(false);

        if (currentUser != null && currentUser.getRole() == UserRole.ADMIN) {
            btnAdminToggle = new JButton("Leaderboard: Enabled");
            btnAdminToggle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnAdminToggle.setBackground(new Color(220, 252, 231));
            btnAdminToggle.setForeground(new Color(22, 101, 52));
            btnAdminToggle.setFocusPainted(false);
            btnAdminToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnAdminToggle.addActionListener(e -> toggleAdminLeaderboard());
            headerActions.add(btnAdminToggle);
        }

        btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setBackground(Color.WHITE);
        btnRefresh.setForeground(new Color(51, 65, 85));
        btnRefresh.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> refreshLeaderboard());
        headerActions.add(btnRefresh);

        headerRow.add(headerActions, BorderLayout.EAST);
        topContainer.add(headerRow);
        topContainer.add(Box.createRigidArea(new Dimension(0, 14)));

        // Mode Switching Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        toolbar.setOpaque(false);

        btnQuizMode = new JButton("🎯 Specific Quiz Ranking");
        btnQuizMode.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnQuizMode.setFocusPainted(false);
        btnQuizMode.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnQuizMode.addActionListener(e -> {
            currentMode = MODE_QUIZ;
            updateModeButtons();
            quizComboBox.setVisible(true);
            refreshLeaderboard();
        });

        btnGlobalMode = new JButton("🌐 Global Platform Rankings");
        btnGlobalMode.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGlobalMode.setFocusPainted(false);
        btnGlobalMode.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGlobalMode.addActionListener(e -> {
            currentMode = MODE_GLOBAL;
            updateModeButtons();
            quizComboBox.setVisible(false);
            refreshLeaderboard();
        });

        toolbar.add(btnQuizMode);
        toolbar.add(btnGlobalMode);

        // Quiz selector combo box
        quizComboBox = new JComboBox<>();
        quizComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        quizComboBox.setPreferredSize(new Dimension(300, 36));
        quizComboBox.setBackground(Color.WHITE);
        quizComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Quiz q) {
                    setText(q.getTitle() + " (" + q.getDurationMinutes() + " mins)");
                } else if (value == null) {
                    setText("-- Select a Quiz --");
                }
                return this;
            }
        });
        quizComboBox.addActionListener(e -> {
            if (MODE_QUIZ.equals(currentMode)) {
                refreshLeaderboard();
            }
        });
        toolbar.add(quizComboBox);

        topContainer.add(toolbar);
        topContainer.add(Box.createRigidArea(new Dimension(0, 14)));

        // Disabled Alert Banner
        disabledBanner = new JPanel(new BorderLayout());
        disabledBanner.setBackground(new Color(254, 242, 242));
        disabledBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(252, 165, 165)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        JLabel lblDisabled = new JLabel("⚠️ Public Leaderboard is currently disabled by administrator.");
        lblDisabled.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDisabled.setForeground(new Color(185, 28, 28));
        disabledBanner.add(lblDisabled, BorderLayout.WEST);
        disabledBanner.setVisible(false);
        topContainer.add(disabledBanner);

        // Podium Top 3 Cards
        podiumPanel = createPodiumPanel();
        topContainer.add(podiumPanel);

        add(topContainer, BorderLayout.NORTH);

        // 2. Table for Leaderboard
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        leaderboardTable = new JTable(tableModel);
        leaderboardTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        leaderboardTable.setRowHeight(38);
        leaderboardTable.setShowGrid(false);
        leaderboardTable.setIntercellSpacing(new Dimension(0, 0));
        leaderboardTable.setSelectionBackground(new Color(241, 245, 249));
        leaderboardTable.setSelectionForeground(new Color(15, 23, 42));

        JTableHeader tableHeader = leaderboardTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHeader.setBackground(new Color(241, 245, 249));
        tableHeader.setForeground(new Color(71, 85, 105));
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        lblEmptyLeaderboard = new JLabel("No leaderboard data is available.", SwingConstants.CENTER);
        lblEmptyLeaderboard.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblEmptyLeaderboard.setForeground(new Color(100, 116, 139));
        lblEmptyLeaderboard.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        lblEmptyLeaderboard.setVisible(false);
        tableContainer.add(lblEmptyLeaderboard, BorderLayout.SOUTH);

        add(tableContainer, BorderLayout.CENTER);

        updateModeButtons();
    }

    private JPanel createPodiumPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 16, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // 2nd Place (Silver)
        lblPodium2 = new JLabel("<html><center>🥈 <b>2nd Place</b><br><span style='color:#64748B;'>--</span></center></html>", SwingConstants.CENTER);
        panel.add(createPodiumCard(lblPodium2, new Color(241, 245, 249), new Color(148, 163, 184)));

        // 1st Place (Gold)
        lblPodium1 = new JLabel("<html><center>🥇 <b>Champion (1st)</b><br><span style='color:#B45309;'>--</span></center></html>", SwingConstants.CENTER);
        panel.add(createPodiumCard(lblPodium1, new Color(254, 243, 199), new Color(245, 158, 11)));

        // 3rd Place (Bronze)
        lblPodium3 = new JLabel("<html><center>🥉 <b>3rd Place</b><br><span style='color:#78350F;'>--</span></center></html>", SwingConstants.CENTER);
        panel.add(createPodiumCard(lblPodium3, new Color(255, 237, 213), new Color(217, 119, 6)));

        return panel;
    }

    private JPanel createPodiumCard(JLabel label, Color bgColor, Color borderColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(label, BorderLayout.CENTER);
        return card;
    }

    private void updateModeButtons() {
        if (MODE_QUIZ.equals(currentMode)) {
            btnQuizMode.setBackground(new Color(59, 130, 246));
            btnQuizMode.setForeground(Color.WHITE);
            btnQuizMode.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

            btnGlobalMode.setBackground(Color.WHITE);
            btnGlobalMode.setForeground(new Color(71, 85, 105));
            btnGlobalMode.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        } else {
            btnGlobalMode.setBackground(new Color(59, 130, 246));
            btnGlobalMode.setForeground(Color.WHITE);
            btnGlobalMode.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

            btnQuizMode.setBackground(Color.WHITE);
            btnQuizMode.setForeground(new Color(71, 85, 105));
            btnQuizMode.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        }
    }

    private void loadQuizzes() {
        quizComboBox.removeAllItems();
        List<Quiz> quizzes = quizService.getApprovedQuizzes();
        for (Quiz q : quizzes) {
            quizComboBox.addItem(q);
        }
    }

    public void refreshLeaderboard() {
        boolean enabled = leaderboardService.isLeaderboardEnabled();
        disabledBanner.setVisible(!enabled);

        if (btnAdminToggle != null) {
            btnAdminToggle.setText("Leaderboard: " + (enabled ? "Enabled" : "Disabled"));
            btnAdminToggle.setBackground(enabled ? new Color(220, 252, 231) : new Color(254, 226, 226));
            btnAdminToggle.setForeground(enabled ? new Color(22, 101, 52) : new Color(185, 28, 28));
        }

        if (!enabled) {
            tableModel.setRowCount(0);
            podiumPanel.setVisible(false);
            if (lblEmptyLeaderboard != null) lblEmptyLeaderboard.setVisible(false);
            return;
        }

        podiumPanel.setVisible(true);

        if (MODE_QUIZ.equals(currentMode)) {
            loadQuizLeaderboard();
        } else {
            loadGlobalLeaderboard();
        }
    }

    private void loadQuizLeaderboard() {
        tableModel.setColumnIdentifiers(new Object[]{"Rank", "Participant Name", "Email", "Score", "Percentage", "Completed At"});
        tableModel.setRowCount(0);

        Quiz selectedQuiz = (Quiz) quizComboBox.getSelectedItem();
        if (selectedQuiz == null) {
            clearPodium();
            if (lblEmptyLeaderboard != null) lblEmptyLeaderboard.setVisible(true);
            return;
        }

        List<LeaderboardEntry> list = leaderboardService.getQuizLeaderboard(selectedQuiz.getId());
        updatePodium(list);

        if (list == null || list.isEmpty()) {
            if (lblEmptyLeaderboard != null) lblEmptyLeaderboard.setVisible(true);
            return;
        }

        if (lblEmptyLeaderboard != null) lblEmptyLeaderboard.setVisible(false);
        for (LeaderboardEntry entry : list) {
            String rankStr = formatRank(entry.getRank());
            String scoreStr = entry.getScore() + " / " + entry.getTotalQuestions();
            String pctStr = String.format("%.1f%%", entry.getPercentage());
            String dateStr = entry.getCompletedAt() != null ? entry.getCompletedAt().format(DATE_FORMATTER) : "N/A";

            tableModel.addRow(new Object[]{
                    rankStr,
                    entry.getParticipantName(),
                    entry.getParticipantEmail(),
                    scoreStr,
                    pctStr,
                    dateStr
            });
        }

        applyCellRenderers();
    }

    private void loadGlobalLeaderboard() {
        tableModel.setColumnIdentifiers(new Object[]{"Rank", "Participant Name", "Email", "Quizzes Completed", "Total Score", "Avg Percentage", "Last Active"});
        tableModel.setRowCount(0);

        List<LeaderboardEntry> list = leaderboardService.getGlobalLeaderboard();
        updatePodium(list);

        if (list == null || list.isEmpty()) {
            if (lblEmptyLeaderboard != null) lblEmptyLeaderboard.setVisible(true);
            return;
        }

        if (lblEmptyLeaderboard != null) lblEmptyLeaderboard.setVisible(false);
        for (LeaderboardEntry entry : list) {
            String rankStr = formatRank(entry.getRank());
            String pctStr = String.format("%.1f%%", entry.getAveragePercentage());
            String dateStr = entry.getCompletedAt() != null ? entry.getCompletedAt().format(DATE_FORMATTER) : "N/A";

            tableModel.addRow(new Object[]{
                    rankStr,
                    entry.getParticipantName(),
                    entry.getParticipantEmail(),
                    entry.getQuizzesCompleted(),
                    entry.getTotalScore(),
                    pctStr,
                    dateStr
            });
        }

        applyCellRenderers();
    }

    private String formatRank(int rank) {
        return switch (rank) {
            case 1 -> "🥇 1st";
            case 2 -> "🥈 2nd";
            case 3 -> "🥉 3rd";
            default -> "#" + rank;
        };
    }

    private void updatePodium(List<LeaderboardEntry> list) {
        if (list.size() >= 1) {
            LeaderboardEntry e1 = list.get(0);
            lblPodium1.setText("<html><center>🥇 <b>Champion (1st)</b><br><span style='font-size:13px; font-weight:bold; color:#B45309;'>" +
                    e1.getParticipantName() + "</span><br><span style='color:#78350F; font-size:11px;'>" +
                    (MODE_QUIZ.equals(currentMode) ? String.format("%.1f%% (%d pts)", e1.getPercentage(), e1.getScore()) : String.format("%.1f%% avg (%d pts)", e1.getAveragePercentage(), e1.getTotalScore())) +
                    "</span></center></html>");
        } else {
            lblPodium1.setText("<html><center>🥇 <b>Champion (1st)</b><br><span style='color:#B45309;'>--</span></center></html>");
        }

        if (list.size() >= 2) {
            LeaderboardEntry e2 = list.get(1);
            lblPodium2.setText("<html><center>🥈 <b>2nd Place</b><br><span style='font-size:13px; font-weight:bold; color:#475569;'>" +
                    e2.getParticipantName() + "</span><br><span style='color:#64748B; font-size:11px;'>" +
                    (MODE_QUIZ.equals(currentMode) ? String.format("%.1f%% (%d pts)", e2.getPercentage(), e2.getScore()) : String.format("%.1f%% avg (%d pts)", e2.getAveragePercentage(), e2.getTotalScore())) +
                    "</span></center></html>");
        } else {
            lblPodium2.setText("<html><center>🥈 <b>2nd Place</b><br><span style='color:#64748B;'>--</span></center></html>");
        }

        if (list.size() >= 3) {
            LeaderboardEntry e3 = list.get(2);
            lblPodium3.setText("<html><center>🥉 <b>3rd Place</b><br><span style='font-size:13px; font-weight:bold; color:#92400E;'>" +
                    e3.getParticipantName() + "</span><br><span style='color:#B45309; font-size:11px;'>" +
                    (MODE_QUIZ.equals(currentMode) ? String.format("%.1f%% (%d pts)", e3.getPercentage(), e3.getScore()) : String.format("%.1f%% avg (%d pts)", e3.getAveragePercentage(), e3.getTotalScore())) +
                    "</span></center></html>");
        } else {
            lblPodium3.setText("<html><center>🥉 <b>3rd Place</b><br><span style='color:#78350F;'>--</span></center></html>");
        }
    }

    private void clearPodium() {
        lblPodium1.setText("<html><center>🥇 <b>Champion (1st)</b><br><span style='color:#B45309;'>--</span></center></html>");
        lblPodium2.setText("<html><center>🥈 <b>2nd Place</b><br><span style='color:#64748B;'>--</span></center></html>");
        lblPodium3.setText("<html><center>🥉 <b>3rd Place</b><br><span style='color:#78350F;'>--</span></center></html>");
    }

    private void applyCellRenderers() {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer boldRankRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                if (row == 0) {
                    setForeground(new Color(217, 119, 6)); // Gold
                } else if (row == 1) {
                    setForeground(new Color(100, 116, 139)); // Silver
                } else if (row == 2) {
                    setForeground(new Color(180, 83, 9)); // Bronze
                } else {
                    setForeground(new Color(71, 85, 105));
                }
                return this;
            }
        };

        if (leaderboardTable.getColumnCount() > 0) {
            leaderboardTable.getColumnModel().getColumn(0).setCellRenderer(boldRankRenderer);
            leaderboardTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        }
    }

    private void toggleAdminLeaderboard() {
        boolean currentlyEnabled = leaderboardService.isLeaderboardEnabled();
        boolean newState = !currentlyEnabled;
        leaderboardService.setLeaderboardEnabled(newState);
        JOptionPane.showMessageDialog(this,
                "Public Leaderboard has been " + (newState ? "ENABLED" : "DISABLED") + ".",
                "Leaderboard Configuration",
                JOptionPane.INFORMATION_MESSAGE);
        refreshLeaderboard();
    }
}
