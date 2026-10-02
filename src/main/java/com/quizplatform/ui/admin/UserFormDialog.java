package com.quizplatform.ui.admin;

import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import com.quizplatform.service.UserService;
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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Modal dialog for creating and editing platform users.
 */
public class UserFormDialog extends JDialog {

    private final UserService userService;
    private final User existingUser;
    private boolean saved = false;

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<UserRole> roleComboBox;
    private JCheckBox activeCheckBox;

    public UserFormDialog(Frame parent, UserService userService, User existingUser) {
        super(parent, existingUser == null ? "Add New User" : "Edit User", true);
        this.userService = userService;
        this.existingUser = existingUser;
        initUI();
    }

    private void initUI() {
        setSize(440, 480);
        setResizable(false);
        setLocationRelativeTo(getOwner());

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(248, 250, 252));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Form Card Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        // Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Full Name *");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(nameLabel, gbc);

        gbc.gridy = 1;
        nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(240, 32));
        formPanel.add(nameField, gbc);

        // Email
        gbc.gridy = 2;
        JLabel emailLabel = new JLabel("Email Address *");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(emailLabel, gbc);

        gbc.gridy = 3;
        emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(240, 32));
        formPanel.add(emailField, gbc);

        // Password
        gbc.gridy = 4;
        String passLabelText = existingUser == null ? "Password *" : "New Password (optional)";
        JLabel passLabel = new JLabel(passLabelText);
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(passLabel, gbc);

        gbc.gridy = 5;
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(240, 32));
        formPanel.add(passwordField, gbc);

        // Role
        gbc.gridy = 6;
        JLabel roleLabel = new JLabel("Role *");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(roleLabel, gbc);

        gbc.gridy = 7;
        roleComboBox = new JComboBox<>(UserRole.values());
        roleComboBox.setPreferredSize(new Dimension(240, 32));
        formPanel.add(roleComboBox, gbc);

        // Active Status
        gbc.gridy = 8;
        activeCheckBox = new JCheckBox("Active Account", true);
        activeCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        activeCheckBox.setOpaque(false);
        formPanel.add(activeCheckBox, gbc);

        rootPanel.add(formPanel, BorderLayout.CENTER);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveButton.setPreferredSize(new Dimension(100, 35));
        saveButton.setBackground(new Color(37, 99, 235));
        saveButton.setForeground(Color.WHITE);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> onSave());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelButton.setPreferredSize(new Dimension(90, 35));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        rootPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Prepopulate if in edit mode
        if (existingUser != null) {
            nameField.setText(existingUser.getName());
            emailField.setText(existingUser.getEmail());
            roleComboBox.setSelectedItem(existingUser.getRole());
            activeCheckBox.setSelected(existingUser.isActive());
        }

        setContentPane(rootPanel);
    }

    private void onSave() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        UserRole role = (UserRole) roleComboBox.getSelectedItem();
        boolean active = activeCheckBox.isSelected();

        // 1. Validation
        if (ValidationUtil.isBlank(name)) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        if (ValidationUtil.isBlank(email)) {
            JOptionPane.showMessageDialog(this, "Email cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return;
        }

        if (existingUser == null && ValidationUtil.isBlank(password)) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty for new users.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocus();
            return;
        }

        if (role == null) {
            JOptionPane.showMessageDialog(this, "Please select a user role.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (existingUser == null) {
                // Check duplicate email
                if (userService.emailExists(email)) {
                    JOptionPane.showMessageDialog(this, "An account with this email already exists.", "Duplicate Email", JOptionPane.ERROR_MESSAGE);
                    emailField.requestFocus();
                    return;
                }

                User newUser = new User(name, email, password, role);
                newUser.setActive(active);
                userService.createUser(newUser);
                saved = true;
                JOptionPane.showMessageDialog(this, "User created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                // If email changed, check duplicate
                if (!email.equalsIgnoreCase(existingUser.getEmail()) && userService.emailExists(email)) {
                    JOptionPane.showMessageDialog(this, "An account with this email already exists.", "Duplicate Email", JOptionPane.ERROR_MESSAGE);
                    emailField.requestFocus();
                    return;
                }

                existingUser.setName(name);
                existingUser.setEmail(email);
                if (!password.isEmpty()) {
                    existingUser.setPassword(password);
                } else {
                    existingUser.setPassword(null); // Keep unchanged
                }
                existingUser.setRole(role);
                existingUser.setActive(active);

                boolean success = userService.updateUser(existingUser);
                if (success) {
                    saved = true;
                    JOptionPane.showMessageDialog(this, "User updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Unable to update user. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to save user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    // Getters for automated testing
    public JTextField getNameField() {
        return nameField;
    }

    public JTextField getEmailField() {
        return emailField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public JComboBox<UserRole> getRoleComboBox() {
        return roleComboBox;
    }

    public JCheckBox getActiveCheckBox() {
        return activeCheckBox;
    }

    public void triggerSave() {
        onSave();
    }
}
