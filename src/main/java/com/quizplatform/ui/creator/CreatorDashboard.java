package com.quizplatform.ui.creator;

import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizStatus;
import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import com.quizplatform.service.QuestionService;
import com.quizplatform.service.QuizAttemptService;
import com.quizplatform.service.QuizService;
import com.quizplatform.service.impl.QuestionServiceImpl;
import com.quizplatform.service.impl.QuizAttemptServiceImpl;
import com.quizplatform.service.impl.QuizServiceImpl;
import com.quizplatform.service.NotificationService;
import com.quizplatform.service.impl.NotificationServiceImpl;
import com.quizplatform.ui.LoginFrame;
import com.quizplatform.ui.common.BaseDashboard;
import com.quizplatform.ui.common.LeaderboardPanel;
import com.quizplatform.ui.common.MessagingPanel;
import com.quizplatform.ui.common.NotificationDialog;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Quiz Creator Dashboard for managing quizzes, authoring questions, inspecting history and viewing participant results.
 * Demonstrates OOP Inheritance by extending BaseDashboard.
 */
public class CreatorDashboard extends BaseDashboard {

    public static final String CARD_DASHBOARD = "DASHBOARD";
    public static final String CARD_MY_QUIZZES = "MY_QUIZZES";
    public static final String CARD_QUESTIONS = "QUESTIONS";
    public static final String CARD_HISTORY = "HISTORY";
    public static final String CARD_RESULTS = "RESULTS";
    public static final String CARD_LEADERBOARD = "LEADERBOARD";
    public static final String CARD_MESSAGING = "MESSAGING";

    private final QuizService quizService;
    private final QuestionService questionService;
    private final QuizAttemptService attemptService;
    private final NotificationService notificationService;

    private CardLayout cardLayout;
    private JPanel cardPanel;

    // Navigation buttons
    private final List<JButton> navButtons = new ArrayList<>();
    private JButton btnNavDashboard;
    private JButton btnNavMyQuizzes;
    private JButton btnNavQuestions;
    private JButton btnNavHistory;
    private JButton btnNavResults;
    private JButton btnNavLeaderboard;
    private JButton btnNavMessaging;
    private JButton btnNotifications;

    // Dashboard Home statistic labels
    private JLabel lblTotalQuizzes;
    private JLabel lblDraftQuizzes;
    private JLabel lblPendingQuizzes;
    private JLabel lblApprovedQuizzes;
    private JLabel lblTotalQuestions;
    private JLabel lblTotalAttempts;

    // Panels
    private MyQuizzesPanel myQuizzesPanel;
    private QuestionManagementPanel questionManagementPanel;
    private QuizHistoryPanel quizHistoryPanel;
    private ParticipantResultsPanel participantResultsPanel;
    private LeaderboardPanel leaderboardPanel;
    private MessagingPanel messagingPanel;

    // Recent quizzes table on dashboard overview
    private DefaultTableModel recentQuizzesModel;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public CreatorDashboard(User user) {
        super(user, "Java Online Quiz Platform - Quiz Creator Console");
        this.quizService = new QuizServiceImpl();
        this.questionService = new QuestionServiceImpl();
        this.attemptService = new QuizAttemptServiceImpl();
        this.notificationService = new NotificationServiceImpl();

        // Authorization check
        if (!SessionManager.isLoggedIn() || !SessionManager.hasRole(UserRole.QUIZ_CREATOR)) {
            JOptionPane.showMessageDialog(null, "Access Denied: Quiz Creator privileges required.", "Authorization Error", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(() -> {
                dispose();
                new LoginFrame().setVisible(true);
            });
            return;
        }

        initUI();
        refreshStatistics();
        updateNotificationBadge();
    }

    private void initUI() {
        setTitle("Java Online Quiz Platform - Quiz Creator Console");
        setSize(1100, 720);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));

        // 1. NORTH: Header
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. WEST: Navigation Sidebar
        mainPanel.add(createSidebarPanel(), BorderLayout.WEST);

        // 3. CENTER: CardLayout Content Area
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(new Color(245, 247, 250));

        // Initialize child panels
        myQuizzesPanel = new MyQuizzesPanel(
                quizService,
                questionService,
                this::refreshAllData,
                this::openQuestionManagementForQuiz
        );

        questionManagementPanel = new QuestionManagementPanel(
                quizService,
                questionService,
                this::refreshAllData
        );

