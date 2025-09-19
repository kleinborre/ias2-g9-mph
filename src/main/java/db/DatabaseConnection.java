package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/payrollsystem_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private static DatabaseConnection instance;

    private DatabaseConnection() {
        // no longer holds a persistent Connection
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /** 
     * Returns a brand-new Connection each time. 
     * Closing it in your DAO / service code won't break anybody else. 
     */
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Error establishing database connection", e);
        }
    }
}