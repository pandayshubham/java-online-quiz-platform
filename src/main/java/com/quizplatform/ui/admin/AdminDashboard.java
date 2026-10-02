package com.quizplatform.ui.admin;

import com.quizplatform.dao.QuizDAO;
import com.quizplatform.dao.UserDAO;
import com.quizplatform.dao.impl.QuizDAOImpl;
import com.quizplatform.dao.impl.UserDAOImpl;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizAttempt;
import com.quizplatform.model.QuizStatus;
import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import com.quizplatform.model.report.PlatformPerformanceSummary;
import com.quizplatform.service.PerformanceReportService;
import com.quizplatform.service.QuizService;
import com.quizplatform.service.SystemSettingsService;
import com.quizplatform.service.UserService;
import com.quizplatform.service.impl.PerformanceReportServiceImpl;
import com.quizplatform.service.impl.QuizServiceImpl;
import com.quizplatform.service.impl.SystemSettingsServiceImpl;
import com.quizplatform.service.impl.UserServiceImpl;
import com.quizplatform.service.NotificationService;
import com.quizplatform.service.impl.NotificationServiceImpl;
import com.quizplatform.ui.LoginFrame;
import com.quizplatform.ui.common.BaseDashboard;
import com.quizplatform.ui.common.LeaderboardPanel;
import com.quizplatform.ui.common.NotificationDialog;
import com.quizplatform.util.CsvExportUtil;
import com.quizplatform.util.SessionManager;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Functional Admin Dashboard.
 * Demonstrates OOP Inheritance by extending BaseDashboard.
 */
public class AdminDashboard extends BaseDashboard {

    private final UserService userService;
    private final PerformanceReportService reportService;
    private final QuizDAO quizDAO;
    private final UserDAO userDAO;
    private final NotificationService notificationService;
    private final QuizService quizService;
    private final SystemSettingsService settingsService;

    private CardLayout cardLayout;
    private JPanel contentArea;
    private UserManagementPanel userManagementPanel;
    private LeaderboardPanel leaderboardPanel;
    private SystemSettingsPanel systemSettingsPanel;
    private JButton btnNotifications;

    // Quiz Approval components
    private JTable pendingQuizzesTable;
    private DefaultTableModel pendingQuizzesModel;
    private List<Quiz> pendingQuizzesList = new ArrayList<>();
    private JLabel lblEmptyPendingQuizzes;

    // Statistics labels
    private JLabel totalUsersVal;
    private JLabel activeUsersVal;
    private JLabel inactiveUsersVal;
    private JLabel adminsVal;
    private JLabel creatorsVal;
    private JLabel participantsVal;

    // Performance Overview components
    private JLabel totalAttemptsVal;
    private JLabel completedAttemptsVal;
    private JLabel timeExpiredAttemptsVal;
    private JLabel avgPlatformScoreVal;
    private JLabel totalQuestionsVal;
    private JLabel totalAnswersVal;
    private JTable recentAttemptsTable;
    private DefaultTableModel recentAttemptsModel;
    private List<QuizAttempt> recentAttemptsList = new ArrayList<>();

    // Navigation buttons tracking
    private final Map<String, JButton> navButtons = new HashMap<>();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AdminDashboard(User user) {
        this(user, new UserServiceImpl());
    }

