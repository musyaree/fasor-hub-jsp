package app.fasorhub;

import app.fasorhub.dao.BookingDao;
import app.fasorhub.dao.FacilityDao;
import app.fasorhub.dao.SlotDao;
import app.fasorhub.dao.UserDao;
import app.fasorhub.exception.SlotUnavailableException;
import app.fasorhub.exception.ValidationException;
import app.fasorhub.model.Booking;
import app.fasorhub.model.BookingStatus;
import app.fasorhub.model.Facility;
import app.fasorhub.model.SlotStatus;
import app.fasorhub.model.TimeSlot;
import app.fasorhub.model.User;
import app.fasorhub.service.BookingService;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Tx;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BookingService.submit (UC02).
 */
class SubmitServiceTest {

    private static final LocalDate TEST_DATE = LocalDate.of(2026, 10, 25);

    private long userId;
    private long futsalAId;

    @BeforeEach
    void setUp() {
        Clock.fixAt(TEST_DATE.atStartOfDay().toInstant(ZoneOffset.ofHours(7)));
        TestDb.fresh(true);

        userId = Tx.run(c -> UserDao.findByEmail(c, "alice@example.com").map(User::id).orElseThrow());
        futsalAId = Tx.run(c -> FacilityDao.findAll(c).stream()
                .filter(f -> f.getName().equals("Lapangan Futsal A"))
                .findFirst()
                .map(Facility::getId)
                .orElseThrow());
    }

    @AfterEach
    void tearDown() {
        Clock.reset();
    }

    private List<Long> availableSlotIds(LocalDate date) {
        return Tx.run(c -> SlotDao.findByFacilityAndDate(c, futsalAId, date).stream()
                .filter(s -> s.status() == SlotStatus.AVAILABLE)
                .map(TimeSlot::getId)
                .toList());
    }

    private Booking findBooking(long bookingId) {
        return Tx.run(c -> BookingDao.find(c, bookingId).orElse(null));
    }

    // UC02-02: Submit booking with one session succeeds
    @Test
    void submitWithOneSessionSucceeds() throws Exception {
        LocalDate date = TEST_DATE.plusDays(5); // Future date with available slots
        List<Long> available = availableSlotIds(date);
        assertTrue(available.size() >= 1, "Need at least one available slot");

        List<Long> slotIds = List.of(available.get(0));

        long bookingId = BookingService.submit(userId, "Test Unit", 10, "0812-1234", "Test purpose", slotIds);

        Booking booking = findBooking(bookingId);
        assertNotNull(booking);
        assertEquals(BookingStatus.PENDING_VERIFICATION, booking.status());
        assertEquals("Test Unit", booking.organizationUnit());
        assertEquals(10, booking.participantCount());
    }

    // UC02-03: Submit with four sessions succeeds (max allowed)
    @Test
    void submitWithFourSessionsSucceeds() throws Exception {
        LocalDate date = TEST_DATE.plusDays(6);
        List<Long> available = availableSlotIds(date);
        assertTrue(available.size() >= 4, "Need four available slots");

        List<Long> slotIds = available.subList(0, 4);

        long bookingId = BookingService.submit(userId, "Sports Club", 20, "0813-5678", "Tournament", slotIds);

        Booking booking = findBooking(bookingId);
        assertNotNull(booking);
        assertEquals(BookingStatus.PENDING_VERIFICATION, booking.status());

        // Verify slots are attached
        List<TimeSlot> attachedSlots = Tx.run(c -> SlotDao.findByBooking(c, bookingId));
        assertEquals(4, attachedSlots.size());
    }

    // UC02-03: Submit with more than four sessions fails
    @Test
    void submitWithMoreThanFourSessionsFails() {
        LocalDate date = TEST_DATE.plusDays(6);
        List<Long> available = availableSlotIds(date);

        // Create a list with 5 slot IDs (duplicating one if needed)
        List<Long> slotIds = List.of(available.get(0), available.get(1), available.get(2), available.get(3), available.get(0));

        assertThrows(ValidationException.class, () ->
                BookingService.submit(userId, "Test", 10, "0812-1234", "Test", slotIds));
    }

    // UC02-05: Submit with incomplete data fails (P03)
    @Test
    void submitWithEmptyUnitFails() {
        LocalDate date = TEST_DATE.plusDays(5);
        List<Long> available = availableSlotIds(date);
        List<Long> slotIds = List.of(available.get(0));

        ValidationException e = assertThrows(ValidationException.class, () ->
                BookingService.submit(userId, "", 10, "0812-1234", "Test", slotIds));
        assertEquals("p03", e.getMessageKey());
    }

