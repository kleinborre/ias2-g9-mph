package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import util.PasswordUtil;

class SecurityPasswordUtilTest {

    @Test
    void hashAndVerifyRoundTrip() {
        String pw = "S0me@Str0ng#Pw";
        String hash = PasswordUtil.hash(pw);
        assertNotNull(hash);
        assertTrue(PasswordUtil.isHash(hash));
        assertTrue(PasswordUtil.verify(pw, hash));
        assertFalse(PasswordUtil.verify("wrong", hash));
    }

    @Test
    void isHashRecognizesBcryptPrefixes() {
        assertTrue(PasswordUtil.isHash("$2a$10$abcdefghijklmnopqrstuvxyzABCDEabc1234567890abcdefghi"));
        assertTrue(PasswordUtil.isHash("$2b$12$abcdefghijklmnopqrstuvxyzABCDEabc1234567890abcdefghi"));
        assertTrue(PasswordUtil.isHash("$2y$08$abcdefghijklmnopqrstuvxyzABCDEabc1234567890abcdefghi"));
        assertFalse(PasswordUtil.isHash(null));
        assertFalse(PasswordUtil.isHash(""));
        assertFalse(PasswordUtil.isHash("not-a-hash"));
    }

    @Test
    void verifyHandlesMalformedHashSafely() {
        assertFalse(PasswordUtil.verify("pw", "totally-invalid-hash"));
        assertFalse(PasswordUtil.verify(null, "$2b$10$abcdefghijklmnopqrstuvxyzABCDEabc1234567890abcdefghi"));
        assertFalse(PasswordUtil.verify("pw", null));
    }
}