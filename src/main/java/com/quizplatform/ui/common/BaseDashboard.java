package com.quizplatform.ui.common;

import com.quizplatform.model.User;
import com.quizplatform.ui.LoginFrame;
import com.quizplatform.util.SessionManager;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Base abstract dashboard window providing genuinely shared state and behavior
 * across role-specific dashboards (AdminDashboard, CreatorDashboard, ParticipantDashboard).
 * 
 * Demonstrates:
 * - Object-Oriented Inheritance (Superclass of all role dashboards)
 * - Polymorphism (Common parent reference and abstract/overridden behavior)
 */
public abstract class BaseDashboard extends JFrame {

    protected final User user;

    public BaseDashboard(User user, String title) {
        super(title);
        this.user = user;
    }

    /**
     * Initializes common frame properties.
     */
    protected void setupBaseFrame(int width, int height, int minWidth, int minHeight) {
        setSize(width, height);
        setMinimumSize(new Dimension(minWidth, minHeight));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Polymorphic method to display the dashboard.
     * Demonstrates runtime polymorphism when invoked via a BaseDashboard reference.
     */
    public void display() {
        setVisible(true);
    }

    /**
     * Returns the currently authenticated user associated with this dashboard.
     */
    public User getUser() {
        return user;
    }

    /**
     * Common logout handling across all dashboards.
     */
    protected void performLogout(String confirmationMessage) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                confirmationMessage,
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }

    /**
     * Abstract method implemented by subclasses to refresh role-specific statistics.
     * Demonstrates polymorphic behavior.
     */
    public abstract void refreshStatistics();

    /**
     * Abstract method implemented by subclasses to update role-specific notification badges.
     * Demonstrates polymorphic behavior.
     */
    public abstract void updateNotificationBadge();
}
