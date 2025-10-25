package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import pojo.User;
import service.LoginService;

class HRLoginServiceTest {

    @Test
    void validCredentialsReturnUser() throws Exception {
        LoginService svc = new LoginService();
        User user = svc.login("avillanueva@motor.ph", "Villanueva@10006");
        assertNotNull(user);
        assertEquals("U10006", user.getUserID());
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
        User user = svc.login("U10006", "Villanueva@10006");
        assertNotNull(user);
        assertEquals("U10006", user.getUserID());
    }
}