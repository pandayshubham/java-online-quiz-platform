package com.quizplatform.ui;

import com.quizplatform.exception.AuthenticationException;
import com.quizplatform.model.User;
import com.quizplatform.service.AuthenticationService;
import com.quizplatform.service.impl.AuthenticationServiceImpl;
import com.quizplatform.ui.admin.AdminDashboard;
import com.quizplatform.ui.creator.CreatorDashboard;
import com.quizplatform.ui.participant.ParticipantDashboard;
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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Login Frame for the Java Online Quiz Platform.
 * Communicates strictly with AuthenticationService to authenticate users
 * and navigate to role-specific dashboards.
 */
public class LoginFrame extends JFrame {

    private final AuthenticationService authService;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JButton loginButton;
    private JButton exitButton;

    public LoginFrame() {
        this(new AuthenticationServiceImpl());
    }

    public LoginFrame(AuthenticationService authService) {
        this.authService = authService;
        initUI();
    }

    private void initUI() {
        setTitle("Java Online Quiz Platform - Login");
        setSize(520, 460);
        setMinimumSize(new Dimension(460, 420));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Root container
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(245, 247, 250));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new GridBagLayout());
        headerPanel.setOpaque(false);

        GridBagConstraints hgbc = new GridBagConstraints();
        hgbc.gridx = 0;
        hgbc.gridy = 0;
        hgbc.insets = new Insets(0, 0, 6, 0);

        JLabel titleLabel = new JLabel("Java Online Quiz Platform", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(30, 41, 59));
        headerPanel.add(titleLabel, hgbc);

        hgbc.gridy = 1;
        hgbc.insets = new Insets(0, 0, 16, 0);
        JLabel subtitleLabel = new JLabel("Sign in to access your portal", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        headerPanel.add(subtitleLabel, hgbc);

        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Card Panel
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        // Email Label & Field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        emailLabel.setForeground(new Color(71, 85, 105));
        formCard.add(emailLabel, gbc);

        gbc.gridy = 1;
        emailField = new JTextField();
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailField.setPreferredSize(new Dimension(280, 36));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        formCard.add(emailField, gbc);

        // Password Label & Field
        gbc.gridy = 2;
        gbc.insets = new Insets(12, 4, 6, 4);
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passwordLabel.setForeground(new Color(71, 85, 105));
        formCard.add(passwordLabel, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(6, 4, 6, 4);
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(280, 36));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        formCard.add(passwordField, gbc);

        // Status / Error message label
        gbc.gridy = 4;
        gbc.insets = new Insets(8, 4, 4, 4);
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(220, 38, 38));
        formCard.add(statusLabel, gbc);

        rootPanel.add(formCard, BorderLayout.CENTER);

        // Bottom Action Buttons Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        actionPanel.setOpaque(false);

        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(130, 40));
        loginButton.setFocusPainted(false);
        loginButton.setBackground(new Color(37, 99, 235));
        loginButton.setForeground(Color.WHITE);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> performLogin());

        JButton resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        resetButton.setPreferredSize(new Dimension(110, 40));
        resetButton.setFocusPainted(false);
        resetButton.setBackground(new Color(241, 245, 249));
        resetButton.setForeground(new Color(71, 85, 105));
        resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetButton.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
        resetButton.addActionListener(e -> resetForm());

        exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exitButton.setPreferredSize(new Dimension(100, 40));
        exitButton.setFocusPainted(false);
        exitButton.setBackground(new Color(148, 163, 184));
        exitButton.setForeground(Color.WHITE);
        exitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitButton.addActionListener(e -> {
            dispose();
            System.exit(0);
        });

        // Trigger login on ENTER key inside email field or password field
        emailField.addActionListener(e -> {
            if (passwordField.getPassword().length > 0) {
                performLogin();
            } else {
                passwordField.requestFocusInWindow();
            }
        });
        passwordField.addActionListener(e -> performLogin());

        actionPanel.add(loginButton);
        actionPanel.add(resetButton);
        actionPanel.add(exitButton);
        rootPanel.add(actionPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    /**
     * Clears all input fields and resets status messages.
     */
    public void resetForm() {
        emailField.setText("");
        passwordField.setText("");
        statusLabel.setText(" ");
        emailField.requestFocusInWindow();
    }

    /**
     * Executes login logic and routes to appropriate dashboard upon success.
     */
    public void performLogin() {
        String email = emailField.getText();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);

        try {
            setButtonsEnabled(false);
            statusLabel.setForeground(new Color(37, 99, 235));
            statusLabel.setText("Authenticating...");

            // Authenticate through the service layer (NO direct DAO or SQL access)
            User user = authService.authenticate(email, password);

            // Establish global in-memory session
            SessionManager.login(user);

            // Close login window
            dispose();

            // Role-based navigation to appropriate dashboard
            // Demonstrates RUNTIME POLYMORPHISM: BaseDashboard reference holding subclass instance
            javax.swing.SwingUtilities.invokeLater(() -> {
                com.quizplatform.ui.common.BaseDashboard dashboard = switch (user.getRole()) {
                    case ADMIN -> new AdminDashboard(user);
                    case QUIZ_CREATOR -> new CreatorDashboard(user);
                    case PARTICIPANT -> new ParticipantDashboard(user);
                };
                dashboard.display(); // Polymorphic method call
            });

        } catch (AuthenticationException ex) {
            setButtonsEnabled(true);
            statusLabel.setForeground(new Color(220, 38, 38));
            statusLabel.setText(ex.getMessage());
        } catch (Exception ex) {
            setButtonsEnabled(true);
            statusLabel.setForeground(new Color(220, 38, 38));
            statusLabel.setText("A system error occurred. Please try again.");
        } finally {
            // Overwrite password memory array
            java.util.Arrays.fill(passwordChars, '\0');
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        if (loginButton != null) loginButton.setEnabled(enabled);
        if (emailField != null) emailField.setEnabled(enabled);
        if (passwordField != null) passwordField.setEnabled(enabled);
    }

    // Accessors for testing
    public JTextField getEmailField() {
        return emailField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }
}
