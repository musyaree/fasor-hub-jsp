package app.fasorhub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fasorhub.dao.BookingDao;
import app.fasorhub.exception.BookingStatusChangedException;
import app.fasorhub.model.Booking;
import app.fasorhub.model.BookingStatus;
import app.fasorhub.model.QueueItem;
import app.fasorhub.model.SlotStatus;
import app.fasorhub.service.BookingService;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Jdbc;
import app.fasorhub.util.Tx;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CancellationServiceTest {

    private static final LocalDate TEST_DATE =
            LocalDate.of(2026, 10, 25);

    @BeforeEach
    void setUp() {
        Clock.fixAt(
                TEST_DATE.atStartOfDay()
                        .toInstant(ZoneOffset.ofHours(7)));

        TestDb.fresh(true);
    }

    @AfterEach
    void tearDown() {
        Clock.reset();
    }

    private Booking fresh(long bookingId) {
        return Tx.run(c ->
                BookingDao.find(c, bookingId).orElseThrow());
    }

    private List<SlotStatus> slotStatuses(long bookingId) {
        return Tx.run(c -> Jdbc.query(
                c,
                "SELECT ts.status "
                        + "FROM time_slots ts "
                        + "JOIN booking_time_slot bts "
                        + "ON bts.time_slot_id = ts.id "
                        + "WHERE bts.booking_id = ? "
                        + "ORDER BY ts.start_time",

                r -> SlotStatus.of(r.getString(1)),
                bookingId));
    }

    private QueueItem firstRequest(BookingStatus previousStatus) {
        return BookingService.pendingCancellations().stream()
                .filter(item ->
                        item.booking().previousStatus() == previousStatus)
                .findFirst()
                .orElseThrow();
    }

    @Test
    void cancellationListContainsUpcomingRequestsOnly() {
        List<QueueItem> queue =
                BookingService.pendingCancellations();

        assertTrue(queue.stream()
                .allMatch(item ->
                        !item.date().isBefore(TEST_DATE)));
    }

    @Test
    void approvedCancellationReleasesSessions() {
        QueueItem item = firstRequest(BookingStatus.APPROVED);
        BookingService.confirmCancellation(item.getId());
        assertEquals(
                BookingStatus.CANCELLED,
                fresh(item.getId()).status());
        assertTrue(slotStatuses(item.getId()).stream()
                .allMatch(status -> status == SlotStatus.AVAILABLE));
    }

    @Test
    void pendingCancellationDoesNotReleaseSessions() {
        QueueItem item =
                firstRequest(BookingStatus.PENDING_VERIFICATION);
        // Pending requests do not own their slots.
        assertTrue(slotStatuses(item.getId()).stream()
                .allMatch(status -> status == SlotStatus.AVAILABLE));
        BookingService.confirmCancellation(item.getId());
        assertEquals(
                BookingStatus.CANCELLED,
                fresh(item.getId()).status());
        assertTrue(slotStatuses(item.getId()).stream()
                .allMatch(status -> status == SlotStatus.AVAILABLE));
    }

    @Test
    void processedRequestReturnsP32() {
        QueueItem item =
                firstRequest(BookingStatus.PENDING_VERIFICATION);
        BookingService.confirmCancellation(item.getId());
        BookingStatusChangedException exception =
                assertThrows(
                        BookingStatusChangedException.class,
                        () -> BookingService.confirmCancellation(
                                item.getId()));

        assertEquals("p32", exception.getMessageKey());
        assertEquals(
                BookingStatus.CANCELLED,
                fresh(item.getId()).status());
    }

    @Test
    void confirmedCancellationCanBeSubmittedAgain() {
        QueueItem item = firstRequest(BookingStatus.APPROVED);
        BookingService.confirmCancellation(item.getId());
        assertTrue(slotStatuses(item.getId()).stream()
                .allMatch(status -> status == SlotStatus.AVAILABLE));
    }

    @Test
    void failedReleaseRollsBackCancellation() {
        QueueItem item = firstRequest(BookingStatus.APPROVED);
        Tx.runVoid(c -> {
            Jdbc.update(c, "DROP TRIGGER IF EXISTS fail_cancellation");
            Jdbc.update(c,
                    "CREATE TRIGGER fail_cancellation "
                            + "BEFORE UPDATE ON time_slots "
                            + "FOR EACH ROW "
                            + "SIGNAL SQLSTATE '45000' "
                            + "SET MESSAGE_TEXT = 'forced release failure'");
            return null;
        });

        try {
            assertThrows(
                    app.fasorhub.util.DataAccessException.class,
                    () -> BookingService.confirmCancellation(item.getId()));
            // The transaction must roll back both status and slot changes.
            assertEquals(
                    BookingStatus.PENDING_CANCELLATION,
                    fresh(item.getId()).status());
            assertTrue(slotStatuses(item.getId()).stream()
                    .allMatch(status -> status == SlotStatus.OCCUPIED));
        } finally {
            Tx.runVoid(c -> {
                Jdbc.update(c, "DROP TRIGGER IF EXISTS fail_cancellation");
                return null;
            });
        }
    }
}