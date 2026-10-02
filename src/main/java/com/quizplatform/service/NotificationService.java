package com.quizplatform.service;

import com.quizplatform.model.Notification;
import java.util.List;

/**
 * Service interface for creating and managing in-app notifications.
 */
public interface NotificationService {

    /**
     * Creates and dispatches a notification to a specific user.
     *
     * @param userId recipient user ID
     * @param title notification title
     * @param message notification message body
     * @return the created Notification entity
     */
    Notification sendNotification(int userId, String title, String message);

    /**
     * Retrieves all notifications for a user, newest first.
     *
     * @param userId user ID
     * @return list of notifications
     */
    List<Notification> getNotificationsForUser(int userId);

    /**
     * Retrieves all unread notifications for a user.
     *
     * @param userId user ID
     * @return list of unread notifications
     */
    List<Notification> getUnreadNotificationsForUser(int userId);

    /**
     * Gets the count of unread notifications for a user.
     *
     * @param userId user ID
     * @return count of unread notifications
     */
    int getUnreadCount(int userId);

    /**
     * Marks a specific notification as read.
     *
     * @param notificationId notification ID
     * @return true if updated
     */
    boolean markAsRead(int notificationId);

    /**
     * Marks all notifications for a user as read.
     *
     * @param userId user ID
     * @return true if updated
     */
    boolean markAllAsRead(int userId);

    /**
     * Deletes a notification by ID.
     *
     * @param notificationId notification ID
     * @return true if deleted
     */
    boolean deleteNotification(int notificationId);
 
    boolean isNotificationsEnabled();
}
