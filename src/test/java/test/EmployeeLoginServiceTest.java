package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import pojo.User;
import service.LoginService;

class EmployeeLoginServiceTest {

    @Test
    void validCredentialsReturnUser() throws Exception {
        LoginService svc = new LoginService();
        User user = svc.login("asanjose@motor.ph", "San Jose@10018");
        assertNotNull(user);
        assertEquals("U10018", user.getUserID());
    }

    @Test
    void invalidCredentialsReturnNull() throws Exception {
        LoginService svc = new LoginService();
        User user = svc.login("invalidUser", "wrongPass");
        assertNull(user);
    }

    @Test
    void validLoginUsingUserIDReturnsUser() throws Exception {
        LoginService svc = new LoginService();
        User user = svc.login("U10018", "San Jose@10018");
        assertNotNull(user);
        assertEquals("U10018", user.getUserID());
    }
}