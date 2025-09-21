package service;

import db.DatabaseConnection;
import pojo.User;
import util.AuditLogger;
import util.PasswordUtil;

import java.sql.*;
import java.time.LocalDateTime;

public class LoginService {

    // policy knobs
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    public User login(String userInput, String plaintextPassword) throws SQLException {
        // 1) Load account by userID or email (never filter by password in SQL)
        String sql =
            "SELECT a.userID, a.passwordHash, a.accountStatus, a.failed_attempts, a.locked_until, " +
            "       ur.role, e.email, e.positionID, CONCAT(e.firstName,' ',e.lastName) AS username " +
            "  FROM authentication a " +
            "  JOIN userrole ur ON a.roleID=ur.roleID " +
            "  LEFT JOIN employee e ON e.userID=a.userID " +
            " WHERE a.userID=? OR e.email=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userInput);
            ps.setString(2, userInput);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    AuditLogger.log(null, "LOGIN_FAILED", "no such user: " + userInput, null);
                    return null;
                }

                String userId       = rs.getString("userID");
                String hash         = rs.getString("passwordHash");
                String status       = rs.getString("accountStatus");
                int failed          = rs.getInt("failed_attempts");
                Timestamp lockedTs  = rs.getTimestamp("locked_until");
                String role         = rs.getString("role");
                String email        = rs.getString("email");
                int positionId      = rs.getInt("positionID");
                String username     = rs.getString("username");

                // 2) If locked, block until lock expires
                if (lockedTs != null && lockedTs.toInstant().isAfter(java.time.Instant.now())) {
                    AuditLogger.log(userId, "LOGIN_LOCKED", "locked_until=" + lockedTs.toString(), null);
                    return null;
                }

                // 3) Verify password using bcrypt
                boolean ok = PasswordUtil.verify(plaintextPassword, hash);

                if (!ok) {
                    // increment failure, lock if threshold reached
                    int newFailed = failed + 1;
                    Timestamp newLocked = null;
                    if (newFailed >= MAX_FAILED_ATTEMPTS) {
                        LocalDateTime until = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
                        newLocked = Timestamp.valueOf(until);
                        AuditLogger.log(userId, "ACCOUNT_LOCKED",
                                "failed_attempts=" + newFailed + ", locked_until=" + until, null);
                    } else {
                        AuditLogger.log(userId, "LOGIN_FAILED",
                                "failed_attempts=" + newFailed, null);
                    }

                    String up =
                            "UPDATE authentication SET failed_attempts=?, locked_until=? WHERE userID=?";
                    try (PreparedStatement upd = conn.prepareStatement(up)) {
                        upd.setInt(1, newFailed);
                        if (newLocked != null) {
                            upd.setTimestamp(2, newLocked);
                        } else {
                            upd.setNull(2, Types.TIMESTAMP);
                        }
                        upd.setString(3, userId);
                        upd.executeUpdate();
                    }
                    return null;
                }

                // 4) Success: reset counters and return user
                String reset = "UPDATE authentication SET failed_attempts=0, locked_until=NULL WHERE userID=?";
                try (PreparedStatement upd = conn.prepareStatement(reset)) {
                    upd.setString(1, userId);
                    upd.executeUpdate();
                }
                AuditLogger.log(userId, "LOGIN_SUCCESS", null, null);

                User u = new User();
                u.setUserID(userId);
                u.setPassword(hash); // still stored in object; avoid exposing this to UI
                u.setAccountStatus(status);
                u.setUserRole(role);
                u.setEmail(email);
                u.setPositionID(positionId);
                u.setUsername(username);
                return u;
            }
        }
    }

    public int getEmployeeIDByUserID(String userID) throws SQLException {
        String sql = "SELECT employeeID FROM employee WHERE userID=?";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("employeeID") : -1;
            }
        }
    }

    public boolean doesUserExist(String userInput) throws SQLException {
        String sql =
          "SELECT 1 FROM authentication a " +
          " LEFT JOIN employee e ON e.userID=a.userID " +
          " WHERE a.userID=? OR e.email=?";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userInput);
            ps.setString(2, userInput);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}