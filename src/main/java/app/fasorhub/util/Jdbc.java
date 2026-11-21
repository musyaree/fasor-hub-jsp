package app.fasorhub.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Small helpers so data access classes stay short and every value is bound as a parameter
public final class Jdbc {
    private Jdbc() {
    }

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet row) throws SQLException;
    }

    public static <T> List<T> query(Connection c, String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                List<T> rows = new ArrayList<>();

                while (rs.next()) {
                    rows.add(mapper.map(rs));
                }

                return rows;
            }
        }
    }

    public static <T> Optional<T> queryOne(Connection c, String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        return query(c, sql, mapper, params).stream().findFirst();
    }

    public static int update(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    public static long insert(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0L;
            }
        }
    }

    public static String marks(int count) {
        return String.join(",", java.util.Collections.nCopies(count, "?"));
    }

    private static void bind(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
