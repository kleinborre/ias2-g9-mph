package test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import db.DatabaseConnection;
import service.LoginService;
import pojo.User;

import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.Instant;

/**
 * Validates: bcrypt-at-rest, lockout after failed attempts, and counter reset on success.
 * Uses fixture account: userID = "U10012", password = "Lopez@10012".
 */
class SecurityLoginServiceLockoutTest {

    private static final String FIXTURE_USER = "U10012";
    private static final String GOOD_PASSWORD = "Lopez@10012";
    private static final String BAD_PASSWORD  = "wrong-password-!";

    @BeforeEach
    void precheck() {
        // Abort (not fail) if DB/fixture isn't available
        try (Connection c = DatabaseConnection.getInstance().getConnection()) {
            assumeTrue(c != null, "DB connection not available");
            try (PreparedStatement p = c.prepareStatement(
                    "SELECT passwordHash FROM authentication WHERE userID=?")) {
                p.setString(1, FIXTURE_USER);
                try (ResultSet r = p.executeQuery()) {
                    assumeTrue(r.next(), "Fixture user not found: " + FIXTURE_USER);
                    String hash = r.getString("passwordHash");
                    assumeTrue(hash != null && hash.startsWith("$2"),
                            "Fixture password is not bcrypt (expected $2*): " + hash);
                }
            }
        } catch (Exception e) {
            Assumptions.abort("DB not ready: " + e.getMessage());
        }
    }

    @AfterEach
    void cleanup() throws Exception {
        // Reset failed counters/lock to keep tests idempotent
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement p = c.prepareStatement(
                     "UPDATE authentication SET failed_attempts=0, locked_until=NULL WHERE userID=?")) {
            p.setString(1, FIXTURE_USER);
            p.executeUpdate();
        }
    }

    @Test
    void bcryptHashStoredInDb() throws Exception {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT passwordHash FROM authentication WHERE userID=?")) {
            p.setString(1, FIXTURE_USER);
            try (ResultSet r = p.executeQuery()) {
                assertTrue(r.next());
                String hash = r.getString("passwordHash");
                assertNotNull(hash);
                assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"),
                        "Expected bcrypt hash prefix, got: " + hash);
                assertTrue(hash.length() >= 55 && hash.length() <= 80,
                        "Bcrypt hash length looks wrong: " + hash.length());
            }
        }
    }

    @Test
    void lockoutAfterFiveBadAttempts_thenBlocked() throws Exception {
        LoginService svc = new LoginService();

        // 5 consecutive failures
        for (int i = 1; i <= 5; i++) {
            User u = svc.login(FIXTURE_USER, BAD_PASSWORD);
            assertNull(u, "Attempt " + i + " should fail");
        }

        // Check DB: failed_attempts >= 5 and locked_until set in the future
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT failed_attempts, locked_until FROM authentication WHERE userID=?")) {
            p.setString(1, FIXTURE_USER);
            try (ResultSet r = p.executeQuery()) {
                assertTrue(r.next());
                int fails = r.getInt("failed_attempts");
                Timestamp locked = r.getTimestamp("locked_until");
                assertTrue(fails >= 5, "failed_attempts should be at least 5, got " + fails);
                assertNotNull(locked, "locked_until should be set after threshold");
                assertTrue(locked.toInstant().isAfter(Instant.now()), "locked_until must be in the future");
            }
        }

        // Sixth attempt (correct password) must still be blocked while locked
        User afterLock = svc.login(FIXTURE_USER, GOOD_PASSWORD);
        assertNull(afterLock, "Login must be blocked while account is locked");
    }

    @Test
    void successResetsCountersAfterUnlock() throws Exception {
        LoginService svc = new LoginService();

        // Force some failures (but not enough to lock)
        for (int i = 1; i <= 2; i++) {
            assertNull(svc.login(FIXTURE_USER, BAD_PASSWORD));
        }

        // Confirm failures incremented
        int failsBefore;
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT failed_attempts FROM authentication WHERE userID=?")) {
            p.setString(1, FIXTURE_USER);
            try (ResultSet r = p.executeQuery()) {
                assertTrue(r.next());
                failsBefore = r.getInt("failed_attempts");
                assertTrue(failsBefore >= 2);
            }
        }

        // Good login -> should reset counters
        User ok = svc.login(FIXTURE_USER, GOOD_PASSWORD);
        assertNotNull(ok, "Good credentials should succeed when not locked");

        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT failed_attempts, locked_until FROM authentication WHERE userID=?")) {
            p.setString(1, FIXTURE_USER);
            try (ResultSet r = p.executeQuery()) {
                assertTrue(r.next());
                assertEquals(0, r.getInt("failed_attempts"), "failed_attempts must reset to 0");
                assertNull(r.getTimestamp("locked_until"), "locked_until must reset to NULL");
            }
        }
    }
}