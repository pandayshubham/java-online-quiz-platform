package com.quizplatform;

import com.quizplatform.exception.ValidationException;
import com.quizplatform.model.Option;
import com.quizplatform.model.Question;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizAttempt;
import com.quizplatform.model.QuizStatus;
import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import com.quizplatform.service.AuthenticationService;
import com.quizplatform.service.QuestionService;
import com.quizplatform.service.QuizAttemptService;
import com.quizplatform.service.QuizService;
import com.quizplatform.service.impl.AuthenticationServiceImpl;
import com.quizplatform.service.impl.QuestionServiceImpl;
import com.quizplatform.service.impl.QuizAttemptServiceImpl;
import com.quizplatform.service.impl.QuizServiceImpl;
import com.quizplatform.ui.creator.CreatorDashboard;
import com.quizplatform.util.SessionManager;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 * Comprehensive verification test suite for Phase 6: Quiz Creator Dashboard & Quiz/Question Management.
 */
public class CreatorDashboardVerificationTest {

    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  PHASE 6: QUIZ CREATOR DASHBOARD & MGMT TEST     ");
        System.out.println("==================================================\n");

        QuizService quizService = new QuizServiceImpl();
        QuestionService questionService = new QuestionServiceImpl();
        QuizAttemptService attemptService = new QuizAttemptServiceImpl();
        AuthenticationService authService = new AuthenticationServiceImpl();

        // 1. Authenticate as seed creator
        User creator = authService.authenticate("creator1@quizplatform.com", "creator123");
        SessionManager.login(creator);

        int testQuizId = 0;
        int testQuestionId = 0;

