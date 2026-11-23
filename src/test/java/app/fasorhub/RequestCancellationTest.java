package app.fasorhub;

import app.fasorhub.dao.BookingDao;
import app.fasorhub.dao.FacilityDao;
import app.fasorhub.dao.SlotDao;
import app.fasorhub.dao.UserDao;
import app.fasorhub.exception.BookingStatusChangedException;
import app.fasorhub.exception.ValidationException;
import app.fasorhub.model.Booking;
import app.fasorhub.model.BookingStatus;
import app.fasorhub.model.Facility;
import app.fasorhub.model.SlotStatus;
import app.fasorhub.model.StatusItem;
import app.fasorhub.model.TimeSlot;
import app.fasorhub.model.User;
import app.fasorhub.service.BookingService;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Tx;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BookingService.requestCancellation (UC04).
 */
class RequestCancellationTest {

    private static final LocalDate TEST_DATE = LocalDate.of(2026, 10, 25);

    private long aliceId;
    private long bobId;
    private long carolId;

    @BeforeEach
    void setUp() {
        Clock.fixAt(TEST_DATE.atStartOfDay().toInstant(ZoneOffset.ofHours(7)));
        TestDb.fresh(true);

        aliceId = Tx.run(c -> UserDao.findByEmail(c, "alice@example.com").map(User::id).orElseThrow());
        bobId = Tx.run(c -> UserDao.findByEmail(c, "bob@example.com").map(User::id).orElseThrow());
        carolId = Tx.run(c -> UserDao.findByEmail(c, "carol@example.com").map(User::id).orElseThrow());
    }

    @AfterEach
    void tearDown() {
        Clock.reset();
    }

    // UC04-01: Cancellable list only shows Pending/Approved bookings dated today or later
    @Test
    void cancellableListShowsOnlyPendingOrApproved() {
        List<StatusItem> items = BookingService.cancellableFor(aliceId);

        // Only P3 (Pending, H+4) and P4 (Approved, H+5) should appear
        // P2 (Rejected) should NOT appear
        // P1 (H-5, past) should NOT appear
        for (StatusItem item : items) {
            assertTrue(
                    item.getStatus() == BookingStatus.PENDING_VERIFICATION
                            || item.getStatus() == BookingStatus.APPROVED,
                    "Only Pending or Approved bookings should be cancellable");
            assertFalse(item.date().isBefore(TEST_DATE),
                    "Only bookings dated today or later should be cancellable");
        }
    }

    // UC04-02: Successful cancellation request changes status
    @Test
    void successfulCancellationRequestChangesStatus() throws Exception {
        List<StatusItem> items = BookingService.cancellableFor(aliceId);
        StatusItem pending = items.stream()
                .filter(i -> i.getStatus() == BookingStatus.PENDING_VERIFICATION)
                .findFirst()
                .orElseThrow();

        BookingService.requestCancellation(aliceId, pending.getId(), "Schedule conflict");

        Booking updated = Tx.run(c -> BookingDao.find(c, pending.getId()).orElseThrow());
        assertEquals(BookingStatus.PENDING_CANCELLATION, updated.status());
        assertEquals(BookingStatus.PENDING_VERIFICATION, updated.previousStatus());
        assertEquals("Schedule conflict", updated.cancellationReason());
        assertNotNull(updated.cancellationRequestedAt());
    }

    // UC04-02: Cancellation of Approved booking saves previous status
    @Test
    void cancellationOfApprovedSavesPreviousStatus() throws Exception {
        List<StatusItem> items = BookingService.cancellableFor(aliceId);
        StatusItem approved = items.stream()
                .filter(i -> i.getStatus() == BookingStatus.APPROVED)
                .findFirst()
                .orElseThrow();

        BookingService.requestCancellation(aliceId, approved.getId(), "Changed plans");

        Booking updated = Tx.run(c -> BookingDao.find(c, approved.getId()).orElseThrow());
        assertEquals(BookingStatus.PENDING_CANCELLATION, updated.status());
        assertEquals(BookingStatus.APPROVED, updated.previousStatus());
    }

    // UC04-03: Empty reason fails with P07
    @Test
    void emptyReasonFailsWithP07() {
        List<StatusItem> items = BookingService.cancellableFor(aliceId);
        StatusItem item = items.get(0);

        ValidationException e = assertThrows(ValidationException.class, () ->
                BookingService.requestCancellation(aliceId, item.getId(), ""));
        assertEquals("p07", e.getMessageKey());
    }

    @Test
    void blankReasonFailsWithP07() {
        List<StatusItem> items = BookingService.cancellableFor(aliceId);
        StatusItem item = items.get(0);

        ValidationException e = assertThrows(ValidationException.class, () ->
                BookingService.requestCancellation(aliceId, item.getId(), "   "));
        assertEquals("p07", e.getMessageKey());
    }

