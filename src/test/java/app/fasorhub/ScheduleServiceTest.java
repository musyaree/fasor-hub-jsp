package app.fasorhub;

import app.fasorhub.dao.FacilityDao;
import app.fasorhub.dao.SlotDao;
import app.fasorhub.model.Cell;
import app.fasorhub.model.DateStatus;
import app.fasorhub.model.Facility;
import app.fasorhub.model.MonthGrid;
import app.fasorhub.model.SlotStatus;
import app.fasorhub.model.TimeSlot;
import app.fasorhub.service.ScheduleService;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Tx;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ScheduleService.monthGrid and slotsOn.
 * <p>
 * Uses the seeded data: Lapangan Futsal A, Lapangan Basket A, etc.
 * Seeder creates slots from H-13 to H+21 (35 days).
 */
class ScheduleServiceTest {

    private static final LocalDate TEST_DATE = LocalDate.of(2026, 10, 25); // H in test plan

    @BeforeEach
    void setUp() {
        // Fix the clock BEFORE seeding so seeder uses the test date
        Clock.fixAt(TEST_DATE.atStartOfDay().toInstant(ZoneOffset.ofHours(7)));
        TestDb.fresh(true);
    }

    @AfterEach
    void tearDown() {
        Clock.reset();
    }

    private long futsalAId() {
        return Tx.run(c -> FacilityDao.findAll(c).stream()
                .filter(f -> f.getName().equals("Lapangan Futsal A"))
                .findFirst()
                .map(Facility::getId)
                .orElseThrow());
    }

    private long basketAId() {
        return Tx.run(c -> FacilityDao.findAll(c).stream()
                .filter(f -> f.getName().equals("Lapangan Basket A"))
                .findFirst()
                .map(Facility::getId)
                .orElseThrow());
    }

    // UC01-01: Status tanggal - Sebagian (Futsal A 28 Okt has 2 Available, 2 Closed)
    @Test
    void monthGridShowsPartialStatusWhenSomeSessionsAvailable() throws Exception {
        long facilityId = futsalAId();
        YearMonth month = YearMonth.of(2026, 10);

        MonthGrid grid = ScheduleService.monthGrid(facilityId, month);

        // Find cell for Oct 28 (H+3)
        Cell oct28 = findCellForDate(grid, LocalDate.of(2026, 10, 28));
        assertNotNull(oct28);
        assertEquals(DateStatus.PARTIAL, oct28.status());
        assertTrue(oct28.isSelectable());
    }

    // UC01-01: Status tanggal - Tersedia (Futsal A 29 Okt all Available)
    @Test
    void monthGridShowsAvailableStatusWhenAllSessionsAvailable() throws Exception {
        long facilityId = futsalAId();
        YearMonth month = YearMonth.of(2026, 10);

        MonthGrid grid = ScheduleService.monthGrid(facilityId, month);

        // Oct 29 should be Available (no closed sessions according to seeder)
        Cell oct29 = findCellForDate(grid, LocalDate.of(2026, 10, 29));
        assertNotNull(oct29);
        assertEquals(DateStatus.AVAILABLE, oct29.status());
        assertTrue(oct29.isSelectable());
    }

    // UC01-01: Status tanggal - Ditutup (Basket A 1 Nov all Closed)
    @Test
    void monthGridShowsClosedStatusWhenAllSessionsClosed() throws Exception {
        long facilityId = basketAId();
        YearMonth month = YearMonth.of(2026, 11);

        MonthGrid grid = ScheduleService.monthGrid(facilityId, month);

        // Nov 1 should be Closed for Basket A
        Cell nov1 = findCellForDate(grid, LocalDate.of(2026, 11, 1));
        assertNotNull(nov1);
        assertEquals(DateStatus.CLOSED, nov1.status());
        assertTrue(nov1.isSelectable()); // Can still view sessions
    }

    // UC01-01: Status tanggal - Penuh (Basket A 3 Nov has booking that makes it full)
    @Test
    void monthGridShowsFullStatusWhenNoSessionsAvailable() throws Exception {
        long facilityId = basketAId();
        YearMonth month = YearMonth.of(2026, 11);

        MonthGrid grid = ScheduleService.monthGrid(facilityId, month);

        // Nov 3 (H+9) has P11 booking (all 4 sessions Approved -> Occupied)
        Cell nov3 = findCellForDate(grid, LocalDate.of(2026, 11, 3));
        assertNotNull(nov3);
        assertEquals(DateStatus.FULL, nov3.status());
        assertTrue(nov3.isSelectable()); // Can still view sessions
    }

    // UC01-04: Tanggal lampau tidak dapat dipilih
    @Test
    void monthGridShowsPastStatusForDateBeforeToday() throws Exception {
        long facilityId = futsalAId();
        YearMonth month = YearMonth.of(2026, 10);

        MonthGrid grid = ScheduleService.monthGrid(facilityId, month);

        // Oct 20 is H-5, should be Past
        Cell oct20 = findCellForDate(grid, LocalDate.of(2026, 10, 20));
        assertNotNull(oct20);
        assertEquals(DateStatus.PAST, oct20.status());
        assertFalse(oct20.isSelectable());
    }