        try {
            System.out.println("--- [PART 1: Quiz & Question Authoring Workflow Tests] ---");

            // Test 1: Create a Quiz
            Quiz newQuiz = new Quiz();
            newQuiz.setCreatorId(creator.getId());
            newQuiz.setTitle("Phase 6 Java Concurrency Quiz");
            newQuiz.setDescription("Advanced test on multithreading and thread pools.");
            newQuiz.setDurationMinutes(20);
            newQuiz.setStatus(QuizStatus.DRAFT);

            testQuizId = quizService.createQuiz(newQuiz);
            assertTrue("1. Create Quiz in DRAFT status assigns ID", testQuizId > 0);

            Quiz fetchedQuiz = quizService.getQuizById(testQuizId);
            assertTrue("   Fetched Quiz status is DRAFT", fetchedQuiz.getStatus() == QuizStatus.DRAFT);
            assertTrue("   Fetched Quiz title matches", "Phase 6 Java Concurrency Quiz".equals(fetchedQuiz.getTitle()));

            // Test 2: Update Quiz details
            fetchedQuiz.setTitle("Phase 6 Java Concurrency Mastery");
            fetchedQuiz.setDurationMinutes(25);
            boolean updated = quizService.updateQuiz(fetchedQuiz);
            assertTrue("2. Update Quiz details succeeds", updated);

            Quiz updatedQuiz = quizService.getQuizById(testQuizId);
            assertTrue("   Updated Quiz duration is 25", updatedQuiz.getDurationMinutes() == 25);
            assertTrue("   Updated Quiz title matches", "Phase 6 Java Concurrency Mastery".equals(updatedQuiz.getTitle()));

            // Test 3: Submitting quiz with 0 questions must fail
            boolean rejectedEmptySubmit = false;
            try {
                quizService.submitQuizForApproval(testQuizId);
            } catch (ValidationException ve) {
                rejectedEmptySubmit = true;
            }
            assertTrue("3. Submitting quiz with 0 questions rejected with ValidationException", rejectedEmptySubmit);

            // Test 4: Add a Question with 4 options (A/B/C/D)
            Question q1 = new Question();
            q1.setQuizId(testQuizId);
            q1.setQuestionText("What is the state of a thread waiting for a monitor lock?");
            q1.setExplanation("A thread that is waiting for a monitor lock is in the BLOCKED state.");
            q1.setQuestionOrder(1);

            List<Option> options = new ArrayList<>();
            options.add(new Option(0, 0, "RUNNABLE", "A", false));
            options.add(new Option(0, 0, "BLOCKED", "B", true)); // Correct option
            options.add(new Option(0, 0, "WAITING", "C", false));
            options.add(new Option(0, 0, "TIMED_WAITING", "D", false));

            Question createdQ = questionService.createQuestion(q1, options);
            testQuestionId = createdQ.getId();
            assertTrue("4. Create Question assigns question ID", testQuestionId > 0);

            // Test 5: Verify question options persisted
            List<Option> savedOptions = questionService.getOptionsByQuestionId(testQuestionId);
            assertTrue("5. Question has exactly 4 options persisted", savedOptions.size() == 4);

            Option correctOpt = questionService.getCorrectOptionByQuestionId(testQuestionId);
            assertTrue("   Correct option is Option B", correctOpt != null && "B".equalsIgnoreCase(correctOpt.getOptionLabel()));
            assertTrue("   Correct option text is 'BLOCKED'", correctOpt != null && "BLOCKED".equals(correctOpt.getOptionText()));

            // Test 6: Update Question and change correct option
            createdQ.setQuestionText("In Java, which state indicates a thread waiting for a monitor lock?");
            options.get(1).setCorrect(false); // remove B
            options.get(2).setCorrect(true);  // set C as correct for test
            boolean qUpdated = questionService.updateQuestion(createdQ, options);
            assertTrue("6. Update Question succeeds", qUpdated);

            Option updatedCorrectOpt = questionService.getCorrectOptionByQuestionId(testQuestionId);
            assertTrue("   Updated correct option is now Option C", updatedCorrectOpt != null && "C".equalsIgnoreCase(updatedCorrectOpt.getOptionLabel()));

            // Restore correct option to B
            options.get(2).setCorrect(false);
            options.get(1).setCorrect(true);
            questionService.updateQuestion(createdQ, options);

            // Test 7: Submit Quiz for Approval (now has 1 question)
            boolean submitted = quizService.submitQuizForApproval(testQuizId);
            assertTrue("7. Submit Quiz with >= 1 question succeeds", submitted);

            Quiz submittedQuiz = quizService.getQuizById(testQuizId);
            assertTrue("   Submitted quiz status is now PENDING_APPROVAL", submittedQuiz.getStatus() == QuizStatus.PENDING_APPROVAL);

            // Test 8: Creator Statistics
            int totalQuizzes = quizService.getQuizCountByCreator(creator.getId());
            int pendingQuizzes = quizService.getQuizCountByCreatorAndStatus(creator.getId(), QuizStatus.PENDING_APPROVAL);
            int creatorQuestions = quizService.getTotalQuestionsCountByCreator(creator.getId());

            assertTrue("8. Creator total quizzes count >= 1", totalQuizzes >= 1);
            assertTrue("   Creator pending quizzes count >= 1", pendingQuizzes >= 1);
            assertTrue("   Creator total questions count >= 1", creatorQuestions >= 1);

            // Test 9: Participant attempts query for creator
            List<QuizAttempt> creatorAttempts = attemptService.getAttemptsByCreator(creator.getId());
            assertTrue("9. getAttemptsByCreator executes without error", creatorAttempts != null);

            // Test 10: Delete question
            boolean qDeleted = questionService.deleteQuestion(testQuestionId);
            assertTrue("10. Delete Question succeeds", qDeleted);
            assertTrue("    Question options cascaded on delete", questionService.getOptionsByQuestionId(testQuestionId).isEmpty());

            // Test 11: Delete quiz
            boolean quizDeleted = quizService.deleteQuiz(testQuizId);
            assertTrue("11. Delete Quiz succeeds", quizDeleted);
            assertTrue("    Quiz no longer found in DB", quizService.getQuizById(testQuizId) == null);

            System.out.println("\n--- [PART 2: Swing Creator Dashboard & UI Navigation Tests] ---");

            // Test 12: Launch CreatorDashboard on EDT
            final CreatorDashboard[] dashboardHolder = new CreatorDashboard[1];
            SwingUtilities.invokeAndWait(() -> {
                CreatorDashboard dashboard = new CreatorDashboard(creator);
                dashboard.setVisible(true);
                dashboardHolder[0] = dashboard;
            });
            CreatorDashboard dashboard = dashboardHolder[0];

            assertTrue("12. CreatorDashboard successfully initialized and visible", dashboard != null && dashboard.isVisible());
            assertTrue("    Window title contains 'Creator Console'", dashboard.getTitle().contains("Creator Console"));

            // Test 13: Verify component tree and subpanels
            List<JButton> buttons = findComponentsOfType(dashboard, JButton.class);
            assertTrue("13. Dashboard contains navigation & action buttons (count >= 5)", buttons.size() >= 5);

            List<JTable> tables = findComponentsOfType(dashboard, JTable.class);
            assertTrue("14. Dashboard contains JTables for quizzes/questions/history", tables.size() >= 1);

            List<JComboBox> combos = findComponentsOfType(dashboard, JComboBox.class);
            assertTrue("15. Dashboard contains JComboBox selectors/filters", combos.size() >= 1);

            // Test 14: Clean up Swing frame and Logout
            SwingUtilities.invokeAndWait(() -> {
                dashboard.dispose();
                SessionManager.logout();
            });

            assertTrue("16. CreatorDashboard disposed on logout", !dashboard.isDisplayable());
            assertTrue("17. SessionManager is cleared after creator logout", !SessionManager.isLoggedIn());

        } catch (Exception e) {
            System.err.println("Unexpected exception in test suite: " + e.getMessage());
            e.printStackTrace();
            failedTests++;
        }

        // Summary
        System.out.println("\n==================================================");
        System.out.println("VERIFICATION SUMMARY:");
        System.out.println("  Total Passed: " + passedTests);
        System.out.println("  Total Failed: " + failedTests);
        if (failedTests == 0) {
            System.out.println("ALL PHASE 6 QUIZ CREATOR CHECKS PASSED!");
        } else {
            System.out.println("SOME TESTS FAILED! PLEASE REVIEW.");
        }
        System.out.println("==================================================");

        if (failedTests > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String message, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + message);
            passedTests++;
        } else {
            System.err.println("  [FAIL] " + message);
            failedTests++;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Component> List<T> findComponentsOfType(Container container, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        for (Component comp : container.getComponents()) {
            if (clazz.isInstance(comp)) {
                result.add((T) comp);
            }
            if (comp instanceof Container childContainer) {
                result.addAll(findComponentsOfType(childContainer, clazz));
            }
        }
        return result;
    }
}
