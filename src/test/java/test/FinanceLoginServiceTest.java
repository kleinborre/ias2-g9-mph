package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import pojo.User;
import service.LoginService;

class FinanceLoginServiceTest {

    @Test
    void validCredentialsReturnUser() throws Exception {
        LoginService svc = new LoginService();
        User user = svc.login("asalcedo@motor.ph", "Salcedo@10011");
        assertNotNull(user);
        assertEquals("U10011", user.getUserID());
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
        User user = svc.login("U10011", "Salcedo@10011");
        assertNotNull(user);
        assertEquals("U10011", user.getUserID());
    }
}