package test;

import db.DatabaseConnection;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.sql.SQLException;

class DatabaseConnectionTest {

    @Test
    void testGetInstanceReturnsValidConnection() throws SQLException {
        // Act
        DatabaseConnection dbc = DatabaseConnection.getInstance();
        Connection conn = dbc.getConnection();

        // Assert
        assertNotNull(dbc, "Instance should not be null");
        assertNotNull(conn, "Connection should not be null");
        assertFalse(conn.isClosed(), "Connection should be open");
    }
}
