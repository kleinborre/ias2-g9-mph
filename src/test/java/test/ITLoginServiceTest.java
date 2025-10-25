package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import pojo.User;
import service.LoginService;

class ITLoginServiceTest {

    @Test
    void validCredentialsReturnUser() throws Exception {
        LoginService svc = new LoginService();
        User user = svc.login("ehernandez@motor.ph", "Hernandez@10005");
        assertNotNull(user);
        assertEquals("U10005", user.getUserID());
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
        User user = svc.login("U10005", "Hernandez@10005");
        assertNotNull(user);
        assertEquals("U10005", user.getUserID());
    }
}