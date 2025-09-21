package util;

import db.DatabaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class AuditLogger {

    private static final Logger LOG = LogManager.getLogger(AuditLogger.class);

    private AuditLogger() { }

    public static void log(String userId, String action, String details, String ipAddr) {
        // DB log
        String sql = "INSERT INTO audit_log (user_id, action, details, ip_addr) VALUES (?,?,?,?)";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, action);
            ps.setString(3, details);
            ps.setString(4, ipAddr);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Do not throw further; never break the business flow on audit failure
            LOG.warn("audit_log insert failed: {}", e.toString());
        }

        // File log
        if (userId == null) userId = "-";
        LOG.info("[audit] user={} action={} details={} ip={}", userId, action, details, ipAddr);
    }
}