    public AdminDashboard(User user, UserService userService) {
        super(user, "Java Online Quiz Platform - Admin Console");
        this.userService = userService;
        this.reportService = new PerformanceReportServiceImpl();
        this.quizDAO = new QuizDAOImpl();
        this.userDAO = new UserDAOImpl();
        this.notificationService = new NotificationServiceImpl();
        this.quizService = new QuizServiceImpl();
        this.settingsService = new SystemSettingsServiceImpl();

        // Admin authorization check
        if (!SessionManager.isLoggedIn() || !SessionManager.hasRole(UserRole.ADMIN)) {
            JOptionPane.showMessageDialog(null, "Access Denied: Administrator privileges required.", "Authorization Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            new LoginFrame().setVisible(true);
            return;
        }

        initUI();
        refreshStatistics();
        updateNotificationBadge();
    }

    private void initUI() {
        setTitle("Java Online Quiz Platform - Admin Dashboard");
        setSize(1020, 680);
        setMinimumSize(new Dimension(880, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(248, 250, 252));

        // 1. NORTH: Header Bar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 23, 42)); // Dark Slate
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel brandLabel = new JLabel("Java Online Quiz Platform  |  Admin Console");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandLabel.setForeground(Color.WHITE);

        JPanel userSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userSection.setOpaque(false);

        String adminName = (user != null) ? user.getName() : "Administrator";
        JLabel welcomeLabel = new JLabel("Logged in as: " + adminName + " (ADMIN)");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        welcomeLabel.setForeground(new Color(226, 232, 240));

        btnNotifications = new JButton("🔔 Notifications");
        btnNotifications.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNotifications.setPreferredSize(new Dimension(145, 30));
        btnNotifications.setBackground(new Color(30, 41, 59));
        btnNotifications.setForeground(new Color(203, 213, 225));
        btnNotifications.setFocusPainted(false);
        btnNotifications.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNotifications.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));
        btnNotifications.addActionListener(e -> {
            new NotificationDialog(this, user, this::updateNotificationBadge).setVisible(true);
        });

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setPreferredSize(new Dimension(85, 30));
        logoutButton.setBackground(new Color(239, 68, 68));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> handleLogout());

        userSection.add(welcomeLabel);
        userSection.add(btnNotifications);
        userSection.add(logoutButton);

