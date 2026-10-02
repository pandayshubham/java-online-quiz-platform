package com.quizplatform.dao;

import com.quizplatform.model.Notification;
import java.util.List;

/**
 * Data Access Object interface for Notification entities.
 */
public interface NotificationDAO {

    int createNotification(Notification notification);

    Notification findById(int id);

    List<Notification> findByUserId(int userId);

    List<Notification> findUnreadByUserId(int userId);

    boolean markAsRead(int notificationId);

    int getUnreadCount(int userId);

    boolean markAllAsRead(int userId);

    boolean deleteNotification(int id);
}
