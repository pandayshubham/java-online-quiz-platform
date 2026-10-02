package com.quizplatform.ui.admin;

import com.quizplatform.exception.DatabaseException;
import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import com.quizplatform.service.UserService;
import com.quizplatform.util.SessionManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Complete User Management Panel for the Admin Dashboard.
 * Supports searching, role/status filtering, creation, modification,
 * activation/deactivation, and deletion with integrity constraints.
 */
public class UserManagementPanel extends JPanel {

    private final UserService userService;
    private final Runnable onDataChangedCallback;

    private JTextField searchField;
    private JButton searchButton;
    private JButton clearButton;
    private JComboBox<String> roleFilter;
    private JComboBox<String> statusFilter;

    private JTable userTable;
    private DefaultTableModel tableModel;

    private JButton addButton;
    private JButton editButton;
    private JButton activateButton;
    private JButton deactivateButton;
    private JButton deleteButton;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public UserManagementPanel(UserService userService, Runnable onDataChangedCallback) {
        this.userService = userService;
        this.onDataChangedCallback = onDataChangedCallback;
        initUI();
        refreshUserTable();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 15));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. NORTH: Filter & Search Toolbar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        // Search components
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(160, 32));
        searchField.setToolTipText("Search by name or email");

        searchButton = new JButton("Search");
        searchButton.setPreferredSize(new Dimension(85, 32));
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.addActionListener(e -> refreshUserTable());

        clearButton = new JButton("Clear");
        clearButton.setPreferredSize(new Dimension(75, 32));
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.addActionListener(e -> {
            searchField.setText("");
            roleFilter.setSelectedIndex(0);
            statusFilter.setSelectedIndex(0);
            refreshUserTable();
        });

        // Role filter
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roleFilter = new JComboBox<>(new String[]{"All Roles", "ADMIN", "QUIZ_CREATOR", "PARTICIPANT"});
        roleFilter.setPreferredSize(new Dimension(130, 32));
        roleFilter.addActionListener(e -> refreshUserTable());

        // Status filter
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusFilter = new JComboBox<>(new String[]{"All Status", "ACTIVE", "INACTIVE"});
        statusFilter.setPreferredSize(new Dimension(110, 32));
        statusFilter.addActionListener(e -> refreshUserTable());

        filterPanel.add(searchLabel);
        filterPanel.add(searchField);
        filterPanel.add(searchButton);
        filterPanel.add(clearButton);
        filterPanel.add(new JLabel(" | "));
        filterPanel.add(roleLabel);
        filterPanel.add(roleFilter);
        filterPanel.add(statusLabel);
        filterPanel.add(statusFilter);

        add(filterPanel, BorderLayout.NORTH);

        // 2. CENTER: JTable with user records
        String[] columnNames = {"ID", "Name", "Email", "Role", "Status", "Created At"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only table cells
            }
        };

        userTable = new JTable(tableModel);
        userTable.setRowHeight(28);
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setGridColor(new Color(241, 245, 249));

        // Center align ID, Role, Status, and Date columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        userTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        userTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        userTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        userTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        userTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        add(scrollPane, BorderLayout.CENTER);

        // 3. SOUTH: Action Buttons Toolbar
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        addButton = createButton("+ Add User", new Color(37, 99, 235), Color.WHITE);
        addButton.addActionListener(e -> onAddUser());

        editButton = createButton("Edit User", new Color(79, 70, 229), Color.WHITE);
        editButton.addActionListener(e -> onEditUser());

        activateButton = createButton("Activate", new Color(16, 149, 106), Color.WHITE);
        activateButton.addActionListener(e -> onToggleActive(true));

        deactivateButton = createButton("Deactivate", new Color(217, 119, 6), Color.WHITE);
        deactivateButton.addActionListener(e -> onToggleActive(false));

        deleteButton = createButton("Delete User", new Color(220, 38, 38), Color.WHITE);
        deleteButton.addActionListener(e -> onDeleteUser());

        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(activateButton);
        actionPanel.add(deactivateButton);
        actionPanel.add(deleteButton);

        add(actionPanel, BorderLayout.SOUTH);
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(115, 34));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Refreshes the JTable with current search query and filter criteria.
     */
    public void refreshUserTable() {
        if (!checkAdminAuthorization()) {
            return;
        }

        String search = searchField.getText().trim();
        String selectedRole = (String) roleFilter.getSelectedItem();
        String selectedStatus = (String) statusFilter.getSelectedItem();

        UserRole role = null;
        if (selectedRole != null && !selectedRole.equals("All Roles")) {
            role = UserRole.valueOf(selectedRole);
        }

        Boolean active = null;
        if ("ACTIVE".equalsIgnoreCase(selectedStatus)) {
            active = true;
        } else if ("INACTIVE".equalsIgnoreCase(selectedStatus)) {
            active = false;
        }

        try {
            List<User> users = userService.searchAndFilterUsers(search, role, active);
            tableModel.setRowCount(0);

            for (User u : users) {
                String createdStr = u.getCreatedAt() != null ? u.getCreatedAt().format(DATE_FORMATTER) : "N/A";
                tableModel.addRow(new Object[]{
                    u.getId(),
                    u.getName(),
                    u.getEmail(),
                    u.getRole().name(),
                    u.isActive() ? "ACTIVE" : "INACTIVE",
                    createdStr
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load users: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAddUser() {
        if (!checkAdminAuthorization()) return;

        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        UserFormDialog dialog = new UserFormDialog(parent, userService, null);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            refreshUserTable();
            notifyDataChanged();
        }
    }

    private void onEditUser() {
        if (!checkAdminAuthorization()) return;

        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        User user = userService.getUserById(userId);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Selected user could not be found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        UserFormDialog dialog = new UserFormDialog(parent, userService, user);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            refreshUserTable();
            notifyDataChanged();
        }
    }

    private void onToggleActive(boolean targetStatus) {
        if (!checkAdminAuthorization()) return;

        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);

        // Prevent self-deactivation
        User currentAdmin = SessionManager.getCurrentUser();
        if (!targetStatus && currentAdmin != null && currentAdmin.getId() == userId) {
            JOptionPane.showMessageDialog(this, "You cannot deactivate your own account.", "Action Prohibited", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean updated = userService.setUserActiveStatus(userId, targetStatus);
            if (updated) {
                String action = targetStatus ? "activated" : "deactivated";
                JOptionPane.showMessageDialog(this, "User successfully " + action + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshUserTable();
                notifyDataChanged();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update user status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to update status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDeleteUser() {
        if (!checkAdminAuthorization()) return;

        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);

        // Prevent self-deletion
        User currentAdmin = SessionManager.getCurrentUser();
        if (currentAdmin != null && currentAdmin.getId() == userId) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account.", "Action Prohibited", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this user?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = userService.deleteUser(userId);
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "User deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshUserTable();
                    notifyDataChanged();
                } else {
                    JOptionPane.showMessageDialog(this, "Unable to delete user.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (DatabaseException ex) {
                // Friendly error message preserving foreign keys
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete User", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An unexpected error occurred while deleting user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean checkAdminAuthorization() {
        if (!SessionManager.isLoggedIn() || !SessionManager.hasRole(UserRole.ADMIN)) {
            JOptionPane.showMessageDialog(this, "Access Denied: Administrator privileges required.", "Authorization Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void notifyDataChanged() {
        if (onDataChangedCallback != null) {
            onDataChangedCallback.run();
        }
    }

    // Accessors for testing
    public JTable getUserTable() {
        return userTable;
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public JButton getSearchButton() {
        return searchButton;
    }

    public JButton getClearButton() {
        return clearButton;
    }

    public JComboBox<String> getRoleFilter() {
        return roleFilter;
    }

    public JComboBox<String> getStatusFilter() {
        return statusFilter;
    }

    public JButton getAddButton() {
        return addButton;
    }

    public JButton getEditButton() {
        return editButton;
    }

    public JButton getActivateButton() {
        return activateButton;
    }

    public JButton getDeactivateButton() {
        return deactivateButton;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }
}
