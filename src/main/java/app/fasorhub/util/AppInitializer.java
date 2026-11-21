package app.fasorhub.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

// Prepares the database when the application starts
@WebListener
public class AppInitializer implements ServletContextListener {
    private static final Logger LOG = Logger.getLogger(AppInitializer.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        Db.init();
        createSchema();

        if ("true".equalsIgnoreCase(System.getenv("SEED_ON_START")) && Seeder.seedIfEmpty()) {
            LOG.info("Seeded development data");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Db.close();
    }

    // Reads the schema file and runs each statement in order
    public static void createSchema() {
        try (InputStream in = AppInitializer.class.getResourceAsStream("/schema.sql")) {
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8).replaceAll("(?m)^--.*$", "");

            try (Connection c = Db.get(); Statement st = c.createStatement()) {
                for (String statement : sql.split(";\\s*\\n")) {
                    if (!statement.isBlank()) {
                        st.execute(statement);
                    }
                }
            }
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Could not create the schema", e);
        }
    }
}
