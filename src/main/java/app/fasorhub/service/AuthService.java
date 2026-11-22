package app.fasorhub.service;

import app.fasorhub.dao.UserDao;
import app.fasorhub.exception.ValidationException;
import app.fasorhub.model.Role;
import app.fasorhub.model.User;
import app.fasorhub.util.Db;
import app.fasorhub.util.Passwords;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Optional;

public final class AuthService {

    private AuthService() {
    }

    public static User register(String name, String email, String password, String identity, String phone)
            throws ValidationException, SQLException {

        if (isBlank(name) || isBlank(email) || isBlank(password) || isBlank(identity) || isBlank(phone)) {
            throw new ValidationException("p23");
        }

        try (Connection conn = Db.get()) {
            if (UserDao.findByEmail(conn, email.trim()).isPresent()) {
                throw new ValidationException("p22");
            }

            long userId;
            try {
                userId = UserDao.insert(conn, name.trim(), email.trim(), Passwords.hash(password),
                        Role.BORROWER, identity.trim(), phone.trim());
            } catch (SQLIntegrityConstraintViolationException e) {
                throw new ValidationException("p22");
            }

            return UserDao.findById(conn, userId)
                    .map(User::withoutPassword)
                    .orElseThrow(() -> new SQLException("Failed to retrieve created user"));
        }
    }

    public static User login(String email, String password) throws ValidationException, SQLException {
        if (isBlank(email) || isBlank(password)) {
            throw new ValidationException("p21");
        }

        try (Connection conn = Db.get()) {
            Optional<User> found = UserDao.findByEmail(conn, email.trim());

            if (found.isEmpty() || !Passwords.verify(password, found.get().passwordHash())) {
                throw new ValidationException("p21");
            }

            return found.get().withoutPassword();
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
