package com.quizplatform;

import com.quizplatform.dao.AnswerDAO;
import com.quizplatform.dao.MessageDAO;
import com.quizplatform.dao.NotificationDAO;
import com.quizplatform.dao.OptionDAO;
import com.quizplatform.dao.QuestionDAO;
import com.quizplatform.dao.QuizAttemptDAO;
import com.quizplatform.dao.QuizDAO;
import com.quizplatform.dao.QuizReminderDAO;
import com.quizplatform.dao.SystemSettingDAO;
import com.quizplatform.dao.UserDAO;
import com.quizplatform.dao.impl.AnswerDAOImpl;
import com.quizplatform.dao.impl.MessageDAOImpl;
import com.quizplatform.dao.impl.NotificationDAOImpl;
import com.quizplatform.dao.impl.OptionDAOImpl;
import com.quizplatform.dao.impl.QuestionDAOImpl;
import com.quizplatform.dao.impl.QuizAttemptDAOImpl;
import com.quizplatform.dao.impl.QuizDAOImpl;
import com.quizplatform.dao.impl.QuizReminderDAOImpl;
import com.quizplatform.dao.impl.SystemSettingDAOImpl;
import com.quizplatform.dao.impl.UserDAOImpl;
import com.quizplatform.model.Answer;
import com.quizplatform.model.AttemptStatus;
import com.quizplatform.model.Message;
import com.quizplatform.model.Notification;
import com.quizplatform.model.Option;
import com.quizplatform.model.Question;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizAttempt;
import com.quizplatform.model.QuizReminder;
import com.quizplatform.model.QuizStatus;
import com.quizplatform.model.ReminderStatus;
import com.quizplatform.model.SystemSetting;
import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Standalone verification utility for Model and DAO layers.
 * Performs rigorous manual tests on all DAO operations using seed data and
 * temporary records that are cleanly cleaned up after verification.
 */
public class DatabaseDaoVerification {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("    PHASE 3: MODEL & DAO LAYER VERIFICATION");
        System.out.println("==================================================");

        UserDAO userDAO = new UserDAOImpl();
        QuizDAO quizDAO = new QuizDAOImpl();
        QuestionDAO questionDAO = new QuestionDAOImpl();
        OptionDAO optionDAO = new OptionDAOImpl();
        QuizAttemptDAO attemptDAO = new QuizAttemptDAOImpl();
        AnswerDAO answerDAO = new AnswerDAOImpl();
        MessageDAO messageDAO = new MessageDAOImpl();
        NotificationDAO notificationDAO = new NotificationDAOImpl();
        SystemSettingDAO settingDAO = new SystemSettingDAOImpl();
        QuizReminderDAO reminderDAO = new QuizReminderDAOImpl();

        // 1. Verify UserDAO using seed data
        System.out.println("\n[Testing UserDAO]");
        User admin = userDAO.findByEmail("admin@quizplatform.com");
        check("UserDAO.findByEmail(admin)", admin != null && admin.getRole() == UserRole.ADMIN);

        List<User> allUsers = userDAO.findAll();
        check("UserDAO.findAll() size >= 6", allUsers != null && allUsers.size() >= 6);

        List<User> creators = userDAO.findByRole(UserRole.QUIZ_CREATOR);
        check("UserDAO.findByRole(QUIZ_CREATOR) count == 2", creators != null && creators.size() == 2);

        List<User> participants = userDAO.findByRole(UserRole.PARTICIPANT);
        check("UserDAO.findByRole(PARTICIPANT) count == 3", participants != null && participants.size() == 3);

        boolean emailExists = userDAO.existsByEmail("student1@quizplatform.com");
        check("UserDAO.existsByEmail(student1)", emailExists);

        // 2. Verify SystemSettingDAO using seed data
        System.out.println("\n[Testing SystemSettingDAO]");
        List<SystemSetting> settings = settingDAO.findAll();
        check("SystemSettingDAO.findAll() size >= 5", settings != null && settings.size() >= 5);

        SystemSetting durationSetting = settingDAO.findByKey("default_quiz_duration");
        check("SystemSettingDAO.findByKey(default_quiz_duration) == 15", durationSetting != null && "15".equals(durationSetting.getSettingValue()));

        boolean keyExists = settingDAO.existsByKey("leaderboard_enabled");
        check("SystemSettingDAO.existsByKey(leaderboard_enabled)", keyExists);

        // 3. Temporary Entity Testing (Quiz, Question, Option, Attempt, Answer, Message, Notification, Reminder)
        System.out.println("\n[Testing Interactive CRUD & Dynamic Queries with Temporary Data]");

        int creatorId = creators.get(0).getId();
        int participantId = participants.get(0).getId();

