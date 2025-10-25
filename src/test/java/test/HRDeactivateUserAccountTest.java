package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import service.UserService;
import pojo.User;
import util.SessionManager;

public class HRDeactivateUserAccountTest {

    private UserService userService;
    private static final String TARGET_USER_ID = "U10034";
    private static final String HR_USER_ID = "U10006"; // HR's user ID
    private static final int HR_EMPLOYEE_ID = 10006;

    @BeforeEach
    void setUp() {
        // Set session as HR user
        SessionManager.setSession(HR_USER_ID, HR_EMPLOYEE_ID);

        userService = new UserService();
    }

    @Test
    void testDeactivateUserAccount() {
        // 1. Ensure user is Active before test
        User user = userService.getUserByUserID(TARGET_USER_ID);
        assertNotNull(user, "User with ID " + TARGET_USER_ID + " should exist in the system.");
        user.setAccountStatus("Active");
        userService.updateUser(user);

        // 2. Deactivate user as HR
        user.setAccountStatus("Deactivated");
        userService.updateUser(user);

        // 3. Fetch again and check status
        User updated = userService.getUserByUserID(TARGET_USER_ID);
        assertNotNull(updated, "User record should still exist after deactivation.");
        assertEquals("Deactivated", updated.getAccountStatus(), "Account status should be 'Deactivated' after update.");

        // 4. Optionally, verify session is still HR
        assertEquals(HR_USER_ID, SessionManager.getUserID(), "Current session user should be HR");
    }

    @AfterEach
    void tearDown() {
        // Optional: reset status back to Active for further testing
        User user = userService.getUserByUserID(TARGET_USER_ID);
        if (user != null && !"Active".equals(user.getAccountStatus())) {
            user.setAccountStatus("Active");
            userService.updateUser(user);
        }
        SessionManager.clearSession();
    }
}
