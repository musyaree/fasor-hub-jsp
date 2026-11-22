package app.fasorhub;

import app.fasorhub.dao.FacilityDao;
import app.fasorhub.dao.SlotDao;
import app.fasorhub.exception.ValidationException;
import app.fasorhub.model.Facility;
import app.fasorhub.service.FacilityService;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Jdbc;
import app.fasorhub.util.Tx;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FacilityServiceTest {

    @BeforeEach
    void setUp() {
        TestDb.fresh(true);
    }

    private static long facilityCount() {
        return TestDb.count("SELECT COUNT(*) FROM facilities");
    }

    private static long slotCount(long facilityId) {
        return TestDb.count("SELECT COUNT(*) FROM time_slots WHERE facility_id = ?", facilityId);
    }

    private static Facility find(long id) {
        return Tx.run(c -> FacilityDao.find(c, id).orElse(null));
    }

    private static long seededFacilityWithBookings() {
        return TestDb.count("SELECT id FROM facilities WHERE name = 'Lapangan Futsal A'");
    }

    private static long typeId(String name) {
        return TestDb.count("SELECT id FROM facility_types WHERE name = ?", name);
    }

    private static String keyOf(ValidationException e) {
        return e.getMessageKey();
    }

    // FAC-01
    @Test
    void createAddsTheFacilityWithThirtyFiveDaysOfAvailableSessions() throws Exception {
        long before = facilityCount();

        long id = FacilityService.create("Lapangan Futsal B", typeId("Futsal"), "Indoor court");

        assertEquals(before + 1, facilityCount());
        assertEquals("Lapangan Futsal B", find(id).getName());
        assertEquals("Indoor court", find(id).getDescription());
        assertEquals(35 * 4, slotCount(id));
        assertEquals(35 * 4, TestDb.count("SELECT COUNT(*) FROM time_slots WHERE facility_id = ? AND status = '1'", id));

        LocalDate today = Clock.today();
        assertEquals(1, TestDb.count("SELECT COUNT(*) FROM time_slots WHERE facility_id = ? AND `date` = ? AND start_time = '08:00:00'",
                id, java.sql.Date.valueOf(today)));
        assertEquals(1, TestDb.count("SELECT COUNT(*) FROM time_slots WHERE facility_id = ? AND `date` = ? AND start_time = '08:00:00'",
                id, java.sql.Date.valueOf(today.plusDays(34))));
        assertEquals(0, TestDb.count("SELECT COUNT(*) FROM time_slots WHERE facility_id = ? AND `date` = ?",
                id, java.sql.Date.valueOf(today.plusDays(35))));
    }

    // FAC-02
    @Test
    void invalidInputIsRejectedAndNothingIsSaved() {
        long before = facilityCount();

        assertEquals("p38", keyOf(assertThrows(ValidationException.class, () -> FacilityService.create(null, typeId("Futsal"), null))));
        assertEquals("p38", keyOf(assertThrows(ValidationException.class, () -> FacilityService.create("   ", typeId("Futsal"), null))));
        assertEquals("p38", keyOf(assertThrows(ValidationException.class, () -> FacilityService.create("x".repeat(51), typeId("Futsal"), null))));
        assertEquals("p39", keyOf(assertThrows(ValidationException.class, () -> FacilityService.create("Court", null, null))));
        assertEquals("p39", keyOf(assertThrows(ValidationException.class, () -> FacilityService.create("Court", 999_999L, null))));
        assertEquals("p40", keyOf(assertThrows(ValidationException.class, () -> FacilityService.create("Court", typeId("Futsal"), "x".repeat(201)))));

        assertEquals(before, facilityCount());
    }

    // FAC-02
    @Test
    void aTypeThatNoLongerExistsGivesP39() {
        assertEquals("p39", keyOf(assertThrows(ValidationException.class, () -> FacilityService.create("Court", 999_999L, null))));
        assertEquals("p39", keyOf(assertThrows(ValidationException.class,
                () -> FacilityService.update(seededFacilityWithBookings(), "Court", 999_999L, null))));
    }

    // FAC-02
    @Test
    void theLimitsThemselvesAreAccepted() throws Exception {
        long id = FacilityService.create("x".repeat(50), typeId("Tennis"), "y".repeat(200));

        assertEquals(50, find(id).getName().length());
        assertEquals(200, find(id).getDescription().length());
    }

    // FAC-02
    @Test
    void editUsesTheSameValidation() {
        long id = seededFacilityWithBookings();

        assertEquals("p38", keyOf(assertThrows(ValidationException.class, () -> FacilityService.update(id, "", typeId("Futsal"), null))));
        assertEquals("p39", keyOf(assertThrows(ValidationException.class, () -> FacilityService.update(id, "Court", null, null))));
        assertEquals("p40", keyOf(assertThrows(ValidationException.class, () -> FacilityService.update(id, "Court", typeId("Futsal"), "x".repeat(201)))));
        assertEquals("Lapangan Futsal A", find(id).getName());
    }

    // FAC-02
    @Test
    void textIsTrimmedAndABlankDescriptionIsStoredAsNothing() throws Exception {
        long id = FacilityService.create("  Court Z  ", typeId("Basketball"), "   ");

        assertEquals("Court Z", find(id).getName());
        assertNull(find(id).getDescription());
    }

    // FAC-03
    @Test
    void editChangesTheDetailsAndKeepsTheSessions() throws Exception {
        long id = seededFacilityWithBookings();
        long slotsBefore = slotCount(id);

        FacilityService.update(id, "Lapangan Futsal Utama", typeId("Basketball"), "Renovated");

        Facility changed = find(id);
        assertEquals("Lapangan Futsal Utama", changed.getName());
        assertEquals("Basketball", changed.getType().getName());
        assertEquals("Renovated", changed.getDescription());
        assertEquals(slotsBefore, slotCount(id));
    }

    // FAC-03
    @Test
    void editingAFacilityThatNoLongerExistsGivesP01() {
        assertEquals("p01", keyOf(assertThrows(ValidationException.class, () -> FacilityService.update(999_999L, "Court", typeId("Futsal"), null))));
    }

    // FAC-04
    @Test
    void deleteRemovesAFacilityWithoutBookingsTogetherWithItsSessions() throws Exception {
        long id = FacilityService.create("Temporary Court", typeId("Badminton"), null);
        long before = facilityCount();

        FacilityService.delete(id);

        assertEquals(before - 1, facilityCount());
        assertNull(find(id));
        assertEquals(0, slotCount(id));
    }

    // FAC-05
    @Test
    void deleteIsRefusedWhenTheFacilityHasBookings() {
        long id = seededFacilityWithBookings();
        long facilities = facilityCount();
        long slots = slotCount(id);

        assertEquals("p37", keyOf(assertThrows(ValidationException.class, () -> FacilityService.delete(id))));

        assertEquals(facilities, facilityCount());
        assertEquals(slots, slotCount(id));
    }

    // FAC-05
    @Test
    void aRejectedOrCancelledBookingAlsoBlocksTheDelete() {
        long rejected = TestDb.count("SELECT ts.facility_id FROM bookings b JOIN booking_time_slot bts ON bts.booking_id = b.id "
                + "JOIN time_slots ts ON ts.id = bts.time_slot_id WHERE b.status = '3' LIMIT 1");
        long cancelled = TestDb.count("SELECT ts.facility_id FROM bookings b JOIN booking_time_slot bts ON bts.booking_id = b.id "
                + "JOIN time_slots ts ON ts.id = bts.time_slot_id WHERE b.status = '5' LIMIT 1");

        assertEquals("p37", keyOf(assertThrows(ValidationException.class, () -> FacilityService.delete(rejected))));
        assertEquals("p37", keyOf(assertThrows(ValidationException.class, () -> FacilityService.delete(cancelled))));
    }

    // FAC-04
    @Test
    void deletingAFacilityThatNoLongerExistsGivesP01() {
        assertEquals("p01", keyOf(assertThrows(ValidationException.class, () -> FacilityService.delete(999_999L))));
    }

    // FAC-05
    @Test
    void deleteWaitsForABookingBeingSubmittedAndThenRefuses() throws Exception {
        long id = FacilityService.create("Busy Court", typeId("Futsal"), null);
        long userId = TestDb.count("SELECT id FROM users WHERE email = 'alice@example.com'");

        CountDownLatch holding = new CountDownLatch(1);
        ExecutorService pool = Executors.newSingleThreadExecutor();

        Future<?> submit = pool.submit(() -> Tx.runVoid(c -> {
            List<Long> slotIds = Jdbc.query(c, "SELECT id FROM time_slots WHERE facility_id = ? ORDER BY id LIMIT 1 FOR UPDATE",
                    r -> r.getLong(1), id);
            long bookingId = Jdbc.insert(c, "INSERT INTO bookings (user_id, organization_unit, participant_count, contact, purpose) "
                    + "VALUES (?, 'Unit', 5, '0812', 'Practice')", userId);
            Jdbc.update(c, "INSERT INTO booking_time_slot (booking_id, time_slot_id) VALUES (?, ?)", bookingId, slotIds.get(0));

            holding.countDown();

            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return null;
        }));

        assertTrue(holding.await(5, TimeUnit.SECONDS));

        ValidationException refused = assertThrows(ValidationException.class, () -> FacilityService.delete(id));
        submit.get(5, TimeUnit.SECONDS);
        pool.shutdown();

        assertEquals("p37", refused.getMessageKey());
        assertNotNull(find(id));
        assertEquals(35 * 4, slotCount(id));
    }
}
