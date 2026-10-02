package com.quizplatform;

import com.quizplatform.config.DatabaseConnection;
import com.quizplatform.dao.LeaderboardDAO;
import com.quizplatform.dao.MessageDAO;
import com.quizplatform.dao.NotificationDAO;
import com.quizplatform.dao.QuizDAO;
import com.quizplatform.dao.QuizReminderDAO;
import com.quizplatform.dao.UserDAO;
import com.quizplatform.dao.impl.LeaderboardDAOImpl;
import com.quizplatform.dao.impl.MessageDAOImpl;
import com.quizplatform.dao.impl.NotificationDAOImpl;
import com.quizplatform.dao.impl.QuizDAOImpl;
import com.quizplatform.dao.impl.QuizReminderDAOImpl;
import com.quizplatform.dao.impl.UserDAOImpl;
import com.quizplatform.model.LeaderboardEntry;
import com.quizplatform.model.Message;
import com.quizplatform.model.Notification;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizReminder;
import com.quizplatform.model.ReminderStatus;
import com.quizplatform.model.User;
import com.quizplatform.service.AuthenticationService;
import com.quizplatform.service.LeaderboardService;
import com.quizplatform.service.MessageService;
import com.quizplatform.service.NotificationService;
import com.quizplatform.service.QuizAttemptService;
import com.quizplatform.service.QuizReminderService;
import com.quizplatform.service.QuizService;
import com.quizplatform.service.impl.AuthenticationServiceImpl;
import com.quizplatform.service.impl.LeaderboardServiceImpl;
import com.quizplatform.service.impl.MessageServiceImpl;
import com.quizplatform.service.impl.NotificationServiceImpl;
import com.quizplatform.service.impl.QuizAttemptServiceImpl;
import com.quizplatform.service.impl.QuizReminderServiceImpl;
import com.quizplatform.service.impl.QuizServiceImpl;
import com.quizplatform.ui.admin.AdminDashboard;
import com.quizplatform.ui.common.LeaderboardPanel;
import com.quizplatform.ui.common.MessagingPanel;
import com.quizplatform.ui.common.NotificationDialog;
import com.quizplatform.ui.creator.CreatorDashboard;
import com.quizplatform.ui.participant.MyRemindersPanel;
import com.quizplatform.ui.participant.ParticipantDashboard;
import com.quizplatform.util.SessionManager;
import java.awt.GraphicsEnvironment;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * Phase 10 Verification Suite: Leaderboard, Messaging, Notifications, and Quiz Reminders.
 */
