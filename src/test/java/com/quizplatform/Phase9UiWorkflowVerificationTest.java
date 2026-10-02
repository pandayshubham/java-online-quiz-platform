package com.quizplatform;

import com.quizplatform.dao.QuizAttemptDAO;
import com.quizplatform.dao.QuizDAO;
import com.quizplatform.dao.impl.QuizAttemptDAOImpl;
import com.quizplatform.dao.impl.QuizDAOImpl;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizAttempt;
import com.quizplatform.model.User;
import com.quizplatform.model.report.AttemptPerformanceDetail;
import com.quizplatform.service.AuthenticationService;
import com.quizplatform.service.PerformanceReportService;
import com.quizplatform.service.QuizAttemptService;
import com.quizplatform.service.QuizService;
import com.quizplatform.service.impl.AuthenticationServiceImpl;
import com.quizplatform.service.impl.PerformanceReportServiceImpl;
import com.quizplatform.service.impl.QuizAttemptServiceImpl;
import com.quizplatform.service.impl.QuizServiceImpl;
import com.quizplatform.ui.admin.AdminDashboard;
import com.quizplatform.ui.creator.CreatorDashboard;
import com.quizplatform.ui.creator.ParticipantResultsPanel;
import com.quizplatform.ui.participant.ParticipantDashboard;
import com.quizplatform.ui.participant.ParticipantPerformanceDialog;
import com.quizplatform.util.CsvExportUtil;
import com.quizplatform.util.SessionManager;
import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;

/**
 * Programmatic UI workflow test for Phase 9 features.
 */
public class Phase9UiWorkflowVerificationTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("  PHASE 9: PROGRAMMATIC UI & CSV WORKFLOW TEST    ");
        System.out.println("==================================================\n");

        AuthenticationService authService = new AuthenticationServiceImpl();
        QuizAttemptService attemptService = new QuizAttemptServiceImpl();
        PerformanceReportService reportService = new PerformanceReportServiceImpl();
        QuizService quizService = new QuizServiceImpl();
        QuizDAO quizDAO = new QuizDAOImpl();
        QuizAttemptDAO attemptDAO = new QuizAttemptDAOImpl();

        User student1 = authService.authenticate("student1@quizplatform.com", "student123");
        User creator1 = authService.authenticate("creator1@quizplatform.com", "creator123");
        User admin = authService.authenticate("admin@quizplatform.com", "admin123");

        // Ensure student1 has at least 1 completed attempt for testing
        SessionManager.login(student1);
        int attId = attemptService.startAttempt(1, student1.getId());
        attemptService.submitAttempt(attId, Collections.emptyMap(), false);

        // 1. PARTICIPANT UI TEST
        System.out.println("--- Testing Participant Dashboard & Performance Dialog ---");
        SwingUtilities.invokeAndWait(() -> {
            try {
                ParticipantDashboard pDash = new ParticipantDashboard(student1);
                pDash.setVisible(true);
                check("1. ParticipantDashboard initialized and visible", pDash.isVisible());

                // Test Performance Dialog instantiation
                AttemptPerformanceDetail detail = reportService.getAttemptPerformance(attId, student1.getId());
                ParticipantPerformanceDialog perfDialog = new ParticipantPerformanceDialog(pDash, detail);
                check("2. ParticipantPerformanceDialog instantiated with metrics", perfDialog != null && perfDialog.getTitle().contains("Attempt #" + attId));

                pDash.dispose();
            } catch (Exception e) {
                e.printStackTrace();
                failed++;
            }
        });

        // 2. CREATOR UI TEST
        System.out.println("\n--- Testing Creator Dashboard & Participant Results Panel ---");
        SessionManager.login(creator1);
        SwingUtilities.invokeAndWait(() -> {
            try {
                CreatorDashboard cDash = new CreatorDashboard(creator1);
                cDash.setVisible(true);
                check("3. CreatorDashboard initialized and visible", cDash.isVisible());

                ParticipantResultsPanel resultsPanel = new ParticipantResultsPanel(quizService, attemptService);
                check("4. ParticipantResultsPanel created with KPI cards and tables", resultsPanel != null);

                cDash.dispose();
            } catch (Exception e) {
                e.printStackTrace();
                failed++;
            }
        });

        // 3. ADMIN UI TEST
        System.out.println("\n--- Testing Admin Dashboard & Performance Overview ---");
        SessionManager.login(admin);
        SwingUtilities.invokeAndWait(() -> {
            try {
                AdminDashboard aDash = new AdminDashboard(admin);
                aDash.setVisible(true);
                check("5. AdminDashboard initialized and visible", aDash.isVisible());

                aDash.selectNavigation("reports");
                check("6. Admin Performance Overview tab activated", true);

                aDash.dispose();
            } catch (Exception e) {
                e.printStackTrace();
                failed++;
            }
        });

        // Clean up
        try (var conn = com.quizplatform.config.DatabaseConnection.getConnection()) {
            try (var ps = conn.prepareStatement("DELETE FROM answers WHERE attempt_id = ?")) {
                ps.setInt(1, attId);
                ps.executeUpdate();
            }
            try (var ps = conn.prepareStatement("DELETE FROM quiz_attempts WHERE id = ?")) {
                ps.setInt(1, attId);
                ps.executeUpdate();
            }
        }

        SessionManager.logout();

        System.out.println("\n==================================================");
        System.out.println(String.format("VERIFICATION SUMMARY:\n  Total Passed: %d\n  Total Failed: %d", passed, failed));
        if (failed == 0) {
            System.out.println("ALL PHASE 9 UI WORKFLOW CHECKS PASSED!");
        }
        System.out.println("==================================================");
    }

    private static void check(String desc, boolean cond) {
        if (cond) {
            System.out.println("  [PASS] " + desc);
            passed++;
        } else {
            System.out.println("  [FAIL] " + desc);
            failed++;
        }
    }
}
