package app.fasorhub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.fasorhub.dao.BookingDao;
import app.fasorhub.dao.SlotDao;
import app.fasorhub.exception.ValidationException;
import app.fasorhub.model.BookingStatus;
import app.fasorhub.model.Facility;
import app.fasorhub.model.SlotStatus;
import app.fasorhub.service.ReportService;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Jdbc;
import app.fasorhub.util.Tx;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReportServiceTest {

    @BeforeEach
    void setUp() {
        TestDb.fresh(true);
    }

    @Test
    void reportsSeededBookingsAndCountsStatuses() {
        // The seeder creates an approved Tennis booking five days from today.
        LocalDate date = Clock.today().plusDays(5);

        ReportService.Report report = ReportService.generate(date, date, null);

        assertEquals(1, report.details().size());
        assertEquals(BookingStatus.APPROVED, report.details().get(0).booking().status());
        assertEquals(1L, count(report, BookingStatus.APPROVED));

        BookingDao.FacilityUsage tennis = report.facilityUsage().stream()
                .filter(row -> row.facilityName().equals("Lapangan Tenis 1"))
                .findFirst()
                .orElseThrow();

        assertEquals(1L, tennis.bookingCount());
        assertEquals(1L, tennis.occupiedSessions());
        assertEquals(12L, tennis.bookableSessions());
        assertEquals(8, tennis.usagePercent());
    }

    @Test
    void excludesPendingCancellationFromUsageButIncludesItsStatusCount() {
        // The seeder creates a pending cancellation six days from today.
        LocalDate date = Clock.today().plusDays(6);

        ReportService.Report report = ReportService.generate(date, date, null);

        assertEquals(1L, count(report, BookingStatus.PENDING_CANCELLATION));
        assertEquals(0L, report.facilityUsage().stream()
                .mapToLong(BookingDao.FacilityUsage::bookingCount)
                .sum());
    }

    @Test
    void closedSessionsAreExcludedFromUsageDenominator() {
        // The seeder creates an approved four-session Basketball booking nine days from today.
        LocalDate date = Clock.today().plusDays(9);
        Facility basketball = ReportService.facilities().stream()
                .filter(facility -> facility.name().equals("Lapangan Basket A"))
                .findFirst()
                .orElseThrow();

        List<Long> availableSlots = Tx.run(c -> Jdbc.query(
                c,
                "SELECT id FROM time_slots WHERE facility_id = ? AND `date` = ? AND status = '1' "
                        + "ORDER BY id LIMIT 4",
                row -> row.getLong(1),
                basketball.id(),
                java.sql.Date.valueOf(date)));

        Tx.runVoid(c -> {
            SlotDao.setStatus(c, availableSlots, SlotStatus.CLOSED);
            return null;
        });

        ReportService.Report report = ReportService.generate(date, date, basketball.id());
        BookingDao.FacilityUsage usage = report.facilityUsage().get(0);

        assertEquals(1L, usage.bookingCount());
        assertEquals(4L, usage.occupiedSessions());
        assertEquals(8L, usage.bookableSessions());
        assertEquals(50, usage.usagePercent());
    }

    @Test
    void rejectsInvalidDateRangeWithP19() {
        LocalDate today = Clock.today();

        ValidationException error = assertThrows(
                ValidationException.class,
                () -> ReportService.generate(today.plusDays(1), today, null));

        assertEquals("p19", error.getMessageKey());
    }

    @Test
    void reportsP20WhenThePeriodHasNoBookings() {
        LocalDate date = Clock.today().plusDays(40);

        ValidationException error = assertThrows(
                ValidationException.class,
                () -> ReportService.generate(date, date, null));

        assertEquals("p20", error.getMessageKey());
    }

    private long count(ReportService.Report report, BookingStatus status) {
        return report.statusCounts().stream()
                .filter(item -> item.status() == status)
                .findFirst()
                .orElseThrow()
                .count();
    }
}