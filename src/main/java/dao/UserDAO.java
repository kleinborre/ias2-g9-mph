package dao;

import pojo.User;
import java.sql.SQLException;
import java.util.List;

public interface UserDAO {

    User getUserByUserID(String userID) throws SQLException;

    User getUserByEmail(String email) throws SQLException;

    User getUserByUsername(String username) throws SQLException;

    List<User> getAllUsers() throws SQLException;

    void addUser(User user) throws SQLException;

    void updateUser(User user) throws SQLException;

    void deleteUser(String userID) throws SQLException;
}