package com.quizplatform;

import com.quizplatform.model.Answer;
import com.quizplatform.model.AttemptStatus;
import com.quizplatform.model.Option;
import com.quizplatform.model.Question;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizAttempt;
import com.quizplatform.model.User;
import com.quizplatform.service.AuthenticationService;
import com.quizplatform.service.QuestionService;
import com.quizplatform.service.QuizAttemptService;
import com.quizplatform.service.QuizService;
import com.quizplatform.service.impl.AuthenticationServiceImpl;
import com.quizplatform.service.impl.QuestionServiceImpl;
import com.quizplatform.service.impl.QuizAttemptServiceImpl;
import com.quizplatform.service.impl.QuizServiceImpl;
import com.quizplatform.ui.participant.QuizResultDialog;
import com.quizplatform.util.SessionManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;

/**
 * Verification test suite for Quiz Start -> Countdown -> Timer Expiry (00:00) -> Auto-submit -> TIME_EXPIRED lifecycle.
 */
public class TimerExpiryWorkflowVerificationTest {

    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  TIMER EXPIRY (00:00) AUTO-SUBMISSION TEST       ");
        System.out.println("==================================================\n");

        QuizService quizService = new QuizServiceImpl();
        QuestionService questionService = new QuestionServiceImpl();
        QuizAttemptService attemptService = new QuizAttemptServiceImpl();
        AuthenticationService authService = new AuthenticationServiceImpl();

        // 1. Authenticate as participant
        User participant = authService.authenticate("student2@quizplatform.com", "student123");
        SessionManager.login(participant);

        try {
            // Step 1: Select approved quiz
            List<Quiz> approvedQuizzes = quizService.getApprovedQuizzes();
            assertTrue("1. Found approved quiz for testing", !approvedQuizzes.isEmpty());
            Quiz quiz = approvedQuizzes.get(0);

            // Step 2: Quiz Start -> Create IN_PROGRESS attempt
            int attemptId = attemptService.startAttempt(quiz.getId(), participant.getId());
            assertTrue("2. Quiz Start creates attempt with ID > 0", attemptId > 0);

            QuizAttempt inProgressAttempt = attemptService.getAttemptById(attemptId);
            assertTrue("   Attempt status is initially IN_PROGRESS", inProgressAttempt.getStatus() == AttemptStatus.IN_PROGRESS);
            assertTrue("   Attempt completed_at is initially null", inProgressAttempt.getCompletedAt() == null);

            // Step 3: User answers ONLY Question 1, leaving remaining questions unanswered
            List<Question> questions = questionService.getQuestionsByQuizId(quiz.getId());
            assertTrue("3. Quiz has at least 2 questions", questions.size() >= 2);

            Map<Integer, Integer> partialAnswers = new HashMap<>();
            Question q1 = questions.get(0);
            Option correctOpt1 = questionService.getCorrectOptionByQuestionId(q1.getId());
            partialAnswers.put(q1.getId(), correctOpt1.getId()); // answered Q1 correctly

            // Step 4: Timer reaches 00:00 -> Auto-submit triggered with timeExpired = true
            System.out.println("   [Simulating countdown timer reaching 00:00 -> auto-submit triggered]");
            QuizAttempt expiredAttempt = attemptService.submitAttempt(attemptId, partialAnswers, true);

            // Step 5: Verify status is TIME_EXPIRED
            assertTrue("4. Auto-submit sets attempt status to TIME_EXPIRED", expiredAttempt.getStatus() == AttemptStatus.TIME_EXPIRED);

            // Step 6: Verify Score calculation
            assertTrue("5. Score calculated accurately (score == 1)", expiredAttempt.getScore() == 1);
            assertTrue("   Correct answers count == 1", expiredAttempt.getCorrectAnswers() == 1);
            assertTrue("   Unanswered questions count == " + (questions.size() - 1),
                    expiredAttempt.getUnansweredQuestions() == (questions.size() - 1));

            // Step 7: Verify completed timestamp & percentage
            assertTrue("6. Completed timestamp (completed_at) saved", expiredAttempt.getCompletedAt() != null);
            assertTrue("   Percentage calculated properly", expiredAttempt.getPercentage() > 0.0);

            // Step 8: Verify individual Answer records saved
            List<Answer> savedAnswers = attemptService.getAnswersByAttempt(attemptId);
            assertTrue("7. All question answer rows saved in answers table", savedAnswers.size() == questions.size());

            int nullSelectedCount = 0;
            for (Answer a : savedAnswers) {
                if (a.getSelectedOptionId() == null) {
                    nullSelectedCount++;
                    assertTrue("   Unanswered question marked is_correct = false", !a.isCorrect());
                }
            }
            assertTrue("   Unanswered questions saved with selected_option_id = NULL", nullSelectedCount == (questions.size() - 1));

            // Step 9: Verify Result Screen presentation of TIME_EXPIRED
            final QuizResultDialog[] resultHolder = new QuizResultDialog[1];
            SwingUtilities.invokeAndWait(() -> {
                QuizResultDialog dialog = new QuizResultDialog(null, expiredAttempt, quiz, questionService, attemptService);
                resultHolder[0] = dialog;
            });
            QuizResultDialog resultDialog = resultHolder[0];
            assertTrue("8. QuizResultDialog successfully instantiated for TIME_EXPIRED attempt", resultDialog != null);

            SwingUtilities.invokeAndWait(() -> {
                resultDialog.dispose();
                SessionManager.logout();
            });

            assertTrue("9. Test cleaned up and session cleared", !SessionManager.isLoggedIn());

        } catch (Exception ex) {
            System.err.println("Unexpected exception in timer test: " + ex.getMessage());
            ex.printStackTrace();
            failedTests++;
        }

        // Summary
        System.out.println("\n==================================================");
        System.out.println("VERIFICATION SUMMARY:");
        System.out.println("  Total Passed: " + passedTests);
        System.out.println("  Total Failed: " + failedTests);
        if (failedTests == 0) {
            System.out.println("ALL TIMER EXPIRY & AUTO-SUBMIT CHECKS PASSED!");
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
}
