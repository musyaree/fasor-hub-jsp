package app.fasorhub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.fasorhub.dao.BookingDao;
import app.fasorhub.dao.SlotDao;
import app.fasorhub.exception.BookingStatusChangedException;
import app.fasorhub.exception.SlotUnavailableException;
import app.fasorhub.model.Booking;
import app.fasorhub.model.BookingStatus;
import app.fasorhub.model.SlotStatus;
import app.fasorhub.service.BookingService;
import app.fasorhub.util.Jdbc;
import app.fasorhub.util.Tx;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VerificationServiceTest {
    @BeforeEach
    void setUp() {
        TestDb.fresh(true);
    }

    private long booking(String email, String purpose) {
        return Tx.run(c -> Jdbc.queryOne(c, "SELECT b.id FROM bookings b JOIN users u ON u.id = b.user_id WHERE u.email = ? AND b.purpose = ?",
                r -> r.getLong(1), email, purpose).orElseThrow());
    }

    private long p3() { return booking("pengguna@contoh.id", "Futsal practice"); }

    private long p5() { return booking("budi@contoh.id", "Lecturer sports activity"); }

    private long p6() { return booking("budi@contoh.id", "Badminton practice"); }

    private long p12() { return booking("budi@contoh.id", "Lecturer futsal practice"); }

    private Booking fresh(long id) {
        return Tx.run(c -> BookingDao.find(c, id).orElseThrow());
    }

    private List<SlotStatus> slotStatuses(long bookingId) {
        return Tx.run(c -> Jdbc.query(c, "SELECT ts.status FROM time_slots ts JOIN booking_time_slot bts ON bts.time_slot_id = ts.id "
                + "WHERE bts.booking_id = ? ORDER BY ts.start_time", r -> SlotStatus.of(r.getString(1)), bookingId));
    }

    private void forceStatus(long bookingId, String status, String previous) {
        Tx.runVoid(c -> {
            Jdbc.update(c, "UPDATE bookings SET status = ?, previous_status = ? WHERE id = ?", status, previous, bookingId);
            return null;
        });
    }

    @Test
    void approveOccupiesAllSlotsOfTheBooking() {
        long id = p6();
        BookingService.approve(id);
        assertEquals(BookingStatus.APPROVED, fresh(id).status());
        assertEquals(List.of(SlotStatus.OCCUPIED, SlotStatus.OCCUPIED), slotStatuses(id));
    }

    @Test
    void secondBookingForTheSameSlotIsBlocked() {
        BookingService.approve(p3());
        SlotUnavailableException e = assertThrows(SlotUnavailableException.class, () -> BookingService.approve(p5()));
        assertEquals("p15", e.getMessageKey());
        assertEquals(BookingStatus.PENDING_VERIFICATION, fresh(p5()).status());
        assertEquals(List.of(SlotStatus.OCCUPIED), slotStatuses(p5()));
    }

    @Test
    void oneClosedSlotBlocksTheWholeBooking() {
        long id = p6();
        Tx.runVoid(c -> {
            long first = Jdbc.queryOne(c, "SELECT t.id FROM time_slots t JOIN booking_time_slot bts ON bts.time_slot_id = t.id "
                    + "WHERE bts.booking_id = ? ORDER BY t.start_time LIMIT 1", r -> r.getLong(1), id).orElseThrow();
            Jdbc.update(c, "UPDATE time_slots SET status = '3' WHERE id = ?", first);
            return null;
        });
        assertEquals("p15", assertThrows(SlotUnavailableException.class, () -> BookingService.approve(id)).getMessageKey());
        assertEquals(BookingStatus.PENDING_VERIFICATION, fresh(id).status());
        assertEquals(List.of(SlotStatus.CLOSED, SlotStatus.AVAILABLE), slotStatuses(id), "The other slot must not be locked.");
    }

    @Test
    void slotInThePastCannotBeApproved() {
        long id = p12();
        assertEquals("p15", assertThrows(SlotUnavailableException.class, () -> BookingService.approve(id)).getMessageKey());
        assertEquals(BookingStatus.PENDING_VERIFICATION, fresh(id).status());
        assertEquals(List.of(SlotStatus.AVAILABLE), slotStatuses(id));
    }

    @Test
    void bookingThatChangedStatusIsNotApproved() {
        long id = p3();
        forceStatus(id, "4", "1");
        assertEquals("p30", assertThrows(BookingStatusChangedException.class, () -> BookingService.approve(id)).getMessageKey());
        assertEquals(BookingStatus.PENDING_CANCELLATION, fresh(id).status());
        assertEquals(List.of(SlotStatus.AVAILABLE), slotStatuses(id));
    }

    @Test
    void approvingTwiceDoesNotLockTheSlotsTwice() {
        long id = p3();
        BookingService.approve(id);
        assertThrows(BookingStatusChangedException.class, () -> BookingService.approve(id));
    }

    @Test
    void rejectStoresTheReasonAndLeavesTheSlotsAlone() {
        long id = p5();
        BookingService.reject(id, "The court is closed for maintenance");
        Booking booking = fresh(id);
        assertEquals(BookingStatus.REJECTED, booking.status());
        assertEquals("The court is closed for maintenance", booking.rejectionReason());
        assertEquals(List.of(SlotStatus.AVAILABLE), slotStatuses(id));
    }

    @Test
    void rejectDoesNotOverwriteABookingThatChangedStatus() {
        long id = p3();
        forceStatus(id, "4", "1");
        assertEquals("p30", assertThrows(BookingStatusChangedException.class, () -> BookingService.reject(id, "Any reason")).getMessageKey());
        Booking booking = fresh(id);
        assertEquals(BookingStatus.PENDING_CANCELLATION, booking.status());
        assertNull(booking.rejectionReason());
    }

    @Test
    void concurrentApprovalsLetExactlyOneWin() throws Exception {
        long a = p3();
        long b = p5();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Boolean>> results = List.of(pool.submit(() -> attempt(go, a)), pool.submit(() -> attempt(go, b)));
        go.countDown();
        int wins = 0;
        for (Future<Boolean> f : results) {
            wins += f.get() ? 1 : 0;
        }
        pool.shutdown();
        assertEquals(1, wins);
        assertEquals(1, TestDb.count("SELECT COUNT(*) FROM bookings WHERE status = '2' AND id IN (?, ?)", a, b));
    }

    private boolean attempt(CountDownLatch go, long id) throws InterruptedException {
        go.await();
        try {
            BookingService.approve(id);
            return true;
        } catch (SlotUnavailableException e) {
            return false;
        }
    }
}
