package app.fasorhub;

import app.fasorhub.dao.BookingDao;
import app.fasorhub.dao.FacilityDao;
import app.fasorhub.dao.UserDao;
import app.fasorhub.exception.ValidationException;
import app.fasorhub.model.BookingStatus;
import app.fasorhub.model.Facility;
import app.fasorhub.model.HistoryItem;
import app.fasorhub.model.QueueItem;
import app.fasorhub.model.User;
import app.fasorhub.service.BookingService;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Tx;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingFilterTest {

    @BeforeEach
    void setUp() {
        TestDb.fresh(true);
    }

    @Test
    void historyCombinesStatusAndFacilityFilters() throws Exception {
        Facility tennis = facility("Lapangan Tenis 1");

        List<HistoryItem> items = BookingService.history(
                user("manager@example.com"),
                BookingStatus.APPROVED,
                tennis.id(),
                null,
                null);

        assertEquals(1, items.size());
        assertEquals(BookingStatus.APPROVED, items.get(0).getStatus());
        assertEquals("Lapangan Tenis 1", items.get(0).getFacilityName());
    }

    @Test
    void historyDateRangeIncludesBothBoundaryDates() throws Exception {
        LocalDate startDate = Clock.today().plusDays(3);
        LocalDate endDate = Clock.today().plusDays(5);

        List<HistoryItem> items = BookingService.history(
                user("manager@example.com"), null, null, startDate, endDate);

        assertEquals(5, items.size());
        assertTrue(items.stream().allMatch(item ->
                !item.date().isBefore(startDate) && !item.date().isAfter(endDate)));
    }

    @Test
    void invalidHistoryDateRangeFailsWithP19() {
        LocalDate today = Clock.today();

        ValidationException error = assertThrows(
                ValidationException.class,
                () -> BookingService.history(
                        user("manager@example.com"),
                        null,
                        null,
                        today.plusDays(5),
                        today.plusDays(3)));

        assertEquals("p19", error.getMessageKey());
    }

    @Test
    void borrowerHistoryFilterDoesNotReturnOtherUsersBookings() throws Exception {
        User alice = user("alice@example.com");

        List<HistoryItem> allAliceHistory = BookingService.history(alice);
        List<HistoryItem> cancelledHistory = BookingService.history(
                alice, BookingStatus.CANCELLED, null, null, null);

        assertFalse(allAliceHistory.isEmpty());
        assertTrue(cancelledHistory.isEmpty());
    }

    @Test
    void verificationQueueFiltersByFacility() {
        Facility futsal = facility("Lapangan Futsal A");

        List<QueueItem> queue = Tx.run(
                c -> BookingDao.findQueue(c, Clock.today(), futsal.id()));

        assertEquals(2, queue.size());
        assertTrue(queue.stream().allMatch(item ->
                item.getFacilityName().equals("Lapangan Futsal A")));
    }

    @Test
    void verificationQueueCanBeEmptyForSelectedFacility() {
        Facility tennis = facility("Lapangan Tenis 1");

        List<QueueItem> allQueue = Tx.run(
                c -> BookingDao.findQueue(c, Clock.today()));
        List<QueueItem> filteredQueue = Tx.run(
                c -> BookingDao.findQueue(c, Clock.today(), tennis.id()));

        assertFalse(allQueue.isEmpty());
        assertTrue(filteredQueue.isEmpty());
    }

    @Test
    void cancellationQueueFiltersByFacility() {
        Facility tennis = facility("Lapangan Tenis 1");

        List<QueueItem> queue = BookingService.pendingCancellations(tennis.id());

        assertEquals(1, queue.size());
        assertTrue(queue.stream().allMatch(item ->
                item.getFacilityName().equals("Lapangan Tenis 1")));
    }

    @Test
    void cancellationQueueCanBeEmptyForSelectedFacility() {
        Facility basketball = facility("Lapangan Basket A");

        List<QueueItem> allQueue = BookingService.pendingCancellations();
        List<QueueItem> filteredQueue =
                BookingService.pendingCancellations(basketball.id());

        assertFalse(allQueue.isEmpty());
        assertTrue(filteredQueue.isEmpty());
    }

    private static Facility facility(String name) {
        return Tx.run(c -> FacilityDao.findAll(c).stream()
                .filter(item -> item.name().equals(name))
                .findFirst()
                .orElseThrow());
    }

    private static User user(String email) {
        return Tx.run(c -> UserDao.findByEmail(c, email).orElseThrow());
    }
}