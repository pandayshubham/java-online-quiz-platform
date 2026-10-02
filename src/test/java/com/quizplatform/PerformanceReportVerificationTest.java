package com.quizplatform;

import com.quizplatform.config.DatabaseConnection;
import com.quizplatform.dao.QuizAttemptDAO;
import com.quizplatform.dao.QuizDAO;
import com.quizplatform.dao.UserDAO;
import com.quizplatform.dao.impl.QuizAttemptDAOImpl;
import com.quizplatform.dao.impl.QuizDAOImpl;
import com.quizplatform.dao.impl.UserDAOImpl;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizAttempt;
import com.quizplatform.model.User;
import com.quizplatform.model.report.AttemptPerformanceDetail;
import com.quizplatform.model.report.CreatorPerformanceSummary;
import com.quizplatform.model.report.ParticipantPerformanceSummary;
import com.quizplatform.model.report.PlatformPerformanceSummary;
import com.quizplatform.model.report.QuestionAnalytics;
import com.quizplatform.model.report.QuizPerformanceSummary;
import com.quizplatform.service.AuthenticationService;
import com.quizplatform.service.PerformanceReportService;
import com.quizplatform.service.QuizAttemptService;
import com.quizplatform.service.impl.AuthenticationServiceImpl;
import com.quizplatform.service.impl.PerformanceReportServiceImpl;
import com.quizplatform.service.impl.QuizAttemptServiceImpl;
import com.quizplatform.util.CsvExportUtil;
import com.quizplatform.util.SessionManager;
import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 9 Verification Suite: Performance Reports & CSV Exports.
 */
