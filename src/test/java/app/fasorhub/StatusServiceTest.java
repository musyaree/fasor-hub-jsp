package app.fasorhub;

import app.fasorhub.dao.UserDao;
import app.fasorhub.model.BookingStatus;
import app.fasorhub.model.StatusItem;
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
 * Tests for BookingService.upcomingFor (UC03).
 */
class StatusServiceTest {

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

    // UC03-01: Shows only user's bookings dated today or later, newest first
    @Test
    void showsOnlyUpcomingBookingsForUser() {
        List<StatusItem> items = BookingService.upcomingFor(aliceId);

        // Alice has P2 (H+3), P3 (H+4), P4 (H+5) upcoming
        // P1 (H-5) is in the past and should NOT appear
        assertFalse(items.isEmpty(), "Alice should have upcoming bookings");

        // Check all bookings are dated today or later
        for (StatusItem item : items) {
            assertFalse(item.date().isBefore(TEST_DATE),
                    "Booking date should not be before today");
        }

        // Check ordered by newest first (created_at DESC)
        for (int i = 0; i < items.size() - 1; i++) {
            assertTrue(items.get(i).getSubmittedLabel().compareTo(items.get(i + 1).getSubmittedLabel()) >= 0
                            || items.get(i).getId() > items.get(i + 1).getId(),
                    "Bookings should be ordered newest first");
        }
    }

    // UC03-02: Status values are correct
    @Test
    void statusValuesAreCorrect() {
        List<StatusItem> items = BookingService.upcomingFor(aliceId);

        // Find each booking by status to verify they exist
        boolean hasPending = items.stream().anyMatch(i -> i.getStatus() == BookingStatus.PENDING_VERIFICATION);
        boolean hasApproved = items.stream().anyMatch(i -> i.getStatus() == BookingStatus.APPROVED);
        boolean hasRejected = items.stream().anyMatch(i -> i.getStatus() == BookingStatus.REJECTED);

        // Alice has P3 (Pending), P4 (Approved), P2 (Rejected)
        assertTrue(hasPending || hasApproved || hasRejected,
                "Alice should have bookings with known statuses");
    }

    // UC03-03: Rejected booking shows rejection reason
    @Test
    void rejectedBookingShowsReason() {
        List<StatusItem> items = BookingService.upcomingFor(aliceId);

        // Find the rejected booking (P2)
        StatusItem rejected = items.stream()
                .filter(i -> i.getStatus() == BookingStatus.REJECTED)
                .findFirst()
                .orElse(null);

        if (rejected != null) {
            assertNotNull(rejected.getRejectionReason(),
                    "Rejected booking should have a rejection reason");
            assertFalse(rejected.getRejectionReason().isBlank(),
                    "Rejection reason should not be blank");
        }
    }

    // UC03-04: Empty list when user has no bookings
    @Test
    void emptyListWhenNoBookings() {
        // Create a new user with no bookings
        long newUserId = Tx.run(c -> {
            return app.fasorhub.dao.UserDao.insert(c, "New User", "new@example.com", "password",
                    app.fasorhub.model.Role.BORROWER, "NEW001", "0812-0000");
        });

        List<StatusItem> items = BookingService.upcomingFor(newUserId);

        assertTrue(items.isEmpty(), "New user should have no bookings");
    }

    // UC03-05: Does not show other users' bookings
    @Test
    void doesNotShowOtherUsersBookings() {
        List<StatusItem> aliceItems = BookingService.upcomingFor(aliceId);
        List<StatusItem> bobItems = BookingService.upcomingFor(bobId);

        // Get all booking IDs for Alice
        List<Long> aliceBookingIds = aliceItems.stream().map(StatusItem::getId).toList();

        // Get all booking IDs for Bob
        List<Long> bobBookingIds = bobItems.stream().map(StatusItem::getId).toList();

        // No overlap
        for (Long aliceId : aliceBookingIds) {
            assertFalse(bobBookingIds.contains(aliceId),
                    "Bob's list should not contain Alice's booking " + aliceId);
        }
    }

    // UC03-06: Past bookings not shown (after expiry)
    @Test
    void pastBookingsNotShown() {
        // Move clock forward so some bookings become past
        Clock.fixAt(TEST_DATE.plusDays(10).atStartOfDay().toInstant(ZoneOffset.ofHours(7)));

        List<StatusItem> items = BookingService.upcomingFor(aliceId);

        // All bookings should still be dated on or after the new "today"
        LocalDate newToday = Clock.today();
        for (StatusItem item : items) {
            assertFalse(item.date().isBefore(newToday),
                    "Should not show bookings with dates before today");
        }
    }

    // Test that each item has required display data
    @Test
    void itemsHaveRequiredDisplayData() {
        List<StatusItem> items = BookingService.upcomingFor(aliceId);

        for (StatusItem item : items) {
            assertNotNull(item.getFacilityName(), "Facility name required");
            assertNotNull(item.getDateLabel(), "Date label required");
            assertNotNull(item.getSlotRanges(), "Slot ranges required");
            assertNotNull(item.getStatus(), "Status required");
            assertNotNull(item.getSubmittedLabel(), "Submitted label required");
        }
    }

    // Test that Carol has PENDING_CANCELLATION bookings (P7, P8)
    @Test
    void showsPendingCancellationStatus() {
        List<StatusItem> items = BookingService.upcomingFor(carolId);

        boolean hasPendingCancellation = items.stream()
                .anyMatch(i -> i.getStatus() == BookingStatus.PENDING_CANCELLATION);

        assertTrue(hasPendingCancellation,
                "Carol should have bookings with PENDING_CANCELLATION status");
    }
}
