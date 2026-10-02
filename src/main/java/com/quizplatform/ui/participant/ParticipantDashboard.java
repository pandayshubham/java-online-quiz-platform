package com.quizplatform.ui.participant;

import com.quizplatform.dao.UserDAO;
import com.quizplatform.dao.impl.UserDAOImpl;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizAttempt;
import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import com.quizplatform.model.report.AttemptPerformanceDetail;
import com.quizplatform.model.report.ParticipantPerformanceSummary;
import com.quizplatform.model.report.QuizPerformanceSummary;
import com.quizplatform.service.PerformanceReportService;
import com.quizplatform.service.QuestionService;
import com.quizplatform.service.QuizAttemptService;
import com.quizplatform.service.QuizService;
import com.quizplatform.service.impl.PerformanceReportServiceImpl;
import com.quizplatform.service.impl.QuestionServiceImpl;
import com.quizplatform.service.impl.QuizAttemptServiceImpl;
import com.quizplatform.service.impl.QuizServiceImpl;
import com.quizplatform.service.NotificationService;
import com.quizplatform.service.QuizReminderService;
import com.quizplatform.service.impl.NotificationServiceImpl;
import com.quizplatform.service.impl.QuizReminderServiceImpl;
import com.quizplatform.ui.LoginFrame;
import com.quizplatform.ui.common.BaseDashboard;
import com.quizplatform.ui.common.LeaderboardPanel;
import com.quizplatform.ui.common.MessagingPanel;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Participant Dashboard for browsing available approved quizzes, taking exams,
 * tracking personal attempt history, and reviewing detailed performance reports.
 * Demonstrates OOP Inheritance by extending BaseDashboard.
 */
public class ParticipantDashboard extends BaseDashboard {

    public static final String CARD_AVAILABLE = "AVAILABLE_QUIZZES";
    public static final String CARD_HISTORY = "MY_HISTORY";
    public static final String CARD_PERFORMANCE = "MY_PERFORMANCE";
    public static final String CARD_LEADERBOARD = "LEADERBOARD";
    public static final String CARD_MESSAGING = "MESSAGING";
    public static final String CARD_REMINDERS = "MY_REMINDERS";

    private final QuizService quizService;
    private final QuestionService questionService;
    private final QuizAttemptService attemptService;
    private final PerformanceReportService reportService;
    private final UserDAO userDAO;
    private final NotificationService notificationService;
    private final QuizReminderService reminderService;

    private CardLayout cardLayout;
    private JPanel cardPanel;

    private JButton btnNavAvailable;
    private JButton btnNavHistory;
    private JButton btnNavPerformance;
    private JButton btnNavLeaderboard;
    private JButton btnNavMessaging;
    private JButton btnNavReminders;
    private JButton btnNotifications;
    private final List<JButton> navButtons = new ArrayList<>();

    // Phase 10 Panels
    private LeaderboardPanel leaderboardPanel;
    private MessagingPanel messagingPanel;
    private MyRemindersPanel myRemindersPanel;
    private javax.swing.Timer reminderTimer;

    // Header KPI Metric Labels
    private JLabel lblAvailableQuizzes;
    private JLabel lblTotalAttempts;
    private JLabel lblCompletedAttempts;
    private JLabel lblAvgScore;
    private JLabel lblHighScore;

    // Available Quizzes Table
    private JTable availableTable;
    private DefaultTableModel availableModel;
    private JTextField searchField;
    private List<Quiz> currentApprovedQuizzes = new ArrayList<>();
    private JLabel lblEmptyAvailableQuizzes;

    // Attempt History Table
    private JTable historyTable;
    private DefaultTableModel historyModel;
    private JLabel lblEmptyHistory;
    private List<QuizAttempt> currentAttempts = new ArrayList<>();

    // Performance Section Components
    private JLabel perfTotalAttemptsVal;
    private JLabel perfCompletedVal;
    private JLabel perfTimeExpiredVal;
    private JLabel perfAvgScoreVal;
    private JLabel perfBestScoreVal;
    private JLabel perfAnsweredVal;
    private JLabel perfCorrectVal;
    private JLabel perfIncorrectVal;
    private JLabel perfUnansweredVal;
    private JProgressBar perfAccuracyBar;
    private JLabel perfAccuracyVal;

    // Performance by Quiz Components
    private JComboBox<Quiz> perfQuizCombo;
    private JLabel quizPerfAttemptsVal;
    private JLabel quizPerfBestScoreVal;
    private JLabel quizPerfAvgScoreVal;
    private JLabel quizPerfLatestScoreVal;
    private JLabel quizPerfHighPctVal;
    private JLabel quizPerfLatestStatusVal;

