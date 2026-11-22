package app.fasorhub;

import app.fasorhub.dao.BookingDao;
import app.fasorhub.model.QueueItem;
import app.fasorhub.service.BookingService;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Tx;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VerificationQueueTest {

    @BeforeEach
    void setUp() {
        TestDb.fresh(true);
    }

    private static List<QueueItem> queue() {
        return Tx.run(c -> BookingDao.findQueue(c, Clock.today()));
    }

    private static QueueItem itemOf(String requester, String facility) {
        return queue().stream().filter(i -> i.getRequesterName().startsWith(requester) && i.getFacilityName().contains(facility)).findFirst().orElseThrow();
    }

    // UC07-01
    @Test
    void queueHoldsOnlyWaitingBookingsWithOldestFirst() {
        List<QueueItem> queue = queue();

        assertEquals(3, queue.size());
        assertEquals("Alice Johnson", queue.get(0).getRequesterName());
        assertEquals("Bob Smith", queue.get(1).getRequesterName());
        assertTrue(queue.get(1).getFacilityName().contains("Futsal"));
        assertTrue(queue.get(2).getFacilityName().contains("Badminton"));
        assertTrue(queue.get(0).createdAt().isBefore(queue.get(1).createdAt()));
        assertTrue(queue.get(1).createdAt().isBefore(queue.get(2).createdAt()));
    }

    // UC07-02
    @Test
    void itemCarriesTheDetailsShownOnTheScreen() {
        QueueItem badminton = itemOf("Bob", "Badminton");

        assertEquals("5025251102", badminton.getRequesterIdentity());
        assertEquals("Badminton practice", badminton.getPurpose());
        assertEquals(2, badminton.slots().size());
        assertEquals("13:00-15:00, 15:00-17:00", badminton.getSlotRanges());
        assertTrue(badminton.isAllAvailable());
    }

    // UC07-03
    @Test
    void approvedAndRejectedBookingsLeaveTheQueue() {
        BookingService.approve(itemOf("Alice", "Futsal").getId());
        assertEquals(2, queue().size());

        BookingService.reject(itemOf("Bob", "Badminton").getId(), "Not available");
        assertEquals(1, queue().size());
    }

    // UC07-04
    @Test
    void competingBookingStaysInTheQueueButCannotBeApproved() {
        BookingService.approve(itemOf("Alice", "Futsal").getId());

        QueueItem competing = itemOf("Bob", "Futsal");
        assertFalse(competing.isAllAvailable());
    }

    // UC07-12
    @Test
    void queueIsEmptyOnceEveryBookingIsDecided() {
        for (QueueItem item : queue()) {
            BookingService.reject(item.getId(), "No");
        }

        assertTrue(queue().isEmpty());
    }
}