    @Test
    void submitWithZeroParticipantsFails() {
        LocalDate date = TEST_DATE.plusDays(5);
        List<Long> available = availableSlotIds(date);
        List<Long> slotIds = List.of(available.get(0));

        ValidationException e = assertThrows(ValidationException.class, () ->
                BookingService.submit(userId, "Test", 0, "0812-1234", "Test", slotIds));
        assertEquals("p03", e.getMessageKey());
    }

    @Test
    void submitWithEmptyContactFails() {
        LocalDate date = TEST_DATE.plusDays(5);
        List<Long> available = availableSlotIds(date);
        List<Long> slotIds = List.of(available.get(0));

        ValidationException e = assertThrows(ValidationException.class, () ->
                BookingService.submit(userId, "Test", 10, "  ", "Test", slotIds));
        assertEquals("p03", e.getMessageKey());
    }

    @Test
    void submitWithEmptyPurposeFails() {
        LocalDate date = TEST_DATE.plusDays(5);
        List<Long> available = availableSlotIds(date);
        List<Long> slotIds = List.of(available.get(0));

        ValidationException e = assertThrows(ValidationException.class, () ->
                BookingService.submit(userId, "Test", 10, "0812-1234", null, slotIds));
        assertEquals("p03", e.getMessageKey());
    }

    // UC02-07: Submit for occupied slot fails (P04)
    @Test
    void submitForOccupiedSlotFails() {
        // Tenis 1 on H+5 has an Approved booking (P4), so the slot is OCCUPIED
        long tenisId = Tx.run(c -> FacilityDao.findAll(c).stream()
                .filter(f -> f.getName().equals("Lapangan Tenis 1"))
                .findFirst()
                .map(Facility::getId)
                .orElseThrow());

        LocalDate date = TEST_DATE.plusDays(5);
        List<TimeSlot> slots = Tx.run(c -> SlotDao.findByFacilityAndDate(c, tenisId, date));
        List<Long> occupiedIds = slots.stream()
                .filter(s -> s.status() == SlotStatus.OCCUPIED)
                .map(TimeSlot::getId)
                .toList();

        if (!occupiedIds.isEmpty()) {
            SlotUnavailableException e = assertThrows(SlotUnavailableException.class, () ->
                    BookingService.submit(userId, "Test", 10, "0812-1234", "Test", occupiedIds));
            assertEquals("p04", e.getMessageKey());
        }
    }

    // UC02-07: Submit for closed slot fails (P04)
    @Test
    void submitForClosedSlotFails() {
        // Futsal A on H+3 (Oct 28) has two CLOSED slots
        LocalDate date = TEST_DATE.plusDays(3);
        List<TimeSlot> slots = Tx.run(c -> SlotDao.findByFacilityAndDate(c, futsalAId, date));
        List<Long> closedIds = slots.stream()
                .filter(s -> s.status() == SlotStatus.CLOSED)
                .map(TimeSlot::getId)
                .toList();

        assertFalse(closedIds.isEmpty(), "Need closed slots for this test");

        SlotUnavailableException e = assertThrows(SlotUnavailableException.class, () ->
                BookingService.submit(userId, "Test", 10, "0812-1234", "Test", closedIds));
        assertEquals("p04", e.getMessageKey());
    }

    // UC02-08: Submit for past date fails (P04)
    @Test
    void submitForPastDateFails() {
        LocalDate pastDate = TEST_DATE.minusDays(5);
        List<TimeSlot> slots = Tx.run(c -> SlotDao.findByFacilityAndDate(c, futsalAId, pastDate));

        if (!slots.isEmpty()) {
            List<Long> slotIds = List.of(slots.get(0).getId());

            SlotUnavailableException e = assertThrows(SlotUnavailableException.class, () ->
                    BookingService.submit(userId, "Test", 10, "0812-1234", "Test", slotIds));
            assertEquals("p04", e.getMessageKey());
        }
    }

    // UC02-09: Submit with slots from different facilities fails
    @Test
    void submitWithSlotsFromDifferentFacilitiesFails() {
        long basketId = Tx.run(c -> FacilityDao.findAll(c).stream()
                .filter(f -> f.getName().equals("Lapangan Basket A"))
                .findFirst()
                .map(Facility::getId)
                .orElseThrow());

        LocalDate date = TEST_DATE.plusDays(6);
        List<Long> futsalSlots = Tx.run(c -> SlotDao.findByFacilityAndDate(c, futsalAId, date).stream()
                .filter(s -> s.status() == SlotStatus.AVAILABLE)
                .map(TimeSlot::getId)
                .toList());
        List<Long> basketSlots = Tx.run(c -> SlotDao.findByFacilityAndDate(c, basketId, date).stream()
                .filter(s -> s.status() == SlotStatus.AVAILABLE)
                .map(TimeSlot::getId)
                .toList());

        if (!futsalSlots.isEmpty() && !basketSlots.isEmpty()) {
            List<Long> mixedSlots = List.of(futsalSlots.get(0), basketSlots.get(0));

            SlotUnavailableException e = assertThrows(SlotUnavailableException.class, () ->
                    BookingService.submit(userId, "Test", 10, "0812-1234", "Test", mixedSlots));
            assertEquals("p04", e.getMessageKey());
        }
    }

