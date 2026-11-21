package app.fasorhub.util;

import app.fasorhub.dao.BookingDao;
import app.fasorhub.dao.FacilityDao;
import app.fasorhub.dao.SlotDao;
import app.fasorhub.dao.UserDao;
import app.fasorhub.model.BookingStatus;
import app.fasorhub.model.FacilityType;
import app.fasorhub.model.Role;
import app.fasorhub.model.SlotStatus;
import app.fasorhub.model.TimeSlot;
import app.fasorhub.service.ScheduleService;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Fills an empty database with demo accounts facilities time slots and bookings with dates relative to today
public final class Seeder {
    private Seeder() {
    }

    // Does nothing when users already exist so real data is never overwritten
    public static boolean seedIfEmpty() {
        return Tx.run(c -> {
            if (UserDao.count(c) > 0) {
                return false;
            }

            seed(c);

            return true;
        });
    }

    private record Row(String email, int facility, int dayOffset, int[] slots, BookingStatus status, int[] created, String purpose,
                       BookingStatus previous, String cancelReason, int[] cancelAt, String rejectReason) {
    }

    private static void seed(Connection c) throws SQLException {
        String hash = Passwords.hash("password");

        UserDao.insert(c, "Alice Johnson", "alice@example.com", hash, Role.BORROWER, "5025251101", "0812-3456-7890");
        UserDao.insert(c, "Bob Smith", "bob@example.com", hash, Role.BORROWER, "5025251102", "0813-1111-2222");
        UserDao.insert(c, "Carol Davis", "carol@example.com", hash, Role.BORROWER, "Example Student Association", "0857-3333-4444");
        UserDao.insert(c, "Facility Manager", "manager@example.com", hash, Role.MANAGER, null, null);

        List<Long> facilityIds = new ArrayList<>();
        facilityIds.add(FacilityDao.insert(c, "Lapangan Futsal A", FacilityType.FUTSAL, null));
        facilityIds.add(FacilityDao.insert(c, "Lapangan Basket A", FacilityType.BASKETBALL, null));
        facilityIds.add(FacilityDao.insert(c, "Lapangan Badminton 1", FacilityType.BADMINTON, null));
        facilityIds.add(FacilityDao.insert(c, "Lapangan Tenis 1", FacilityType.TENNIS, null));

        LocalDate today = Clock.today();
        LocalDate first = today.minusDays(13);

        for (int position = 0; position < facilityIds.size(); position++) {
            for (int day = 0; day < 35; day++) {
                int offset = day - 13;

                for (int index = 0; index < ScheduleService.SESSIONS.size(); index++) {
                    LocalTime[] session = ScheduleService.SESSIONS.get(index);

                    boolean closed = (position == 0 && offset == 3 && index >= 2) || (position == 1 && offset == 7);
                    SlotDao.insertIfAbsent(c, facilityIds.get(position), first.plusDays(day), session[0], session[1],
                            closed ? SlotStatus.CLOSED : SlotStatus.AVAILABLE);
                }
            }
        }

        BookingStatus ap = BookingStatus.APPROVED;
        BookingStatus pe = BookingStatus.PENDING_VERIFICATION;
        BookingStatus pc = BookingStatus.PENDING_CANCELLATION;

        List<Row> rows = List.of(
                new Row("alice@example.com", 1, -5, new int[] {0}, ap, new int[] {7, 9, 10}, "Regular futsal practice", null, null, null, null),
                new Row("alice@example.com", 2, 3, new int[] {0, 3}, BookingStatus.REJECTED, new int[] {1, 8, 5}, "Basketball practice", null, null, null,
                        Messages.get("p16")),
                new Row("alice@example.com", 1, 4, new int[] {1}, pe, new int[] {1, 8, 15}, "Futsal practice", null, null, null, null),
                new Row("alice@example.com", 4, 5, new int[] {2}, ap, new int[] {2, 10, 30}, "Tennis sparring", null, null, null, null),
                new Row("bob@example.com", 1, 4, new int[] {1}, pe, new int[] {1, 9, 40}, "Lecturer sports activity", null, null, null, null),
                new Row("bob@example.com", 3, 4, new int[] {2, 3}, pe, new int[] {1, 11, 0}, "Badminton practice", null, null, null, null),
                new Row("carol@example.com", 4, 6, new int[] {0}, pc, new int[] {3, 13, 0}, "Tennis practice", ap, "The event was cancelled", new int[] {0, 7, 30}, null),
                new Row("carol@example.com", 1, 8, new int[] {1}, pc, new int[] {2, 15, 20}, "Futsal practice", pe, "Clashes with a class schedule", new int[] {0, 8, 45}, null),
                new Row("carol@example.com", 2, -3, new int[] {2}, BookingStatus.CANCELLED, new int[] {6, 12, 0}, "Basketball practice", ap, "Rain", new int[] {4, 9, 0}, null),
                new Row("carol@example.com", 2, -4, new int[] {0}, BookingStatus.REJECTED, new int[] {7, 12, 0}, "Basketball practice", null, null, null,
                        "The court is in use for another activity"),
                new Row("bob@example.com", 2, 9, new int[] {0, 1, 2, 3}, ap, new int[] {5, 10, 0}, "Internal tournament", null, null, null, null),
                new Row("bob@example.com", 1, -3, new int[] {1}, pe, new int[] {6, 16, 0}, "Lecturer futsal practice", null, null, null, null),
                new Row("carol@example.com", 4, -2, new int[] {1}, pc, new int[] {5, 9, 0}, "Tennis practice", ap, "The activity was moved", new int[] {3, 8, 0}, null));

        for (Row row : rows) {
            var user = UserDao.findByEmail(c, row.email).orElseThrow();

            LocalDateTime created = today.minusDays(row.created[0]).atTime(row.created[1], row.created[2]);
            Timestamp requested = row.cancelAt == null ? null
                    : Timestamp.valueOf(today.minusDays(row.cancelAt[0]).atTime(row.cancelAt[1], row.cancelAt[2]));
            String unit = "Example Student Association".equals(user.identity()) ? "Example Student Association" : "Informatics Student";

            long bookingId = Jdbc.insert(c, "INSERT INTO bookings (user_id, organization_unit, participant_count, contact, purpose, status, "
                    + "rejection_reason, cancellation_reason, previous_status, cancellation_requested_at, created_at) VALUES (?, ?, 10, ?, ?, ?, ?, ?, ?, ?, ?)",
                    user.id(), unit, user.phone(), row.purpose, row.status.code(), row.rejectReason, row.cancelReason,
                    row.previous == null ? null : row.previous.code(), requested, Timestamp.valueOf(created));

            List<TimeSlot> daySlots = SlotDao.findByFacilityAndDate(c, facilityIds.get(row.facility - 1), today.plusDays(row.dayOffset));
            List<Long> slotIds = new ArrayList<>();

            for (int index : row.slots) {
                slotIds.add(daySlots.get(index).id());
            }

            BookingDao.attachSlots(c, bookingId, slotIds);

            boolean locks = row.status == ap || (row.status == pc && row.previous == ap);
            if (locks) {
                SlotDao.setStatus(c, slotIds, SlotStatus.OCCUPIED);
            }
        }
    }
}
