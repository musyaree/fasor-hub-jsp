package app.fasorhub;

import app.fasorhub.dao.BookingDao;
import app.fasorhub.model.BookingStatus;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Tx;
import app.fasorhub.dao.FacilityDao;
import app.fasorhub.dao.UserDao;
import app.fasorhub.model.Role;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingFilterTest {

    @BeforeEach
    void setUp() {
        TestDb.fresh(true);
    }

    @AfterEach
    void tearDown() {
        Clock.reset();
    }

    // UC05-08: facility and status filters may be combined.
    @Test
    void historySupportsFacilityAndStatusFilters() {
        long tennisId = Tx.run(c -> FacilityDao.findAll(c).stream()
                .filter(f -> f.name().contains("Tenis"))
                .findFirst()
                .orElseThrow()
                .id());

        BookingDao.BookingFilter filter =
                new BookingDao.BookingFilter(BookingStatus.APPROVED, tennisId, null, null);
        List<BookingDao.UpcomingBooking> result =
                Tx.run(c -> BookingDao.findHistory(c, null, filter));
        assertFalse(result.isEmpty());
        for (BookingDao.UpcomingBooking row : result) {
            assertEquals(BookingStatus.APPROVED, row.booking().status());
        }
    }

    // UC05-09: inclusive date bounds are applied.
    @Test
    void historySupportsInclusiveDateRange() {
        LocalDate start = Clock.today().plusDays(3);
        LocalDate end = Clock.today().plusDays(5);
        BookingDao.BookingFilter filter =
                new BookingDao.BookingFilter(null, null, start, end);
        List<BookingDao.UpcomingBooking> result =
                Tx.run(c -> BookingDao.findHistory(c, null, filter));
        assertFalse(result.isEmpty());
    }

    // UC05-10: reversed date ranges are rejected before querying.
    @Test
    void reversedDateRangeIsInvalid() {
        BookingDao.BookingFilter filter =
                new BookingDao.BookingFilter(null, null, LocalDate.of(2026, 10, 30), LocalDate.of(2026, 10, 25));
        assertTrue(BookingDao.invalidDateRange(filter));
    }

    // UC05-11: valid filter with no match returns an empty result.
    @Test
    void canceledFilterCanProduceNoResults() {
        long aliceId = Tx.run(c -> UserDao.findByEmail(c, "alice@example.com")
                .orElseThrow()
                .id());
        BookingDao.BookingFilter filter =
                new BookingDao.BookingFilter(
                        BookingStatus.CANCELLED,
                        null,
                        null,
                        null);
        List<BookingDao.UpcomingBooking> result =
                Tx.run(c -> BookingDao.findHistory(c, aliceId, filter));
        assertTrue(result.isEmpty());
    }
}