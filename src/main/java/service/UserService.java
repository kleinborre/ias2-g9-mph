package service;

import dao.UserDAO;
import daoimpl.UserDAOImpl;
import pojo.User;
import util.PasswordUtil;
import util.AuditLogger;

import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.util.List;

public class UserService {

    private UserDAO userDAO;

    public UserService() {
        userDAO = new UserDAOImpl();
    }

    public User getUserByUserID(String userID) {
        try {
            return userDAO.getUserByUserID(userID);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving user by userID", e);
        }
    }

    public User getUserByEmail(String email) {
        try {
            return userDAO.getUserByEmail(email);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving user by email", e);
        }
    }

    public User getUserByUsername(String username) {
        try {
            return userDAO.getUserByUsername(username);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving user by username", e);
        }
    }

    public List<User> getAllUsers() {
        try {
            return userDAO.getAllUsers();
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all users", e);
        }
    }

    public void addUser(User user) {
        try {
            String pwd = user.getPassword();
            if (!PasswordUtil.isHash(pwd)) {
                user.setPassword(PasswordUtil.hash(pwd));
            }
            userDAO.addUser(user);
            AuditLogger.log(user.getUserID(), "USER_CREATED", "role=" + user.getUserRole(), null);
        } catch (SQLException e) {
            throw new RuntimeException("Error adding user", e);
        }
    }

    public void updateUser(User user) {
        try {
            String pwd = user.getPassword();
            if (!PasswordUtil.isHash(pwd)) {
                user.setPassword(PasswordUtil.hash(pwd));
            }
            userDAO.updateUser(user);
            AuditLogger.log(user.getUserID(), "USER_UPDATED", "role=" + user.getUserRole(), null);
        } catch (SQLException e) {
            if (e instanceof SQLNonTransientConnectionException
                || e.getMessage().toLowerCase().contains("connection is closed")) {
                try {
                    userDAO = new UserDAOImpl();
                    userDAO.updateUser(user);
                    AuditLogger.log(user.getUserID(), "USER_UPDATED", "retry after reconnect", null);
                    return;
                } catch (SQLException ex2) {
                    throw new RuntimeException("Error updating user after reconnect", ex2);
                }
            }
            throw new RuntimeException("Error updating user", e);
        }
    }

    public void deleteUser(String userID) {
        try {
            userDAO.deleteUser(userID);
            AuditLogger.log(userID, "USER_DELETED", null, null);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }
}