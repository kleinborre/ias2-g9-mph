package test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dao.UserDAO;
import daoimpl.UserDAOImpl;
import dao.EmployeeDAO;
import daoimpl.EmployeeDAOImpl;
import db.DatabaseConnection;
import pojo.User;
import pojo.Employee;

import org.junit.jupiter.api.*;

import java.sql.Connection;

/**
 * Validates that DAOs do not concatenate SQL and resist basic injection payloads.
 * We don't expect any crash or 'broad match' from injection strings.
 */
class SecurityDAOInjectionResistanceTest {

    @BeforeEach
    void dbAvailable() {
        try (Connection c = DatabaseConnection.getInstance().getConnection()) {
            assumeTrue(c != null, "DB connection not available");
        } catch (Exception e) {
            Assumptions.abort("DB not ready: " + e.getMessage());
        }
    }

    @Test
    void userDaoRejectsClassicSqliInEmail() throws Exception {
        UserDAO dao = new UserDAOImpl();
        String payload = "anything' OR '1'='1";
        User u = dao.getUserByEmail(payload);
        assertNull(u, "Email-based lookup must not match via SQL injection payload");
    }

    @Test
    void userDaoRejectsClassicSqliInUserId() throws Exception {
        UserDAO dao = new UserDAOImpl();
        String payload = "U10012' OR '1'='1";
        User u = dao.getUserByUserID(payload);
        assertNull(u, "UserID lookup must not match via SQL injection payload");
    }

    @Test
    void employeeDaoRejectsClassicSqliInUserId() throws Exception {
        EmployeeDAO edao = new EmployeeDAOImpl();
        String payload = "U10012' OR '1'='1";
        Employee e = edao.getEmployeeByUserID(payload);
        assertNull(e, "Employee lookup by userID must not match via SQL injection payload");
    }
}