        quizHistoryPanel = new QuizHistoryPanel(quizService, questionService, attemptService);
        participantResultsPanel = new ParticipantResultsPanel(quizService, attemptService);
        leaderboardPanel = new LeaderboardPanel(user);
        messagingPanel = new MessagingPanel(user);

        // Add cards
        cardPanel.add(createDashboardHomePanel(), CARD_DASHBOARD);
        cardPanel.add(myQuizzesPanel, CARD_MY_QUIZZES);
        cardPanel.add(questionManagementPanel, CARD_QUESTIONS);
        cardPanel.add(quizHistoryPanel, CARD_HISTORY);
        cardPanel.add(participantResultsPanel, CARD_RESULTS);
        cardPanel.add(leaderboardPanel, CARD_LEADERBOARD);
        cardPanel.add(messagingPanel, CARD_MESSAGING);

        mainPanel.add(cardPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
        setActiveNavButton(btnNavDashboard);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 23, 42)); // Dark slate
        header.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        // Brand title
        JLabel brandLabel = new JLabel("Java Online Quiz Platform  |  Creator Console");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandLabel.setForeground(Color.WHITE);
        header.add(brandLabel, BorderLayout.WEST);

        // Right side: User name badge & Logout button
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        String creatorName = (user != null && user.getName() != null) ? user.getName() : "Quiz Creator";
        JLabel userLabel = new JLabel("Logged in as: " + creatorName + " (QUIZ_CREATOR)");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLabel.setForeground(new Color(203, 213, 225));

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

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setPreferredSize(new Dimension(85, 32));
        logoutButton.setFocusPainted(false);
        logoutButton.setBackground(new Color(239, 68, 68)); // red
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setBorder(BorderFactory.createEmptyBorder());
        logoutButton.addActionListener(e -> handleLogout());

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
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        JLabel menuTitle = new JLabel("NAVIGATION");
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        menuTitle.setForeground(new Color(148, 163, 184));
        menuTitle.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 0));
        sidebar.add(menuTitle);

        btnNavDashboard = createNavButton("Dashboard / Overview", CARD_DASHBOARD);
        btnNavMyQuizzes = createNavButton("My Quizzes", CARD_MY_QUIZZES);
        btnNavQuestions = createNavButton("Question Management", CARD_QUESTIONS);
        btnNavHistory = createNavButton("Quiz History", CARD_HISTORY);
        btnNavResults = createNavButton("Participant Results", CARD_RESULTS);
        btnNavLeaderboard = createNavButton("Leaderboard 🏆", CARD_LEADERBOARD);
        btnNavMessaging = createNavButton("Messages 💬", CARD_MESSAGING);

        sidebar.add(btnNavDashboard);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(btnNavMyQuizzes);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(btnNavQuestions);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(btnNavHistory);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(btnNavResults);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(btnNavLeaderboard);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(btnNavMessaging);

        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private JButton createNavButton(String title, String cardName) {
        JButton button = new JButton(title);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(new Color(226, 232, 240));
        button.setBackground(new Color(30, 41, 59));
        button.setMaximumSize(new Dimension(200, 40));
        button.setPreferredSize(new Dimension(200, 40));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> {
            cardLayout.show(cardPanel, cardName);
            setActiveNavButton(button);

            if (CARD_MY_QUIZZES.equals(cardName)) {
                myQuizzesPanel.refreshQuizzes();
            } else if (CARD_QUESTIONS.equals(cardName)) {
                questionManagementPanel.reloadQuizzesList();
                questionManagementPanel.loadQuestionsForSelectedQuiz();
            } else if (CARD_HISTORY.equals(cardName)) {
                quizHistoryPanel.refreshHistory();
            } else if (CARD_RESULTS.equals(cardName)) {
                participantResultsPanel.reloadQuizzes();
                participantResultsPanel.refreshResults();
            } else if (CARD_DASHBOARD.equals(cardName)) {
                refreshStatistics();
            } else if (CARD_LEADERBOARD.equals(cardName)) {
                if (leaderboardPanel != null) leaderboardPanel.refreshLeaderboard();
            } else if (CARD_MESSAGING.equals(cardName)) {
                if (messagingPanel != null) messagingPanel.loadContacts();
            }
        });

        navButtons.add(button);
        return button;
    }

    private void setActiveNavButton(JButton activeButton) {
        for (JButton btn : navButtons) {
            if (btn == activeButton) {
                btn.setBackground(new Color(79, 70, 229)); // Purple active
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else {
                btn.setBackground(new Color(30, 41, 59));
                btn.setForeground(new Color(226, 232, 240));
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
        }
    }

    private JPanel createDashboardHomePanel() {
        JPanel homePanel = new JPanel(new BorderLayout(0, 20));
        homePanel.setBackground(new Color(248, 250, 252));
        homePanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Header / Welcome area
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setOpaque(false);

        JLabel welcomeTitle = new JLabel("Welcome back, " + (user != null ? user.getName() : "Creator"));
        welcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeTitle.setForeground(new Color(15, 23, 42));

        JLabel welcomeSub = new JLabel("Author quizzes, curate questions, track submissions, and monitor student metrics.");
        welcomeSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        welcomeSub.setForeground(new Color(100, 116, 139));

        JPanel welcomeText = new JPanel();
        welcomeText.setLayout(new BoxLayout(welcomeText, BoxLayout.Y_AXIS));
        welcomeText.setOpaque(false);
        welcomeText.add(welcomeTitle);
        welcomeText.add(Box.createRigidArea(new Dimension(0, 4)));
        welcomeText.add(welcomeSub);
        welcomePanel.add(welcomeText, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh Overview");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setBackground(new Color(241, 245, 249));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshStatistics());
        welcomePanel.add(refreshBtn, BorderLayout.EAST);

        homePanel.add(welcomePanel, BorderLayout.NORTH);

        // Center: Cards & Quick Actions
        JPanel centerContent = new JPanel(new BorderLayout(0, 20));
        centerContent.setOpaque(false);

        // 6 KPI Metric Cards
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 16, 16));
        statsGrid.setOpaque(false);

        lblTotalQuizzes = new JLabel("0", SwingConstants.CENTER);
        lblDraftQuizzes = new JLabel("0", SwingConstants.CENTER);
        lblPendingQuizzes = new JLabel("0", SwingConstants.CENTER);
        lblApprovedQuizzes = new JLabel("0", SwingConstants.CENTER);
        lblTotalQuestions = new JLabel("0", SwingConstants.CENTER);
        lblTotalAttempts = new JLabel("0", SwingConstants.CENTER);

        statsGrid.add(createStatCard("TOTAL QUIZZES", lblTotalQuizzes, new Color(124, 58, 237)));
        statsGrid.add(createStatCard("DRAFT QUIZZES", lblDraftQuizzes, new Color(100, 116, 139)));
        statsGrid.add(createStatCard("PENDING APPROVAL", lblPendingQuizzes, new Color(217, 119, 6)));
        statsGrid.add(createStatCard("APPROVED QUIZZES", lblApprovedQuizzes, new Color(16, 185, 129)));
        statsGrid.add(createStatCard("TOTAL QUESTIONS", lblTotalQuestions, new Color(37, 99, 235)));
        statsGrid.add(createStatCard("PARTICIPANT ATTEMPTS", lblTotalAttempts, new Color(236, 72, 153)));

        centerContent.add(statsGrid, BorderLayout.NORTH);

        // Quick Actions & Recent Quizzes
        JPanel bottomContainer = new JPanel(new BorderLayout(0, 15));
        bottomContainer.setOpaque(false);

        // Quick Action Bar
        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        quickActions.setOpaque(false);

        JLabel qaLabel = new JLabel("Quick Actions:");
        qaLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        qaLabel.setForeground(new Color(71, 85, 105));
        quickActions.add(qaLabel);

        JButton createQuizBtn = new JButton("+ Create New Quiz");
        createQuizBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        createQuizBtn.setBackground(new Color(124, 58, 237));
        createQuizBtn.setForeground(Color.WHITE);
        createQuizBtn.setFocusPainted(false);
        createQuizBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createQuizBtn.addActionListener(e -> {
            cardLayout.show(cardPanel, CARD_MY_QUIZZES);
            setActiveNavButton(btnNavMyQuizzes);
            myQuizzesPanel.refreshQuizzes();
        });
        quickActions.add(createQuizBtn);

        JButton manageQBtn = new JButton("Manage Questions");
        manageQBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        manageQBtn.setBackground(new Color(241, 245, 249));
        manageQBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        manageQBtn.addActionListener(e -> {
            cardLayout.show(cardPanel, CARD_QUESTIONS);
            setActiveNavButton(btnNavQuestions);
            questionManagementPanel.reloadQuizzesList();
            questionManagementPanel.loadQuestionsForSelectedQuiz();
        });
        quickActions.add(manageQBtn);

        JButton viewResultsBtn = new JButton("View Participant Results");
        viewResultsBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewResultsBtn.setBackground(new Color(241, 245, 249));
        viewResultsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewResultsBtn.addActionListener(e -> {
            cardLayout.show(cardPanel, CARD_RESULTS);
            setActiveNavButton(btnNavResults);
            participantResultsPanel.reloadQuizzes();
            participantResultsPanel.refreshResults();
        });
        quickActions.add(viewResultsBtn);

        bottomContainer.add(quickActions, BorderLayout.NORTH);

        // Recent Quizzes table
        String[] columns = {"ID", "Title", "Duration", "Status", "Created At"};
        recentQuizzesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable recentTable = new JTable(recentQuizzesModel);
        recentTable.setRowHeight(32);
        recentTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        recentTable.setShowHorizontalLines(true);
        recentTable.setShowVerticalLines(false);
        recentTable.setGridColor(new Color(241, 245, 249));

        JTableHeader recentHeader = recentTable.getTableHeader();
        recentHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        recentHeader.setBackground(new Color(248, 250, 252));
        recentHeader.setForeground(new Color(71, 85, 105));

        JScrollPane recentScroll = new JScrollPane(recentTable);
        recentScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                "Recent Quizzes Created",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(71, 85, 105)
        ));
        recentScroll.getViewport().setBackground(Color.WHITE);
        bottomContainer.add(recentScroll, BorderLayout.CENTER);

        centerContent.add(bottomContainer, BorderLayout.CENTER);
        homePanel.add(centerContent, BorderLayout.CENTER);

        return homePanel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240)),
                        BorderFactory.createEmptyBorder(14, 18, 14, 18)
                )
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(new Color(15, 23, 42));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    @Override
    public void refreshStatistics() {
        if (user == null) return;

        try {
            int total = quizService.getQuizCountByCreator(user.getId());
            int draft = quizService.getQuizCountByCreatorAndStatus(user.getId(), QuizStatus.DRAFT);
            int pending = quizService.getQuizCountByCreatorAndStatus(user.getId(), QuizStatus.PENDING_APPROVAL);
            int approved = quizService.getQuizCountByCreatorAndStatus(user.getId(), QuizStatus.APPROVED);
            int questions = quizService.getTotalQuestionsCountByCreator(user.getId());
            int attempts = quizService.getTotalAttemptsCountByCreator(user.getId());

            lblTotalQuizzes.setText(String.valueOf(total));
            lblDraftQuizzes.setText(String.valueOf(draft));
            lblPendingQuizzes.setText(String.valueOf(pending));
            lblApprovedQuizzes.setText(String.valueOf(approved));
            lblTotalQuestions.setText(String.valueOf(questions));
            lblTotalAttempts.setText(String.valueOf(attempts));

            // Reload recent quizzes table
            recentQuizzesModel.setRowCount(0);
            List<Quiz> quizzes = quizService.getQuizzesByCreator(user.getId());
            int limit = Math.min(5, quizzes.size());
            for (int i = 0; i < limit; i++) {
                Quiz q = quizzes.get(i);
                recentQuizzesModel.addRow(new Object[]{
                        "#" + q.getId(),
                        q.getTitle(),
                        q.getDurationMinutes() + " mins",
                        q.getStatus().name(),
                        q.getCreatedAt() != null ? q.getCreatedAt().format(DATE_FORMATTER) : "N/A"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error refreshing creator statistics: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshAllData() {
        refreshStatistics();
        if (myQuizzesPanel != null) myQuizzesPanel.refreshQuizzes();
        if (quizHistoryPanel != null) quizHistoryPanel.refreshHistory();
        if (participantResultsPanel != null) participantResultsPanel.refreshResults();
    }

    public void openQuestionManagementForQuiz(Quiz quiz) {
        cardLayout.show(cardPanel, CARD_QUESTIONS);
        setActiveNavButton(btnNavQuestions);
        questionManagementPanel.setSelectedQuiz(quiz);
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
        performLogout("Are you sure you want to log out of the Quiz Creator Console?");
    }
}
