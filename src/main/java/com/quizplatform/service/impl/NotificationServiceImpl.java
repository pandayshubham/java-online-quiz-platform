package com.quizplatform.service.impl;

import com.quizplatform.dao.NotificationDAO;
import com.quizplatform.dao.SystemSettingDAO;
import com.quizplatform.dao.impl.NotificationDAOImpl;
import com.quizplatform.dao.impl.SystemSettingDAOImpl;
import com.quizplatform.model.Notification;
import com.quizplatform.model.SystemSetting;
import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import com.quizplatform.service.NotificationService;
import com.quizplatform.util.SessionManager;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of NotificationService.
 */
public class NotificationServiceImpl implements NotificationService {

    private final NotificationDAO notificationDAO;
    private final SystemSettingDAO systemSettingDAO;

    public NotificationServiceImpl() {
        this(new NotificationDAOImpl(), new SystemSettingDAOImpl());
    }

    public NotificationServiceImpl(NotificationDAO notificationDAO) {
        this(notificationDAO, new SystemSettingDAOImpl());
    }

    public NotificationServiceImpl(NotificationDAO notificationDAO, SystemSettingDAO systemSettingDAO) {
        this.notificationDAO = notificationDAO;
        this.systemSettingDAO = systemSettingDAO;
    }

    public boolean isNotificationsEnabled() {
        if (systemSettingDAO == null) return true;
        SystemSetting s = systemSettingDAO.findByKey("notifications_enabled");
        if (s == null || s.getSettingValue() == null) return true;
        return Boolean.parseBoolean(s.getSettingValue().trim());
    }

    @Override
    public Notification sendNotification(int userId, String title, String message) {
        if (!isNotificationsEnabled()) {
            return null;
        }
        if (title == null || title.trim().isEmpty() || message == null || message.trim().isEmpty()) {
            return null;
        }
        Notification notification = new Notification(userId, title.trim(), message.trim());
        int id = notificationDAO.createNotification(notification);
        notification.setId(id);
        return notification;
    }

    @Override
    public List<Notification> getNotificationsForUser(int userId) {
        if (userId <= 0) {
            return Collections.emptyList();
        }
        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null && currentUser.getRole() != UserRole.ADMIN && currentUser.getId() != userId) {
                throw new SecurityException("Access Denied: You can only view your own notifications.");
            }
        }
        return notificationDAO.findByUserId(userId);
    }

    @Override
    public List<Notification> getUnreadNotificationsForUser(int userId) {
        if (userId <= 0) {
            return Collections.emptyList();
        }
        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null && currentUser.getRole() != UserRole.ADMIN && currentUser.getId() != userId) {
                throw new SecurityException("Access Denied: You can only view your own notifications.");
            }
        }
        return notificationDAO.findUnreadByUserId(userId);
    }

    @Override
    public int getUnreadCount(int userId) {
        if (userId <= 0) {
            return 0;
        }
        return notificationDAO.getUnreadCount(userId);
    }

    @Override
    public boolean markAsRead(int notificationId) {
        if (notificationId <= 0) {
            return false;
        }
        return notificationDAO.markAsRead(notificationId);
    }

    @Override
    public boolean markAllAsRead(int userId) {
        if (userId <= 0) {
            return false;
        }
        return notificationDAO.markAllAsRead(userId);
    }

    @Override
    public boolean deleteNotification(int notificationId) {
        if (notificationId <= 0) {
            return false;
        }
        return notificationDAO.deleteNotification(notificationId);
    }
}
