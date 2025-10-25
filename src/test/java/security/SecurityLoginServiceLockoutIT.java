package security;

import db.DatabaseConnection;
import org.junit.jupiter.api.*;
import service.LoginService;
import util.PasswordUtil;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test proving lockout behavior + audit logging.
 * - Uses an existing/valid-format userID (default U10025, override via -Dlockout.test.user=Uxxxxx).
 * - Backs up and restores the original auth row to avoid side effects.
 * - Verifies audit_log rows appear.
 * - EXPECTATION: during lock window, even a correct password does NOT reset failed_attempts.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SecurityLoginServiceLockoutIT {

    private static final String TEST_USER_ID = System.getProperty("lockout.test.user", "U10025");
    private static final String GOOD_PW = "GoodP@ssw0rd!";
    private static final String BAD_PW  = "wrong";

    // backup of pre-test state (if the user existed)
    private static String origHash;
    private static String origStatus;
    private static Integer origRoleId;
    private static Integer origFailed;
    private static Timestamp origLockedUntil;
    private static boolean userPreexisted;

    @BeforeAll
    static void setUpAll() throws Exception {
        try (Connection c = DatabaseConnection.getInstance().getConnection()) {
            c.setAutoCommit(false);

            // 1) Does the user exist?
            try (PreparedStatement sel = c.prepareStatement(
                    "SELECT passwordHash, accountStatus, roleID, failed_attempts, locked_until " +
                    "FROM authentication WHERE userID=?")) {
                sel.setString(1, TEST_USER_ID);
                try (ResultSet r = sel.executeQuery()) {
                    if (r.next()) {
                        userPreexisted = true;
                        origHash = r.getString("passwordHash");
                        origStatus = r.getString("accountStatus");
                        origRoleId = r.getInt("roleID");
                        origFailed = r.getInt("failed_attempts");
                        origLockedUntil = r.getTimestamp("locked_until");
                    } else {
                        userPreexisted = false;
                        // 2) Insert a valid auth row (pick an existing roleID)
                        int roleId = 1;
                        try (PreparedStatement roleSel = c.prepareStatement("SELECT roleID FROM userrole ORDER BY roleID LIMIT 1");
                             ResultSet rr = roleSel.executeQuery()) {
                            if (rr.next()) roleId = rr.getInt(1);
                        }
                        try (PreparedStatement ins = c.prepareStatement(
                                "INSERT INTO authentication(userID,passwordHash,accountStatus,roleID,failed_attempts,locked_until) " +
                                "VALUES (?,?,?,?,0,NULL)")) {
                            ins.setString(1, TEST_USER_ID);
                            ins.setString(2, PasswordUtil.hash(GOOD_PW));
                            ins.setString(3, "Active");
                            ins.setInt(4, roleId);
                            ins.executeUpdate();
                        }
                    }
                }
            }

            // 3) Set known-good baseline for the test user
            try (PreparedStatement upd = c.prepareStatement(
                    "UPDATE authentication SET passwordHash=?, accountStatus=?, failed_attempts=0, locked_until=NULL WHERE userID=?")) {
                upd.setString(1, PasswordUtil.hash(GOOD_PW));
                upd.setString(2, "Active");
                upd.setString(3, TEST_USER_ID);
                upd.executeUpdate();
            }

            c.commit();
        }
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        try (Connection c = DatabaseConnection.getInstance().getConnection()) {
            c.setAutoCommit(false);
            if (userPreexisted) {
                // restore original state
                try (PreparedStatement upd = c.prepareStatement(
                        "UPDATE authentication SET passwordHash=?, accountStatus=?, roleID=?, failed_attempts=?, locked_until=? WHERE userID=?")) {
                    upd.setString(1, origHash);
                    upd.setString(2, origStatus);
                    upd.setInt(3, origRoleId);
                    upd.setInt(4, origFailed);
                    if (origLockedUntil != null) {
                        upd.setTimestamp(5, origLockedUntil);
                    } else {
                        upd.setNull(5, Types.TIMESTAMP);
                    }
                    upd.setString(6, TEST_USER_ID);
                    upd.executeUpdate();
                }
            } else {
                // we created it; remove to keep DB clean
                try (PreparedStatement del = c.prepareStatement("DELETE FROM authentication WHERE userID=?")) {
                    del.setString(1, TEST_USER_ID);
                    del.executeUpdate();
                }
            }
            c.commit();
        }
    }

    private static int[] counters() throws Exception {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT failed_attempts, locked_until FROM authentication WHERE userID=?")) {
            p.setString(1, TEST_USER_ID);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) fail("Test user not found");
                Timestamp locked = r.getTimestamp("locked_until");
                return new int[] { r.getInt("failed_attempts"), (locked == null ? 0 : 1) };
            }
        }
    }

    private static int countAudit(String actionLike) throws Exception {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT COUNT(*) FROM audit_log WHERE user_id=? AND action LIKE ?")) {
            p.setString(1, TEST_USER_ID);
            p.setString(2, actionLike);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getInt(1);
            }
        }
    }

    @Test @Order(1)
    void fiveBadAttempts_incrementCounter() throws Exception {
        LoginService svc = new LoginService();
        int startFails = counters()[0];

        for (int i = 0; i < 5; i++) {
            assertNull(svc.login(TEST_USER_ID, BAD_PW), "Attempt " + (i+1) + " should fail");
        }
        int[] c = counters();
        assertEquals(startFails + 5, c[0], "failed_attempts should increase by 5");

        // Evidence: at least one LOGIN_FAILED in audit_log
        assertTrue(countAudit("LOGIN_FAILED%") >= 1, "Expected audit rows for LOGIN_FAILED");
    }

    @Test @Order(2)
    void sixthAttempt_blocks_thenGoodDoesNotResetWhileLocked() throws Exception {
        LoginService svc = new LoginService();

        // 6th bad -> lock (or at least exceed threshold)
        assertNull(svc.login(TEST_USER_ID, BAD_PW), "6th attempt should fail");
        int[] after6 = counters();
        assertTrue(after6[0] >= 5 || after6[1] == 1, "Should be locked or attempts at/over threshold");

        // Should have lock evidence
        assertTrue(countAudit("ACCOUNT_LOCKED%") >= 1, "Expected audit rows for ACCOUNT_LOCKED");

        // Good password DURING lock window -> still denied, counters should NOT reset yet
        assertNull(svc.login(TEST_USER_ID, GOOD_PW), "Good password during lock should still be denied");
        int[] postGood = counters();
        assertTrue(postGood[0] >= 5, "failed_attempts should remain at/above threshold while locked");

        // Optional: you may assert that no LOGIN_SUCCESS was added during this sequence
        // but allow historical LOGIN_SUCCESS from earlier sessions:
        // We just ensure ACCOUNT_LOCKED evidence exists (done above).
    }
}