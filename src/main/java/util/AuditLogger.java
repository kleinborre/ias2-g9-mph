package util;

import db.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Writes audit entries to both DB (audit_log) and file (log4j2).
 * Now null/size-safe and schema-tolerant with a fallback INSERT.
 */
public final class AuditLogger {

    private static final Logger LOG = LogManager.getLogger(AuditLogger.class);

    // DB column max sizes we must respect to avoid “Data too long …”
    private static final int MAX_USERID   = 10;   // audit_log.user_id VARCHAR(10)
    private static final int MAX_ACTION   = 64;   // audit_log.action  VARCHAR(64)
    private static final int MAX_IP_ADDR  = 64;   // audit_log.ip_addr VARCHAR(64)
    // details is TEXT, no strict cap, but we’ll bound to keep logs tidy
    private static final int MAX_DETAILS  = 4000;

    private AuditLogger() { }

    public static void log(String userId, String action, String details, String ipAddr) {
        // ---- Normalize values to avoid DB constraint errors ----
        final String uid = safe(userId, "-", MAX_USERID);
        final String act = safe(action, "-", MAX_ACTION);
        final String det = safe(details, null, MAX_DETAILS); // TEXT can be NULL
        final String ip  = safe(ipAddr, null, MAX_IP_ADDR);  // can be NULL

        // ---- Try primary INSERT (explicit ts) ----
        final String sqlWithTs =
                "INSERT INTO audit_log (ts, user_id, action, details, ip_addr) " +
                "VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?)";

        boolean inserted = tryInsert(sqlWithTs, uid, act, det, ip, true);

        // ---- Fallback: rely on DB default for ts (if exists) ----
        if (!inserted) {
            final String sqlNoTs =
                    "INSERT INTO audit_log (user_id, action, details, ip_addr) " +
                    "VALUES (?, ?, ?, ?)";
            inserted = tryInsert(sqlNoTs, uid, act, det, ip, false);
        }

        // ---- Always write file log (even if DB insert failed) ----
        LOG.info("[audit] user={} action={} details={} ip={}", uid, act, det, ip);
    }

    private static boolean tryInsert(String sql, String uid, String act, String det, String ip, boolean withTs) {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            // Be explicit about autocommit for maximum portability
            try {
                c.setAutoCommit(true);
            } catch (SQLException ignored) {
                // some drivers disallow toggling; safe to ignore
            }

            int i = 1;
            ps.setString(i++, uid);
            ps.setString(i++, act);
            if (withTs) {
                // when withTs = true, parameters are: 1=user_id, 2=action, 3=details, 4=ip_addr
                ps.setString(i++, det);
                ps.setString(i  , ip);
            } else {
                // when withTs = false, parameters are: 1=user_id, 2=action, 3=details, 4=ip_addr
                ps.setString(i++, det);
                ps.setString(i  , ip);
            }

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            // We WARN with the exact SQL shape and key fields to aid troubleshooting
            LOG.warn("audit_log insert failed (withTs={}): userId={}, action={}, err={}",
                    withTs, uid, act, e.toString());
            return false;
        }
    }

    /** Safe truncation + defaulting helper. */
    private static String safe(String v, String def, int maxLen) {
        String s = (v == null || v.isBlank()) ? def : v;
        if (s == null) return null; // allow NULL if def is null
        return (s.length() > maxLen) ? s.substring(0, maxLen) : s;
    }
}