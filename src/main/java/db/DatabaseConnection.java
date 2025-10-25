package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Centralized DB connection with optional TLS toggle.
 * Defaults remain the same for local dev, but you can enable TLS in prod by:
 *  -Ddb.ssl=true (or env DB_SSL=true)
 * and optionally override URL/USER/PASS via env or system props:
 *  DB_URL / DB_USER / DB_PASS  (or -Ddb.url / -Ddb.user / -Ddb.pass)
 */
public class DatabaseConnection {

    // Dev defaults (unchanged)
    private static final String DEV_URL      = "jdbc:mysql://localhost:3306/payrollsystem_db";
    private static final String DEV_USER     = "root";
    private static final String DEV_PASSWORD = "";

    private static DatabaseConnection instance;

    private DatabaseConnection() { }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) instance = new DatabaseConnection();
        return instance;
    }

    public Connection getConnection() {
        try {
            String url  = firstNonNull(
                    sysProp("db.url"),
                    env("DB_URL"),
                    DEV_URL
            );

            String user = firstNonNull(
                    sysProp("db.user"),
                    env("DB_USER"),
                    DEV_USER
            );

            String pass = firstNonNull(
                    sysProp("db.pass"),
                    env("DB_PASS"),
                    DEV_PASSWORD
            );

            boolean useSsl = parseBoolean(firstNonNull(
                    sysProp("db.ssl"),
                    env("DB_SSL"),
                    "false"
            ));

            // If SSL requested, append safe flags (works with MySQL 8+/Connector/J 9+)
            if (useSsl && !url.contains("useSSL=")) {
                String sep = url.contains("?") ? "&" : "?";
                url = url + sep + "useSSL=true&requireSSL=true&verifyServerCertificate=true";
            }

            // Add common connection options to reduce warnings/timezone issues
            if (!url.contains("serverTimezone=")) {
                String sep = url.contains("?") ? "&" : "?";
                url = url + sep + "serverTimezone=UTC";
            }
            if (!url.contains("characterEncoding=")) {
                String sep = url.contains("?") ? "&" : "?";
                url = url + sep + "characterEncoding=UTF-8";
            }

            return DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            throw new RuntimeException("Error establishing database connection", e);
        }
    }

    // helpers
    private static String env(String k)     { return System.getenv(k); }
    private static String sysProp(String k) { return System.getProperty(k); }

    private static String firstNonNull(String a, String b, String c) {
        return a != null ? a : (b != null ? b : c);
    }

    private static boolean parseBoolean(String v) {
        return Objects.equals(v, "true") || Objects.equals(v, "TRUE") || Objects.equals(v, "1");
    }
}