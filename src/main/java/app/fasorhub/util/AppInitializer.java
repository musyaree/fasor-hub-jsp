package app.fasorhub.util;
import app.fasorhub.service.BookingService;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

// Prepares the database when the application starts
@WebListener
public class AppInitializer implements ServletContextListener {
    private static final Logger LOG = Logger.getLogger(AppInitializer.class.getName());

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        event.getServletContext().setAttribute("assetVersion", System.currentTimeMillis());
        Db.init();
        createSchema();

        if ("true".equalsIgnoreCase(System.getenv("SEED_ON_START")) && Seeder.seedIfEmpty()) {
            LOG.info("Seeded development data");
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "fasorhub-jobs");
            thread.setDaemon(true);

            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> runJobs(), 0, 1, TimeUnit.HOURS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        Db.close();
    }

    static void runJobs() {
        runJobs(BookingService::expireAllOverdue);
    }

    // A failed round is logged and the next hour tries again
    static void runJobs(IntSupplier work) {
        try {
            int handled = work.getAsInt();

            LOG.info("Overdue bookings handled: " + handled);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Scheduled job failed", e);
        }
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
