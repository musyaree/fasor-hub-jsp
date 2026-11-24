package app.fasorhub;

import app.fasorhub.dao.FacilityDao;
import app.fasorhub.dao.SlotDao;
import app.fasorhub.model.Facility;
import app.fasorhub.model.SlotStatus;
import app.fasorhub.model.TimeSlot;
import app.fasorhub.service.ScheduleService;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Tx;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManageScheduleTest {
    private static final LocalDate TEST_DATE =
            LocalDate.of(2026, 10, 25);

    @BeforeEach
    void setUp() {
        // Fix the clock before seeding so test dates are predictable.
        Clock.fixAt(TEST_DATE.atStartOfDay()
                .toInstant(ZoneOffset.ofHours(7)));
        TestDb.fresh(true);
    }

    @AfterEach
    void tearDown() {
        Clock.reset();
    }

    @Test
    void uc0605KeepsNewlyOccupiedSlotAndSavesOtherChanges() {
        long facilityId = futsalAId();
        LocalDate futureDate = TEST_DATE.plusDays(20);

        List<TimeSlot> slots = Tx.run(connection ->
                SlotDao.findByFacilityAndDate(
                        connection, facilityId, futureDate));

        TimeSlot slotToClose = slots.get(0);
        TimeSlot slotToOpen = slots.get(1);

        // Set known starting statuses for this test.
        Tx.runVoid(connection -> {
            SlotDao.setStatus(
                    connection,
                    List.of(slotToClose.id()),
                    SlotStatus.AVAILABLE);
            SlotDao.setStatus(
                    connection,
                    List.of(slotToOpen.id()),
                    SlotStatus.CLOSED);
            return null;
        });

        // Simulate another manager approving a booking after the page was loaded.
        Tx.runVoid(connection -> {
            SlotDao.setStatus(
                    connection,
                    List.of(slotToClose.id()),
                    SlotStatus.OCCUPIED);
            return null;
        });

        Map<Long, SlotStatus> changes = new HashMap<>();
        changes.put(slotToClose.id(), SlotStatus.CLOSED);
        changes.put(slotToOpen.id(), SlotStatus.AVAILABLE);

        boolean conflict = ScheduleService.save(changes);

        assertTrue(conflict);
        assertEquals(
                SlotStatus.OCCUPIED,
                findSlot(slotToClose.id()).status());
        assertEquals(
                SlotStatus.AVAILABLE,
                findSlot(slotToOpen.id()).status());
    }

    @Test
    void uc0606DoesNotChangeOccupiedOrPastSlots() {
        long facilityId = futsalAId();
        LocalDate futureDate = TEST_DATE.plusDays(20);
        LocalDate pastDate = TEST_DATE.minusDays(1);

        TimeSlot futureSlot = Tx.run(connection ->
                SlotDao.findByFacilityAndDate(
                                connection, facilityId, futureDate)
                        .get(0));

        TimeSlot pastSlot = Tx.run(connection ->
                SlotDao.findByFacilityAndDate(
                                connection, facilityId, pastDate)
                        .get(0));

        // Put both slots into known starting states before attempting the changes.
        Tx.runVoid(connection -> {
            SlotDao.setStatus(
                    connection,
                    List.of(futureSlot.id()),
                    SlotStatus.OCCUPIED);
            SlotDao.setStatus(
                    connection,
                    List.of(pastSlot.id()),
                    SlotStatus.AVAILABLE);
            return null;
        });

        Map<Long, SlotStatus> directChanges = new HashMap<>();
        directChanges.put(futureSlot.id(), SlotStatus.CLOSED);
        directChanges.put(pastSlot.id(), SlotStatus.CLOSED);

        boolean conflict = ScheduleService.save(directChanges);

        assertTrue(conflict);
        assertEquals(
                SlotStatus.OCCUPIED,
                findSlot(futureSlot.id()).status());
        assertEquals(
                SlotStatus.AVAILABLE,
                findSlot(pastSlot.id()).status());
    }

    private long futsalAId() {
        return Tx.run(connection -> FacilityDao.findAll(connection).stream()
                .filter(facility ->
                        facility.getName().equals("Lapangan Futsal A"))
                .findFirst()
                .map(Facility::getId)
                .orElseThrow());
    }

    private TimeSlot findSlot(long slotId) {
        return Tx.run(connection ->
                SlotDao.find(connection, slotId).orElseThrow());
    }
}