package app.fasorhub.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

// Connection pool settings come from environment variables so the same build runs on any machine
public final class Db {
    private static HikariDataSource dataSource;

    private Db() {
    }

    public static synchronized void init() {
        if (dataSource != null) {
            return;
        }

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl(jdbcUrl());

        config.setUsername(env("DB_USER", env("MYSQLUSER", "fasor")));
        config.setPassword(env("DB_PASSWORD", env("MYSQLPASSWORD", "fasor")));

        config.setMaximumPoolSize(Integer.parseInt(env("DB_POOL", "8")));
        config.setConnectionTimeout(15_000);

        dataSource = new HikariDataSource(config);
    }

    public static synchronized void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    public static Connection get() throws SQLException {
        if (dataSource == null) {
            init();
        }

        return dataSource.getConnection();
    }

    // A full url wins otherwise the url is built from host port and database name
    private static String jdbcUrl() {
        String full = System.getenv("DB_URL");
        if (full != null && !full.isBlank()) {
            return full;
        }

        String host = env("DB_HOST", env("MYSQLHOST", "127.0.0.1"));
        String port = env("DB_PORT", env("MYSQLPORT", "3308"));
        String name = env("DB_NAME", env("MYSQLDATABASE", "fasor_hub"));

        return "jdbc:mysql://" + host + ":" + port + "/" + name
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta"
                + "&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci";
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }

        return value == null || value.isBlank() ? fallback : value;
    }
}
