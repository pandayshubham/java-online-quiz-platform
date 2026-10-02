package com.quizplatform.ui.common;

import com.quizplatform.model.Notification;
import com.quizplatform.model.User;
import com.quizplatform.service.NotificationService;
import com.quizplatform.service.impl.NotificationServiceImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * Modern notification panel dialog displaying system alerts, reminders,
 * attempt summaries, and messages.
 */
public class NotificationDialog extends JDialog {

    private final User currentUser;
    private final NotificationService notificationService;
    private final Runnable onDismissCallback;

    private JPanel notificationsContainer;
    private JLabel lblTitleBadge;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public NotificationDialog(Frame parent, User currentUser, Runnable onDismissCallback) {
        super(parent, "Notifications & Alerts", true);
        this.currentUser = currentUser;
        this.notificationService = new NotificationServiceImpl();
        this.onDismissCallback = onDismissCallback;

        initUI();
        loadNotifications();
    }

    private void initUI() {
        setSize(480, 560);
        setMinimumSize(new Dimension(420, 480));
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(248, 250, 252));

        // 1. Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 23, 42)); // Dark slate
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        lblTitleBadge = new JLabel("🔔 Notifications");
        lblTitleBadge.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitleBadge.setForeground(Color.WHITE);
        headerPanel.add(lblTitleBadge, BorderLayout.WEST);

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerActions.setOpaque(false);

        JButton btnMarkAll = new JButton("Mark all as read");
        btnMarkAll.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnMarkAll.setForeground(new Color(147, 197, 253));
        btnMarkAll.setBackground(new Color(30, 41, 59));
        btnMarkAll.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        btnMarkAll.setFocusPainted(false);
        btnMarkAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMarkAll.addActionListener(e -> {
            notificationService.markAllAsRead(currentUser.getId());
            loadNotifications();
            if (onDismissCallback != null) onDismissCallback.run();
        });
        headerActions.add(btnMarkAll);

        headerPanel.add(headerActions, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Scrollable notification items container
        notificationsContainer = new JPanel();
        notificationsContainer.setLayout(new BoxLayout(notificationsContainer, BoxLayout.Y_AXIS));
        notificationsContainer.setBackground(new Color(248, 250, 252));
        notificationsContainer.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JScrollPane scrollPane = new JScrollPane(notificationsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(new Color(248, 250, 252));
        add(scrollPane, BorderLayout.CENTER);

        // 3. Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClose.setPreferredSize(new Dimension(90, 32));
        btnClose.setBackground(new Color(241, 245, 249));
        btnClose.setForeground(new Color(51, 65, 85));
        btnClose.setFocusPainted(false);
        btnClose.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> {
            dispose();
            if (onDismissCallback != null) onDismissCallback.run();
        });
        footer.add(btnClose);

        add(footer, BorderLayout.SOUTH);
    }

    private void loadNotifications() {
        notificationsContainer.removeAll();

        List<Notification> list = notificationService.getNotificationsForUser(currentUser.getId());
        int unreadCount = notificationService.getUnreadCount(currentUser.getId());
        lblTitleBadge.setText("🔔 Notifications (" + unreadCount + " unread)");

        if (list.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
            emptyPanel.setOpaque(false);
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(60, 20, 60, 20));

            JLabel lblEmpty = new JLabel("No notifications yet");
            lblEmpty.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblEmpty.setForeground(new Color(100, 116, 139));
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblSub = new JLabel("You're all caught up! System alerts will appear here.");
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblSub.setForeground(new Color(148, 163, 184));
            lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

            emptyPanel.add(lblEmpty);
            emptyPanel.add(Box.createRigidArea(new Dimension(0, 6)));
            emptyPanel.add(lblSub);
            notificationsContainer.add(emptyPanel);
        } else {
            for (Notification n : list) {
                notificationsContainer.add(createNotificationCard(n));
                notificationsContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        notificationsContainer.revalidate();
        notificationsContainer.repaint();
    }

    private JPanel createNotificationCard(Notification notification) {
        JPanel card = new JPanel(new BorderLayout(10, 6));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setPreferredSize(new Dimension(420, 80));

        boolean unread = !notification.isRead();
        card.setBackground(unread ? new Color(240, 249, 255) : Color.WHITE);
        Color borderColor = unread ? new Color(59, 130, 246) : new Color(226, 232, 240);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, unread ? 4 : 1, 1, 1, borderColor),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        // Center content: Title & Body
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel(notification.getTitle());
        lblTitle.setFont(new Font("Segoe UI", unread ? Font.BOLD : Font.PLAIN, 13));
        lblTitle.setForeground(unread ? new Color(30, 58, 138) : new Color(30, 41, 59));
        titleRow.add(lblTitle, BorderLayout.WEST);

        String timeStr = notification.getCreatedAt() != null ? notification.getCreatedAt().format(DATE_FORMATTER) : "";
        JLabel lblTime = new JLabel(timeStr);
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTime.setForeground(new Color(148, 163, 184));
        titleRow.add(lblTime, BorderLayout.EAST);
        textPanel.add(titleRow);

        textPanel.add(Box.createRigidArea(new Dimension(0, 4)));

        JLabel lblMessage = new JLabel("<html><body style='width: 320px;'>" + notification.getMessage() + "</body></html>");
        lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMessage.setForeground(new Color(71, 85, 105));
        textPanel.add(lblMessage);

        card.add(textPanel, BorderLayout.CENTER);

        // Right action: Mark read / Delete
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actionPanel.setOpaque(false);

        if (unread) {
            JButton btnRead = new JButton("✓");
            btnRead.setToolTipText("Mark as read");
            btnRead.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btnRead.setPreferredSize(new Dimension(28, 24));
            btnRead.setBackground(new Color(219, 234, 254));
            btnRead.setForeground(new Color(30, 64, 175));
            btnRead.setFocusPainted(false);
            btnRead.setBorder(BorderFactory.createEmptyBorder());
            btnRead.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRead.addActionListener(e -> {
                notificationService.markAsRead(notification.getId());
                loadNotifications();
                if (onDismissCallback != null) onDismissCallback.run();
            });
            actionPanel.add(btnRead);
        }

        JButton btnDelete = new JButton("✕");
        btnDelete.setToolTipText("Delete notification");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnDelete.setPreferredSize(new Dimension(28, 24));
        btnDelete.setBackground(new Color(241, 245, 249));
        btnDelete.setForeground(new Color(148, 163, 184));
        btnDelete.setFocusPainted(false);
        btnDelete.setBorder(BorderFactory.createEmptyBorder());
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(e -> {
            notificationService.deleteNotification(notification.getId());
            loadNotifications();
            if (onDismissCallback != null) onDismissCallback.run();
        });
        actionPanel.add(btnDelete);

        card.add(actionPanel, BorderLayout.EAST);

        return card;
    }
}
