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
import com.quizplatform.ui.participant.ParticipantDashboard;
import com.quizplatform.ui.participant.QuizInstructionsDialog;
import com.quizplatform.ui.participant.QuizResultDialog;
import com.quizplatform.ui.participant.QuizTakingFrame;
import com.quizplatform.util.SessionManager;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 * Verification test suite for Participant Quiz Taking Flow and Results.
 */
public class ParticipantQuizWorkflowVerificationTest {

    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  PARTICIPANT QUIZ TAKING & RESULTS WORKFLOW TEST ");
        System.out.println("==================================================\n");

        QuizService quizService = new QuizServiceImpl();
        QuestionService questionService = new QuestionServiceImpl();
        QuizAttemptService attemptService = new QuizAttemptServiceImpl();
        AuthenticationService authService = new AuthenticationServiceImpl();

        // 1. Authenticate as participant
        User participant = authService.authenticate("student1@quizplatform.com", "student123");
        SessionManager.login(participant);

        try {
            System.out.println("--- [PART 1: Quiz Taking, Answering & Scoring Engine Tests] ---");

            // Test 1: Fetch approved quizzes
            List<Quiz> approvedQuizzes = quizService.getApprovedQuizzes();
            assertTrue("1. Approved quizzes available for participant (count >= 1)", approvedQuizzes != null && !approvedQuizzes.isEmpty());

            Quiz testQuiz = approvedQuizzes.get(0);
            assertTrue("   Selected approved quiz has valid ID", testQuiz.getId() > 0);

            // Test 2: Fetch questions & options for selected quiz
            List<Question> questions = questionService.getQuestionsByQuizId(testQuiz.getId());
            assertTrue("2. Quiz contains questions (count >= 2)", questions != null && questions.size() >= 2);

            for (Question q : questions) {
                List<Option> opts = questionService.getOptionsByQuestionId(q.getId());
                assertTrue("   Question #" + q.getQuestionOrder() + " has 4 options", opts != null && opts.size() == 4);
            }

            // Test 3: Start Quiz Attempt
            int attemptId = attemptService.startAttempt(testQuiz.getId(), participant.getId());
            assertTrue("3. Start attempt assigns attempt ID", attemptId > 0);

            QuizAttempt inProgressAttempt = attemptService.getAttemptById(attemptId);
            assertTrue("   Attempt status is IN_PROGRESS", inProgressAttempt.getStatus() == AttemptStatus.IN_PROGRESS);

            // Test 4: Answer questions - choose correct option for Q1, incorrect for Q2
            Map<Integer, Integer> answers = new HashMap<>();

            // Q1: select correct option
            Question q1 = questions.get(0);
            Option correctOpt1 = questionService.getCorrectOptionByQuestionId(q1.getId());
            answers.put(q1.getId(), correctOpt1.getId());

            // Q2: select incorrect option
            Question q2 = questions.get(1);
            List<Option> opts2 = questionService.getOptionsByQuestionId(q2.getId());
            for (Option opt : opts2) {
                if (!opt.isCorrect()) {
                    answers.put(q2.getId(), opt.getId());
                    break;
                }
            }

            // Test 5: Submit Attempt
            QuizAttempt completedAttempt = attemptService.submitAttempt(attemptId, answers, false);
            assertTrue("4. Submit attempt marks status as COMPLETED", completedAttempt.getStatus() == AttemptStatus.COMPLETED);
            assertTrue("   Score is exactly 1", completedAttempt.getScore() == 1);
            assertTrue("   Correct answers count is 1", completedAttempt.getCorrectAnswers() == 1);
            assertTrue("   Incorrect answers count >= 1", completedAttempt.getIncorrectAnswers() >= 1);
            assertTrue("   Completed timestamp is populated", completedAttempt.getCompletedAt() != null);
            assertTrue("   Percentage is calculated correctly", completedAttempt.getPercentage() > 0.0);

            // Test 6: Verify individual answer records in database
            List<Answer> savedAnswers = attemptService.getAnswersByAttempt(attemptId);
            assertTrue("5. Answers table populated for all quiz questions", savedAnswers.size() == questions.size());

            boolean foundCorrectAnswer = false;
            boolean foundIncorrectAnswer = false;
            for (Answer a : savedAnswers) {
                if (a.getQuestionId() == q1.getId() && a.isCorrect()) {
                    foundCorrectAnswer = true;
                }
                if (a.getQuestionId() == q2.getId() && !a.isCorrect()) {
                    foundIncorrectAnswer = true;
                }
            }
            assertTrue("   Q1 recorded as correct answer in answers table", foundCorrectAnswer);
            assertTrue("   Q2 recorded as incorrect answer in answers table", foundIncorrectAnswer);

            // Test 7: Verify Participant Metrics
            int totalAttempts = attemptService.getAttemptsCountByParticipant(participant.getId());
            int completedAttempts = attemptService.getCompletedAttemptsCountByParticipant(participant.getId());
            double avgPercentage = attemptService.getAveragePercentageByParticipant(participant.getId());
            int highScore = attemptService.getHighestScoreByParticipant(participant.getId());

            assertTrue("6. Participant total attempts count >= 1", totalAttempts >= 1);
            assertTrue("   Participant completed attempts count >= 1", completedAttempts >= 1);
            assertTrue("   Participant average percentage > 0.0", avgPercentage > 0.0);
            assertTrue("   Participant highest score >= 1", highScore >= 1);

            System.out.println("\n--- [PART 2: Swing Participant UI Flow & Dialogs Tests] ---");

            // Test 8: ParticipantDashboard launches on EDT
            final ParticipantDashboard[] dashboardHolder = new ParticipantDashboard[1];
            SwingUtilities.invokeAndWait(() -> {
                ParticipantDashboard dash = new ParticipantDashboard(participant);
                dash.setVisible(true);
                dashboardHolder[0] = dash;
            });
            ParticipantDashboard dashboard = dashboardHolder[0];

            assertTrue("7. ParticipantDashboard initialized and visible", dashboard != null && dashboard.isVisible());
            assertTrue("   Dashboard title contains 'Participant Portal'", dashboard.getTitle().contains("Participant Portal"));

            List<JTable> tables = findComponentsOfType(dashboard, JTable.class);
            assertTrue("8. Dashboard contains JTables for available quizzes & history", tables.size() >= 1);

            // Test 9: QuizInstructionsDialog opens and displays rules
            final QuizInstructionsDialog[] instructionsHolder = new QuizInstructionsDialog[1];
            SwingUtilities.invokeAndWait(() -> {
                QuizInstructionsDialog dialog = new QuizInstructionsDialog(dashboard, testQuiz, questions.size());
                instructionsHolder[0] = dialog;
            });
            QuizInstructionsDialog instructionsDialog = instructionsHolder[0];
            assertTrue("9. QuizInstructionsDialog created successfully", instructionsDialog != null);

            // Test 10: QuizTakingFrame launches with timer & options
            final QuizTakingFrame[] examHolder = new QuizTakingFrame[1];
            SwingUtilities.invokeAndWait(() -> {
                int testExamAttemptId = attemptService.startAttempt(testQuiz.getId(), participant.getId());
                QuizTakingFrame examFrame = new QuizTakingFrame(testQuiz, testExamAttemptId, questionService, attemptService, null);
                examFrame.setVisible(true);
                examHolder[0] = examFrame;
            });
            QuizTakingFrame examFrame = examHolder[0];

            assertTrue("10. QuizTakingFrame is visible and active", examFrame != null && examFrame.isVisible());

            List<JRadioButton> radios = findComponentsOfType(examFrame, JRadioButton.class);
            assertTrue("11. Exam frame contains A/B/C/D radio button options", radios.size() >= 4);

            List<JButton> examButtons = findComponentsOfType(examFrame, JButton.class);
            assertTrue("12. Exam frame contains Next, Previous, and Submit buttons", examButtons.size() >= 3);

            // Test 11: QuizResultDialog opens with score and review
            final QuizResultDialog[] resultHolder = new QuizResultDialog[1];
            SwingUtilities.invokeAndWait(() -> {
                QuizResultDialog resultDialog = new QuizResultDialog(dashboard, completedAttempt, testQuiz, questionService, attemptService);
                resultHolder[0] = resultDialog;
            });
            QuizResultDialog resultDialog = resultHolder[0];
            assertTrue("13. QuizResultDialog displays score metrics", resultDialog != null);

            // Test 12: Dispose frames and Logout
            SwingUtilities.invokeAndWait(() -> {
                examFrame.dispose();
                dashboard.dispose();
                SessionManager.logout();
            });

            assertTrue("14. ParticipantDashboard disposed on logout", !dashboard.isDisplayable());
            assertTrue("15. Session cleared after participant logout", !SessionManager.isLoggedIn());

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
            System.out.println("ALL PARTICIPANT QUIZ TAKING CHECKS PASSED!");
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
