package app.fasorhub.dao;

import app.fasorhub.model.Role;
import app.fasorhub.model.User;
import app.fasorhub.util.Jdbc;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

// Reads and writes the users table
public final class UserDao {
    private static final String COLUMNS = "id, name, email, password, role, identity, phone";

    private UserDao() {
    }

    static User map(ResultSet row) throws SQLException {
        return new User(row.getLong("id"), row.getString("name"), row.getString("email"), row.getString("password"),
                Role.of(row.getString("role")), row.getString("identity"), row.getString("phone"));
    }

    public static Optional<User> findByEmail(Connection c, String email) throws SQLException {
        return Jdbc.queryOne(c, "SELECT " + COLUMNS + " FROM users WHERE email = ?", UserDao::map, email);
    }

    public static Optional<User> findById(Connection c, long id) throws SQLException {
        return Jdbc.queryOne(c, "SELECT " + COLUMNS + " FROM users WHERE id = ?", UserDao::map, id);
    }

    public static long insert(Connection c, String name, String email, String passwordHash, Role role, String identity, String phone)
            throws SQLException {
        return Jdbc.insert(c, "INSERT INTO users (name, email, password, role, identity, phone) VALUES (?, ?, ?, ?, ?, ?)",
                name, email, passwordHash, role.code(), identity, phone);
    }

    public static long count(Connection c) throws SQLException {
        return Jdbc.queryOne(c, "SELECT COUNT(*) FROM users", r -> r.getLong(1)).orElse(0L);
    }
}