        Quiz tempQuiz = new Quiz(creatorId, "Test Java Fundamentals", "Quiz for DAO verification", 20, QuizStatus.APPROVED);
        int tempQuizId = 0;
        int tempQuestionId = 0;
        int tempOption1Id = 0;
        int tempOption2Id = 0;
        int tempAttemptId = 0;
        int tempAnswerId = 0;
        int tempMsgId = 0;
        int tempNotifId = 0;
        int tempReminderId = 0;

        try {
            // QuizDAO
            tempQuizId = quizDAO.createQuiz(tempQuiz);
            final int createdQuizId = tempQuizId;
            check("QuizDAO.createQuiz()", createdQuizId > 0);

            Quiz fetchedQuiz = quizDAO.findById(createdQuizId);
            check("QuizDAO.findById()", fetchedQuiz != null && "Test Java Fundamentals".equals(fetchedQuiz.getTitle()));

            List<Quiz> approvedList = quizDAO.findApprovedQuizzes();
            check("QuizDAO.findApprovedQuizzes() contains test quiz", containsQuiz(approvedList, createdQuizId));

            quizDAO.updateStatus(createdQuizId, QuizStatus.PENDING_APPROVAL);
            List<Quiz> pendingList = quizDAO.findPendingApproval();
            check("QuizDAO.findPendingApproval() after status update", containsQuiz(pendingList, createdQuizId));

            // QuestionDAO
            Question tempQuestion = new Question(createdQuizId, "What is the return type of main() method?", "Standard Java signature", 1);
            tempQuestionId = questionDAO.createQuestion(tempQuestion);
            final int createdQuestionId = tempQuestionId;
            check("QuestionDAO.createQuestion()", createdQuestionId > 0);

            List<Question> quizQuestions = questionDAO.findByQuizId(createdQuizId);
            check("QuestionDAO.findByQuizId()", quizQuestions != null && quizQuestions.size() == 1);

            // OptionDAO
            Option optA = new Option(createdQuestionId, "void", "A", true);
            Option optB = new Option(createdQuestionId, "int", "B", false);
            tempOption1Id = optionDAO.createOption(optA);
            tempOption2Id = optionDAO.createOption(optB);
            check("OptionDAO.createOption()", tempOption1Id > 0 && tempOption2Id > 0);

            List<Option> questionOptions = optionDAO.findByQuestionId(createdQuestionId);
            check("OptionDAO.findByQuestionId() count == 2", questionOptions != null && questionOptions.size() == 2);

            Option correctOpt = optionDAO.findCorrectOptionByQuestionId(createdQuestionId);
            check("OptionDAO.findCorrectOptionByQuestionId() label == 'A'", correctOpt != null && "A".equals(correctOpt.getOptionLabel()));

            // QuizAttemptDAO
            QuizAttempt tempAttempt = new QuizAttempt(createdQuizId, participantId);
            tempAttemptId = attemptDAO.createAttempt(tempAttempt);
            final int createdAttemptId = tempAttemptId;
            check("QuizAttemptDAO.createAttempt()", createdAttemptId > 0);

            List<QuizAttempt> participantAttempts = attemptDAO.findByParticipantId(participantId);
            check("QuizAttemptDAO.findByParticipantId()", containsAttempt(participantAttempts, createdAttemptId));

            tempAttempt.setCompletedAt(LocalDateTime.now());
            tempAttempt.setScore(100);
            tempAttempt.setTotalQuestions(1);
            tempAttempt.setCorrectAnswers(1);
            tempAttempt.setPercentage(100.0);
            tempAttempt.setStatus(AttemptStatus.COMPLETED);
            boolean attemptUpdated = attemptDAO.updateAttempt(tempAttempt);
            check("QuizAttemptDAO.updateAttempt()", attemptUpdated);

            double avgScore = attemptDAO.getAverageScoreByQuizId(createdQuizId);
            check("QuizAttemptDAO.getAverageScoreByQuizId() == 100.0", avgScore == 100.0);

            int highestScore = attemptDAO.getHighestScoreByQuizId(createdQuizId);
            check("QuizAttemptDAO.getHighestScoreByQuizId() == 100", highestScore == 100);

            // AnswerDAO
            Answer tempAnswer = new Answer(createdAttemptId, createdQuestionId, tempOption1Id, true);
            tempAnswerId = answerDAO.createAnswer(tempAnswer);
            final int createdAnswerId = tempAnswerId;
            check("AnswerDAO.createAnswer()", createdAnswerId > 0);

            List<Answer> attemptAnswers = answerDAO.findByAttemptId(createdAttemptId);
            check("AnswerDAO.findByAttemptId() size == 1", attemptAnswers != null && attemptAnswers.size() == 1);

            // MessageDAO
            Message tempMsg = new Message(participantId, creatorId, "Can you provide more questions on OOP?");
            tempMsgId = messageDAO.createMessage(tempMsg);
            final int createdMsgId = tempMsgId;
            check("MessageDAO.createMessage()", createdMsgId > 0);

            List<Message> receivedMsgs = messageDAO.findByReceiverId(creatorId);
            check("MessageDAO.findByReceiverId()", containsMessage(receivedMsgs, createdMsgId));

            List<Message> conv = messageDAO.findConversation(participantId, creatorId);
            check("MessageDAO.findConversation()", conv != null && !conv.isEmpty());

            boolean msgRead = messageDAO.markAsRead(createdMsgId);
            check("MessageDAO.markAsRead()", msgRead);

            // NotificationDAO
            Notification tempNotif = new Notification(participantId, "Welcome!", "Explore available Java quizzes.");
            tempNotifId = notificationDAO.createNotification(tempNotif);
            final int createdNotifId = tempNotifId;
            check("NotificationDAO.createNotification()", createdNotifId > 0);

            List<Notification> unreadNotifs = notificationDAO.findUnreadByUserId(participantId);
            check("NotificationDAO.findUnreadByUserId()", containsNotification(unreadNotifs, createdNotifId));

            boolean notifRead = notificationDAO.markAsRead(createdNotifId);
            check("NotificationDAO.markAsRead()", notifRead);

            // QuizReminderDAO
            QuizReminder tempReminder = new QuizReminder(participantId, createdQuizId, LocalDateTime.now().plusDays(1));
            tempReminderId = reminderDAO.createReminder(tempReminder);
            final int createdReminderId = tempReminderId;
            check("QuizReminderDAO.createReminder()", createdReminderId > 0);

            List<QuizReminder> participantReminders = reminderDAO.findByParticipantId(participantId);
            check("QuizReminderDAO.findByParticipantId()", containsReminder(participantReminders, createdReminderId));

            List<QuizReminder> pendingReminders = reminderDAO.findPendingReminders();
            check("QuizReminderDAO.findPendingReminders()", containsReminder(pendingReminders, createdReminderId));

            boolean reminderUpdated = reminderDAO.updateReminderStatus(createdReminderId, ReminderStatus.SENT);
            check("QuizReminderDAO.updateReminderStatus(SENT)", reminderUpdated);

        } finally {
            // Clean up temporary records in reverse dependency order
            System.out.println("\n[Cleaning Up Temporary Test Records]");
            if (tempReminderId > 0) reminderDAO.deleteReminder(tempReminderId);
            if (tempNotifId > 0) notificationDAO.deleteNotification(tempNotifId);
            if (tempMsgId > 0) messageDAO.deleteMessage(tempMsgId);
            if (tempAnswerId > 0) answerDAO.deleteAnswer(tempAnswerId);
            if (tempAttemptId > 0) {
                try (var conn = com.quizplatform.config.DatabaseConnection.getConnection();
                     var ps = conn.prepareStatement("DELETE FROM quiz_attempts WHERE id = ?")) {
                    ps.setInt(1, tempAttemptId);
                    ps.executeUpdate();
                } catch (Exception ignored) {}
            }
            if (tempOption1Id > 0) optionDAO.deleteOption(tempOption1Id);
            if (tempOption2Id > 0) optionDAO.deleteOption(tempOption2Id);
            if (tempQuestionId > 0) questionDAO.deleteQuestion(tempQuestionId);
            if (tempQuizId > 0) quizDAO.deleteQuiz(tempQuizId);
            System.out.println("[Clean Up Complete - Database Restored to Seed State]");
        }