        headerPanel.add(brandLabel, BorderLayout.WEST);
        headerPanel.add(userSection, BorderLayout.EAST);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. WEST: Sidebar Navigation
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(Color.WHITE);
        sidebarPanel.setPreferredSize(new Dimension(200, 0));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(226, 232, 240)));

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebarPanel.add(createNavButton("Dashboard", "dashboard"));
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebarPanel.add(createNavButton("User Management", "users"));
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebarPanel.add(createNavButton("Leaderboard 🏆", "leaderboard"));
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebarPanel.add(createNavButton("Quiz Approval", "quizzes"));
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebarPanel.add(createNavButton("Reports", "reports"));
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebarPanel.add(createNavButton("Settings", "settings"));
        sidebarPanel.add(Box.createVerticalGlue());

        rootPanel.add(sidebarPanel, BorderLayout.WEST);

        // 3. CENTER: CardLayout Content Area
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(new Color(248, 250, 252));

        // Card 1: Dashboard Home (Statistics)
        contentArea.add(createHomePanel(), "dashboard");

        // Card 2: User Management (Functional)
        userManagementPanel = new UserManagementPanel(userService, this::refreshStatistics);
        contentArea.add(userManagementPanel, "users");

        // Card: Leaderboard (Functional)
        leaderboardPanel = new LeaderboardPanel(user);
        contentArea.add(leaderboardPanel, "leaderboard");

        // Card 3: Quiz Approval (Functional)
        contentArea.add(createQuizApprovalPanel(), "quizzes");

        // Card 4: Reports (Functional Platform Performance Overview)
        contentArea.add(createPerformanceOverviewPanel(), "reports");

        // Card 5: Settings (Functional System Settings Panel)
        systemSettingsPanel = new SystemSettingsPanel(settingsService);
        contentArea.add(systemSettingsPanel, "settings");

        rootPanel.add(contentArea, BorderLayout.CENTER);
        setContentPane(rootPanel);

        // Set default active tab
        setActiveNav("dashboard");
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setMaximumSize(new Dimension(185, 38));
        btn.setPreferredSize(new Dimension(185, 38));
        btn.setAlignmentX(JButton.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(51, 65, 85));

        btn.addActionListener(e -> {
            cardLayout.show(contentArea, cardName);
            setActiveNav(cardName);

            if ("dashboard".equals(cardName)) {
                refreshStatistics();
            } else if ("reports".equals(cardName)) {
                refreshPerformanceOverview();
            } else if ("leaderboard".equals(cardName)) {
                if (leaderboardPanel != null) leaderboardPanel.refreshLeaderboard();
            } else if ("quizzes".equals(cardName)) {
                refreshPendingQuizzes();
            } else if ("settings".equals(cardName)) {
                if (systemSettingsPanel != null) systemSettingsPanel.loadSettings();
            }
        });

        navButtons.put(cardName, btn);
        return btn;
    }

    private void setActiveNav(String activeCardName) {
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equals(activeCardName)) {
                btn.setBackground(new Color(238, 242, 255));
                btn.setForeground(new Color(79, 70, 229));
                btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else {
                btn.setBackground(Color.WHITE);
                btn.setForeground(new Color(51, 65, 85));
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
        }
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Welcome banner
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Color.WHITE);
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        JLabel titleLbl = new JLabel("System Overview");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(new Color(15, 23, 42));

        JLabel subLbl = new JLabel("Real-time summary of registered users and platform role breakdown.");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(new Color(100, 116, 139));

        JPanel bannerText = new JPanel();
        bannerText.setLayout(new BoxLayout(bannerText, BoxLayout.Y_AXIS));
        bannerText.setOpaque(false);
        bannerText.add(titleLbl);
        bannerText.add(Box.createRigidArea(new Dimension(0, 4)));
        bannerText.add(subLbl);
        banner.add(bannerText, BorderLayout.WEST);

        panel.add(banner, BorderLayout.NORTH);

        // 6 Statistic KPI cards
        JPanel kpiContainer = new JPanel(new GridLayout(2, 3, 16, 16));
        kpiContainer.setOpaque(false);

        totalUsersVal = new JLabel("0", SwingConstants.CENTER);
        activeUsersVal = new JLabel("0", SwingConstants.CENTER);
        inactiveUsersVal = new JLabel("0", SwingConstants.CENTER);
        adminsVal = new JLabel("0", SwingConstants.CENTER);
        creatorsVal = new JLabel("0", SwingConstants.CENTER);
        participantsVal = new JLabel("0", SwingConstants.CENTER);

        kpiContainer.add(createStatCard("TOTAL REGISTERED USERS", totalUsersVal, new Color(79, 70, 229)));
        kpiContainer.add(createStatCard("ACTIVE USERS", activeUsersVal, new Color(16, 185, 129)));
        kpiContainer.add(createStatCard("INACTIVE / SUSPENDED", inactiveUsersVal, new Color(239, 68, 68)));
        kpiContainer.add(createStatCard("ADMINISTRATORS", adminsVal, new Color(217, 119, 6)));
        kpiContainer.add(createStatCard("QUIZ CREATORS", creatorsVal, new Color(139, 92, 246)));
        kpiContainer.add(createStatCard("PARTICIPANTS", participantsVal, new Color(59, 130, 246)));

        panel.add(kpiContainer, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240)),
                        BorderFactory.createEmptyBorder(16, 20, 16, 20)
                )
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(new Color(15, 23, 42));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createPerformanceOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Platform Performance Overview");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel subLabel = new JLabel("Comprehensive platform-wide metrics and recent quiz attempts.");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(100, 116, 139));

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 2)));
        titlePanel.add(subLabel);
        topBar.add(titlePanel, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        JButton exportCsvBtn = new JButton("Export Recent Attempts CSV");
        exportCsvBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        exportCsvBtn.setBackground(new Color(241, 245, 249));
        exportCsvBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportCsvBtn.addActionListener(e -> handleExportAdminCsv());
        controls.add(exportCsvBtn);

        JButton refreshBtn = new JButton("Refresh Overview");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setBackground(new Color(241, 245, 249));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshPerformanceOverview());
        controls.add(refreshBtn);

        topBar.add(controls, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        // Center: KPI cards + Recent attempts table
        JPanel centerContainer = new JPanel(new BorderLayout(0, 12));
        centerContainer.setOpaque(false);

        // KPI Row
        JPanel kpiRow = new JPanel(new GridLayout(1, 6, 12, 0));
        kpiRow.setOpaque(false);
        kpiRow.setPreferredSize(new Dimension(0, 72));

        totalAttemptsVal = new JLabel("0", SwingConstants.CENTER);
        completedAttemptsVal = new JLabel("0", SwingConstants.CENTER);
        timeExpiredAttemptsVal = new JLabel("0", SwingConstants.CENTER);
        avgPlatformScoreVal = new JLabel("0.0", SwingConstants.CENTER);
        totalQuestionsVal = new JLabel("0", SwingConstants.CENTER);
        totalAnswersVal = new JLabel("0", SwingConstants.CENTER);

        kpiRow.add(createStatCard("TOTAL ATTEMPTS", totalAttemptsVal, new Color(79, 70, 229)));
        kpiRow.add(createStatCard("COMPLETED", completedAttemptsVal, new Color(16, 185, 129)));
        kpiRow.add(createStatCard("TIME EXPIRED", timeExpiredAttemptsVal, new Color(220, 38, 38)));
        kpiRow.add(createStatCard("AVG SCORE", avgPlatformScoreVal, new Color(37, 99, 235)));
        kpiRow.add(createStatCard("TOTAL QUESTIONS", totalQuestionsVal, new Color(217, 119, 6)));
        kpiRow.add(createStatCard("TOTAL ANSWERS", totalAnswersVal, new Color(139, 92, 246)));

        centerContainer.add(kpiRow, BorderLayout.NORTH);

        // Recent attempts table
        JPanel tableContainer = new JPanel(new BorderLayout(0, 6));
        tableContainer.setOpaque(false);

        JLabel tableHeaderLbl = new JLabel("Recent Platform Quiz Attempts");
        tableHeaderLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHeaderLbl.setForeground(new Color(30, 41, 59));
        tableContainer.add(tableHeaderLbl, BorderLayout.NORTH);

        String[] columns = {"Participant", "Quiz", "Creator", "Score", "Percentage", "Status", "Attempt Date"};
        recentAttemptsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        recentAttemptsTable = new JTable(recentAttemptsModel);
        recentAttemptsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentAttemptsTable.setRowHeight(34);
        recentAttemptsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        recentAttemptsTable.setGridColor(new Color(241, 245, 249));
        recentAttemptsTable.setShowHorizontalLines(true);
        recentAttemptsTable.setShowVerticalLines(false);

        JTableHeader th = recentAttemptsTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 11));
        th.setBackground(new Color(248, 250, 252));
        th.setForeground(new Color(71, 85, 105));
        th.setPreferredSize(new Dimension(th.getWidth(), 34));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        recentAttemptsTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        recentAttemptsTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        recentAttemptsTable.getColumnModel().getColumn(4).setPreferredWidth(85);
        recentAttemptsTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        recentAttemptsTable.getColumnModel().getColumn(6).setPreferredWidth(125);
        recentAttemptsTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        recentAttemptsTable.getColumnModel().getColumn(5).setPreferredWidth(95);
        recentAttemptsTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                if (!isSelected) {
                    String status = value != null ? value.toString() : "";
                    switch (status) {
                        case "COMPLETED" -> {
                            lbl.setForeground(new Color(22, 101, 52));
                            lbl.setBackground(new Color(220, 252, 231));
                        }
                        case "IN_PROGRESS" -> {
                            lbl.setForeground(new Color(29, 78, 216));
                            lbl.setBackground(new Color(219, 234, 254));
                        }
                        default -> {
                            lbl.setForeground(new Color(153, 27, 27));
                            lbl.setBackground(new Color(254, 226, 226));
                        }
                    }
                    lbl.setOpaque(true);
                }
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(recentAttemptsTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scroll.getViewport().setBackground(Color.WHITE);
        tableContainer.add(scroll, BorderLayout.CENTER);

        centerContainer.add(tableContainer, BorderLayout.CENTER);
        panel.add(centerContainer, BorderLayout.CENTER);

        return panel;
    }

    public void refreshPerformanceOverview() {
        try {
            PlatformPerformanceSummary summary = reportService.getPlatformSummary();
            totalAttemptsVal.setText(String.valueOf(summary.getTotalQuizAttempts()));
            completedAttemptsVal.setText(String.valueOf(summary.getCompletedAttempts()));
            timeExpiredAttemptsVal.setText(String.valueOf(summary.getTimeExpiredAttempts()));
            avgPlatformScoreVal.setText(String.format("%.1f", summary.getAveragePlatformScore()));
            totalQuestionsVal.setText(String.valueOf(summary.getTotalQuizQuestions()));
            totalAnswersVal.setText(String.valueOf(summary.getTotalAnswers()));

            // Load recent attempts
            recentAttemptsModel.setRowCount(0);
            recentAttemptsList = reportService.getRecentPlatformAttempts(50);

            for (QuizAttempt a : recentAttemptsList) {
                User participant = userDAO.findById(a.getParticipantId());
                String pName = participant != null ? participant.getName() : ("User #" + a.getParticipantId());

                Quiz quiz = quizDAO.findById(a.getQuizId());
                String qTitle = quiz != null ? quiz.getTitle() : ("Quiz #" + a.getQuizId());

                User creator = (quiz != null) ? userDAO.findById(quiz.getCreatorId()) : null;
                String cName = creator != null ? creator.getName() : "Unknown";

                String dateStr = a.getCompletedAt() != null ? a.getCompletedAt().format(DATE_FORMATTER) :
                        (a.getStartedAt() != null ? a.getStartedAt().format(DATE_FORMATTER) : "N/A");

                recentAttemptsModel.addRow(new Object[]{
                        pName,
                        qTitle,
                        cName,
                        a.getScore(),
                        String.format("%.1f%%", a.getPercentage()),
                        a.getStatus().name(),
                        dateStr
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load performance data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleExportAdminCsv() {
        if (recentAttemptsList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No recent attempts available to export.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Recent Attempts CSV");
        fileChooser.setSelectedFile(new File("Platform_Recent_Attempts.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            }

            try {
                Map<Integer, String> quizTitleMap = new HashMap<>();
                Map<Integer, String> creatorNameMap = new HashMap<>();
                Map<Integer, String> partNameMap = new HashMap<>();

                for (QuizAttempt a : recentAttemptsList) {
                    Quiz q = quizDAO.findById(a.getQuizId());
                    if (q != null) {
                        quizTitleMap.put(q.getId(), q.getTitle());
                        User creator = userDAO.findById(q.getCreatorId());
                        if (creator != null) {
                            creatorNameMap.put(q.getId(), creator.getName());
                        }
                    }
                    User p = userDAO.findById(a.getParticipantId());
                    if (p != null) {
                        partNameMap.put(p.getId(), p.getName());
                    }
                }

                CsvExportUtil.exportAdminRecentAttempts(fileToSave, recentAttemptsList, quizTitleMap, creatorNameMap, partNameMap);
                JOptionPane.showMessageDialog(this, "Platform attempts exported successfully to:\n" + fileToSave.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to export report: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createPlaceholderPanel(String title, String message) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(new Color(15, 23, 42));

        JLabel msgLbl = new JLabel(message);
        msgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        msgLbl.setForeground(new Color(100, 116, 139));

        card.add(titleLbl);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(msgLbl);
        card.add(Box.createVerticalGlue());

        panel.add(card);
        return panel;
    }

    /**
     * Queries the database via UserService to refresh all statistic counts.
     */
    @Override
    public void refreshStatistics() {
        try {
            int total = userService.getTotalUsers();
            int active = userService.getActiveUsers();
            int inactive = userService.getInactiveUsers();
            int admins = userService.getUserCountByRole(UserRole.ADMIN);
            int creators = userService.getUserCountByRole(UserRole.QUIZ_CREATOR);
            int participants = userService.getUserCountByRole(UserRole.PARTICIPANT);

            if (totalUsersVal != null) totalUsersVal.setText(String.valueOf(total));
            if (activeUsersVal != null) activeUsersVal.setText(String.valueOf(active));
            if (inactiveUsersVal != null) inactiveUsersVal.setText(String.valueOf(inactive));
            if (adminsVal != null) adminsVal.setText(String.valueOf(admins));
            if (creatorsVal != null) creatorsVal.setText(String.valueOf(creators));
            if (participantsVal != null) participantsVal.setText(String.valueOf(participants));
        } catch (Exception ex) {
            System.err.println("Failed to refresh statistics: " + ex.getMessage());
        }
    }

    @Override
    public void updateNotificationBadge() {
        if (btnNotifications == null) return;
        int unread = notificationService.getUnreadCount(user.getId());
        if (unread > 0) {
            btnNotifications.setText("🔔 Notifications (" + unread + ")");
            btnNotifications.setBackground(new Color(245, 158, 11)); // Amber
            btnNotifications.setForeground(Color.WHITE);
        } else {
            btnNotifications.setText("🔔 Notifications");
            btnNotifications.setBackground(new Color(30, 41, 59));
            btnNotifications.setForeground(new Color(203, 213, 225));
        }
    }

    private void handleLogout() {
        performLogout("Are you sure you want to log out of the Admin Console?");
    }

    private JPanel createQuizApprovalPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLbl = new JLabel("Quiz Approval & Moderation");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setForeground(new Color(15, 23, 42));

        JLabel subLbl = new JLabel("Review creator quizzes submitted for approval before they become available to participants.");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLbl.setForeground(new Color(100, 116, 139));

        titlePanel.add(titleLbl);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 4)));
        titlePanel.add(subLbl);
        topBar.add(titlePanel, BorderLayout.WEST);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setBackground(Color.WHITE);
        btnRefresh.setForeground(new Color(51, 65, 85));
        btnRefresh.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> refreshPendingQuizzes());
        topBar.add(btnRefresh, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"Quiz ID", "Title", "Creator ID", "Duration (mins)", "Status", "Created At"};
        pendingQuizzesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        pendingQuizzesTable = new JTable(pendingQuizzesModel);
        pendingQuizzesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pendingQuizzesTable.setRowHeight(36);
        pendingQuizzesTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pendingQuizzesTable.setShowGrid(false);
        pendingQuizzesTable.setIntercellSpacing(new Dimension(0, 0));
        pendingQuizzesTable.setSelectionBackground(new Color(238, 242, 255));
        pendingQuizzesTable.setSelectionForeground(new Color(15, 23, 42));

        JTableHeader th = pendingQuizzesTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(241, 245, 249));
        th.setForeground(new Color(71, 85, 105));
        th.setPreferredSize(new Dimension(0, 38));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        pendingQuizzesTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        pendingQuizzesTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        pendingQuizzesTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        pendingQuizzesTable.getColumnModel().getColumn(2).setPreferredWidth(85);
        pendingQuizzesTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        pendingQuizzesTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        pendingQuizzesTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        pendingQuizzesTable.getColumnModel().getColumn(4).setPreferredWidth(95);

        JScrollPane scrollPane = new JScrollPane(pendingQuizzesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setOpaque(false);
        centerContainer.add(scrollPane, BorderLayout.CENTER);

        lblEmptyPendingQuizzes = new JLabel("No quizzes pending approval.", SwingConstants.CENTER);
        lblEmptyPendingQuizzes.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblEmptyPendingQuizzes.setForeground(new Color(100, 116, 139));
        lblEmptyPendingQuizzes.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        lblEmptyPendingQuizzes.setVisible(false);
        centerContainer.add(lblEmptyPendingQuizzes, BorderLayout.SOUTH);

        panel.add(centerContainer, BorderLayout.CENTER);

        // Action Buttons: Approve / Reject
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        bottomBar.setOpaque(false);

        JButton btnReject = new JButton("Reject Quiz");
        btnReject.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnReject.setBackground(new Color(239, 68, 68)); // Red
        btnReject.setForeground(Color.WHITE);
        btnReject.setPreferredSize(new Dimension(130, 38));
        btnReject.setFocusPainted(false);
        btnReject.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReject.addActionListener(e -> handleRejectQuiz());

        JButton btnApprove = new JButton("Approve Quiz");
        btnApprove.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnApprove.setBackground(new Color(16, 185, 129)); // Green
        btnApprove.setForeground(Color.WHITE);
        btnApprove.setPreferredSize(new Dimension(140, 38));
        btnApprove.setFocusPainted(false);
        btnApprove.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnApprove.addActionListener(e -> handleApproveQuiz());

        bottomBar.add(btnReject);
        bottomBar.add(btnApprove);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    public void refreshPendingQuizzes() {
        if (pendingQuizzesModel == null) return;
        pendingQuizzesModel.setRowCount(0);
        try {
            pendingQuizzesList = quizService.getPendingQuizzes();
            if (pendingQuizzesList == null || pendingQuizzesList.isEmpty()) {
                if (lblEmptyPendingQuizzes != null) lblEmptyPendingQuizzes.setVisible(true);
            } else {
                if (lblEmptyPendingQuizzes != null) lblEmptyPendingQuizzes.setVisible(false);
                for (Quiz q : pendingQuizzesList) {
                    String created = q.getCreatedAt() != null ? q.getCreatedAt().format(DATE_FORMATTER) : "N/A";
                    pendingQuizzesModel.addRow(new Object[]{
                            q.getId(),
                            q.getTitle(),
                            q.getCreatorId(),
                            q.getDurationMinutes(),
                            q.getStatus().name(),
                            created
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load pending quizzes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleApproveQuiz() {
        int row = pendingQuizzesTable.getSelectedRow();
        if (row < 0 || row >= pendingQuizzesList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a pending quiz to approve.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Quiz quiz = pendingQuizzesList.get(row);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to approve quiz #" + quiz.getId() + " (\"" + quiz.getTitle() + "\")?",
                "Confirm Approval",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = quizService.updateQuizStatus(quiz.getId(), QuizStatus.APPROVED);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Quiz approved successfully and is now active for participants.", "Quiz Approved", JOptionPane.INFORMATION_MESSAGE);
                    refreshPendingQuizzes();
                } else {
                    JOptionPane.showMessageDialog(this, "Unable to approve quiz. Please check status.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to approve quiz: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleRejectQuiz() {
        int row = pendingQuizzesTable.getSelectedRow();
        if (row < 0 || row >= pendingQuizzesList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a pending quiz to reject.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Quiz quiz = pendingQuizzesList.get(row);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reject quiz #" + quiz.getId() + " (\"" + quiz.getTitle() + "\")?",
                "Confirm Rejection",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = quizService.updateQuizStatus(quiz.getId(), QuizStatus.REJECTED);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Quiz has been rejected.", "Quiz Rejected", JOptionPane.INFORMATION_MESSAGE);
                    refreshPendingQuizzes();
                } else {
                    JOptionPane.showMessageDialog(this, "Unable to reject quiz. Please check status.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to reject quiz: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Accessors for automated and Swing testing
    public UserManagementPanel getUserManagementPanel() {
        return userManagementPanel;
    }

    public SystemSettingsPanel getSystemSettingsPanel() {
        return systemSettingsPanel;
    }

    public JTable getPendingQuizzesTable() {
        return pendingQuizzesTable;
    }

    public DefaultTableModel getPendingQuizzesModel() {
        return pendingQuizzesModel;
    }

    public void selectNavigation(String cardName) {
        JButton btn = navButtons.get(cardName);
        if (btn != null) {
            btn.doClick();
        }
    }

    public JLabel getTotalUsersLabel() {
        return totalUsersVal;
    }

    public JLabel getActiveUsersLabel() {
        return activeUsersVal;
    }

    public JLabel getInactiveUsersLabel() {
        return inactiveUsersVal;
    }
}
