package app.fasorhub.service;

import app.fasorhub.dao.UserDao;
import app.fasorhub.exception.ValidationException;
import app.fasorhub.model.Role;
import app.fasorhub.model.User;
import app.fasorhub.util.Db;
import app.fasorhub.util.Passwords;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Handles user registration and authentication.
 * <p>
 * Registration is for borrowers only (role = BORROWER).
 * Login uses BCrypt for password verification.
 * Validation errors are thrown as ValidationException with message keys.
 */
public final class AuthService {

    private AuthService() {
    }

    /**
     * Registers a new borrower account.
     *
     * @param name     full name (required)
     * @param email    email address (required, must be unique)
     * @param password password (required)
     * @param identity NRP, NIP, or organization name (required)
     * @param phone    phone number (required)
     * @return the created user (without password hash)
     * @throws ValidationException if validation fails (p22 for duplicate email, p23 for missing fields)
     */
    public static User register(String name, String email, String password, String identity, String phone)
            throws ValidationException, SQLException {

        // Validate required fields
        if (isBlank(name) || isBlank(email) || isBlank(password) || isBlank(identity) || isBlank(phone)) {
            throw new ValidationException("p23");
        }

        try (Connection conn = Db.get()) {
            // Check if email already exists
            Optional<User> existing = UserDao.findByEmail(conn, email.trim());
            if (existing.isPresent()) {
                throw new ValidationException("p22");
            }

            // Hash password and create user
            String passwordHash = Passwords.hash(password);
            long userId = UserDao.insert(conn, name.trim(), email.trim(), passwordHash, Role.BORROWER, identity.trim(), phone.trim());

            // Return user without password
            return UserDao.findById(conn, userId)
                    .map(User::withoutPassword)
                    .orElseThrow(() -> new SQLException("Failed to retrieve created user"));
        }
    }

    /**
     * Authenticates a user by email and password.
     *
     * @param email    email address
     * @param password password
     * @return the authenticated user (without password hash)
     * @throws ValidationException if credentials are invalid (p21)
     */
    public static User login(String email, String password) throws ValidationException, SQLException {
        // Same error message for wrong email or wrong password (security best practice)
        if (isBlank(email) || isBlank(password)) {
            throw new ValidationException("p21");
        }

        try (Connection conn = Db.get()) {
            Optional<User> userOpt = UserDao.findByEmail(conn, email.trim());

            if (userOpt.isEmpty()) {
                // User not found - but don't reveal this
                throw new ValidationException("p21");
            }

            User user = userOpt.get();

            if (!Passwords.verify(password, user.passwordHash())) {
                // Wrong password - same message as user not found
                throw new ValidationException("p21");
            }

            // Return user without password hash for session storage
            return user.withoutPassword();
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
