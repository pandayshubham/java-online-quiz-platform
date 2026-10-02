package com.quizplatform.dao;

import com.quizplatform.model.QuizReminder;
import com.quizplatform.model.ReminderStatus;
import java.util.List;

/**
 * Data Access Object interface for QuizReminder entities.
 */
public interface QuizReminderDAO {

    int createReminder(QuizReminder reminder);

    QuizReminder findById(int id);

    List<QuizReminder> findByParticipantId(int participantId);

    List<QuizReminder> findPendingReminders();

    List<QuizReminder> findDueReminders();

    boolean updateReminderStatus(int id, ReminderStatus status);

    boolean deleteReminder(int id);
}
