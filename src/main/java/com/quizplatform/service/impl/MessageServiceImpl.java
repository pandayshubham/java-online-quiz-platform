package com.quizplatform.service.impl;

import com.quizplatform.dao.MessageDAO;
import com.quizplatform.dao.UserDAO;
import com.quizplatform.dao.impl.MessageDAOImpl;
import com.quizplatform.dao.impl.UserDAOImpl;
import com.quizplatform.model.Message;
import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import com.quizplatform.service.MessageService;
import com.quizplatform.service.NotificationService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.quizplatform.dao.SystemSettingDAO;
import com.quizplatform.dao.impl.SystemSettingDAOImpl;
import com.quizplatform.exception.ValidationException;
import com.quizplatform.util.SessionManager;
import com.quizplatform.util.ValidationUtil;

/**
 * Implementation of MessageService.
 */
public class MessageServiceImpl implements MessageService {

    private final MessageDAO messageDAO;
    private final NotificationService notificationService;
    private final UserDAO userDAO;
    private final SystemSettingDAO systemSettingDAO;

    public MessageServiceImpl() {
        this(new MessageDAOImpl(), new NotificationServiceImpl(), new UserDAOImpl(), new SystemSettingDAOImpl());
    }

    public MessageServiceImpl(MessageDAO messageDAO) {
        this(messageDAO, new NotificationServiceImpl(), new UserDAOImpl(), new SystemSettingDAOImpl());
    }

    public MessageServiceImpl(MessageDAO messageDAO, NotificationService notificationService, UserDAO userDAO) {
        this(messageDAO, notificationService, userDAO, new SystemSettingDAOImpl());
    }

    public MessageServiceImpl(MessageDAO messageDAO, NotificationService notificationService, UserDAO userDAO, SystemSettingDAO systemSettingDAO) {
        this.messageDAO = messageDAO;
        this.notificationService = notificationService;
        this.userDAO = userDAO;
        this.systemSettingDAO = systemSettingDAO;
    }

    public boolean isMessagingEnabled() {
        if (systemSettingDAO == null) return true;
        com.quizplatform.model.SystemSetting s = systemSettingDAO.findByKey("messaging_enabled");
        if (s == null || s.getSettingValue() == null) return true;
        return Boolean.parseBoolean(s.getSettingValue().trim());
    }

    @Override
    public int sendMessage(int senderId, int receiverId, String text) {
        if (!isMessagingEnabled()) {
            throw new ValidationException("Messaging is currently disabled by administrator.");
        }
        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null && currentUser.getRole() != UserRole.ADMIN && currentUser.getId() != senderId) {
                throw new SecurityException("Access Denied: You cannot send messages as another user.");
            }
        }
        if (ValidationUtil.isNullOrBlank(text)) {
            throw new ValidationException("Message text cannot be empty.");
        }
        if (text.length() > 2000) {
            throw new ValidationException("Message cannot exceed 2000 characters.");
        }
        Message message = new Message(senderId, receiverId, text.trim());
        int msgId = messageDAO.createMessage(message);

        // Dispatch notification to receiver
        if (msgId > 0 && notificationService != null && userDAO != null) {
            try {
                User sender = userDAO.findById(senderId);
                String senderName = (sender != null && sender.getName() != null) ? sender.getName() : "User #" + senderId;
                String snippet = text.trim();
                if (snippet.length() > 60) {
                    snippet = snippet.substring(0, 57) + "...";
                }
                notificationService.sendNotification(
                        receiverId,
                        "💬 Message from " + senderName,
                        snippet
                );
            } catch (Exception ignored) {
                // Non-blocking notification dispatch
            }
        }

        return msgId;
    }

    @Override
    public List<Message> getConversation(int user1, int user2) {
        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null && currentUser.getRole() != UserRole.ADMIN) {
                if (currentUser.getId() != user1 && currentUser.getId() != user2) {
                    throw new SecurityException("Access Denied: You cannot view conversations between other users.");
                }
            }
        }
        return messageDAO.findConversation(user1, user2);
    }

    @Override
    public List<Message> getReceivedMessages(int receiverId) {
        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null && currentUser.getRole() != UserRole.ADMIN) {
                if (currentUser.getId() != receiverId) {
                    throw new SecurityException("Access Denied: You can only view your own received messages.");
                }
            }
        }
        return messageDAO.findByReceiverId(receiverId);
    }

    @Override
    public int getUnreadCount(int receiverId) {
        return messageDAO.getUnreadCount(receiverId);
    }

    @Override
    public boolean markAsRead(int messageId) {
        return messageDAO.markAsRead(messageId);
    }

    @Override
    public boolean markConversationAsRead(int receiverId, int senderId) {
        return messageDAO.markConversationAsRead(receiverId, senderId);
    }

    @Override
    public List<User> getAvailableContacts(User currentUser) {
        if (currentUser == null) {
            return Collections.emptyList();
        }

        List<User> contacts = new ArrayList<>();
        Set<Integer> contactIds = new HashSet<>();

        if (currentUser.getRole() == UserRole.PARTICIPANT) {
            // Participants can message Quiz Creators
            List<User> creators = userDAO.findByRole(UserRole.QUIZ_CREATOR);
            for (User u : creators) {
                if (u.isActive() && contactIds.add(u.getId())) {
                    contacts.add(u);
                }
            }
        } else if (currentUser.getRole() == UserRole.QUIZ_CREATOR) {
            // Quiz Creators can message Participants
            List<User> participants = userDAO.findByRole(UserRole.PARTICIPANT);
            for (User u : participants) {
                if (u.isActive() && contactIds.add(u.getId())) {
                    contacts.add(u);
                }
            }
        } else {
            // Admins can contact all users
            List<User> all = userDAO.findAll();
            for (User u : all) {
                if (u.getId() != currentUser.getId() && u.isActive() && contactIds.add(u.getId())) {
                    contacts.add(u);
                }
            }
        }

        // Also add any users who already exchanged messages
        List<Integer> partnerIds = messageDAO.findChatPartnerIds(currentUser.getId());
        for (Integer pid : partnerIds) {
            if (contactIds.add(pid)) {
                User partner = userDAO.findById(pid);
                if (partner != null && partner.isActive()) {
                    contacts.add(partner);
                }
            }
        }

        return contacts;
    }
}
