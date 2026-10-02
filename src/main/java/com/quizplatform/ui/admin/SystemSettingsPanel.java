package com.quizplatform.ui.admin;

import com.quizplatform.model.SystemSetting;
import com.quizplatform.model.UserRole;
import com.quizplatform.service.SystemSettingsService;
import com.quizplatform.service.impl.SystemSettingsServiceImpl;
import com.quizplatform.util.SessionManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * Professional Admin Panel for managing global system settings and feature toggles.
 * Only accessible to ADMIN role.
 */
public class SystemSettingsPanel extends JPanel {

    private final SystemSettingsService settingsService;

    // Feature Toggle Checkboxes
    private JCheckBox chkLeaderboard;
    private JCheckBox chkReminders;
    private JCheckBox chkQuizAttempts;
    private JCheckBox chkMessaging;
    private JCheckBox chkNotifications;

    // Numerical/Constraint Fields
    private JTextField txtDefaultDuration;
    private JTextField txtMaxDuration;
    private JTextField txtMaxAttempts;

    // Status / Message Label
    private JLabel lblStatus;
    private JButton btnSave;
    private JButton btnRefresh;

    public SystemSettingsPanel() {
        this(new SystemSettingsServiceImpl());
    }

    public SystemSettingsPanel(SystemSettingsService settingsService) {
        this.settingsService = settingsService;
        initUI();
        loadSettings();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Top Bar: Title & Action Buttons
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel titleGroup = new JPanel();
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setOpaque(false);

        JLabel titleLbl = new JLabel("System Settings & Feature Toggles");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(new Color(15, 23, 42));

        JLabel subLbl = new JLabel("Control global platform capabilities, feature availability, and quiz rules in real time.");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(new Color(100, 116, 139));

        titleGroup.add(titleLbl);
        titleGroup.add(Box.createRigidArea(new Dimension(0, 4)));
        titleGroup.add(subLbl);
        topBar.add(titleGroup, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        btnRefresh = new JButton("🔄 Refresh Settings");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setBackground(new Color(241, 245, 249));
        btnRefresh.setForeground(new Color(51, 65, 85));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        btnRefresh.addActionListener(e -> loadSettings());

        btnSave = new JButton("💾 Save Changes");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSave.setBackground(new Color(37, 99, 235));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(29, 78, 216), 1),
                BorderFactory.createEmptyBorder(6, 18, 6, 18)
        ));
        btnSave.addActionListener(e -> handleSaveSettings());

        actions.add(btnRefresh);
        actions.add(btnSave);
        topBar.add(actions, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // 2. Center Content: Settings Cards
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Card 1: Feature Toggles
        contentPanel.add(createFeatureTogglesCard());
        contentPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        // Card 2: Platform Constraints
        contentPanel.add(createConstraintsCard());
        contentPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Status bar
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(new Color(22, 101, 52));
        contentPanel.add(lblStatus);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createFeatureTogglesCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(18, 22, 18, 22)
        ));

        JLabel secHeader = new JLabel("Feature Availability Toggles");
        secHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        secHeader.setForeground(new Color(30, 41, 59));
        card.add(secHeader);

        JLabel secDesc = new JLabel("Enable or disable platform features globally. Historical user data is preserved when features are disabled.");
        secDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        secDesc.setForeground(new Color(100, 116, 139));
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(secDesc);
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        chkLeaderboard = createToggleRow(
                "🏆 Global & Quiz Leaderboards (leaderboard_enabled)",
                "Allow participants and creators to view platform-wide and quiz-specific performance rankings.",
                card
        );

        chkReminders = createToggleRow(
                "⏰ Quiz Reminders & Scheduling (reminders_enabled)",
                "Allow participants to schedule quiz reminders and dispatch automated 30-second due notifications.",
                card
        );

        chkQuizAttempts = createToggleRow(
                "🎯 Quiz Attempts Engine (quiz_attempts_enabled)",
                "Allow participants to start new attempts on approved quizzes. When disabled, ongoing attempts can complete but new ones are blocked.",
                card
        );

        chkMessaging = createToggleRow(
                "💬 Direct Messaging (messaging_enabled)",
                "Allow participants and quiz creators to exchange real-time private messages. When disabled, historical chats remain viewable.",
                card
        );

        chkNotifications = createToggleRow(
                "🔔 Application Notifications (notifications_enabled)",
                "Allow the platform to generate and dispatch system alerts and notifications to user inboxes.",
                card
        );

        return card;
    }

    private JCheckBox createToggleRow(String title, String desc, JPanel parent) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        row.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        JCheckBox chk = new JCheckBox(title);
        chk.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chk.setForeground(new Color(15, 23, 42));
        chk.setOpaque(false);
        chk.setFocusPainted(false);
        chk.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel descLbl = new JLabel("<html><body style='width: 500px; color: #64748B; font-size: 11px;'>" + desc + "</body></html>");

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(chk);
        textPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        textPanel.add(descLbl);

        row.add(textPanel, BorderLayout.CENTER);
        parent.add(row);
        parent.add(Box.createRigidArea(new Dimension(0, 8)));
        return chk;
    }

    private JPanel createConstraintsCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(18, 22, 18, 22)
        ));

        JLabel secHeader = new JLabel("Platform Limits & Configuration");
        secHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        secHeader.setForeground(new Color(30, 41, 59));
        card.add(secHeader);

        JLabel secDesc = new JLabel("Configure default and maximum constraints enforced across quiz creation and examination.");
        secDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        secDesc.setForeground(new Color(100, 116, 139));
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(secDesc);
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 1: Default Quiz Duration
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblDefaultDur = new JLabel("Default Quiz Duration (mins):");
        lblDefaultDur.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDefaultDur.setForeground(new Color(71, 85, 105));
        formGrid.add(lblDefaultDur, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtDefaultDuration = createStyledTextField("15");
        formGrid.add(txtDefaultDuration, gbc);

        // Row 2: Maximum Quiz Duration
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblMaxDur = new JLabel("Maximum Quiz Duration (mins):");
        lblMaxDur.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMaxDur.setForeground(new Color(71, 85, 105));
        formGrid.add(lblMaxDur, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtMaxDuration = createStyledTextField("180");
        formGrid.add(txtMaxDuration, gbc);

        // Row 3: Maximum Attempts
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblMaxAtt = new JLabel("Maximum Attempts Per Quiz:");
        lblMaxAtt.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMaxAtt.setForeground(new Color(71, 85, 105));
        formGrid.add(lblMaxAtt, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtMaxAttempts = createStyledTextField("3");
        formGrid.add(txtMaxAttempts, gbc);

        card.add(formGrid);
        return card;
    }

    private JTextField createStyledTextField(String defaultValue) {
        JTextField tf = new JTextField(defaultValue);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setPreferredSize(new Dimension(160, 32));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return tf;
    }

    public void loadSettings() {
        try {
            chkLeaderboard.setSelected(settingsService.isLeaderboardEnabled());
            chkReminders.setSelected(settingsService.isRemindersEnabled());
            chkQuizAttempts.setSelected(settingsService.isQuizAttemptsEnabled());
            chkMessaging.setSelected(settingsService.isMessagingEnabled());
            chkNotifications.setSelected(settingsService.isNotificationsEnabled());

            txtDefaultDuration.setText(String.valueOf(settingsService.getIntSetting("default_quiz_duration", 15)));
            txtMaxDuration.setText(String.valueOf(settingsService.getIntSetting("maximum_quiz_duration", 180)));
            txtMaxAttempts.setText(String.valueOf(settingsService.getIntSetting("maximum_attempts", 3)));

            lblStatus.setForeground(new Color(22, 101, 52));
            lblStatus.setText("Settings loaded successfully from database.");
        } catch (Exception ex) {
            lblStatus.setForeground(new Color(220, 38, 38));
            lblStatus.setText("Unable to load settings: " + ex.getMessage());
        }
    }

    private void handleSaveSettings() {
        // Enforce admin authorization check
        if (SessionManager.isLoggedIn() && !SessionManager.hasRole(UserRole.ADMIN)) {
            JOptionPane.showMessageDialog(this, "Access Denied: Only administrators can modify system settings.", "Authorization Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate constraint inputs
        int defaultDur;
        int maxDur;
        int maxAtt;
        try {
            defaultDur = Integer.parseInt(txtDefaultDuration.getText().trim());
            maxDur = Integer.parseInt(txtMaxDuration.getText().trim());
            maxAtt = Integer.parseInt(txtMaxAttempts.getText().trim());

            if (defaultDur <= 0 || maxDur <= 0 || maxAtt <= 0) {
                JOptionPane.showMessageDialog(this, "All duration and attempt limits must be positive numbers.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (defaultDur > maxDur) {
                JOptionPane.showMessageDialog(this, "Default duration cannot exceed maximum allowable duration.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid integers for duration and attempt limits.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            settingsService.setLeaderboardEnabled(chkLeaderboard.isSelected());
            settingsService.setRemindersEnabled(chkReminders.isSelected());
            settingsService.setQuizAttemptsEnabled(chkQuizAttempts.isSelected());
            settingsService.setMessagingEnabled(chkMessaging.isSelected());
            settingsService.setNotificationsEnabled(chkNotifications.isSelected());

            settingsService.updateSetting("default_quiz_duration", String.valueOf(defaultDur), "Default quiz duration in minutes");
            settingsService.updateSetting("maximum_quiz_duration", String.valueOf(maxDur), "Maximum allowable quiz duration in minutes");
            settingsService.updateSetting("maximum_attempts", String.valueOf(maxAtt), "Maximum number of attempts allowed per quiz");

            lblStatus.setForeground(new Color(22, 101, 52));
            lblStatus.setText("System settings saved successfully.");
            JOptionPane.showMessageDialog(this, "System settings have been successfully updated.", "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            lblStatus.setForeground(new Color(220, 38, 38));
            lblStatus.setText("Failed to save settings: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Unable to save system settings: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Accessors for testing
    public JCheckBox getChkLeaderboard() {
        return chkLeaderboard;
    }

    public JCheckBox getChkReminders() {
        return chkReminders;
    }

    public JCheckBox getChkQuizAttempts() {
        return chkQuizAttempts;
    }

    public JCheckBox getChkMessaging() {
        return chkMessaging;
    }

    public JCheckBox getChkNotifications() {
        return chkNotifications;
    }

    public JButton getBtnSave() {
        return btnSave;
    }

    public JButton getBtnRefresh() {
        return btnRefresh;
    }
}
