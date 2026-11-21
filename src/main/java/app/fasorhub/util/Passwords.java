package app.fasorhub.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

// Passwords are stored only as strong one way hashes
public final class Passwords {
    private Passwords() {
    }

    public static String hash(String password) {
        return BCrypt.withDefaults().hashToString(10, password.toCharArray());
    }
}
