package app.fasorhub;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fasorhub.model.BookingStatus;
import app.fasorhub.model.Role;
import app.fasorhub.model.SlotStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SeederTest {
    @BeforeEach
    void setUp() {
        TestDb.fresh(true);
    }

    @Test
    void seedsAccountsFacilitiesSlotsAndBookings() {
        assertEquals(4, TestDb.count("SELECT COUNT(*) FROM users"));
        assertEquals(1, TestDb.count("SELECT COUNT(*) FROM users WHERE role = ?", Role.MANAGER.code()));
        assertEquals(4, TestDb.count("SELECT COUNT(*) FROM facilities"));
        assertEquals(560, TestDb.count("SELECT COUNT(*) FROM time_slots"));
        assertEquals(13, TestDb.count("SELECT COUNT(*) FROM bookings"));
    }

    @Test
    void seedsTheClosedAndOccupiedSlots() {
        assertEquals(6, TestDb.count("SELECT COUNT(*) FROM time_slots WHERE status = ?", SlotStatus.CLOSED.code()));
        assertEquals(8, TestDb.count("SELECT COUNT(*) FROM time_slots WHERE status = ?", SlotStatus.OCCUPIED.code()));
    }

    @Test
    void everyOccupiedSlotIsLockedByExactlyOneBooking() {
        long unlocked = TestDb.count("SELECT COUNT(*) FROM time_slots ts WHERE ts.status = '2' AND "
                + "(SELECT COUNT(*) FROM booking_time_slot bts JOIN bookings b ON b.id = bts.booking_id WHERE bts.time_slot_id = ts.id "
                + "AND (b.status = '2' OR (b.status = '4' AND b.previous_status = '2'))) <> 1");
        assertEquals(0, unlocked);
    }

    @Test
    void twoPendingBookingsTargetTheSameSlot() {
        assertEquals(1, TestDb.count("SELECT COUNT(*) FROM (SELECT time_slot_id FROM booking_time_slot GROUP BY time_slot_id HAVING COUNT(*) >= 2) s"));
        assertEquals(1, TestDb.count("SELECT COUNT(*) FROM time_slots WHERE status = '1' AND id IN "
                + "(SELECT time_slot_id FROM booking_time_slot GROUP BY time_slot_id HAVING COUNT(*) >= 2)"));
    }

    @Test
    void expiredBookingsAreSeededForTheScheduledJob() {
        String past = "EXISTS (SELECT 1 FROM booking_time_slot bts JOIN time_slots ts ON ts.id = bts.time_slot_id "
                + "WHERE bts.booking_id = b.id AND ts.`date` < CURDATE())";
        assertEquals(1, TestDb.count("SELECT COUNT(*) FROM bookings b WHERE b.status = '1' AND " + past));
        assertEquals(1, TestDb.count("SELECT COUNT(*) FROM bookings b WHERE b.status = '4' AND " + past));
    }

    @Test
    void enumLabelsComeFromTheMessageFile() {
        assertEquals("Pending Verification", BookingStatus.PENDING_VERIFICATION.getLabel());
        assertEquals("Occupied", SlotStatus.OCCUPIED.getLabel());
        assertEquals("Manager", Role.MANAGER.getLabel());
    }
}
