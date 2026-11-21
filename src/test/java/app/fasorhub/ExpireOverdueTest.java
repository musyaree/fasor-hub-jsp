package app.fasorhub;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fasorhub.model.BookingStatus;
import app.fasorhub.model.SlotStatus;
import app.fasorhub.service.BookingService;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Jdbc;
import app.fasorhub.util.Messages;
import app.fasorhub.util.Tx;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Automatic rejection and cancellation of bookings whose date has passed
class ExpireOverdueTest {
    @BeforeEach
    void setUp() {
        TestDb.fresh(true);
    }

    @AfterEach
    void resetClock() {
        Clock.fixToday(null);
    }

    private long booking(String email, String purpose) {
        return Tx.run(c -> Jdbc.queryOne(c, "SELECT b.id FROM bookings b JOIN users u ON u.id = b.user_id WHERE u.email = ? AND b.purpose = ?",
                r -> r.getLong(1), email, purpose).orElseThrow());
    }

    private long overduePendingCancellation() {
        return Tx.run(c -> Jdbc.queryOne(c, "SELECT b.id FROM bookings b WHERE b.status = '4' AND EXISTS (SELECT 1 FROM booking_time_slot bts "
                + "JOIN time_slots ts ON ts.id = bts.time_slot_id WHERE bts.booking_id = b.id AND ts.`date` < CURDATE())", r -> r.getLong(1)).orElseThrow());
    }

    private String field(long bookingId, String column) {
        return Tx.run(c -> Jdbc.queryOne(c, "SELECT " + column + " FROM bookings WHERE id = ?", r -> r.getString(1), bookingId).orElseThrow());
    }

    private List<SlotStatus> slotStatuses(long bookingId) {
        return Tx.run(c -> Jdbc.query(c, "SELECT ts.status FROM time_slots ts JOIN booking_time_slot bts ON bts.time_slot_id = ts.id "
                + "WHERE bts.booking_id = ? ORDER BY ts.start_time", r -> SlotStatus.of(r.getString(1)), bookingId));
    }

    @Test
    void kon01OverduePendingBookingIsRejectedWithP26() {
        long id = booking("bob@example.com", "Lecturer futsal practice");

        BookingService.expireAllOverdue();

        assertEquals(BookingStatus.REJECTED.code(), field(id, "status"));
        assertEquals(Messages.get("p26"), field(id, "rejection_reason"));
    }

    @Test
    void kon02OverdueCancellationOfApprovedBookingReleasesItsSlot() {
        long id = overduePendingCancellation();
        assertEquals(List.of(SlotStatus.OCCUPIED), slotStatuses(id));

        BookingService.expireAllOverdue();

        assertEquals(BookingStatus.CANCELLED.code(), field(id, "status"));
        assertEquals(List.of(SlotStatus.AVAILABLE), slotStatuses(id));
    }

    @Test
    void kon03OverdueCancellationOfPendingBookingChangesNoSlot() {
        long id = booking("bob@example.com", "Lecturer futsal practice");
        Tx.runVoid(c -> {
            Jdbc.update(c, "UPDATE bookings SET status = '4', previous_status = '1' WHERE id = ?", id);
            return null;
        });
        long occupiedBefore = TestDb.count("SELECT COUNT(*) FROM time_slots WHERE status = '2'");

        BookingService.expireOverdue(id);

        assertEquals(BookingStatus.CANCELLED.code(), field(id, "status"));
        assertEquals(occupiedBefore, TestDb.count("SELECT COUNT(*) FROM time_slots WHERE status = '2'"));
        assertEquals(List.of(SlotStatus.AVAILABLE), slotStatuses(id));
    }

    @Test
    void kon04ApprovedBookingInThePastIsLeftAlone() {
        long id = booking("alice@example.com", "Regular futsal practice");

        BookingService.expireAllOverdue();

        assertEquals(BookingStatus.APPROVED.code(), field(id, "status"));
        assertEquals(List.of(SlotStatus.OCCUPIED), slotStatuses(id));
    }

    @Test
    void kon05RunningTwiceGivesTheSameResult() {
        assertEquals(2, BookingService.expireAllOverdue());
        assertEquals(0, BookingService.expireAllOverdue());
    }

    @Test
    void kon05TwoRunsAtTheSameTimeChangeEachBookingOnce() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Integer>> runs = List.of(pool.submit(() -> run(go)), pool.submit(() -> run(go)));
        go.countDown();

        int changed = 0;

        for (Future<Integer> run : runs) {
            changed += run.get();
        }

        pool.shutdown();

        assertEquals(2, changed);
        assertEquals(0, TestDb.count("SELECT COUNT(*) FROM bookings b WHERE b.status IN ('1', '4') AND EXISTS (SELECT 1 FROM booking_time_slot bts "
                + "JOIN time_slots ts ON ts.id = bts.time_slot_id WHERE bts.booking_id = b.id AND ts.`date` < CURDATE())"));
    }

    private int run(CountDownLatch go) throws InterruptedException {
        go.await();

        return BookingService.expireAllOverdue();
    }

    @Test
    void kon07BookingIsOverdueOnlyAfterMidnightOfItsDate() {
        long id = booking("alice@example.com", "Futsal practice");
        LocalDate bookingDate = Clock.today().plusDays(4);

        Clock.fixToday(bookingDate);
        BookingService.expireAllOverdue();
        assertEquals(BookingStatus.PENDING_VERIFICATION.code(), field(id, "status"));

        Clock.fixToday(bookingDate.plusDays(1));
        BookingService.expireAllOverdue();
        assertEquals(BookingStatus.REJECTED.code(), field(id, "status"));
    }
}