    // UC01-04: Tanggal tanpa sesi tidak dapat dipilih
    @Test
    void monthGridShowsEmptyStatusForDateWithNoSessions() throws Exception {
        long facilityId = futsalAId();
        // Go to a month far in the future where no slots exist
        YearMonth futureMonth = YearMonth.of(2026, 12);

        MonthGrid grid = ScheduleService.monthGrid(facilityId, futureMonth);

        // Dec 15 should have no sessions (beyond H+34)
        Cell dec15 = findCellForDate(grid, LocalDate.of(2026, 12, 15));
        assertNotNull(dec15);
        assertEquals(DateStatus.EMPTY, dec15.status());
        assertFalse(dec15.isSelectable());
    }

    // UC01-02: Memilih tanggal menampilkan sesi
    @Test
    void slotsOnReturnsFourSessionsForDate() throws Exception {
        long facilityId = futsalAId();
        LocalDate date = LocalDate.of(2026, 10, 28); // H+3

        List<TimeSlot> slots = ScheduleService.slotsOn(facilityId, date);

        assertEquals(4, slots.size());
        // First two should be Available, last two Closed (per seeder)
        assertEquals(SlotStatus.AVAILABLE, slots.get(0).status());
        assertEquals(SlotStatus.AVAILABLE, slots.get(1).status());
        assertEquals(SlotStatus.CLOSED, slots.get(2).status());
        assertEquals(SlotStatus.CLOSED, slots.get(3).status());
    }

    // UC01-05: Sebelum tanggal dipilih panel sesi menampilkan P25
    @Test
    void slotsOnReturnsEmptyListForDateWithNoSlots() throws Exception {
        long facilityId = futsalAId();
        LocalDate farFuture = LocalDate.of(2026, 12, 25);

        List<TimeSlot> slots = ScheduleService.slotsOn(facilityId, farFuture);

        assertTrue(slots.isEmpty());
    }

    // UC01-03: Mengganti fasilitas dan bulan
    @Test
    void monthGridNavigationPreviousIsDisabledForCurrentMonth() throws Exception {
        long facilityId = futsalAId();
        YearMonth currentMonth = YearMonth.of(2026, 10);

        MonthGrid grid = ScheduleService.monthGrid(facilityId, currentMonth);

        assertFalse(grid.hasPrevious()); // Cannot go to past month
        assertTrue(grid.hasNext());
    }

    @Test
    void monthGridNavigationPreviousIsEnabledForFutureMonth() throws Exception {
        long facilityId = futsalAId();
        YearMonth futureMonth = YearMonth.of(2026, 11);

        MonthGrid grid = ScheduleService.monthGrid(facilityId, futureMonth);

        assertTrue(grid.hasPrevious()); // Can go back to current month
        assertTrue(grid.hasNext());
    }

    // UC01-06: Bulan tanpa sesi
    @Test
    void hasSelectableDatesReturnsFalseWhenNoSelectableCells() throws Exception {
        long facilityId = futsalAId();
        YearMonth farFuture = YearMonth.of(2026, 12);

        MonthGrid grid = ScheduleService.monthGrid(facilityId, farFuture);

        assertFalse(ScheduleService.hasSelectableDates(grid));
    }

    @Test
    void hasSelectableDatesReturnsTrueWhenSelectableCellsExist() throws Exception {
        long facilityId = futsalAId();
        YearMonth month = YearMonth.of(2026, 10);

        MonthGrid grid = ScheduleService.monthGrid(facilityId, month);

        assertTrue(ScheduleService.hasSelectableDates(grid));
    }

    @Test
    void monthGridTitleIsCorrect() throws Exception {
        YearMonth month = YearMonth.of(2026, 10);

        MonthGrid grid = ScheduleService.monthGrid(futsalAId(), month);

        assertEquals("October 2026", grid.getTitle());
    }

    @Test
    void monthGridHasPaddingCellsAtStartOfMonth() throws Exception {
        // Oct 2026 starts on Thursday, so needs 3 padding cells (Mon-Wed)
        long facilityId = futsalAId();
        YearMonth month = YearMonth.of(2026, 10);

        MonthGrid grid = ScheduleService.monthGrid(facilityId, month);

        // First 3 cells should be padding
        assertTrue(grid.cells().get(0).isPadding());
        assertTrue(grid.cells().get(1).isPadding());
        assertTrue(grid.cells().get(2).isPadding());
        // Fourth cell should be Oct 1
        assertFalse(grid.cells().get(3).isPadding());
        assertEquals(1, grid.cells().get(3).date().getDayOfMonth());
    }

    @Test
    void cellMarksToday() throws Exception {
        long facilityId = futsalAId();
        YearMonth month = YearMonth.of(2026, 10);

        MonthGrid grid = ScheduleService.monthGrid(facilityId, month);

        Cell today = findCellForDate(grid, TEST_DATE);
        assertNotNull(today);
        assertTrue(today.isToday());

        // Another day should not be marked as today
        Cell tomorrow = findCellForDate(grid, TEST_DATE.plusDays(1));
        assertNotNull(tomorrow);
        assertFalse(tomorrow.isToday());
    }

    private Cell findCellForDate(MonthGrid grid, LocalDate date) {
        return grid.cells().stream()
                .filter(c -> date.equals(c.date()))
                .findFirst()
                .orElse(null);
    }
}
