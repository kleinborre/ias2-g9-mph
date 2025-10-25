package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import pojo.User;
import service.LoginService;

class ManagerLoginServiceTest {

    @Test
    void validCredentialsReturnUser() throws Exception {
        LoginService svc = new LoginService();
        User user = svc.login("mgarcia@motor.ph", "Garcia@10001");
        assertNotNull(user);
        assertEquals("U10001", user.getUserID());
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
        User user = svc.login("U10001", "Garcia@10001");
        assertNotNull(user);
        assertEquals("U10001", user.getUserID());
    }
}