        System.out.println("\n==================================================");
        System.out.println("VERIFICATION SUMMARY:");
        System.out.println("  Total Passed: " + testsPassed);
        System.out.println("  Total Failed: " + testsFailed);
        if (testsFailed == 0) {
            System.out.println("ALL DAO & MODEL TESTS PASSED SUCCESSFULLY!");
        } else {
            System.err.println("SOME TESTS FAILED!");
            System.exit(1);
        }
        System.out.println("==================================================");
    }

    private static boolean containsQuiz(List<Quiz> list, int id) {
        for (Quiz q : list) {
            if (q.getId() == id) return true;
        }
        return false;
    }

    private static boolean containsAttempt(List<QuizAttempt> list, int id) {
        for (QuizAttempt a : list) {
            if (a.getId() == id) return true;
        }
        return false;
    }

    private static boolean containsMessage(List<Message> list, int id) {
        for (Message m : list) {
            if (m.getId() == id) return true;
        }
        return false;
    }

    private static boolean containsNotification(List<Notification> list, int id) {
        for (Notification n : list) {
            if (n.getId() == id) return true;
        }
        return false;
    }

    private static boolean containsReminder(List<QuizReminder> list, int id) {
        for (QuizReminder r : list) {
            if (r.getId() == id) return true;
        }
        return false;
    }

    private static void check(String testName, boolean condition) {
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + testName);
        } else {
            testsFailed++;
            System.err.println("  [FAIL] " + testName);
        }
    }
}
