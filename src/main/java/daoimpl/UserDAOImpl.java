package daoimpl;

import dao.UserDAO;
import db.DatabaseConnection;
import pojo.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    public UserDAOImpl() {}

    @Override
    public User getUserByUserID(String userID) throws SQLException {
        String sql =
          "SELECT a.userID, CONCAT(e.firstName,' ',e.lastName) AS username," +
          "       e.email, a.passwordHash, a.accountStatus, ur.role, e.positionID " +
          "  FROM authentication a " +
          "  JOIN userrole ur ON a.roleID = ur.roleID " +
          "  JOIN employee   e ON a.userID = e.userID " +
          " WHERE a.userID = ?";
        try ( Connection conn = DatabaseConnection.getInstance().getConnection();
              PreparedStatement ps = conn.prepareStatement(sql) )
        {
            ps.setString(1, userID);
            try ( ResultSet rs = ps.executeQuery() ) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    @Override
    public User getUserByEmail(String email) throws SQLException {
        String sql =
          "SELECT a.userID, CONCAT(e.firstName,' ',e.lastName) AS username," +
          "       e.email, a.passwordHash, a.accountStatus, ur.role, e.positionID " +
          "  FROM authentication a " +
          "  JOIN userrole ur ON a.roleID = ur.roleID " +
          "  JOIN employee   e ON a.userID = e.userID " +
          " WHERE e.email = ?";
        try ( Connection conn = DatabaseConnection.getInstance().getConnection();
              PreparedStatement ps = conn.prepareStatement(sql) )
        {
            ps.setString(1, email);
            try ( ResultSet rs = ps.executeQuery() ) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    @Override
    public User getUserByUsername(String username) throws SQLException {
        String sql =
          "SELECT a.userID, CONCAT(e.firstName,' ',e.lastName) AS username," +
          "       e.email, a.passwordHash, a.accountStatus, ur.role, e.positionID " +
          "  FROM authentication a " +
          "  JOIN userrole ur ON a.roleID = ur.roleID " +
          "  JOIN employee   e ON a.userID = e.userID " +
          " WHERE CONCAT(e.firstName,' ',e.lastName) = ?";
        try ( Connection conn = DatabaseConnection.getInstance().getConnection();
              PreparedStatement ps = conn.prepareStatement(sql) )
        {
            ps.setString(1, username);
            try ( ResultSet rs = ps.executeQuery() ) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql =
          "SELECT a.userID, CONCAT(e.firstName,' ',e.lastName) AS username," +
          "       e.email, a.passwordHash, a.accountStatus, ur.role, e.positionID " +
          "  FROM authentication a " +
          "  JOIN userrole ur ON a.roleID = ur.roleID " +
          "  JOIN employee   e ON a.userID = e.userID";
        try ( Connection conn = DatabaseConnection.getInstance().getConnection();
              PreparedStatement ps = conn.prepareStatement(sql);
              ResultSet rs = ps.executeQuery() )
        {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    @Override
    public void addUser(User user) throws SQLException {
        String authSql =
          "INSERT INTO authentication(userID,passwordHash,roleID,accountStatus) " +
          "VALUES (?, ?, (SELECT roleID FROM userrole WHERE role=?), ?)";
        try ( Connection conn = DatabaseConnection.getInstance().getConnection();
              PreparedStatement ps = conn.prepareStatement(authSql) )
        {
            ps.setString(1, user.getUserID());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getUserRole());
            ps.setString(4, user.getAccountStatus());
            ps.executeUpdate();
        }
        // … then your employee insert as before …
    }

    @Override
    public void updateUser(User user) throws SQLException {
        String authSql =
          "UPDATE authentication a " +
          "  JOIN userrole ur ON a.roleID = ur.roleID " +
          "SET a.passwordHash=?, a.roleID=(SELECT roleID FROM userrole WHERE role=?), a.accountStatus=? " +
          "WHERE a.userID=?";
        try ( Connection conn = DatabaseConnection.getInstance().getConnection();
              PreparedStatement ps = conn.prepareStatement(authSql) )
        {
            ps.setString(1, user.getPassword());
            ps.setString(2, user.getUserRole());
            ps.setString(3, user.getAccountStatus());
            ps.setString(4, user.getUserID());
            ps.executeUpdate();
        }
        // … then your employee update as before …
    }

    @Override
    public void deleteUser(String userID) throws SQLException {
        try ( Connection conn = DatabaseConnection.getInstance().getConnection();
              PreparedStatement ps = conn.prepareStatement("DELETE FROM authentication WHERE userID=?") )
        {
            ps.setString(1, userID);
            ps.executeUpdate();
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserID(       rs.getString("userID"));
        u.setUsername(     rs.getString("username"));
        u.setEmail(        rs.getString("email"));
        u.setPassword(     rs.getString("passwordHash"));
        u.setAccountStatus(rs.getString("accountStatus"));
        u.setUserRole(     rs.getString("role"));
        u.setPositionID(   rs.getInt("positionID"));
        return u;
    }
}