    @Test
    void nullReasonFailsWithP07() {
        List<StatusItem> items = BookingService.cancellableFor(aliceId);
        StatusItem item = items.get(0);

        ValidationException e = assertThrows(ValidationException.class, () ->
                BookingService.requestCancellation(aliceId, item.getId(), null));
        assertEquals("p07", e.getMessageKey());
    }

    // UC04-04: Empty list when user has no cancellable bookings
    @Test
    void emptyListWhenNoCancellableBookings() {
        // Create a new user with no bookings
        long newUserId = Tx.run(c ->
                UserDao.insert(c, "New User", "new@example.com", "password",
                        app.fasorhub.model.Role.BORROWER, "NEW001", "0812-0000"));

        List<StatusItem> items = BookingService.cancellableFor(newUserId);
        assertTrue(items.isEmpty());
    }

    // UC04-05: Booking already changed status fails with P31
    @Test
    void alreadyRejectedBookingFailsWithP31() {
        // Find a rejected booking
        List<StatusItem> allBookings = BookingService.upcomingFor(aliceId);
        StatusItem rejected = allBookings.stream()
                .filter(i -> i.getStatus() == BookingStatus.REJECTED)
                .findFirst()
                .orElse(null);

        if (rejected != null) {
            BookingStatusChangedException e = assertThrows(BookingStatusChangedException.class, () ->
                    BookingService.requestCancellation(aliceId, rejected.getId(), "Want to cancel"));
            assertEquals("p31", e.getMessageKey());
        }
    }

    // UC04-05: Booking already in PendingCancellation fails
    @Test
    void alreadyPendingCancellationFailsWithP31() throws Exception {
        List<StatusItem> items = BookingService.cancellableFor(aliceId);
        StatusItem item = items.get(0);

        // First cancellation succeeds
        BookingService.requestCancellation(aliceId, item.getId(), "First request");

        // Second attempt fails
        BookingStatusChangedException e = assertThrows(BookingStatusChangedException.class, () ->
                BookingService.requestCancellation(aliceId, item.getId(), "Second request"));
        assertEquals("p31", e.getMessageKey());
    }

    // UC04-06: Past date booking cannot be cancelled
    @Test
    void pastDateBookingCannotBeCancelled() {
        // Move clock forward so bookings become past
        Clock.fixAt(TEST_DATE.plusDays(10).atStartOfDay().toInstant(ZoneOffset.ofHours(7)));

        List<StatusItem> items = BookingService.cancellableFor(aliceId);

        // All bookings should be dated on or after the new today
        LocalDate newToday = Clock.today();
        for (StatusItem item : items) {
            assertFalse(item.date().isBefore(newToday));
        }
    }

    // UC04-07: Cannot cancel another user's booking
    @Test
    void cannotCancelOtherUsersBooking() {
        // Get Bob's cancellable bookings
        List<StatusItem> bobItems = BookingService.cancellableFor(bobId);
        if (!bobItems.isEmpty()) {
            StatusItem bobBooking = bobItems.get(0);

            // Alice tries to cancel Bob's booking
            BookingStatusChangedException e = assertThrows(BookingStatusChangedException.class, () ->
                    BookingService.requestCancellation(aliceId, bobBooking.getId(), "Trying to cancel"));
            assertEquals("p31", e.getMessageKey());
        }
    }

    // UC04-07: Non-existent booking fails with P31
    @Test
    void nonExistentBookingFailsWithP31() {
        BookingStatusChangedException e = assertThrows(BookingStatusChangedException.class, () ->
                BookingService.requestCancellation(aliceId, 999999L, "Trying to cancel"));
        assertEquals("p31", e.getMessageKey());
    }

    // Verify cancellable list doesn't show other users' bookings
    @Test
    void cancellableListDoesNotShowOtherUsersBookings() {
        List<StatusItem> aliceItems = BookingService.cancellableFor(aliceId);
        List<StatusItem> bobItems = BookingService.cancellableFor(bobId);

        List<Long> aliceIds = aliceItems.stream().map(StatusItem::getId).toList();
        List<Long> bobIds = bobItems.stream().map(StatusItem::getId).toList();

        for (Long id : aliceIds) {
            assertFalse(bobIds.contains(id), "Bob's list should not contain Alice's booking");
        }
    }

    // Carol's bookings in PENDING_CANCELLATION should NOT appear in cancellable list
    @Test
    void pendingCancellationNotInCancellableList() {
        List<StatusItem> carolItems = BookingService.cancellableFor(carolId);

        // Carol has P7 and P8 which are PENDING_CANCELLATION
        for (StatusItem item : carolItems) {
            assertNotEquals(BookingStatus.PENDING_CANCELLATION, item.getStatus(),
                    "PENDING_CANCELLATION bookings should not be in cancellable list");
        }
    }
}
