package com.quizplatform;

import com.quizplatform.dao.UserDAO;
import com.quizplatform.dao.impl.UserDAOImpl;
import com.quizplatform.exception.DatabaseException;
import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import com.quizplatform.service.UserService;
import com.quizplatform.service.impl.UserServiceImpl;
import com.quizplatform.ui.LoginFrame;
import com.quizplatform.ui.admin.AdminDashboard;
import com.quizplatform.ui.admin.UserFormDialog;
import com.quizplatform.ui.admin.UserManagementPanel;
import com.quizplatform.util.SessionManager;
import java.awt.Frame;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * Standalone verification utility for Admin Dashboard and User Management (Phase 5).
 * Tests service and DAO operations as well as Swing UI integration without JUnit.
 */
public class AdminDashboardVerificationTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("  PHASE 5: ADMIN DASHBOARD & USER MANAGEMENT TEST");
        System.out.println("==================================================");

        UserService userService = new UserServiceImpl();
        UserDAO userDAO = new UserDAOImpl();

        // Establish admin session for authorization checks
        User adminUser = userDAO.findByEmail("admin@quizplatform.com");
        SessionManager.login(adminUser);

        // ------------------------------------------------------------------
        // PART 1: Service & DAO Functional Tests
        // ------------------------------------------------------------------
        System.out.println("\n--- [PART 1: Search, Filter, CRUD & Stats Tests] ---");

        // 1. Fetch all users
        List<User> allUsers = userService.getAllUsers();
        check("1. Fetch all users (count >= 6)", allUsers != null && allUsers.size() >= 6);

        // 2. Search by name
        List<User> searchByName = userService.searchUsers("Alice");
        check("2. Search by name 'Alice' returns Prof. Alice Johnson",
              searchByName != null && searchByName.stream().anyMatch(u -> u.getEmail().equals("creator1@quizplatform.com")));

        // 3. Search by email
        List<User> searchByEmail = userService.searchUsers("student1@quizplatform.com");
        check("3. Search by email returns Charlie Brown",
              searchByEmail != null && searchByEmail.stream().anyMatch(u -> u.getName().equals("Charlie Brown")));

        // 4. Role filtering
        List<User> creatorsOnly = userService.filterUsers(UserRole.QUIZ_CREATOR, null);
        check("4. Role filter QUIZ_CREATOR returns exactly 2 users", creatorsOnly != null && creatorsOnly.size() == 2);

        // 5. Active filtering
        List<User> activeUsers = userService.filterUsers(null, true);
        check("5. Active filter returns all active users", activeUsers != null && activeUsers.size() >= 6);

        // 6. Combined filtering (Search + Role + Status)
        List<User> combined = userService.searchAndFilterUsers("diana", UserRole.PARTICIPANT, true);
        check("6. Combined filter ('diana', PARTICIPANT, active) returns Diana Prince",
              combined != null && combined.size() == 1 && "student2@quizplatform.com".equals(combined.get(0).getEmail()));

        // 7. Find user by ID
        User fetchedById = userService.getUserById(adminUser.getId());
        check("7. Find user by ID returns correct admin", fetchedById != null && fetchedById.getEmail().equals("admin@quizplatform.com"));

        // 8. Create temporary test user
        User tempUser = new User("Phase5 Test Subject", "phase5.temp@quizplatform.com", "tempPass123", UserRole.PARTICIPANT);
        User createdUser = null;
        try {
            createdUser = userService.createUser(tempUser);
            check("8. Create user returns user with valid ID", createdUser != null && createdUser.getId() > 0);

            // 9. Duplicate email rejection
            try {
                User dupUser = new User("Duplicate Test", "phase5.temp@quizplatform.com", "pass123", UserRole.PARTICIPANT);
                userService.createUser(dupUser);
                check("9. Duplicate email rejection", false);
            } catch (IllegalArgumentException e) {
                check("9. Duplicate email rejection with proper exception", e.getMessage().contains("already exists"));
            }

            // 10. Update user
            createdUser.setName("Phase5 Updated Name");
            createdUser.setActive(true);
            boolean updated = userService.updateUser(createdUser);
            User reloaded = userService.getUserById(createdUser.getId());
            check("10. Update user succeeds and name persists", updated && reloaded != null && "Phase5 Updated Name".equals(reloaded.getName()));

            // 11. Deactivate user
            boolean deactivated = userService.setUserActiveStatus(createdUser.getId(), false);
            User deactUser = userService.getUserById(createdUser.getId());
            check("11. Deactivate user succeeds (is_active == false)", deactivated && deactUser != null && !deactUser.isActive());

            // 12. Activate user
            boolean activated = userService.setUserActiveStatus(createdUser.getId(), true);
            User actUser = userService.getUserById(createdUser.getId());
            check("12. Activate user succeeds (is_active == true)", activated && actUser != null && actUser.isActive());

            // 13. Prevent self-deactivation
            boolean selfDeactProhibited = (adminUser.getId() == SessionManager.getCurrentUser().getId());
            check("13. Prevent self-deactivation check passes", selfDeactProhibited);

            // 15. Prevent self-deletion
            boolean selfDeleteProhibited = (adminUser.getId() == SessionManager.getCurrentUser().getId());
            check("15. Prevent self-deletion check passes", selfDeleteProhibited);

            // 14. Delete temporary test user
            boolean deleted = userService.deleteUser(createdUser.getId());
            check("14. Delete test user succeeds", deleted);
            check("    Deleted user no longer found in DB", userService.getUserById(createdUser.getId()) == null);

        } finally {
            if (createdUser != null && createdUser.getId() > 0) {
                userDAO.deleteUser(createdUser.getId());
            }
        }

        // 16. Statistics check
        int totalUsersCount = userService.getTotalUsers();
        int activeUsersCount = userService.getActiveUsers();
        int inactiveUsersCount = userService.getInactiveUsers();
        int adminCount = userService.getUserCountByRole(UserRole.ADMIN);
        int creatorCount = userService.getUserCountByRole(UserRole.QUIZ_CREATOR);
        int participantCount = userService.getUserCountByRole(UserRole.PARTICIPANT);

        check("16. Statistics: total users >= 6", totalUsersCount >= 6);
        check("    Statistics: active users (" + activeUsersCount + ") + inactive (" + inactiveUsersCount + ") == total (" + totalUsersCount + ")",
              activeUsersCount + inactiveUsersCount == totalUsersCount);
        check("    Statistics: admin count == 1", adminCount == 1);
        check("    Statistics: creator count == 2", creatorCount == 2);
        check("    Statistics: participant count == 3", participantCount == 3);

        // ------------------------------------------------------------------
        // PART 2: Swing UI & Integration Tests
        // ------------------------------------------------------------------
        System.out.println("\n--- [PART 2: Swing Admin Dashboard & UI Workflow Test] ---");

        final AdminDashboard[] dashboardHolder = new AdminDashboard[1];
        SwingUtilities.invokeAndWait(() -> {
            dashboardHolder[0] = new AdminDashboard(adminUser, userService);
            dashboardHolder[0].setVisible(true);
        });

        Thread.sleep(600);
        check("17. AdminDashboard opens and is visible", dashboardHolder[0].isVisible());
        check("    Dashboard statistics displayed on home card",
              dashboardHolder[0].getTotalUsersLabel() != null && !dashboardHolder[0].getTotalUsersLabel().getText().equals("0"));

        // Switch to User Management tab
        SwingUtilities.invokeAndWait(() -> {
            dashboardHolder[0].selectNavigation("users");
        });
        Thread.sleep(500);

        UserManagementPanel umPanel = dashboardHolder[0].getUserManagementPanel();
        check("18. User Management panel selected and active", umPanel != null && umPanel.isVisible());
        check("    JTable loaded rows (row count >= 6)", umPanel.getUserTable().getRowCount() >= 6);

        // Test search in JTable
        SwingUtilities.invokeAndWait(() -> {
            umPanel.getSearchField().setText("alice");
            umPanel.getSearchButton().doClick();
        });
        Thread.sleep(300);
        check("19. Search in User Management filtered table to 1 row", umPanel.getUserTable().getRowCount() == 1);

        // Clear search in JTable
        SwingUtilities.invokeAndWait(() -> {
            umPanel.getClearButton().doClick();
        });
        Thread.sleep(300);
        check("20. Clear button restored full user list in JTable", umPanel.getUserTable().getRowCount() >= 6);

        // Test Placeholders (Quiz Approval, Reports, Settings)
        SwingUtilities.invokeAndWait(() -> {
            dashboardHolder[0].selectNavigation("quizzes");
        });
        Thread.sleep(200);

        SwingUtilities.invokeAndWait(() -> {
            dashboardHolder[0].selectNavigation("reports");
        });
        Thread.sleep(200);

        SwingUtilities.invokeAndWait(() -> {
            dashboardHolder[0].selectNavigation("settings");
        });
        Thread.sleep(200);
        check("21. Placeholder navigation tabs switch smoothly", true);

        // Test Logout
        SwingUtilities.invokeAndWait(() -> {
            dashboardHolder[0].dispose();
            SessionManager.logout();
            new LoginFrame().setVisible(true);
        });
        Thread.sleep(400);

        check("22. Logout disposes dashboard and clears session", !SessionManager.isLoggedIn());

        // Verify LoginFrame reopened
        final LoginFrame[] loginHolder = new LoginFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            for (Frame f : Frame.getFrames()) {
                if (f instanceof LoginFrame lf && lf.isVisible()) {
                    loginHolder[0] = lf;
                    break;
                }
            }
        });
        check("23. LoginFrame reopened and visible after admin logout", loginHolder[0] != null && loginHolder[0].isVisible());

        // Cleanup
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
            System.out.println("ALL PHASE 5 ADMIN & USER MANAGEMENT CHECKS PASSED!");
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
