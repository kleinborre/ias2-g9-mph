package test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import db.DatabaseConnection;
import util.AuditLogger;

import org.junit.jupiter.api.*;

import java.sql.*;

/**
 * Writes a synthetic audit event and verifies the DB captured it.
 * Cleans up the inserted row afterward.
 */
class SecurityAuditLoggerTest {

    private Long insertedId = null;

    @BeforeEach
    void ensureDb() {
        try (Connection c = DatabaseConnection.getInstance().getConnection()) {
            assumeTrue(c != null, "DB connection not available");
        } catch (Exception e) {
            Assumptions.abort("DB not ready: " + e.getMessage());
        }
    }

    @Test
    void auditRowIsInserted() throws Exception {
        String user = "U10012";
        String action = "TEST_AUDIT_EVENT";
        String details = "unit-test proof";
        String ip = "127.0.0.1";

        // Emit
        AuditLogger.log(user, action, details, ip);

        // Verify (grab the latest matching row)
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT id, user_id, action, details, ip_addr, ts " +
                     "FROM audit_log WHERE user_id=? AND action=? ORDER BY id DESC LIMIT 1")) {
            p.setString(1, user);
            p.setString(2, action);
            try (ResultSet r = p.executeQuery()) {
                assertTrue(r.next(), "Expected one audit row");
                insertedId = r.getLong("id");
                assertEquals(user, r.getString("user_id"));
                assertEquals(action, r.getString("action"));
                assertEquals(details, r.getString("details"));
                assertEquals(ip, r.getString("ip_addr"));
                assertNotNull(r.getTimestamp("ts"));
            }
        }
    }

    @AfterEach
    void cleanup() throws Exception {
        if (insertedId == null) return;
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM audit_log WHERE id=?")) {
            p.setLong(1, insertedId);
            p.executeUpdate();
        }
    }
}