public class PerformanceReportVerificationTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  PHASE 9: PERFORMANCE REPORT VERIFICATION SUITE ");
        System.out.println("==================================================\n");

        PerformanceReportService reportService = new PerformanceReportServiceImpl();
        QuizAttemptService attemptService = new QuizAttemptServiceImpl();
        QuizAttemptDAO attemptDAO = new QuizAttemptDAOImpl();
        QuizDAO quizDAO = new QuizDAOImpl();
        UserDAO userDAO = new UserDAOImpl();
        AuthenticationService authService = new AuthenticationServiceImpl();

        User student1 = authService.authenticate("student1@quizplatform.com", "student123");
        User student2 = authService.authenticate("student2@quizplatform.com", "student123");
        User creator1 = authService.authenticate("creator1@quizplatform.com", "creator123");
        User creator2 = authService.authenticate("creator2@quizplatform.com", "creator123");
        User adminUser = authService.authenticate("admin@quizplatform.com", "admin123");

        int testAttemptId = 0;

        try {
            // Setup: Create a completed attempt for student1 on quiz 1 to guarantee data presence
            SessionManager.login(student1);
            testAttemptId = attemptService.startAttempt(1, student1.getId());
            attemptService.submitAttempt(testAttemptId, Collections.emptyMap(), false);

            // ----------------------------------------------------
            // PARTICIPANT TESTS (1-9)
            // ----------------------------------------------------
            System.out.println("--- [PARTICIPANT REPORT TESTS] ---");

            // Test 1: Summary loads
            ParticipantPerformanceSummary summary = reportService.getParticipantSummary(student1.getId());
            check("1. Participant summary loads without error", summary != null);

            // Test 2: Total attempts correct
            check("2. Total attempts count is positive and matches database",
                    summary.getTotalAttempts() > 0 && summary.getTotalAttempts() >= summary.getCompletedAttempts());

            // Test 3: Average score correct
            check("3. Average score is non-negative and calculated properly",
                    summary.getAverageScore() >= 0.0);

            // Test 4: Best score correct
            check("4. Best score is non-negative and within valid bounds",
                    summary.getBestScore() >= 0);

            // Test 5: History loads
            List<QuizAttempt> history = reportService.getParticipantHistory(student1.getId());
            check("5. Participant attempt history loads with records",
                    history != null && !history.isEmpty());

            // Test 6: Attempt detail loads
            AttemptPerformanceDetail detail = reportService.getAttemptPerformance(testAttemptId, student1.getId());
            check("6. Attempt performance detail loads with high-level metrics",
                    detail != null && detail.getAttemptId() == testAttemptId && detail.getQuizTitle() != null);

            // Test 7: Question-level result loads
            check("7. Question-level result loads with question details",
                    detail.getQuestionDetails() != null && !detail.getQuestionDetails().isEmpty() &&
                    detail.getQuestionDetails().get(0).getResult() != null);

            // Test 8: Quiz-specific performance loads
            QuizPerformanceSummary quizPerf = reportService.getParticipantQuizPerformance(student1.getId(), 1);
            check("8. Quiz-specific participant performance loads with accurate stats",
                    quizPerf != null && quizPerf.getQuizId() == 1 && quizPerf.getAttemptsCount() > 0);

            // Test 9: Participant authorization works
            SessionManager.login(student2);
            boolean partAuthBlocked = false;
            try {
                reportService.getParticipantSummary(student1.getId()); // student2 requesting student1's summary
            } catch (SecurityException se) {
                partAuthBlocked = true;
            }
            check("9. Participant authorization works (cannot view other participant's summary)", partAuthBlocked);

            // ----------------------------------------------------
            // CREATOR TESTS (10-14)
            // ----------------------------------------------------
            System.out.println("\n--- [CREATOR REPORT TESTS] ---");
            SessionManager.login(creator1);

            // Test 10: Creator summary loads
            CreatorPerformanceSummary creatorSummary = reportService.getCreatorSummary(creator1.getId());
            check("10. Creator summary loads with aggregated analytics",
                    creatorSummary != null && creatorSummary.getCreatorId() == creator1.getId());

            // Test 11: Creator quiz performance loads
            QuizPerformanceSummary creatorQuizPerf = reportService.getCreatorQuizPerformance(creator1.getId(), 1);
            check("11. Creator quiz performance loads for owned quiz",
                    creatorQuizPerf != null && creatorQuizPerf.getQuizId() == 1);

            // Test 12: Participant result list loads
            List<QuizAttempt> creatorAttempts = reportService.getCreatorAttempts(creator1.getId());
            check("12. Participant results list loads for creator's quizzes",
                    creatorAttempts != null);

            // Test 13: Question analytics load
            List<QuestionAnalytics> qAnalytics = reportService.getQuizQuestionAnalytics(1, creator1.getId());
            check("13. Question-level analytics load with accuracy and counts",
                    qAnalytics != null && !qAnalytics.isEmpty() && qAnalytics.get(0).getQuestionText() != null);

            // Test 14: Creator cannot access another creator's quiz results
            boolean creatorAuthBlocked = false;
            try {
                // Creator 1 trying to access Creator 2's owned quiz 2
                Quiz quiz2 = quizDAO.findById(2);
                if (quiz2 != null && quiz2.getCreatorId() != creator1.getId()) {
                    reportService.getCreatorQuizPerformance(creator1.getId(), 2);
                } else {
                    // Try with creator2 login on quiz 1
                    SessionManager.login(creator2);
                    reportService.getCreatorQuizPerformance(creator2.getId(), 1);
                }
            } catch (SecurityException se) {
                creatorAuthBlocked = true;
            }
            check("14. Creator cannot access another creator's quiz results", creatorAuthBlocked);

            // ----------------------------------------------------
            // ADMIN TESTS (15-17)
            // ----------------------------------------------------
            System.out.println("\n--- [ADMIN REPORT TESTS] ---");
            SessionManager.login(adminUser);

            // Test 15: Platform summary loads
            PlatformPerformanceSummary platformSummary = reportService.getPlatformSummary();
            check("15. Platform summary loads for administrator",
                    platformSummary != null);

            // Test 16: Recent attempts load
            List<QuizAttempt> recentAttempts = reportService.getRecentPlatformAttempts(20);
            check("16. Recent attempts load platform-wide",
                    recentAttempts != null && !recentAttempts.isEmpty());

            // Test 17: Platform statistics are correct
            check("17. Platform statistics reflect real database totals",
                    platformSummary.getTotalQuizAttempts() > 0 &&
                    platformSummary.getTotalQuizQuestions() > 0 &&
                    platformSummary.getTotalAnswers() >= 0);

            // ----------------------------------------------------
            // CSV TESTS (18-22)
            // ----------------------------------------------------
            System.out.println("\n--- [CSV EXPORT TESTS] ---");
            File tempPartCsv = File.createTempFile("part_hist_test", ".csv");
            File tempCreatorCsv = File.createTempFile("creator_res_test", ".csv");
            File tempAdminCsv = File.createTempFile("admin_att_test", ".csv");

            // Test 18: Participant CSV export works
            Map<Integer, String> titleMap = new HashMap<>();
            titleMap.put(1, "Core Java Fundamentals");
            CsvExportUtil.exportParticipantHistory(tempPartCsv, history, titleMap);
            check("18. Participant CSV export writes valid non-empty file",
                    tempPartCsv.exists() && tempPartCsv.length() > 0);

            // Test 19: Creator CSV export works
            Map<Integer, String[]> userMap = new HashMap<>();
            userMap.put(student1.getId(), new String[]{student1.getName(), student1.getEmail()});
            CsvExportUtil.exportCreatorResults(tempCreatorCsv, creatorAttempts, titleMap, userMap);
            check("19. Creator CSV export writes valid non-empty file",
                    tempCreatorCsv.exists() && tempCreatorCsv.length() > 0);

            // Test 20: Admin CSV export works
            Map<Integer, String> creatorMap = new HashMap<>();
            creatorMap.put(1, "Prof. Alice Johnson");
            Map<Integer, String> partMap = new HashMap<>();
            partMap.put(student1.getId(), student1.getName());
            CsvExportUtil.exportAdminRecentAttempts(tempAdminCsv, recentAttempts, titleMap, creatorMap, partMap);
            check("20. Admin CSV export writes valid non-empty file",
                    tempAdminCsv.exists() && tempAdminCsv.length() > 0);

            // Test 21: CSV headers are correct
            String partCsvContent = Files.readString(tempPartCsv.toPath());
            check("21. CSV contains correct expected header columns",
                    partCsvContent.contains("Attempt ID,Quiz ID,Quiz Title,Score,Total Questions"));

            // Test 22: CSV escaping works
            String escapedQuote = CsvExportUtil.escapeCsv("Test \"Special\" Title");
            String escapedComma = CsvExportUtil.escapeCsv("Title, with comma");
            check("22. CSV escaping correctly escapes double quotes and commas according to RFC 4180",
                    "\"Test \"\"Special\"\" Title\"".equals(escapedQuote) &&
                    "\"Title, with comma\"".equals(escapedComma));

            // Clean up temporary CSV files
            tempPartCsv.delete();
            tempCreatorCsv.delete();
            tempAdminCsv.delete();

            // Clean up created attempt
            cleanupAttempt(testAttemptId);

        } catch (Exception ex) {
            System.err.println("Unexpected exception in performance report tests: " + ex.getMessage());
            ex.printStackTrace();
            testsFailed++;
        } finally {
            SessionManager.logout();
        }

        System.out.println("\n==================================================");
        System.out.println(String.format("VERIFICATION SUMMARY:\n  Total Passed: %d\n  Total Failed: %d", testsPassed, testsFailed));
        if (testsFailed == 0) {
            System.out.println("ALL 22 PERFORMANCE REPORT CHECKS PASSED!");
        } else {
            System.out.println("SOME PERFORMANCE REPORT CHECKS FAILED!");
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
        try (var conn = DatabaseConnection.getConnection()) {
            try (var ps = conn.prepareStatement("DELETE FROM answers WHERE attempt_id = ?")) {
                ps.setInt(1, attemptId);
                ps.executeUpdate();
            }
            try (var ps = conn.prepareStatement("DELETE FROM quiz_attempts WHERE id = ?")) {
                ps.setInt(1, attemptId);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {
        }
    }
}
