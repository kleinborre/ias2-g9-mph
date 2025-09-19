package service;

import dao.UserDAO;
import daoimpl.UserDAOImpl;
import pojo.User;

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
            userDAO.addUser(user);
        } catch (SQLException e) {
            throw new RuntimeException("Error adding user", e);
        }
    }

    public void updateUser(User user) {
        try {
            userDAO.updateUser(user);
        } catch (SQLException e) {
            // if it's a closed connection, retry once
            if (e instanceof SQLNonTransientConnectionException
                || e.getMessage().toLowerCase().contains("connection is closed")) {
                try {
                    userDAO = new UserDAOImpl();
                    userDAO.updateUser(user);
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
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }
}