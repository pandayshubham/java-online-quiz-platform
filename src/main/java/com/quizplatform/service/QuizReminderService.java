package com.quizplatform.service;

import com.quizplatform.model.QuizReminder;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for scheduling and processing quiz reminders.
 */
public interface QuizReminderService {

    /**
     * Checks if quiz reminders are enabled platform-wide.
     *
     * @return true if enabled, false otherwise
     */
    boolean isRemindersEnabled();

    /**
     * Enables or disables reminders via system settings.
     *
     * @param enabled true to enable, false to disable
     */
    void setRemindersEnabled(boolean enabled);

    /**
     * Schedules a reminder for a participant and quiz.
     *
     * @param participantId ID of the participant
     * @param quizId ID of the quiz
     * @param reminderTime target reminder date/time in the future
     * @return created QuizReminder entity
     */
    QuizReminder scheduleReminder(int participantId, int quizId, LocalDateTime reminderTime);

    /**
     * Retrieves all reminders scheduled by a participant.
     *
     * @param participantId ID of the participant
     * @return list of reminders
     */
    List<QuizReminder> getRemindersForParticipant(int participantId);

    /**
     * Cancels a pending reminder.
     *
     * @param reminderId reminder ID
     * @param participantId participant ID for authorization
     * @return true if cancelled
     */
    boolean cancelReminder(int reminderId, int participantId);

    /**
     * Scans and processes all pending reminders that are currently due.
     * Marks them as SENT and dispatches an in-app notification to each participant.
     *
     * @return number of reminders processed and notifications sent
     */
    int processDueReminders();
}
