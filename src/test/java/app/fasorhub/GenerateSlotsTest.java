package app.fasorhub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fasorhub.model.SlotStatus;
import app.fasorhub.service.ScheduleService;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Jdbc;
import app.fasorhub.util.Tx;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Every facility keeps sessions from today through the next 34 days
class GenerateSlotsTest {
    private static final int FACILITIES = 4;
    private static final int DAYS = ScheduleService.SLOT_DAYS;
    private static final int PER_DAY = ScheduleService.SESSIONS.size();

    @BeforeEach
    void setUp() {
        TestDb.fresh(true);
    }

    @AfterEach
    void resetClock() {
        Clock.reset();
    }

    private long count(LocalDate from, LocalDate to) {
        return Tx.run(c -> Jdbc.queryOne(c, "SELECT COUNT(*) FROM time_slots WHERE `date` BETWEEN ? AND ?",
                r -> r.getLong(1), Date.valueOf(from), Date.valueOf(to)).orElse(0L));
    }

    private long total() {
        return Tx.run(c -> Jdbc.queryOne(c, "SELECT COUNT(*) FROM time_slots", r -> r.getLong(1)).orElse(0L));
    }

    private Map<Long, String> statuses() {
        Map<Long, String> map = new HashMap<>();

        Tx.run(c -> Jdbc.query(c, "SELECT id, status FROM time_slots", r -> map.put(r.getLong(1), r.getString(2))));

        return map;
    }

    @Test
    void kon13EveryFacilityHasSessionsForTheWholeHorizon() {
        LocalDate today = Clock.today();
        LocalDate last = today.plusDays(DAYS - 1);

        int created = ScheduleService.generateForAllFacilities();

        // The seeder covers today through 21 days ahead, so the last 13 days are new
        assertEquals(FACILITIES * 13 * PER_DAY, created);
        assertEquals((long) FACILITIES * DAYS * PER_DAY, count(today, last));

        long perDay = Tx.run(c -> Jdbc.queryOne(c, "SELECT COUNT(*) FROM time_slots WHERE facility_id = ? AND `date` = ?",
                r -> r.getLong(1), 1L, Date.valueOf(last)).orElse(0L));

        assertEquals(PER_DAY, perDay);

        long notAvailable = Tx.run(c -> Jdbc.queryOne(c, "SELECT COUNT(*) FROM time_slots WHERE `date` > ? AND status <> '1'",
                r -> r.getLong(1), Date.valueOf(today.plusDays(21))).orElse(0L));

        assertEquals(0, notAvailable);
    }

    @Test
    void kon14RunningTwiceChangesNothing() {
        ScheduleService.generateForAllFacilities();
        long after = total();

        int second = ScheduleService.generateForAllFacilities();

        assertEquals(0, second);
        assertEquals(after, total());
    }

    @Test
    void kon15ExistingSessionsKeepTheirStatus() {
        LocalDate day = Clock.today().plusDays(5);

        // One session closed by a manager, on top of the sessions the seeder already occupied
        Tx.runVoid(c -> {
            Jdbc.update(c, "UPDATE time_slots SET status = '3' WHERE facility_id = 1 AND `date` = ? AND start_time = '08:00:00'", Date.valueOf(day));
            return null;
        });

        Map<Long, String> before = statuses();

        assertTrue(before.containsValue(SlotStatus.CLOSED.code()));
        assertTrue(before.containsValue(SlotStatus.OCCUPIED.code()));

        ScheduleService.generateForAllFacilities();

        Map<Long, String> after = statuses();

        for (Map.Entry<Long, String> entry : before.entrySet()) {
            assertEquals(entry.getValue(), after.get(entry.getKey()), "session " + entry.getKey());
        }
    }

    @Test
    void missingDaysInsideTheHorizonAreFilled() {
        LocalDate day = Clock.today().plusDays(40);

        // A day far ahead has no sessions yet, so move the clock so that it falls inside the horizon
        Clock.fixAt(day.minusDays(DAYS - 1).atTime(8, 0).toInstant(ZoneOffset.ofHours(7)));

        ScheduleService.generateForAllFacilities();

        assertEquals((long) FACILITIES * PER_DAY, count(day, day));
    }

    @Test
    void theHorizonMovesForwardWithTheClock() {
        LocalDate later = Clock.today().plusDays(10);

        Clock.fixAt(later.atTime(8, 0).toInstant(ZoneOffset.ofHours(7)));
        ScheduleService.generateForAllFacilities();

        assertEquals((long) FACILITIES * PER_DAY, count(later.plusDays(DAYS - 1), later.plusDays(DAYS - 1)));
        assertEquals(0, count(later.plusDays(DAYS), later.plusDays(DAYS)));
    }

    @Test
    void overlappingRunsNeverDuplicateASession() throws Exception {
        int runs = 4;
        ExecutorService pool = Executors.newFixedThreadPool(runs);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> results = new ArrayList<>();

        for (int i = 0; i < runs; i++) {
            results.add(pool.submit(() -> {
                start.await();

                return ScheduleService.generateForAllFacilities();
            }));
        }

        start.countDown();

        int created = 0;

        for (Future<Integer> result : results) {
            created += result.get();
        }

        pool.shutdown();

        // Together the runs added exactly the missing sessions, each one once
        assertEquals(FACILITIES * 13 * PER_DAY, created);
        assertEquals((long) FACILITIES * DAYS * PER_DAY, count(Clock.today(), Clock.today().plusDays(DAYS - 1)));

        long duplicates = Tx.run(c -> Jdbc.queryOne(c, "SELECT COUNT(*) FROM (SELECT 1 FROM time_slots GROUP BY facility_id, `date`, start_time "
                + "HAVING COUNT(*) > 1) d", r -> r.getLong(1)).orElse(0L));

        assertEquals(0, duplicates);
    }
}
