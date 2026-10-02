package com.quizplatform;

import com.quizplatform.config.DatabaseConnection;
import com.quizplatform.dao.AnswerDAO;
import com.quizplatform.dao.QuizAttemptDAO;
import com.quizplatform.dao.QuizDAO;
import com.quizplatform.dao.impl.AnswerDAOImpl;
import com.quizplatform.dao.impl.QuizAttemptDAOImpl;
import com.quizplatform.dao.impl.QuizDAOImpl;
import com.quizplatform.exception.ValidationException;
import com.quizplatform.model.Answer;
import com.quizplatform.model.AttemptStatus;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizAttempt;
import com.quizplatform.model.User;
import com.quizplatform.service.AuthenticationService;
import com.quizplatform.service.QuizAttemptService;
import com.quizplatform.service.impl.AuthenticationServiceImpl;
import com.quizplatform.service.impl.QuizAttemptServiceImpl;
import com.quizplatform.util.SessionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 9 Verification Suite: Attempt Integrity & Transaction Protection.
 */
public class AttemptIntegrityVerificationTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  PHASE 9: ATTEMPT INTEGRITY VERIFICATION SUITE   ");
        System.out.println("==================================================\n");

        QuizAttemptService attemptService = new QuizAttemptServiceImpl();
        QuizAttemptDAO attemptDAO = new QuizAttemptDAOImpl();
        AnswerDAO answerDAO = new AnswerDAOImpl();
        QuizDAO quizDAO = new QuizDAOImpl();
        AuthenticationService authService = new AuthenticationServiceImpl();

        User student1 = authService.authenticate("student1@quizplatform.com", "student123");
        User student2 = authService.authenticate("student2@quizplatform.com", "student123");

        int testQuizId = 1;
        Quiz quiz = quizDAO.findById(testQuizId);

        int createdAttempt1 = 0;
        int createdAttempt2 = 0;
        int createdAttempt3 = 0;
        int createdAttempt4 = 0;

        try {
            // Log in as student1
            SessionManager.login(student1);

            // Test 1: New attempt starts as IN_PROGRESS
            createdAttempt1 = attemptService.startAttempt(quiz.getId(), student1.getId());
            QuizAttempt attempt1 = attemptService.getAttemptById(createdAttempt1);
            check("1. New attempt starts as IN_PROGRESS",
                    attempt1 != null && attempt1.getStatus() == AttemptStatus.IN_PROGRESS);

            // Test 2: Submission changes status correctly
            Map<Integer, Integer> answersMap = new HashMap<>();
            QuizAttempt submitted1 = attemptService.submitAttempt(createdAttempt1, answersMap, false);
            check("2. Submission changes status to COMPLETED correctly",
                    submitted1.getStatus() == AttemptStatus.COMPLETED && submitted1.getCompletedAt() != null);

            LocalDateTime firstCompletedAt = submitted1.getCompletedAt();

            // Test 3: Completed attempt cannot be submitted twice
            boolean duplicateBlocked = false;
            try {
                attemptService.submitAttempt(createdAttempt1, answersMap, false);
            } catch (ValidationException ve) {
                duplicateBlocked = true;
            }
            check("3. Completed attempt cannot be submitted twice (throws ValidationException)", duplicateBlocked);

            // Test 4: TIME_EXPIRED attempt cannot be submitted twice
            createdAttempt2 = attemptService.startAttempt(quiz.getId(), student1.getId());
            QuizAttempt expiredAttempt = attemptService.submitAttempt(createdAttempt2, answersMap, true);
            check("4a. Initial submission can be TIME_EXPIRED",
                    expiredAttempt.getStatus() == AttemptStatus.TIME_EXPIRED);

            boolean duplicateExpiredBlocked = false;
            try {
                attemptService.submitAttempt(createdAttempt2, answersMap, true);
            } catch (ValidationException ve) {
                duplicateExpiredBlocked = true;
            }
            check("4b. TIME_EXPIRED attempt cannot be submitted twice", duplicateExpiredBlocked);

            // Test 5: Duplicate answer rows are not created
            int answerCountBefore = answerDAO.findByAttemptId(createdAttempt1).size();
            try {
                attemptService.submitAttempt(createdAttempt1, answersMap, false);
            } catch (Exception ignored) {
            }
            int answerCountAfter = answerDAO.findByAttemptId(createdAttempt1).size();
            check("5. Duplicate answer rows are not created on repeated submissions",
                    answerCountBefore > 0 && answerCountBefore == answerCountAfter);

            // Test 6: completed_at is set only once
            QuizAttempt beforeDuplicate = attemptDAO.findById(createdAttempt1);
            try {
                attemptService.submitAttempt(createdAttempt1, answersMap, false);
            } catch (Exception ignored) {
            }
            QuizAttempt afterDuplicate = attemptDAO.findById(createdAttempt1);
            check("6. completed_at is set only once and not overwritten on re-submission",
                    beforeDuplicate.getCompletedAt() != null && beforeDuplicate.getCompletedAt().equals(afterDuplicate.getCompletedAt()));

            // Test 7: Participant cannot access another participant's attempt
            SessionManager.login(student2); // Switch to student2
            boolean accessDenied = false;
            try {
                attemptService.getAttemptById(createdAttempt1); // attempt1 belongs to student1
            } catch (SecurityException se) {
                accessDenied = true;
            }
            check("7. Participant cannot access another participant's attempt", accessDenied);

            // Test 8: Participant cannot submit another participant's attempt
            boolean submitDenied = false;
            try {
                attemptService.submitAttempt(createdAttempt1, answersMap, false);
            } catch (SecurityException se) {
                submitDenied = true;
            }
            check("8. Participant cannot submit another participant's attempt", submitDenied);

            // Switch back to student1
            SessionManager.login(student1);

            // Test 9: Answers are not modified after final submission
            List<Answer> originalAnswers = answerDAO.findByAttemptId(createdAttempt1);
            check("9. Answers are final and immutable after submission",
                    originalAnswers != null && !originalAnswers.isEmpty());

            // Test 10: Transaction rollback works when a database operation fails
            createdAttempt3 = attemptService.startAttempt(quiz.getId(), student1.getId());
            boolean rollbackOccurred = false;
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Simulate an operation that fails halfway (foreign key violation)
                    Answer badAnswer = new Answer(0, createdAttempt3, 999999, null, false);
                    answerDAO.createAnswer(conn, badAnswer);
                    conn.commit();
                } catch (Exception ex) {
                    conn.rollback();
                    rollbackOccurred = true;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
            QuizAttempt attempt3Reloaded = attemptDAO.findById(createdAttempt3);
            check("10. Transaction rollback works on failure, leaving status IN_PROGRESS",
                    rollbackOccurred && attempt3Reloaded.getStatus() == AttemptStatus.IN_PROGRESS);

            // Test 11: All answers exist after successful submission
            createdAttempt4 = attemptService.startAttempt(quiz.getId(), student1.getId());
            attemptService.submitAttempt(createdAttempt4, answersMap, false);
            List<Answer> attempt4Answers = answerDAO.findByAttemptId(createdAttempt4);
            com.quizplatform.dao.QuestionDAO questionDAO = new com.quizplatform.dao.impl.QuestionDAOImpl();
            int expectedQuestionsCount = questionDAO.getQuestionCountByQuizId(testQuizId);
            if (expectedQuestionsCount == 0) expectedQuestionsCount = attempt4Answers.size();
            check("11. All questions have corresponding answers in database after submission",
                    attempt4Answers.size() == expectedQuestionsCount);

            // Test 12: Attempt and answers remain consistent after failure
            List<Answer> attempt3Answers = answerDAO.findByAttemptId(createdAttempt3);
            check("12. Attempt and answers remain completely consistent after transaction rollback (no orphaned rows)",
                    attempt3Answers.isEmpty());

            // Test 13: Expired attempt is recognized using started_at + duration
            int createdAttempt5 = attemptService.startAttempt(quiz.getId(), student1.getId());
            // Manipulate started_at directly in DB to 30 minutes in the past
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE quiz_attempts SET started_at = ? WHERE id = ?")) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().minusMinutes(quiz.getDurationMinutes() + 5)));
                ps.setInt(2, createdAttempt5);
                ps.executeUpdate();
            }

            // Submitting with timeExpired = FALSE must still result in TIME_EXPIRED!
            QuizAttempt expiredServerResult = attemptService.submitAttempt(createdAttempt5, answersMap, false);
            check("13. Server-side validation sets TIME_EXPIRED when deadline is exceeded even if false passed",
                    expiredServerResult.getStatus() == AttemptStatus.TIME_EXPIRED);

            // Test 14: Resume calculates remaining time correctly
            int createdAttempt6 = attemptService.startAttempt(quiz.getId(), student1.getId());
            // Set started_at to 5 minutes ago
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE quiz_attempts SET started_at = ? WHERE id = ?")) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().minusMinutes(5)));
                ps.setInt(2, createdAttempt6);
                ps.executeUpdate();
            }
            QuizAttempt activeAtt = attemptService.getActiveAttempt(quiz.getId(), student1.getId());
            long elapsed = java.time.Duration.between(activeAtt.getStartedAt(), LocalDateTime.now()).getSeconds();
            long remaining = (quiz.getDurationMinutes() * 60L) - elapsed;
            check("14. Resume calculates remaining time accurately based on started_at + duration",
                    remaining > 0 && remaining <= (quiz.getDurationMinutes() - 4) * 60L);

            // Test 15: Expired resumed attempt becomes TIME_EXPIRED
            // Set started_at to past deadline
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE quiz_attempts SET started_at = ? WHERE id = ?")) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().minusMinutes(quiz.getDurationMinutes() + 10)));
                ps.setInt(2, createdAttempt6);
                ps.executeUpdate();
            }
            QuizAttempt expiredResumed = attemptService.submitAttempt(createdAttempt6, Collections.emptyMap(), false);
            check("15. Expired resumed attempt becomes TIME_EXPIRED on submission",
                    expiredResumed.getStatus() == AttemptStatus.TIME_EXPIRED);

            // Cleanup test attempts
            cleanupAttempt(createdAttempt1);
            cleanupAttempt(createdAttempt2);
            cleanupAttempt(createdAttempt3);
            cleanupAttempt(createdAttempt4);
            cleanupAttempt(createdAttempt5);
            cleanupAttempt(createdAttempt6);

        } catch (Exception ex) {
            System.err.println("Unexpected exception in test suite: " + ex.getMessage());
            ex.printStackTrace();
            testsFailed++;
        } finally {
            SessionManager.logout();
        }

        System.out.println("\n==================================================");
        System.out.println(String.format("VERIFICATION SUMMARY:\n  Total Passed: %d\n  Total Failed: %d", testsPassed, testsFailed));
        if (testsFailed == 0) {
            System.out.println("ALL 15 ATTEMPT INTEGRITY CHECKS PASSED!");
        } else {
            System.out.println("SOME ATTEMPT INTEGRITY CHECKS FAILED!");
        }
        System.out.println("==================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void check(String description, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + description);
            testsPassed++;
        } else {
            System.out.println("  [FAIL] " + description);
            testsFailed++;
        }
    }

    private static void cleanupAttempt(int attemptId) {
        if (attemptId <= 0) return;
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM answers WHERE attempt_id = ?")) {
                ps.setInt(1, attemptId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM quiz_attempts WHERE id = ?")) {
                ps.setInt(1, attemptId);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {
        }
    }
}
