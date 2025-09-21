package test;

import static org.junit.jupiter.api.Assertions.*;

import dao.UserDAO;
import pojo.User;
import service.UserService;
import util.PasswordUtil;
import util.FieldAccessTest;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

/**
 * Proves UserService hashes plaintext before delegating to DAO.
 * We inject a fake DAO via reflection to capture the User passed down.
 */
class SecurityUserServiceHashingTest {

    static class CapturingUserDAO implements UserDAO {
        volatile User lastAdded;
        volatile User lastUpdated;

        @Override public void addUser(User user) { this.lastAdded = cloneUser(user); }
        @Override public void updateUser(User user) { this.lastUpdated = cloneUser(user); }

        // --- Unused methods for this test ---
        @Override public User getUserByUserID(String userID) throws SQLException { return null; }
        @Override public User getUserByEmail(String email) throws SQLException { return null; }
        @Override public User getUserByUsername(String username) throws SQLException { return null; }
        @Override public java.util.List<User> getAllUsers() throws SQLException { return java.util.Collections.emptyList(); }
        @Override public void deleteUser(String userID) throws SQLException { /* no-op */ }

        private static User cloneUser(User u) {
            if (u == null) return null;
            User c = new User();
            c.setUserID(u.getUserID());
            c.setUsername(u.getUsername());
            c.setEmail(u.getEmail());
            c.setPassword(u.getPassword());
            c.setAccountStatus(u.getAccountStatus());
            c.setUserRole(u.getUserRole());
            c.setPositionID(u.getPositionID());
            return c;
        }
    }

    @Test
    void addUserHashesPasswordBeforeDao() {
        UserService svc = new UserService();
        CapturingUserDAO fake = new CapturingUserDAO();

        // swap out the real DAO with our fake using the provided test utility
        FieldAccessTest.getField(svc, "userDAO", Object.class); // assert field exists
        setPrivateField(svc, "userDAO", fake);

        User u = new User();
        u.setUserID("TESTSEC001");
        u.setUsername("Test Security");
        u.setEmail("tsec@example.com");
        u.setUserRole("EMPLOYEE");
        u.setAccountStatus("Active");
        u.setPassword("Plaintext#123");

        svc.addUser(u);

        assertNotNull(fake.lastAdded, "DAO should receive one user");
        assertTrue(PasswordUtil.isHash(fake.lastAdded.getPassword()),
                "Password must be bcrypt-hashed before hitting DAO");
        assertFalse(PasswordUtil.verify("not-it", fake.lastAdded.getPassword()));
        assertTrue(PasswordUtil.verify("Plaintext#123", fake.lastAdded.getPassword()));
    }

    @Test
    void updateUserHashesPasswordBeforeDao() {
        UserService svc = new UserService();
        CapturingUserDAO fake = new CapturingUserDAO();
        setPrivateField(svc, "userDAO", fake);

        User u = new User();
        u.setUserID("TESTSEC002");
        u.setUserRole("EMPLOYEE");
        u.setAccountStatus("Active");
        u.setPassword("Another$Pw!");

        svc.updateUser(u);

        assertNotNull(fake.lastUpdated, "DAO should receive an updated user");
        assertTrue(PasswordUtil.isHash(fake.lastUpdated.getPassword()),
                "Password must be bcrypt-hashed before hitting DAO");
        assertTrue(PasswordUtil.verify("Another$Pw!", fake.lastUpdated.getPassword()));
    }

    // --- small helper using reflection (kept local to avoid changing main util classes) ---
    private static void setPrivateField(Object target, String field, Object value) {
        try {
            java.lang.reflect.Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            fail("Reflection failed to set field '" + field + "': " + e.getMessage());
        }
    }
}