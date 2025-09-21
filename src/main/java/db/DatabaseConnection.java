package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Development: local, non-SSL
    private static final String URL      = "jdbc:mysql://localhost:3306/payrollsystem_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    // Production (example): enable TLS once server certificates are configured
    // private static final String URL =
    //     "jdbc:mysql://db-host:3306/payrollsystem_db?useSSL=true&requireSSL=true&verifyServerCertificate=true";

    private static DatabaseConnection instance;

    private DatabaseConnection() { }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Error establishing database connection", e);
        }
    }
}