    // UC02-09: Submit with slots from different dates fails
    @Test
    void submitWithSlotsFromDifferentDatesFails() {
        LocalDate date1 = TEST_DATE.plusDays(5);
        LocalDate date2 = TEST_DATE.plusDays(6);

        List<Long> slots1 = availableSlotIds(date1);
        List<Long> slots2 = availableSlotIds(date2);

        if (!slots1.isEmpty() && !slots2.isEmpty()) {
            List<Long> mixedSlots = List.of(slots1.get(0), slots2.get(0));

            SlotUnavailableException e = assertThrows(SlotUnavailableException.class, () ->
                    BookingService.submit(userId, "Test", 10, "0812-1234", "Test", mixedSlots));
            assertEquals("p04", e.getMessageKey());
        }
    }

    // UC02-11: Two users submit for same slot - both succeed as PendingVerification
    @Test
    void twoUsersSubmitSameSlotBothSucceed() throws Exception {
        long bobId = Tx.run(c -> UserDao.findByEmail(c, "bob@example.com").map(User::id).orElseThrow());

        LocalDate date = TEST_DATE.plusDays(7);
        List<Long> available = availableSlotIds(date);
        assertTrue(available.size() >= 1);
        List<Long> slotIds = List.of(available.get(0));

        // Alice submits
        long aliceBookingId = BookingService.submit(userId, "Alice Unit", 10, "0812-1111", "Alice purpose", slotIds);

        // Bob submits for same slot
        long bobBookingId = BookingService.submit(bobId, "Bob Unit", 15, "0813-2222", "Bob purpose", slotIds);

        // Both should succeed
        assertNotEquals(aliceBookingId, bobBookingId);
        assertEquals(BookingStatus.PENDING_VERIFICATION, findBooking(aliceBookingId).status());
        assertEquals(BookingStatus.PENDING_VERIFICATION, findBooking(bobBookingId).status());

        // Slot should still be AVAILABLE (not locked until approved)
        TimeSlot slot = Tx.run(c -> SlotDao.find(c, available.get(0)).orElseThrow());
        assertEquals(SlotStatus.AVAILABLE, slot.status());
    }

    // UC02-11: Concurrent submissions for same slot
    @Test
    void concurrentSubmissionsForSameSlot() throws Exception {
        long bobId = Tx.run(c -> UserDao.findByEmail(c, "bob@example.com").map(User::id).orElseThrow());

        LocalDate date = TEST_DATE.plusDays(10);
        List<Long> available = availableSlotIds(date);
        assertTrue(available.size() >= 1);
        List<Long> slotIds = List.of(available.get(0));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);

        Future<?> aliceFuture = executor.submit(() -> {
            try {
                startLatch.await();
                BookingService.submit(userId, "Alice", 10, "0812-1111", "Alice", slotIds);
                successCount.incrementAndGet();
            } catch (Exception ignored) {
            }
        });

        Future<?> bobFuture = executor.submit(() -> {
            try {
                startLatch.await();
                BookingService.submit(bobId, "Bob", 10, "0813-2222", "Bob", slotIds);
                successCount.incrementAndGet();
            } catch (Exception ignored) {
            }
        });

        startLatch.countDown();
        aliceFuture.get();
        bobFuture.get();
        executor.shutdown();

        // Both should succeed since PendingVerification doesn't lock slots
        assertEquals(2, successCount.get());
    }

    // Submit with no slots fails
    @Test
    void submitWithNoSlotsFails() {
        ValidationException e = assertThrows(ValidationException.class, () ->
                BookingService.submit(userId, "Test", 10, "0812-1234", "Test", List.of()));
        assertEquals("p03", e.getMessageKey());
    }

    // Submit with null slots fails
    @Test
    void submitWithNullSlotsFails() {
        ValidationException e = assertThrows(ValidationException.class, () ->
                BookingService.submit(userId, "Test", 10, "0812-1234", "Test", null));
        assertEquals("p03", e.getMessageKey());
    }

    // Submit with non-existent slot ID fails
    @Test
    void submitWithNonExistentSlotFails() {
        SlotUnavailableException e = assertThrows(SlotUnavailableException.class, () ->
                BookingService.submit(userId, "Test", 10, "0812-1234", "Test", List.of(999999L)));
        assertEquals("p04", e.getMessageKey());
    }
}