    private final Map<Integer, Quiz> quizCache = new HashMap<>();
    private final Map<Integer, User> creatorCache = new HashMap<>();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ParticipantDashboard(User user) {
        super(user, "Java Online Quiz Platform - Participant Portal");
        this.quizService = new QuizServiceImpl();
        this.questionService = new QuestionServiceImpl();
        this.attemptService = new QuizAttemptServiceImpl();
        this.reportService = new PerformanceReportServiceImpl();
        this.userDAO = new UserDAOImpl();
        this.notificationService = new NotificationServiceImpl();
        this.reminderService = new QuizReminderServiceImpl();

        // Authorization check
        if (!SessionManager.isLoggedIn() || !SessionManager.hasRole(UserRole.PARTICIPANT)) {
            JOptionPane.showMessageDialog(null, "Access Denied: Participant credentials required.", "Authorization Error", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(() -> {
                dispose();
                new LoginFrame().setVisible(true);
            });
            return;
        }

        initUI();
        refreshAllData();
        updateNotificationBadge();
        startReminderPoller();
    }

    private void initUI() {
        setTitle("Java Online Quiz Platform - Participant Portal");
        setSize(1100, 720);
        setMinimumSize(new Dimension(920, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));

        // 1. NORTH: Header
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. WEST: Sidebar Navigation
        mainPanel.add(createSidebarPanel(), BorderLayout.WEST);

        // 3. CENTER: Content Area with KPI Summary & CardLayout Tables
        JPanel centerContainer = new JPanel(new BorderLayout(0, 15));
        centerContainer.setOpaque(false);
        centerContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // KPI Metric Cards
        centerContainer.add(createKpiPanel(), BorderLayout.NORTH);

        // Cards Panel
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        leaderboardPanel = new LeaderboardPanel(user);
        messagingPanel = new MessagingPanel(user);
        myRemindersPanel = new MyRemindersPanel(user);

        cardPanel.add(createAvailableQuizzesPanel(), CARD_AVAILABLE);
        cardPanel.add(createHistoryPanel(), CARD_HISTORY);
        cardPanel.add(createPerformancePanel(), CARD_PERFORMANCE);
        cardPanel.add(leaderboardPanel, CARD_LEADERBOARD);
        cardPanel.add(messagingPanel, CARD_MESSAGING);
        cardPanel.add(myRemindersPanel, CARD_REMINDERS);
        centerContainer.add(cardPanel, BorderLayout.CENTER);

        mainPanel.add(centerContainer, BorderLayout.CENTER);

        setContentPane(mainPanel);
        setActiveNavButton(btnNavAvailable);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 23, 42)); // Dark slate
        header.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel brandLabel = new JLabel("Java Online Quiz Platform  |  Participant Portal");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandLabel.setForeground(Color.WHITE);
        header.add(brandLabel, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        String studentName = (user != null && user.getName() != null) ? user.getName() : "Student";
        JLabel userLabel = new JLabel("Logged in as: " + studentName + " (PARTICIPANT)");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLabel.setForeground(new Color(203, 213, 225));

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setPreferredSize(new Dimension(85, 32));
        logoutButton.setFocusPainted(false);
        logoutButton.setBackground(new Color(239, 68, 68));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setBorder(BorderFactory.createEmptyBorder());
        logoutButton.addActionListener(e -> handleLogout());

        btnNotifications = new JButton("🔔 Notifications");
        btnNotifications.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNotifications.setPreferredSize(new Dimension(145, 32));
        btnNotifications.setFocusPainted(false);
        btnNotifications.setBackground(new Color(30, 41, 59));
        btnNotifications.setForeground(new Color(203, 213, 225));
        btnNotifications.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNotifications.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));
        btnNotifications.addActionListener(e -> {
            new NotificationDialog(this, user, this::updateNotificationBadge).setVisible(true);
        });

        userPanel.add(userLabel);
        userPanel.add(btnNotifications);
        userPanel.add(logoutButton);
        header.add(userPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(30, 41, 59));
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        JLabel menuTitle = new JLabel("MENU");
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        menuTitle.setForeground(new Color(148, 163, 184));
        menuTitle.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 0));
        sidebar.add(menuTitle);

        btnNavAvailable = createNavButton("Available Quizzes", CARD_AVAILABLE);
        btnNavHistory = createNavButton("My History", CARD_HISTORY);
        btnNavPerformance = createNavButton("Performance", CARD_PERFORMANCE);
        btnNavLeaderboard = createNavButton("Leaderboard 🏆", CARD_LEADERBOARD);
        btnNavMessaging = createNavButton("Messages 💬", CARD_MESSAGING);
        btnNavReminders = createNavButton("My Reminders ⏰", CARD_REMINDERS);

        sidebar.add(btnNavAvailable);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnNavHistory);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnNavPerformance);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnNavLeaderboard);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnNavMessaging);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnNavReminders);

        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private JButton createNavButton(String title, String cardName) {
        JButton button = new JButton(title);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(new Color(226, 232, 240));
        button.setBackground(new Color(30, 41, 59));
        button.setMaximumSize(new Dimension(190, 40));
        button.setPreferredSize(new Dimension(190, 40));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> {
            cardLayout.show(cardPanel, cardName);
            setActiveNavButton(button);

            if (CARD_AVAILABLE.equals(cardName)) {
                loadAvailableQuizzes();
            } else if (CARD_HISTORY.equals(cardName)) {
                loadAttemptHistory();
            } else if (CARD_PERFORMANCE.equals(cardName)) {
                loadPerformanceData();
            } else if (CARD_LEADERBOARD.equals(cardName)) {
                if (leaderboardPanel != null) leaderboardPanel.refreshLeaderboard();
            } else if (CARD_MESSAGING.equals(cardName)) {
                if (messagingPanel != null) messagingPanel.loadContacts();
            } else if (CARD_REMINDERS.equals(cardName)) {
                if (myRemindersPanel != null) myRemindersPanel.loadReminders();
            }
        });

        navButtons.add(button);
        return button;
    }

    private void setActiveNavButton(JButton activeButton) {
        for (JButton btn : navButtons) {
            if (btn == activeButton) {
                btn.setBackground(new Color(16, 185, 129)); // Green active
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else {
                btn.setBackground(new Color(30, 41, 59));
                btn.setForeground(new Color(226, 232, 240));
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
        }
    }

    private JPanel createKpiPanel() {
        JPanel kpiPanel = new JPanel(new GridLayout(1, 5, 14, 0));
        kpiPanel.setOpaque(false);

        lblAvailableQuizzes = new JLabel("0", SwingConstants.CENTER);
        lblTotalAttempts = new JLabel("0", SwingConstants.CENTER);
        lblCompletedAttempts = new JLabel("0", SwingConstants.CENTER);
        lblAvgScore = new JLabel("0.0%", SwingConstants.CENTER);
        lblHighScore = new JLabel("0", SwingConstants.CENTER);

        kpiPanel.add(createKpiCard("AVAILABLE QUIZZES", lblAvailableQuizzes, new Color(79, 70, 229)));
        kpiPanel.add(createKpiCard("TOTAL ATTEMPTS", lblTotalAttempts, new Color(37, 99, 235)));
        kpiPanel.add(createKpiCard("COMPLETED", lblCompletedAttempts, new Color(16, 185, 129)));
        kpiPanel.add(createKpiCard("AVERAGE SCORE", lblAvgScore, new Color(217, 119, 6)));
        kpiPanel.add(createKpiCard("BEST SCORE", lblHighScore, new Color(236, 72, 153)));

        return kpiPanel;
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240)),
                        BorderFactory.createEmptyBorder(12, 14, 12, 14)
                )
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(new Color(15, 23, 42));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createAvailableQuizzesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);

        JLabel sectionTitle = new JLabel("Available Approved Quizzes");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionTitle.setForeground(new Color(15, 23, 42));
        topBar.add(sectionTitle, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        controls.add(searchLbl);

        searchField = new JTextField(14);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        controls.add(searchField);

        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchBtn.setBackground(new Color(241, 245, 249));
        searchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchBtn.addActionListener(e -> applyAvailableFilter());
        controls.add(searchBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setBackground(new Color(241, 245, 249));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            loadAvailableQuizzes();
        });
        controls.add(refreshBtn);

        topBar.add(controls, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        // Available Quizzes Table
        String[] columns = {"Quiz ID", "Title", "Duration", "Questions", "Author / Instructor", "Description"};
        availableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        availableTable = new JTable(availableModel);
        availableTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        availableTable.setRowHeight(38);
        availableTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        availableTable.setGridColor(new Color(241, 245, 249));
        availableTable.setShowHorizontalLines(true);
        availableTable.setShowVerticalLines(false);

        JTableHeader header = availableTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        availableTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        availableTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        availableTable.getColumnModel().getColumn(1).setPreferredWidth(230);
        availableTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        availableTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        availableTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        availableTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        availableTable.getColumnModel().getColumn(4).setPreferredWidth(160);
        availableTable.getColumnModel().getColumn(5).setPreferredWidth(260);

        JScrollPane scroll = new JScrollPane(availableTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.add(scroll, BorderLayout.CENTER);

        lblEmptyAvailableQuizzes = new JLabel("No approved quizzes are currently available.", SwingConstants.CENTER);
        lblEmptyAvailableQuizzes.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblEmptyAvailableQuizzes.setForeground(new Color(100, 116, 139));
        lblEmptyAvailableQuizzes.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        lblEmptyAvailableQuizzes.setVisible(false);
        tableContainer.add(lblEmptyAvailableQuizzes, BorderLayout.SOUTH);

        panel.add(tableContainer, BorderLayout.CENTER);

        // Bottom Action Button
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton setReminderBtn = new JButton("⏰ Set Reminder");
        setReminderBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        setReminderBtn.setPreferredSize(new Dimension(150, 40));
        setReminderBtn.setBackground(new Color(59, 130, 246)); // Blue
        setReminderBtn.setForeground(Color.WHITE);
        setReminderBtn.setFocusPainted(false);
        setReminderBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        setReminderBtn.addActionListener(e -> handleSetReminder());
        bottomBar.add(setReminderBtn);

        JButton startQuizBtn = new JButton("Select & Start Quiz →");
        startQuizBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        startQuizBtn.setPreferredSize(new Dimension(190, 40));
        startQuizBtn.setBackground(new Color(16, 185, 129)); // Green
        startQuizBtn.setForeground(Color.WHITE);
        startQuizBtn.setFocusPainted(false);
        startQuizBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startQuizBtn.addActionListener(e -> handleStartQuiz());
        bottomBar.add(startQuizBtn);

        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel sectionTitle = new JLabel("My Quiz Attempt History & Scores");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionTitle.setForeground(new Color(15, 23, 42));
        topBar.add(sectionTitle, BorderLayout.WEST);

        JPanel topControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topControls.setOpaque(false);

        JButton exportCsvBtn = new JButton("Export My History CSV");
        exportCsvBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        exportCsvBtn.setBackground(new Color(241, 245, 249));
        exportCsvBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportCsvBtn.addActionListener(e -> handleExportHistoryCsv());
        topControls.add(exportCsvBtn);

        JButton refreshBtn = new JButton("Refresh History");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setBackground(new Color(241, 245, 249));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadAttemptHistory());
        topControls.add(refreshBtn);

        topBar.add(topControls, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        // Columns: Quiz | Date | Score | Total Questions | Correct | Incorrect | Unanswered | Percentage | Status
        String[] columns = {"Quiz", "Attempt Date", "Score", "Total Questions", "Correct", "Incorrect", "Unanswered", "Percentage", "Status"};
        historyModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        historyTable = new JTable(historyModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setRowHeight(38);
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
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(210);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        historyTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        historyTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(95);
        historyTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(65);
        historyTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(70);
        historyTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(85);
        historyTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(7).setPreferredWidth(85);
        historyTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        historyTable.getColumnModel().getColumn(8).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
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

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.add(scroll, BorderLayout.CENTER);

        lblEmptyHistory = new JLabel("You haven't attempted any quizzes yet.", SwingConstants.CENTER);
        lblEmptyHistory.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblEmptyHistory.setForeground(new Color(100, 116, 139));
        lblEmptyHistory.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        lblEmptyHistory.setVisible(false);
        tableContainer.add(lblEmptyHistory, BorderLayout.SOUTH);

        panel.add(tableContainer, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomBar.setOpaque(false);

        JButton viewPerformanceBtn = new JButton("View Performance");
        viewPerformanceBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        viewPerformanceBtn.setPreferredSize(new Dimension(170, 38));
        viewPerformanceBtn.setBackground(new Color(79, 70, 229));
        viewPerformanceBtn.setForeground(Color.WHITE);
        viewPerformanceBtn.setFocusPainted(false);
        viewPerformanceBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewPerformanceBtn.addActionListener(e -> handleViewPerformance());
        bottomBar.add(viewPerformanceBtn);

        JButton viewPastResultBtn = new JButton("View Answers Summary");
        viewPastResultBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewPastResultBtn.setPreferredSize(new Dimension(180, 38));
        viewPastResultBtn.setBackground(new Color(241, 245, 249));
        viewPastResultBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewPastResultBtn.addActionListener(e -> handleViewPastResult());
        bottomBar.add(viewPastResultBtn);

        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPerformancePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel titleLbl = new JLabel("Performance Summary & Analytics");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(new Color(15, 23, 42));
        topBar.add(titleLbl, BorderLayout.WEST);

        JButton refreshPerfBtn = new JButton("Refresh Analytics");
        refreshPerfBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshPerfBtn.setBackground(new Color(241, 245, 249));
        refreshPerfBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshPerfBtn.addActionListener(e -> loadPerformanceData());
        topBar.add(refreshPerfBtn, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);

        JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setOpaque(false);

        // Row 1: High Level Attempt Metrics
        JPanel row1 = new JPanel(new GridLayout(1, 5, 12, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        perfTotalAttemptsVal = new JLabel("0", SwingConstants.CENTER);
        perfCompletedVal = new JLabel("0", SwingConstants.CENTER);
        perfTimeExpiredVal = new JLabel("0", SwingConstants.CENTER);
        perfAvgScoreVal = new JLabel("0.0", SwingConstants.CENTER);
        perfBestScoreVal = new JLabel("0", SwingConstants.CENTER);

        row1.add(createKpiCard("TOTAL ATTEMPTS", perfTotalAttemptsVal, new Color(79, 70, 229)));
        row1.add(createKpiCard("COMPLETED", perfCompletedVal, new Color(16, 185, 129)));
        row1.add(createKpiCard("TIME EXPIRED", perfTimeExpiredVal, new Color(220, 38, 38)));
        row1.add(createKpiCard("AVERAGE SCORE", perfAvgScoreVal, new Color(37, 99, 235)));
        row1.add(createKpiCard("BEST SCORE", perfBestScoreVal, new Color(236, 72, 153)));

        contentContainer.add(row1);
        contentContainer.add(Box.createRigidArea(new Dimension(0, 14)));

        // Row 2: Question Accuracy and Breakdown
        JPanel row2 = new JPanel(new GridLayout(1, 5, 12, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        perfAnsweredVal = new JLabel("0", SwingConstants.CENTER);
        perfCorrectVal = new JLabel("0", SwingConstants.CENTER);
        perfIncorrectVal = new JLabel("0", SwingConstants.CENTER);
        perfUnansweredVal = new JLabel("0", SwingConstants.CENTER);

        row2.add(createKpiCard("QUESTIONS ANSWERED", perfAnsweredVal, new Color(99, 102, 241)));
        row2.add(createKpiCard("CORRECT ANSWERS", perfCorrectVal, new Color(22, 163, 74)));
        row2.add(createKpiCard("INCORRECT ANSWERS", perfIncorrectVal, new Color(220, 38, 38)));
        row2.add(createKpiCard("UNANSWERED", perfUnansweredVal, new Color(217, 119, 6)));

        // Accuracy visual card
        JPanel accuracyCard = new JPanel(new BorderLayout(0, 4));
        accuracyCard.setBackground(Color.WHITE);
        accuracyCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(16, 185, 129)),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240)),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)
                )
        ));

        JLabel accTitle = new JLabel("ACCURACY");
        accTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        accTitle.setForeground(new Color(100, 116, 139));

        perfAccuracyVal = new JLabel("0.0%", SwingConstants.CENTER);
        perfAccuracyVal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        perfAccuracyVal.setForeground(new Color(15, 23, 42));

        perfAccuracyBar = new JProgressBar(0, 100);
        perfAccuracyBar.setValue(0);
        perfAccuracyBar.setForeground(new Color(16, 185, 129));
        perfAccuracyBar.setBackground(new Color(241, 245, 249));
        perfAccuracyBar.setPreferredSize(new Dimension(100, 6));
        perfAccuracyBar.setBorderPainted(false);

        JPanel accContent = new JPanel(new BorderLayout(0, 4));
        accContent.setOpaque(false);
        accContent.add(perfAccuracyVal, BorderLayout.NORTH);
        accContent.add(perfAccuracyBar, BorderLayout.SOUTH);

        accuracyCard.add(accTitle, BorderLayout.NORTH);
        accuracyCard.add(accContent, BorderLayout.CENTER);
        row2.add(accuracyCard);

        contentContainer.add(row2);
        contentContainer.add(Box.createRigidArea(new Dimension(0, 16)));

        // Row 3: Performance by Quiz Section
        JPanel quizPerfCard = new JPanel(new BorderLayout(0, 12));
        quizPerfCard.setBackground(Color.WHITE);
        quizPerfCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JPanel quizTop = new JPanel(new BorderLayout(10, 0));
        quizTop.setOpaque(false);

        JLabel quizSectionLbl = new JLabel("Performance by Quiz");
        quizSectionLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        quizSectionLbl.setForeground(new Color(15, 23, 42));
        quizTop.add(quizSectionLbl, BorderLayout.WEST);

        JPanel comboWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        comboWrapper.setOpaque(false);

        JLabel selectLbl = new JLabel("Select Quiz:");
        selectLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboWrapper.add(selectLbl);

        perfQuizCombo = new JComboBox<>();
        perfQuizCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        perfQuizCombo.setPreferredSize(new Dimension(280, 32));
        perfQuizCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Quiz q) {
                    setText("#" + q.getId() + " - " + q.getTitle());
                } else if (value == null) {
                    setText("-- Select a Quiz --");
                }
                return this;
            }
        });
        perfQuizCombo.addActionListener(e -> updateQuizPerformanceView());
        comboWrapper.add(perfQuizCombo);

        quizTop.add(comboWrapper, BorderLayout.EAST);
        quizPerfCard.add(quizTop, BorderLayout.NORTH);

        // Metrics grid for selected quiz
        JPanel quizGrid = new JPanel(new GridLayout(1, 6, 10, 0));
        quizGrid.setOpaque(false);

        quizPerfAttemptsVal = new JLabel("0", SwingConstants.CENTER);
        quizPerfBestScoreVal = new JLabel("0", SwingConstants.CENTER);
        quizPerfAvgScoreVal = new JLabel("0.0", SwingConstants.CENTER);
        quizPerfLatestScoreVal = new JLabel("0", SwingConstants.CENTER);
        quizPerfHighPctVal = new JLabel("0.0%", SwingConstants.CENTER);
        quizPerfLatestStatusVal = new JLabel("N/A", SwingConstants.CENTER);

        quizGrid.add(createMiniCard("ATTEMPTS", quizPerfAttemptsVal));
        quizGrid.add(createMiniCard("BEST SCORE", quizPerfBestScoreVal));
        quizGrid.add(createMiniCard("AVERAGE SCORE", quizPerfAvgScoreVal));
        quizGrid.add(createMiniCard("LATEST SCORE", quizPerfLatestScoreVal));
        quizGrid.add(createMiniCard("HIGHEST %", quizPerfHighPctVal));
        quizGrid.add(createMiniCard("LATEST STATUS", quizPerfLatestStatusVal));

        quizPerfCard.add(quizGrid, BorderLayout.CENTER);
        contentContainer.add(quizPerfCard);

        panel.add(contentContainer, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMiniCard(String title, JLabel valLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        titleLbl.setForeground(new Color(100, 116, 139));

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valLabel.setForeground(new Color(15, 23, 42));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);
        return card;
    }

    public void loadAvailableQuizzes() {
        try {
            currentApprovedQuizzes = quizService.getApprovedQuizzes();
            applyAvailableFilter();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading quizzes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyAvailableFilter() {
        String search = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        availableModel.setRowCount(0);

        for (Quiz q : currentApprovedQuizzes) {
            quizCache.put(q.getId(), q);
            User creator = creatorCache.computeIfAbsent(q.getCreatorId(), id -> userDAO.findById(id));
            String creatorName = creator != null ? creator.getName() : "Instructor #" + q.getCreatorId();

            boolean matches = search.isEmpty() ||
                    q.getTitle().toLowerCase().contains(search) ||
                    creatorName.toLowerCase().contains(search) ||
                    String.valueOf(q.getId()).contains(search);

            if (matches) {
                int qCount = questionService.getQuestionCountByQuizId(q.getId());
                availableModel.addRow(new Object[]{
                        "#" + q.getId(),
                        q.getTitle(),
                        q.getDurationMinutes() + " mins",
                        qCount,
                        creatorName,
                        q.getDescription() != null ? q.getDescription() : ""
                });
            }
        }

        if (availableModel.getRowCount() == 0) {
            if (lblEmptyAvailableQuizzes != null) lblEmptyAvailableQuizzes.setVisible(true);
        } else {
            if (lblEmptyAvailableQuizzes != null) lblEmptyAvailableQuizzes.setVisible(false);
        }
    }

    public void loadAttemptHistory() {
        if (user == null) return;
        historyModel.setRowCount(0);

        try {
            currentAttempts = attemptService.getAttemptsByParticipant(user.getId());
            if (currentAttempts == null || currentAttempts.isEmpty()) {
                if (lblEmptyHistory != null) lblEmptyHistory.setVisible(true);
                return;
            }

            if (lblEmptyHistory != null) lblEmptyHistory.setVisible(false);
            for (QuizAttempt a : currentAttempts) {
                Quiz q = quizCache.computeIfAbsent(a.getQuizId(), id -> quizService.getQuizById(id));
                String quizTitle = q != null ? q.getTitle() : ("Quiz #" + a.getQuizId());

                String dateStr = a.getCompletedAt() != null ? a.getCompletedAt().format(DATE_FORMATTER) :
                        (a.getStartedAt() != null ? a.getStartedAt().format(DATE_FORMATTER) : "N/A");

                historyModel.addRow(new Object[]{
                        quizTitle,
                        dateStr,
                        a.getScore(),
                        a.getTotalQuestions(),
                        a.getCorrectAnswers(),
                        a.getIncorrectAnswers(),
                        a.getUnansweredQuestions(),
                        String.format("%.1f%%", a.getPercentage()),
                        a.getStatus().name()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading attempt history: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadPerformanceData() {
        if (user == null) return;

        try {
            ParticipantPerformanceSummary summary = reportService.getParticipantSummary(user.getId());

            perfTotalAttemptsVal.setText(String.valueOf(summary.getTotalAttempts()));
            perfCompletedVal.setText(String.valueOf(summary.getCompletedAttempts()));
            perfTimeExpiredVal.setText(String.valueOf(summary.getTimeExpiredAttempts()));
            perfAvgScoreVal.setText(String.format("%.1f", summary.getAverageScore()));
            perfBestScoreVal.setText(String.valueOf(summary.getBestScore()));

            perfAnsweredVal.setText(String.valueOf(summary.getTotalQuestionsAnswered()));
            perfCorrectVal.setText(String.valueOf(summary.getTotalCorrectAnswers()));
            perfIncorrectVal.setText(String.valueOf(summary.getTotalIncorrectAnswers()));
            perfUnansweredVal.setText(String.valueOf(summary.getTotalUnansweredQuestions()));

            double accuracy = 0.0;
            if (summary.getTotalQuestionsAnswered() > 0) {
                accuracy = (summary.getTotalCorrectAnswers() * 100.0) / summary.getTotalQuestionsAnswered();
            }
            perfAccuracyVal.setText(String.format("%.1f%%", accuracy));
            perfAccuracyBar.setValue((int) Math.round(accuracy));

            // Reload quiz combo for Performance by Quiz
            perfQuizCombo.removeAllItems();
            List<QuizAttempt> attempts = attemptService.getAttemptsByParticipant(user.getId());
            Map<Integer, Quiz> attemptedQuizMap = new HashMap<>();
            for (QuizAttempt a : attempts) {
                Quiz q = quizCache.computeIfAbsent(a.getQuizId(), id -> quizService.getQuizById(id));
                if (q != null) {
                    attemptedQuizMap.put(q.getId(), q);
                }
            }
            for (Quiz q : attemptedQuizMap.values()) {
                perfQuizCombo.addItem(q);
            }

            if (perfQuizCombo.getItemCount() > 0) {
                perfQuizCombo.setSelectedIndex(0);
            } else {
                updateQuizPerformanceView();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading performance summary: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateQuizPerformanceView() {
        Quiz selectedQuiz = (Quiz) perfQuizCombo.getSelectedItem();
        if (selectedQuiz == null) {
            quizPerfAttemptsVal.setText("0");
            quizPerfBestScoreVal.setText("0");
            quizPerfAvgScoreVal.setText("0.0");
            quizPerfLatestScoreVal.setText("0");
            quizPerfHighPctVal.setText("0.0%");
            quizPerfLatestStatusVal.setText("N/A");
            return;
        }

        try {
            QuizPerformanceSummary qs = reportService.getParticipantQuizPerformance(user.getId(), selectedQuiz.getId());
            quizPerfAttemptsVal.setText(String.valueOf(qs.getAttemptsCount()));
            quizPerfBestScoreVal.setText(String.valueOf(qs.getBestScore()));
            quizPerfAvgScoreVal.setText(String.format("%.1f", qs.getAverageScore()));
            quizPerfLatestScoreVal.setText(String.valueOf(qs.getLatestScore()));
            quizPerfHighPctVal.setText(String.format("%.1f%%", qs.getHighestPercentage()));
            quizPerfLatestStatusVal.setText(qs.getLatestStatus());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load performance for selected quiz.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refreshStatistics() {
        refreshAllData();
    }

    public void refreshAllData() {
        if (user == null) return;

        try {
            int availableCount = quizService.getApprovedQuizzes().size();
            int totalAttempts = attemptService.getAttemptsCountByParticipant(user.getId());
            int completed = attemptService.getCompletedAttemptsCountByParticipant(user.getId());
            double avgPercent = attemptService.getAveragePercentageByParticipant(user.getId());
            int highScore = attemptService.getHighestScoreByParticipant(user.getId());

            lblAvailableQuizzes.setText(String.valueOf(availableCount));
            lblTotalAttempts.setText(String.valueOf(totalAttempts));
            lblCompletedAttempts.setText(String.valueOf(completed));
            lblAvgScore.setText(String.format("%.1f%%", avgPercent));
            lblHighScore.setText(String.valueOf(highScore));

            loadAvailableQuizzes();
            loadAttemptHistory();
            loadPerformanceData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error refreshing metrics: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleStartQuiz() {
        int selectedRow = availableTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an approved quiz from the table first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idStr = (String) availableModel.getValueAt(selectedRow, 0);
        int quizId = Integer.parseInt(idStr.replace("#", "").trim());
        Quiz quiz = quizService.getQuizById(quizId);

        if (quiz == null) {
            JOptionPane.showMessageDialog(this, "Quiz not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int qCount = questionService.getQuestionCountByQuizId(quiz.getId());
        if (qCount == 0) {
            JOptionPane.showMessageDialog(this, "This quiz does not have any questions yet. Please try another quiz.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Check for an active IN_PROGRESS attempt (Resume support)
        QuizAttempt activeAttempt = attemptService.getActiveAttempt(quiz.getId(), user.getId());
        if (activeAttempt != null && activeAttempt.getStartedAt() != null) {
            LocalDateTime deadline = activeAttempt.getStartedAt().plusMinutes(quiz.getDurationMinutes());
            if (LocalDateTime.now().isAfter(deadline)) {
                // Deadline has passed: submit as TIME_EXPIRED
                try {
                    attemptService.submitAttempt(activeAttempt.getId(), java.util.Collections.emptyMap(), true);
                    JOptionPane.showMessageDialog(this,
                            "Your previous attempt for this quiz expired while you were away.\nIt has been automatically submitted as TIME_EXPIRED.",
                            "Attempt Expired", JOptionPane.INFORMATION_MESSAGE);
                    refreshAllData();
                    return;
                } catch (Exception ex) {
                    // Handled
                }
            } else {
                long remainingSecs = java.time.Duration.between(LocalDateTime.now(), deadline).getSeconds();
                int choice = JOptionPane.showConfirmDialog(
                        this,
                        String.format("You have an unfinished attempt in progress for this quiz with %02d:%02d remaining.\nWould you like to resume it now?",
                                remainingSecs / 60, remainingSecs % 60),
                        "Resume Quiz Attempt",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (choice == JOptionPane.YES_OPTION) {
                    QuizTakingFrame examFrame = new QuizTakingFrame(
                            quiz,
                            activeAttempt.getId(),
                            (int) remainingSecs,
                            questionService,
                            attemptService,
                            this::refreshAllData
                    );
                    examFrame.setVisible(true);
                    return;
                } else {
                    return;
                }
            }
        }

        // Standard start flow
        QuizInstructionsDialog instructionsDialog = new QuizInstructionsDialog(this, quiz, qCount);
        instructionsDialog.setVisible(true);

        if (instructionsDialog.isConfirmed()) {
            try {
                int attemptId = attemptService.startAttempt(quiz.getId(), user.getId());

                QuizTakingFrame examFrame = new QuizTakingFrame(
                        quiz,
                        attemptId,
                        questionService,
                        attemptService,
                        this::refreshAllData
                );
                examFrame.setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to launch exam: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleViewPerformance() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentAttempts.size()) {
            JOptionPane.showMessageDialog(this, "Please select an attempt from the history table first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        QuizAttempt attempt = currentAttempts.get(selectedRow);
        try {
            AttemptPerformanceDetail detail = reportService.getAttemptPerformance(attempt.getId(), user.getId());
            ParticipantPerformanceDialog dialog = new ParticipantPerformanceDialog(this, detail);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load attempt details: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleViewPastResult() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentAttempts.size()) {
            JOptionPane.showMessageDialog(this, "Please select an attempt from the history table first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        QuizAttempt attempt = currentAttempts.get(selectedRow);
        Quiz quiz = quizCache.computeIfAbsent(attempt.getQuizId(), id -> quizService.getQuizById(id));

        QuizResultDialog resultDialog = new QuizResultDialog(this, attempt, quiz, questionService, attemptService);
        resultDialog.setVisible(true);
    }

    private void handleExportHistoryCsv() {
        if (currentAttempts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No attempt history available to export.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save My History CSV");
        fileChooser.setSelectedFile(new File("My_Quiz_History.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            }

            try {
                Map<Integer, String> titleMap = new HashMap<>();
                for (QuizAttempt a : currentAttempts) {
                    Quiz q = quizCache.computeIfAbsent(a.getQuizId(), id -> quizService.getQuizById(id));
                    if (q != null) {
                        titleMap.put(q.getId(), q.getTitle());
                    }
                }

                CsvExportUtil.exportParticipantHistory(fileToSave, currentAttempts, titleMap);
                JOptionPane.showMessageDialog(this, "Quiz history exported successfully to:\n" + fileToSave.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to export report: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSetReminder() {
        int selectedRow = availableTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a quiz to set a reminder for.", "Select Quiz", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int quizId = (int) availableModel.getValueAt(selectedRow, 0);
        Quiz quiz = quizService.getQuizById(quizId);
        if (quiz != null) {
            new SetReminderDialog(this, user, quiz, () -> {
                if (myRemindersPanel != null) myRemindersPanel.loadReminders();
                updateNotificationBadge();
            }).setVisible(true);
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

    private void startReminderPoller() {
        reminderTimer = new javax.swing.Timer(30000, e -> {
            int dueCount = reminderService.processDueReminders();
            updateNotificationBadge();
            if (dueCount > 0 && myRemindersPanel != null) {
                myRemindersPanel.loadReminders();
            }
        });
        reminderTimer.start();
    }

    private void handleLogout() {
        if (reminderTimer != null) {
            reminderTimer.stop();
        }
        performLogout("Are you sure you want to log out of the Participant Portal?");
    }
}
