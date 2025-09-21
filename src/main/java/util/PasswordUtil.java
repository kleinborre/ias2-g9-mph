package util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {
    // cost 10 is a practical baseline for desktop-grade machines
    private static final int COST = 10;

    private PasswordUtil() { }

    public static String hash(String plaintext) {
        if (plaintext == null) throw new IllegalArgumentException("password is null");
        return BCrypt.hashpw(plaintext, BCrypt.gensalt(COST));
    }

    public static boolean isHash(String value) {
        if (value == null) return false;
        // Basic check for $2a$/$2b$/$2y$ prefix and length ~60
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    public static boolean verify(String plaintext, String bcryptHash) {
        if (plaintext == null || bcryptHash == null) return false;
        try {
            return BCrypt.checkpw(plaintext, bcryptHash);
        } catch (IllegalArgumentException e) {
            // malformed hash
            return false;
        }
    }
}