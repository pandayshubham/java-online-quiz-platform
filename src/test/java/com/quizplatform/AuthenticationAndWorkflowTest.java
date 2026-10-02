package com.quizplatform;

import com.quizplatform.dao.UserDAO;
import com.quizplatform.dao.impl.UserDAOImpl;
import com.quizplatform.exception.AuthenticationException;
import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import com.quizplatform.service.AuthenticationService;
import com.quizplatform.service.impl.AuthenticationServiceImpl;
import com.quizplatform.ui.LoginFrame;
import com.quizplatform.ui.admin.AdminDashboard;
import com.quizplatform.ui.creator.CreatorDashboard;
import com.quizplatform.ui.participant.ParticipantDashboard;
import com.quizplatform.util.SessionManager;
import java.awt.Frame;
import javax.swing.SwingUtilities;

/**
 * Standalone verification utility for Authentication, Session Management,
 * and the complete Swing UI Login/Logout workflow.
 */
public class AuthenticationAndWorkflowTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("  PHASE 4: AUTHENTICATION & WORKFLOW VERIFICATION");
        System.out.println("==================================================");

        AuthenticationService authService = new AuthenticationServiceImpl();
        UserDAO userDAO = new UserDAOImpl();

        // ------------------------------------------------------------------
        // PART 1: Session & Authentication Unit Verification
        // ------------------------------------------------------------------
        System.out.println("\n--- [PART 1: Authentication & Session Tests] ---");

        // 1. Session before login
        SessionManager.logout();
        check("1. SessionManager.isLoggedIn() before login is false", !SessionManager.isLoggedIn());
        check("   SessionManager.getCurrentUser() is null", SessionManager.getCurrentUser() == null);

        // 2. Valid ADMIN Login
        User adminUser = authService.authenticate("admin@quizplatform.com", "admin123");
        check("2. Valid ADMIN authentication succeeds", adminUser != null && adminUser.getRole() == UserRole.ADMIN);
        check("   Admin password sanitized (not in User object)", adminUser.getPassword() == null);

        SessionManager.login(adminUser);
        check("   SessionManager.isLoggedIn() after ADMIN login is true", SessionManager.isLoggedIn());
        check("   SessionManager.hasRole(ADMIN) is true", SessionManager.hasRole(UserRole.ADMIN));
        check("   SessionManager.getCurrentUser() has admin email", "admin@quizplatform.com".equals(SessionManager.getCurrentUser().getEmail()));

        // 3. Logout check
        SessionManager.logout();
        check("3. SessionManager.logout() resets session state", !SessionManager.isLoggedIn() && SessionManager.getCurrentUser() == null);

        // 4. Valid QUIZ_CREATOR Login
        User creatorUser = authService.authenticate("creator1@quizplatform.com", "creator123");
        check("4. Valid QUIZ_CREATOR authentication succeeds", creatorUser != null && creatorUser.getRole() == UserRole.QUIZ_CREATOR);

        // 5. Valid PARTICIPANT Login
        User studentUser = authService.authenticate("student1@quizplatform.com", "student123");
        check("5. Valid PARTICIPANT authentication succeeds", studentUser != null && studentUser.getRole() == UserRole.PARTICIPANT);

        // 6. Wrong Email
        try {
            authService.authenticate("nonexistent@domain.com", "anyPassword");
            check("6. Wrong email rejected", false);
        } catch (AuthenticationException e) {
            check("6. Wrong email rejected with friendly message", "Invalid email or password.".equals(e.getMessage()));
        }

        // 7. Wrong Password
        try {
            authService.authenticate("admin@quizplatform.com", "wrongPassword123");
            check("7. Wrong password rejected", false);
        } catch (AuthenticationException e) {
            check("7. Wrong password rejected with friendly message", "Invalid email or password.".equals(e.getMessage()));
        }

        // 8. Empty Email
        try {
            authService.authenticate("", "password123");
            check("8. Empty email rejected", false);
        } catch (AuthenticationException e) {
            check("8. Empty email rejected with validation message", "Email cannot be empty.".equals(e.getMessage()));
        }

        // 9. Empty Password
        try {
            authService.authenticate("admin@quizplatform.com", "");
            check("9. Empty password rejected", false);
        } catch (AuthenticationException e) {
            check("9. Empty password rejected with validation message", "Password cannot be empty.".equals(e.getMessage()));
        }

        // 10. Invalid Email Format
        try {
            authService.authenticate("invalid-email-format", "password123");
            check("10. Invalid email format rejected", false);
        } catch (AuthenticationException e) {
            check("10. Invalid email format rejected with validation message", "Invalid email format.".equals(e.getMessage()));
        }

        // 11. Inactive User Verification
        int tempInactiveUserId = 0;
        try {
            User inactiveUser = new User("Inactive Tester", "inactive.test@quizplatform.com", "secret123", UserRole.PARTICIPANT);
            inactiveUser.setActive(false);
            tempInactiveUserId = userDAO.createUser(inactiveUser);

            authService.authenticate("inactive.test@quizplatform.com", "secret123");
            check("11. Inactive account rejected", false);
        } catch (AuthenticationException e) {
            check("11. Inactive account rejected with clear notice", e.getMessage().contains("deactivated"));
        } finally {
            if (tempInactiveUserId > 0) {
                userDAO.deleteUser(tempInactiveUserId);
            }
        }

        // ------------------------------------------------------------------
        // PART 2: Swing UI Workflow Verification (EDT)
        // ------------------------------------------------------------------
        System.out.println("\n--- [PART 2: Swing Login & Navigation Workflow Test] ---");

        final LoginFrame[] frameHolder = new LoginFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            frameHolder[0] = new LoginFrame(authService);
            frameHolder[0].setVisible(true);
        });

        Thread.sleep(500);
        check("12. LoginFrame opens and is visible", frameHolder[0].isVisible());
        check("    LoginFrame title is correct", "Java Online Quiz Platform - Login".equals(frameHolder[0].getTitle()));

        // Simulate filling invalid email
        SwingUtilities.invokeAndWait(() -> {
            frameHolder[0].getEmailField().setText("bad-email");
            frameHolder[0].getPasswordField().setText("123");
            frameHolder[0].performLogin();
        });
        check("13. LoginFrame displays error on invalid email", "Invalid email format.".equals(frameHolder[0].getStatusLabel().getText()));

        // Simulate filling correct Admin credentials
        SwingUtilities.invokeAndWait(() -> {
            frameHolder[0].getEmailField().setText("admin@quizplatform.com");
            frameHolder[0].getPasswordField().setText("admin123");
            frameHolder[0].performLogin();
        });

        Thread.sleep(500);
        check("14. LoginFrame disposed after valid login", !frameHolder[0].isVisible());
        check("    Session established for Admin", SessionManager.isLoggedIn() && SessionManager.hasRole(UserRole.ADMIN));

        // Find open AdminDashboard
        final AdminDashboard[] adminDashboardHolder = new AdminDashboard[1];
        SwingUtilities.invokeAndWait(() -> {
            for (Frame f : Frame.getFrames()) {
                if (f instanceof AdminDashboard ad && ad.isVisible()) {
                    adminDashboardHolder[0] = ad;
                    break;
                }
            }
        });
        check("15. AdminDashboard successfully opened and visible", adminDashboardHolder[0] != null && adminDashboardHolder[0].isVisible());

        // Simulate Logout from AdminDashboard
        SwingUtilities.invokeAndWait(() -> {
            if (adminDashboardHolder[0] != null) {
                adminDashboardHolder[0].dispose();
                SessionManager.logout();
                new LoginFrame(authService).setVisible(true);
            }
        });

        Thread.sleep(500);
        check("16. Session terminated on Logout", !SessionManager.isLoggedIn());

        // Verify LoginFrame reopened
        final LoginFrame[] reopenedLoginHolder = new LoginFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            for (Frame f : Frame.getFrames()) {
                if (f instanceof LoginFrame lf && lf.isVisible()) {
                    reopenedLoginHolder[0] = lf;
                    break;
                }
            }
        });
        check("17. LoginFrame reopened and visible after logout", reopenedLoginHolder[0] != null && reopenedLoginHolder[0].isVisible());

        // Cleanup open frames
        SwingUtilities.invokeAndWait(() -> {
            for (Frame f : Frame.getFrames()) {
                f.dispose();
            }
        });

        System.out.println("\n==================================================");
        System.out.println("VERIFICATION SUMMARY:");
        System.out.println("  Total Passed: " + testsPassed);
        System.out.println("  Total Failed: " + testsFailed);
        if (testsFailed == 0) {
            System.out.println("ALL PHASE 4 AUTHENTICATION & WORKFLOW CHECKS PASSED!");
        } else {
            System.err.println("SOME TESTS FAILED!");
            System.exit(1);
        }
        System.out.println("==================================================");
        System.exit(0);
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
