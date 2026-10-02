package com.quizplatform.dao;

import com.quizplatform.model.Message;
import java.util.List;

/**
 * Data Access Object interface for Message entities.
 */
public interface MessageDAO {

    int createMessage(Message message);

    Message findById(int id);

    List<Message> findBySenderId(int senderId);

    List<Message> findByReceiverId(int receiverId);

    List<Message> findConversation(int user1, int user2);

    boolean markAsRead(int messageId);

    int getUnreadCount(int receiverId);

    boolean markConversationAsRead(int receiverId, int senderId);

    List<Integer> findChatPartnerIds(int userId);

    boolean deleteMessage(int id);
}
