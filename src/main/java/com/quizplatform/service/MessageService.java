package com.quizplatform.service;

import com.quizplatform.model.Message;
import java.util.List;

/**
 * Service interface for messaging between participants and quiz creators.
 */
public interface MessageService {

    int sendMessage(int senderId, int receiverId, String text);

    List<Message> getConversation(int user1, int user2);

    List<Message> getReceivedMessages(int receiverId);

    int getUnreadCount(int receiverId);

    boolean markAsRead(int messageId);

    boolean markConversationAsRead(int receiverId, int senderId);

    List<com.quizplatform.model.User> getAvailableContacts(com.quizplatform.model.User currentUser);

    boolean isMessagingEnabled();
}
