package app.fasorhub.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

// The scheduled job must survive a failed round
class AppInitializerTest {
    @Test
    void failedRoundDoesNotStopTheNextRounds() throws Exception {
        AtomicInteger rounds = new AtomicInteger();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> AppInitializer.runJobs(() -> {
            if (rounds.incrementAndGet() == 1) {
                throw new IllegalStateException("simulated failure");
            }

            return 0;
        }), 0, 50, TimeUnit.MILLISECONDS);

        long deadline = System.currentTimeMillis() + 3000;

        while (rounds.get() < 3 && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }

        scheduler.shutdownNow();

        assertTrue(rounds.get() >= 3, "rounds after the failure: " + rounds.get());
    }
}