public class Phase10VerificationTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   PHASE 10: FULL SYSTEM VERIFICATION SUITE       ");
        System.out.println("==================================================\n");

        AuthenticationService authService = new AuthenticationServiceImpl();
        User admin = authService.authenticate("admin@quizplatform.com", "admin123");
        User creator1 = authService.authenticate("creator1@quizplatform.com", "creator123");
        User student1 = authService.authenticate("student1@quizplatform.com", "student123");
        User student2 = authService.authenticate("student2@quizplatform.com", "student123");

        LeaderboardService leaderboardService = new LeaderboardServiceImpl();
        MessageService messageService = new MessageServiceImpl();
        NotificationService notificationService = new NotificationServiceImpl();
        QuizReminderService reminderService = new QuizReminderServiceImpl();
        QuizService quizService = new QuizServiceImpl();
        QuizAttemptService attemptService = new QuizAttemptServiceImpl();

        // ---------------------------------------------------------------------
        // 1. LEADERBOARD TESTS
        // ---------------------------------------------------------------------
        System.out.println("--- [1. 🏆 LEADERBOARD TESTS] ---");

        // Ensure leaderboard enabled
        leaderboardService.setLeaderboardEnabled(true);
        assertTrue("Leaderboard is enabled via system settings", leaderboardService.isLeaderboardEnabled());

        List<Quiz> approvedQuizzes = quizService.getApprovedQuizzes();
        assertTrue("At least one approved quiz exists for testing", !approvedQuizzes.isEmpty());
        int testQuizId = approvedQuizzes.get(0).getId();

        // Check quiz leaderboard
        List<LeaderboardEntry> quizRanking = leaderboardService.getQuizLeaderboard(testQuizId);
        assertNotNull("Quiz leaderboard list is not null", quizRanking);
        if (!quizRanking.isEmpty()) {
            LeaderboardEntry topEntry = quizRanking.get(0);
            assertEquals("First entry has rank 1", 1, topEntry.getRank());
            assertTrue("Top score is non-negative", topEntry.getScore() >= 0);
            assertTrue("Percentage is between 0 and 100", topEntry.getPercentage() >= 0 && topEntry.getPercentage() <= 100);
            assertNotNull("Participant name is populated", topEntry.getParticipantName());
        }

        // Check global platform leaderboard
        List<LeaderboardEntry> globalRanking = leaderboardService.getGlobalLeaderboard();
        assertNotNull("Global leaderboard list is not null", globalRanking);
        if (!globalRanking.isEmpty()) {
            LeaderboardEntry topGlobal = globalRanking.get(0);
            assertEquals("First global entry has rank 1", 1, topGlobal.getRank());
            assertTrue("Global quizzes completed is positive", topGlobal.getQuizzesCompleted() >= 1);
            assertTrue("Global total score is positive", topGlobal.getTotalScore() >= 0);
        }

        // Test leaderboard disabling
        leaderboardService.setLeaderboardEnabled(false);
        assertTrue("Leaderboard is now disabled", !leaderboardService.isLeaderboardEnabled());
        List<LeaderboardEntry> disabledQuizRank = leaderboardService.getQuizLeaderboard(testQuizId);
        assertTrue("Disabled leaderboard returns empty list for quiz", disabledQuizRank.isEmpty());
        List<LeaderboardEntry> disabledGlobalRank = leaderboardService.getGlobalLeaderboard();
        assertTrue("Disabled leaderboard returns empty list for global", disabledGlobalRank.isEmpty());

        // Restore enabled state
        leaderboardService.setLeaderboardEnabled(true);
        assertTrue("Leaderboard re-enabled successfully", leaderboardService.isLeaderboardEnabled());

        // ---------------------------------------------------------------------
        // 2. MESSAGING TESTS
        // ---------------------------------------------------------------------
        System.out.println("\n--- [2. 💬 MESSAGING TESTS] ---");

        // Clear any previous unread messages between student1 and creator1 from prior test runs
        messageService.markConversationAsRead(creator1.getId(), student1.getId());

        int initialUnreadCreator = messageService.getUnreadCount(creator1.getId());

        // Send message from student1 to creator1
        String testMessageText = "Hello Professor, I had a question regarding OOP Encapsulation (Test: " + System.currentTimeMillis() + ")";
        int msgId = messageService.sendMessage(student1.getId(), creator1.getId(), testMessageText);
        assertTrue("Message sent successfully with positive ID", msgId > 0);

        int updatedUnreadCreator = messageService.getUnreadCount(creator1.getId());
        assertEquals("Creator unread messages count incremented by 1", initialUnreadCreator + 1, updatedUnreadCreator);

        // Retrieve conversation
        List<Message> conversation = messageService.getConversation(student1.getId(), creator1.getId());
        assertTrue("Conversation between student1 and creator1 is not empty", !conversation.isEmpty());
        Message lastMsg = conversation.get(conversation.size() - 1);
        assertEquals("Last message text matches sent text", testMessageText, lastMsg.getMessageText());
        assertEquals("Last message sender matches student1", student1.getId(), lastMsg.getSenderId());
        assertEquals("Last message receiver matches creator1", creator1.getId(), lastMsg.getReceiverId());

        // Mark conversation as read
        boolean markedRead = messageService.markConversationAsRead(creator1.getId(), student1.getId());
        assertTrue("Conversation marked as read returned true", markedRead);
        int postReadCount = messageService.getUnreadCount(creator1.getId());
        assertEquals("Creator unread count decreased back after reading conversation", initialUnreadCreator, postReadCount);

        // Creator replies to student1
        String replyText = "Hello Charlie, encapsulation protects internal representation by using getters/setters.";
        int replyId = messageService.sendMessage(creator1.getId(), student1.getId(), replyText);
        assertTrue("Creator reply sent successfully", replyId > 0);
        assertTrue("Student unread count is positive", messageService.getUnreadCount(student1.getId()) > 0);
        messageService.markConversationAsRead(student1.getId(), creator1.getId());

        // Test contact retrieval
        List<User> studentContacts = messageService.getAvailableContacts(student1);
        assertNotNull("Student contacts list is not null", studentContacts);
        boolean containsCreator = studentContacts.stream().anyMatch(u -> u.getId() == creator1.getId());
        assertTrue("Student contacts include Quiz Creator", containsCreator);

        List<User> creatorContacts = messageService.getAvailableContacts(creator1);
        assertNotNull("Creator contacts list is not null", creatorContacts);
        boolean containsStudent = creatorContacts.stream().anyMatch(u -> u.getId() == student1.getId());
        assertTrue("Creator contacts include Student who messaged them", containsStudent);

        // ---------------------------------------------------------------------
        // 3. NOTIFICATIONS TESTS
        // ---------------------------------------------------------------------
        System.out.println("\n--- [3. 🔔 NOTIFICATIONS TESTS] ---");

        int initialNotifCount = notificationService.getUnreadCount(student2.getId());

        // Send direct notification
        String notifTitle = "Test Alert";
        String notifBody = "This is a verification test notification: " + System.currentTimeMillis();
        Notification createdNotif = notificationService.sendNotification(student2.getId(), notifTitle, notifBody);
        assertNotNull("Notification created and returned", createdNotif);
        assertTrue("Notification assigned positive ID", createdNotif.getId() > 0);

        int updatedNotifCount = notificationService.getUnreadCount(student2.getId());
        assertEquals("Unread notifications count incremented", initialNotifCount + 1, updatedNotifCount);

        // List notifications
        List<Notification> userNotifs = notificationService.getNotificationsForUser(student2.getId());
        assertTrue("User notification list contains new notification", userNotifs.stream().anyMatch(n -> n.getId() == createdNotif.getId()));

        // Mark as read
        boolean readSuccess = notificationService.markAsRead(createdNotif.getId());
        assertTrue("Notification marked as read", readSuccess);
        assertEquals("Unread count decremented after single read", initialNotifCount, notificationService.getUnreadCount(student2.getId()));

        // Mark all as read
        notificationService.sendNotification(student2.getId(), "Alert 2", "Body 2");
        notificationService.sendNotification(student2.getId(), "Alert 3", "Body 3");
        assertTrue("Unread count is at least 2", notificationService.getUnreadCount(student2.getId()) >= 2);
        notificationService.markAllAsRead(student2.getId());
        assertEquals("All notifications marked as read; unread count is 0", 0, notificationService.getUnreadCount(student2.getId()));

        // Event-triggered notification on quiz status update
        Quiz targetQuiz = quizService.getQuizById(testQuizId);
        int targetCreatorId = (targetQuiz != null) ? targetQuiz.getCreatorId() : creator1.getId();
        quizService.updateQuizStatus(testQuizId, com.quizplatform.model.QuizStatus.APPROVED);
        List<Notification> creatorNotifs = notificationService.getNotificationsForUser(targetCreatorId);
        assertTrue("Creator received notification for quiz status update",
                creatorNotifs.stream().anyMatch(n -> n.getTitle().contains("Quiz") || n.getTitle().contains("Approved") || n.getTitle().contains("APPROVED")));

        // ---------------------------------------------------------------------
        // 4. ⏰ QUIZ REMINDERS TESTS
        // ---------------------------------------------------------------------
        System.out.println("\n--- [4. ⏰ QUIZ REMINDERS TESTS] ---");

        // Schedule future reminder
        LocalDateTime futureTime = LocalDateTime.now().plusHours(2);
        QuizReminder reminder = reminderService.scheduleReminder(student1.getId(), testQuizId, futureTime);
        assertNotNull("Reminder scheduled successfully", reminder);
        assertTrue("Reminder ID is positive", reminder.getId() > 0);
        assertEquals("Reminder status is PENDING", ReminderStatus.PENDING, reminder.getReminderStatus());

        // List reminders for participant
        List<QuizReminder> studentReminders = reminderService.getRemindersForParticipant(student1.getId());
        assertTrue("Student reminders list contains newly created reminder",
                studentReminders.stream().anyMatch(r -> r.getId() == reminder.getId()));

        // Test validation: Past reminder throws exception
        boolean pastExceptionCaught = false;
        try {
            reminderService.scheduleReminder(student1.getId(), testQuizId, LocalDateTime.now().minusHours(1));
        } catch (Exception ex) {
            pastExceptionCaught = true;
        }
        assertTrue("ValidationException thrown when scheduling past reminder", pastExceptionCaught);

        // Cancel reminder
        boolean cancelSuccess = reminderService.cancelReminder(reminder.getId(), student1.getId());
        assertTrue("Reminder cancelled successfully", cancelSuccess);
        QuizReminder cancelledReminder = new QuizReminderDAOImpl().findById(reminder.getId());
        assertEquals("Cancelled reminder has status CANCELLED", ReminderStatus.CANCELLED, cancelledReminder.getReminderStatus());

        // Test due reminder processing:
        // Directly insert a due reminder (reminder_time in past) to verify processDueReminders()
        QuizReminderDAO reminderDAO = new QuizReminderDAOImpl();
        QuizReminder dueReminder = new QuizReminder(student1.getId(), testQuizId, LocalDateTime.now().minusMinutes(5));
        int dueId = reminderDAO.createReminder(dueReminder);
        assertTrue("Due reminder created with past timestamp", dueId > 0);

        int processed = reminderService.processDueReminders();
        assertTrue("At least 1 due reminder processed", processed >= 1);

        QuizReminder processedReminder = reminderDAO.findById(dueId);
        assertEquals("Processed reminder status updated to SENT", ReminderStatus.SENT, processedReminder.getReminderStatus());

        // Verify notification dispatched to participant
        List<Notification> studentNotifs = notificationService.getNotificationsForUser(student1.getId());
        boolean hasReminderNotif = studentNotifs.stream().anyMatch(n -> n.getTitle().contains("Reminder"));
        assertTrue("Participant received in-app notification when reminder was due", hasReminderNotif);

        // ---------------------------------------------------------------------
        // 5. 🖥️ UI COMPONENTS & WORKFLOW TESTS
        // ---------------------------------------------------------------------
        System.out.println("\n--- [5. 🖥️ UI COMPONENTS & DASHBOARDS TESTS] ---");

        if (!GraphicsEnvironment.isHeadless()) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    // Test LeaderboardPanel
                    LeaderboardPanel lbPanel = new LeaderboardPanel(student1);
                    assertNotNull("LeaderboardPanel instantiated", lbPanel);
                    lbPanel.refreshLeaderboard();
                    assertTrue("LeaderboardPanel has child components", lbPanel.getComponentCount() > 0);

                    // Test MessagingPanel
                    MessagingPanel msgPanel = new MessagingPanel(student1);
                    assertNotNull("MessagingPanel instantiated", msgPanel);
                    msgPanel.loadContacts();
                    assertTrue("MessagingPanel has child components", msgPanel.getComponentCount() > 0);

                    // Test MyRemindersPanel
                    MyRemindersPanel remindersPanel = new MyRemindersPanel(student1);
                    assertNotNull("MyRemindersPanel instantiated", remindersPanel);
                    remindersPanel.loadReminders();
                    assertTrue("MyRemindersPanel has child components", remindersPanel.getComponentCount() > 0);

                    // Test NotificationDialog
                    NotificationDialog notifDialog = new NotificationDialog(null, student1, null);
                    assertNotNull("NotificationDialog instantiated", notifDialog);
                    notifDialog.dispose();

                    // Test ParticipantDashboard with Phase 10 tabs
                    SessionManager.login(student1);
                    ParticipantDashboard pDash = new ParticipantDashboard(student1);
                    assertNotNull("ParticipantDashboard initialized with Phase 10 integration", pDash);
                    pDash.dispose();

                    // Test CreatorDashboard with Phase 10 tabs
                    SessionManager.login(creator1);
                    CreatorDashboard cDash = new CreatorDashboard(creator1);
                    assertNotNull("CreatorDashboard initialized with Phase 10 integration", cDash);
                    cDash.dispose();

                    // Test AdminDashboard with Phase 10 tabs
                    SessionManager.login(admin);
                    AdminDashboard aDash = new AdminDashboard(admin);
                    assertNotNull("AdminDashboard initialized with Phase 10 integration", aDash);
                    aDash.dispose();

                    SessionManager.logout();
                });
                System.out.println("  [PASS] All Phase 10 UI components, dialogs, and dashboards instantiated cleanly");
                testsPassed++;
            } catch (Exception ex) {
                System.err.println("  [FAIL] UI instantiation error: " + ex.getMessage());
                ex.printStackTrace();
                testsFailed++;
            }
        } else {
            System.out.println("  [SKIP] Headless environment detected; skipping Swing UI rendering assertions.");
        }

        // ---------------------------------------------------------------------
        // SUMMARY
        // ---------------------------------------------------------------------
        System.out.println("\n==================================================");
        System.out.println("VERIFICATION SUMMARY:");
        System.out.println("  Total Passed: " + testsPassed);
        System.out.println("  Total Failed: " + testsFailed);
        if (testsFailed == 0) {
            System.out.println("ALL PHASE 10 VERIFICATION CHECKS PASSED!");
        } else {
            System.out.println("SOME TESTS FAILED! Review test output above.");
        }
        System.out.println("==================================================");
    }

    private static void assertTrue(String message, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + message);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + message);
            testsFailed++;
        }
    }

    private static void assertEquals(String message, Object expected, Object actual) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            System.out.println("  [PASS] " + message + " (Expected: " + expected + ", Actual: " + actual + ")");
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + message + " (Expected: " + expected + ", Actual: " + actual + ")");
            testsFailed++;
        }
    }

    private static void assertNotNull(String message, Object obj) {
        if (obj != null) {
            System.out.println("  [PASS] " + message);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + message + " (Was null)");
            testsFailed++;
        }
    }
}
