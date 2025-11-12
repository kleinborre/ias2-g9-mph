package daoimpl;

import dao.UserDAO;
import db.DatabaseConnection;
import pojo.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    // --- Reused SQL fragments to avoid duplicated string literals (S1192) ---
    private static final String BASE_USER_SELECT =
        "SELECT a.userID, " +
        "       CONCAT(e.firstName,' ',e.lastName) AS username, " +
        "       e.email, a.accountStatus, ur.role, e.positionID " + // passwordHash intentionally excluded
        "  FROM authentication a " +
        "  JOIN userrole ur ON a.roleID = ur.roleID " +
        "  JOIN employee   e ON a.userID = e.userID ";

    private static final String WHERE_USERID   = " WHERE a.userID = ?";
    private static final String WHERE_EMAIL    = " WHERE e.email = ?";
    private static final String WHERE_FULLNAME = " WHERE CONCAT(e.firstName,' ',e.lastName) = ?";

    // --- ResultSet column labels as constants to reduce duplication (S1192) ---
    private static final String COL_USER_ID       = "userID";
    private static final String COL_USERNAME      = "username";
    private static final String COL_EMAIL         = "email";
    private static final String COL_ACC_STATUS    = "accountStatus";
    private static final String COL_ROLE          = "role";
    private static final String COL_POSITION_ID   = "positionID";

    @Override
    public User getUserByUserID(String userID) throws SQLException {
        final String sql = BASE_USER_SELECT + WHERE_USERID;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    @Override
    public User getUserByEmail(String email) throws SQLException {
        final String sql = BASE_USER_SELECT + WHERE_EMAIL;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    @Override
    public User getUserByUsername(String username) throws SQLException {
        final String sql = BASE_USER_SELECT + WHERE_FULLNAME;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        final List<User> users = new ArrayList<>();
        final String sql = BASE_USER_SELECT; // no WHERE
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    @Override
    public void addUser(User user) throws SQLException {
        final String authSql =
            "INSERT INTO authentication(userID,passwordHash,roleID,accountStatus) " +
            "VALUES (?, ?, (SELECT roleID FROM userrole WHERE role=?), ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(authSql)) {
            ps.setString(1, user.getUserID());
            ps.setString(2, user.getPassword()); // expected to be hashed by service
            ps.setString(3, user.getUserRole());
            ps.setString(4, user.getAccountStatus());
            ps.executeUpdate();
        }
        // employee insert handled elsewhere in your flow
    }

    @Override
    public void updateUser(User user) throws SQLException {
        final String authSql =
            "UPDATE authentication a " +
            "  JOIN userrole ur ON a.roleID = ur.roleID " +
            "SET a.passwordHash=?, a.roleID=(SELECT roleID FROM userrole WHERE role=?), a.accountStatus=? " +
            "WHERE a.userID=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(authSql)) {
            ps.setString(1, user.getPassword()); // expected to be hashed by service
            ps.setString(2, user.getUserRole());
            ps.setString(3, user.getAccountStatus());
            ps.setString(4, user.getUserID());
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteUser(String userID) throws SQLException {
        final String sql = "DELETE FROM authentication WHERE userID=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userID);
            ps.executeUpdate();
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserID(       rs.getString(COL_USER_ID));
        u.setUsername(     rs.getString(COL_USERNAME));
        u.setEmail(        rs.getString(COL_EMAIL));
        // IMPORTANT: do NOT copy passwordHash into the domain object
        u.setPassword(     null);
        u.setAccountStatus(rs.getString(COL_ACC_STATUS));
        u.setUserRole(     rs.getString(COL_ROLE));
        u.setPositionID(   rs.getInt(COL_POSITION_ID));
        return u;
    }
}