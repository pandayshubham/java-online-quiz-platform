package com.quizplatform.service.impl;

import com.quizplatform.dao.QuizDAO;
import com.quizplatform.dao.QuizReminderDAO;
import com.quizplatform.dao.SystemSettingDAO;
import com.quizplatform.dao.impl.QuizDAOImpl;
import com.quizplatform.dao.impl.QuizReminderDAOImpl;
import com.quizplatform.dao.impl.SystemSettingDAOImpl;
import com.quizplatform.exception.ValidationException;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizReminder;
import com.quizplatform.model.ReminderStatus;
import com.quizplatform.model.SystemSetting;
import com.quizplatform.service.NotificationService;
import com.quizplatform.service.QuizReminderService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of QuizReminderService.
 */
public class QuizReminderServiceImpl implements QuizReminderService {

    private static final String SETTING_KEY_REMINDERS = "reminders_enabled";

    private final QuizReminderDAO quizReminderDAO;
    private final QuizDAO quizDAO;
    private final NotificationService notificationService;
    private final SystemSettingDAO systemSettingDAO;

    public QuizReminderServiceImpl() {
        this(new QuizReminderDAOImpl(), new QuizDAOImpl(), new NotificationServiceImpl(), new SystemSettingDAOImpl());
    }

    public QuizReminderServiceImpl(QuizReminderDAO quizReminderDAO, QuizDAO quizDAO,
                                  NotificationService notificationService, SystemSettingDAO systemSettingDAO) {
        this.quizReminderDAO = quizReminderDAO;
        this.quizDAO = quizDAO;
        this.notificationService = notificationService;
        this.systemSettingDAO = systemSettingDAO;
    }

    @Override
    public boolean isRemindersEnabled() {
        SystemSetting setting = systemSettingDAO.findByKey(SETTING_KEY_REMINDERS);
        if (setting == null || setting.getSettingValue() == null) {
            return true;
        }
        return Boolean.parseBoolean(setting.getSettingValue().trim());
    }

    @Override
    public void setRemindersEnabled(boolean enabled) {
        if (com.quizplatform.util.SessionManager.isLoggedIn() && !com.quizplatform.util.SessionManager.hasRole(com.quizplatform.model.UserRole.ADMIN)) {
            throw new SecurityException("Access Denied: Only administrators can modify system settings.");
        }
        SystemSetting setting = systemSettingDAO.findByKey(SETTING_KEY_REMINDERS);
        if (setting == null) {
            setting = new SystemSetting(SETTING_KEY_REMINDERS, String.valueOf(enabled), "Global flag to enable or disable quiz reminders");
        } else {
            setting.setSettingValue(String.valueOf(enabled));
        }
        systemSettingDAO.saveOrUpdate(setting);
    }

    @Override
    public QuizReminder scheduleReminder(int participantId, int quizId, LocalDateTime reminderTime) {
        if (!isRemindersEnabled()) {
            throw new ValidationException("Quiz reminders are currently disabled by administrator.");
        }
        if (reminderTime == null) {
            throw new ValidationException("Reminder time must be specified.");
        }
        if (reminderTime.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Reminder time must be set in the future.");
        }

        Quiz quiz = quizDAO.findById(quizId);
        if (quiz == null) {
            throw new ValidationException("Selected quiz not found.");
        }

        QuizReminder reminder = new QuizReminder(participantId, quizId, reminderTime);
        int generatedId = quizReminderDAO.createReminder(reminder);
        reminder.setId(generatedId);
        return reminder;
    }

    @Override
    public List<QuizReminder> getRemindersForParticipant(int participantId) {
        if (participantId <= 0) {
            return Collections.emptyList();
        }
        return quizReminderDAO.findByParticipantId(participantId);
    }

    @Override
    public boolean cancelReminder(int reminderId, int participantId) {
        QuizReminder reminder = quizReminderDAO.findById(reminderId);
        if (reminder == null) {
            return false;
        }
        if (reminder.getParticipantId() != participantId) {
            throw new ValidationException("Unauthorized: Cannot cancel another participant's reminder.");
        }
        return quizReminderDAO.updateReminderStatus(reminderId, ReminderStatus.CANCELLED);
    }

    @Override
    public int processDueReminders() {
        if (!isRemindersEnabled()) {
            return 0;
        }

        List<QuizReminder> dueList = quizReminderDAO.findDueReminders();
        int count = 0;

        for (QuizReminder reminder : dueList) {
            // Update reminder status to SENT
            boolean updated = quizReminderDAO.updateReminderStatus(reminder.getId(), ReminderStatus.SENT);
            if (updated) {
                Quiz quiz = quizDAO.findById(reminder.getQuizId());
                String quizTitle = (quiz != null && quiz.getTitle() != null) ? quiz.getTitle() : "Upcoming Quiz";

                // Dispatch in-app notification to participant
                notificationService.sendNotification(
                        reminder.getParticipantId(),
                        "⏰ Quiz Reminder: " + quizTitle,
                        "Your scheduled reminder for '" + quizTitle + "' is due now! Good luck on your quiz."
                );
                count++;
            }
        }
        return count;
    }
}
