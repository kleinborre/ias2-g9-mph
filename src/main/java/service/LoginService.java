package service;

import db.DatabaseConnection;
import pojo.User;

import java.sql.*;

public class LoginService {

    public User login(String userInput, String password) throws SQLException {
        String sql =
          "SELECT a.userID, a.passwordHash, a.accountStatus, ur.role, " +
          "       e.email, e.positionID, " +
          "       CONCAT(e.firstName,' ',e.lastName) AS username " +
          "  FROM authentication a " +
          "  JOIN userrole ur ON a.roleID=ur.roleID " +
          "  LEFT JOIN employee e ON e.userID=a.userID " +
          " WHERE (a.userID=? OR e.email=?) " +
          "   AND a.passwordHash=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userInput);
            ps.setString(2, userInput);
            ps.setString(3, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setUserID(       rs.getString("userID"));
                    u.setPassword(     rs.getString("passwordHash"));
                    u.setAccountStatus(rs.getString("accountStatus"));
                    u.setUserRole(     rs.getString("role"));
                    u.setEmail(        rs.getString("email"));
                    u.setPositionID(   rs.getInt("positionID"));
                    u.setUsername(     rs.getString("username"));
                    return u;
                }
            }
        }
        return null;
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