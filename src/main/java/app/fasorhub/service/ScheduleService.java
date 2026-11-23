package app.fasorhub.service;

import app.fasorhub.dao.SlotDao;
import app.fasorhub.model.Cell;
import app.fasorhub.model.DateStatus;
import app.fasorhub.model.MonthGrid;
import app.fasorhub.model.SlotStatus;
import app.fasorhub.model.TimeSlot;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Db;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Schedule viewing and slot management.
 * <p>
 * Handles building calendar grids and retrieving session slots.
 */
public final class ScheduleService {
    /** Every day has four sessions */
    public static final List<LocalTime[]> SESSIONS = List.of(
            new LocalTime[] {LocalTime.of(8, 0), LocalTime.of(10, 0)},
            new LocalTime[] {LocalTime.of(10, 0), LocalTime.of(12, 0)},
            new LocalTime[] {LocalTime.of(13, 0), LocalTime.of(15, 0)},
            new LocalTime[] {LocalTime.of(15, 0), LocalTime.of(17, 0)});

    /** Number of days slots are generated ahead */
    public static final int SLOT_DAYS = 35;

    private ScheduleService() {
    }

    /**
     * Creates the four daily sessions of one facility for a number of days.
     * Skips sessions that already exist so running twice is harmless.
     */
    public static void generateSlots(Connection c, long facilityId, LocalDate from, int days) throws SQLException {
        for (int day = 0; day < days; day++) {
            for (LocalTime[] session : SESSIONS) {
                SlotDao.insertIfAbsent(c, facilityId, from.plusDays(day), session[0], session[1], SlotStatus.AVAILABLE);
            }
        }
    }

    /**
     * Builds a monthly calendar grid for a facility.
     *
     * @param facilityId the facility to show
     * @param month      the month to display
     * @return a grid with cells for each day plus padding, and navigation flags
     */
    public static MonthGrid monthGrid(long facilityId, YearMonth month) throws SQLException {
        LocalDate today = Clock.today();
        LocalDate first = month.atDay(1);
        LocalDate last = month.atEndOfMonth();

        // Fetch all slots for this month
        Map<LocalDate, List<TimeSlot>> slotsByDate = new HashMap<>();
        try (Connection c = Db.get()) {
            List<TimeSlot> slots = SlotDao.findByFacilityAndDateRange(c, facilityId, first, last);
            for (TimeSlot slot : slots) {
                slotsByDate.computeIfAbsent(slot.date(), d -> new ArrayList<>()).add(slot);
            }
        }

        // Build cells with leading padding (week starts Monday)
        List<Cell> cells = new ArrayList<>();
        int leadingPadding = (first.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue() + 7) % 7;
        for (int i = 0; i < leadingPadding; i++) {
            cells.add(Cell.padding());
        }

        // Add a cell for each day of the month
        for (int day = 1; day <= last.getDayOfMonth(); day++) {
            LocalDate date = month.atDay(day);
            List<TimeSlot> daySlots = slotsByDate.get(date);
            DateStatus status = computeStatus(date, daySlots, today);
            cells.add(new Cell(date, status, date.equals(today)));
        }

        // Navigation: cannot go to a month entirely in the past
        // Previous is allowed if the month contains today or is in the future
        YearMonth currentMonth = YearMonth.from(today);
        boolean hasPrevious = month.isAfter(currentMonth);
        // Next is always allowed (slots may be generated later)
        boolean hasNext = true;

        return new MonthGrid(month, cells, hasPrevious, hasNext);
    }

    /**
     * Computes the derived status for a date based on its slots.
     */
    static DateStatus computeStatus(LocalDate date, List<TimeSlot> slots, LocalDate today) {
        // Past date
        if (date.isBefore(today)) {
            return DateStatus.PAST;
        }

        // No slots for this date
        if (slots == null || slots.isEmpty()) {
            return DateStatus.EMPTY;
        }

        int available = 0;
        int occupied = 0;
        int closed = 0;

        for (TimeSlot slot : slots) {
            switch (slot.status()) {
                case AVAILABLE -> available++;
                case OCCUPIED -> occupied++;
                case CLOSED -> closed++;
            }
        }

        int total = available + occupied + closed;

        // All available
        if (available == total) {
            return DateStatus.AVAILABLE;
        }

        // All closed
        if (closed == total) {
            return DateStatus.CLOSED;
        }

        // No available, at least one occupied
        if (available == 0 && occupied > 0) {
            return DateStatus.FULL;
        }

        // Some available, some not
        return DateStatus.PARTIAL;
    }

    /**
     * Returns the slots for a facility on a specific date.
     *
     * @param facilityId the facility
     * @param date       the date
     * @return list of time slots ordered by start time, or empty if none exist
     */
    public static List<TimeSlot> slotsOn(long facilityId, LocalDate date) throws SQLException {
        try (Connection c = Db.get()) {
            return SlotDao.findByFacilityAndDate(c, facilityId, date);
        }
    }

    /**
     * Checks if the month has any selectable dates (for P01 message).
     */
    public static boolean hasSelectableDates(MonthGrid grid) {
        return grid.cells().stream().anyMatch(Cell::isSelectable);
    }

    public static boolean save(Map<Long, SlotStatus> changes) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
