package app.fasorhub.dao;

import app.fasorhub.model.Booking;
import app.fasorhub.model.BookingStatus;
import app.fasorhub.model.Facility;
import app.fasorhub.model.QueueItem;
import app.fasorhub.model.TimeSlot;
import app.fasorhub.util.Jdbc;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

// Reads and writes the bookings table
public final class BookingDao {
    private static final String HAS_PAST_SLOT = "EXISTS (SELECT 1 FROM booking_time_slot bts JOIN time_slots ts ON ts.id = bts.time_slot_id "
        + "WHERE bts.booking_id = b.id AND ts.`date` < ?)";

    private BookingDao() {
    }

    static Booking map(ResultSet row) throws SQLException {
        String previous = row.getString("previous_status");

        Timestamp requested = row.getTimestamp("cancellation_requested_at");

        return new Booking(row.getLong("id"), row.getLong("user_id"), row.getString("organization_unit"), row.getInt("participant_count"),
                row.getString("contact"), row.getString("purpose"), BookingStatus.of(row.getString("status")), row.getString("rejection_reason"),
                row.getString("cancellation_reason"), previous == null ? null : BookingStatus.of(previous),
                requested == null ? null : requested.toLocalDateTime());
    }

    public static Optional<Booking> find(Connection c, long id) throws SQLException {
        return Jdbc.queryOne(c, "SELECT * FROM bookings WHERE id = ?", BookingDao::map, id);
    }

    // Locks the booking row so nobody else can change it until the transaction ends
    public static Optional<Booking> lockById(Connection c, long id) throws SQLException {
        return Jdbc.queryOne(c, "SELECT * FROM bookings WHERE id = ? FOR UPDATE", BookingDao::map, id);
    }

    // Bookings still waiting for a decision whose date has already passed
    public static List<Long> findOverdueIds(Connection c, LocalDate today) throws SQLException {
        return Jdbc.query(c, "SELECT b.id FROM bookings b WHERE b.status IN ('1', '4') AND " + HAS_PAST_SLOT + " ORDER BY b.id",
                r -> r.getLong(1), java.sql.Date.valueOf(today));
    }

    public static boolean isOverdue(Connection c, long id, LocalDate today) throws SQLException {
        return Jdbc.queryOne(c, "SELECT COUNT(*) FROM bookings b WHERE b.id = ? AND " + HAS_PAST_SLOT, r -> r.getLong(1), id,
                java.sql.Date.valueOf(today)).orElse(0L) > 0;
    }
    private record Waiting(Booking booking, String requesterName, String requesterIdentity, LocalDateTime createdAt) {
    }

    // Bookings waiting for verification whose date has not passed and the oldest comes first
    public static List<QueueItem> findQueue(Connection c, LocalDate today) throws SQLException {
        Map<Long, Facility> facilities = FacilityDao.findAll(c).stream().collect(Collectors.toMap(Facility::id, Function.identity()));

        List<Waiting> waiting = Jdbc.query(c, "SELECT b.*, u.name AS requester_name, u.identity AS requester_identity FROM bookings b "
                + "JOIN users u ON u.id = b.user_id WHERE b.status = '1' AND NOT " + HAS_PAST_SLOT + " ORDER BY b.created_at, b.id",
                r -> new Waiting(map(r), r.getString("requester_name"), r.getString("requester_identity"),
                        r.getTimestamp("created_at").toLocalDateTime()), java.sql.Date.valueOf(today));

        List<QueueItem> queue = new ArrayList<>();

        for (Waiting row : waiting) {
            List<TimeSlot> slots = SlotDao.findByBooking(c, row.booking().id());
            TimeSlot first = slots.get(0);

            queue.add(new QueueItem(row.booking(), row.requesterName(), row.requesterIdentity(), facilities.get(first.facilityId()), first.date(),
                    slots, row.createdAt(), slots.stream().allMatch(s -> s.isAvailable(today))));
        }

        return queue;
    }

    public static void attachSlots(Connection c, long bookingId, Collection<Long> slotIds) throws SQLException {
        for (Long slotId : slotIds) {
            Jdbc.update(c, "INSERT INTO booking_time_slot (booking_id, time_slot_id) VALUES (?, ?)", bookingId, slotId);
        }
    }

    public static void setStatus(Connection c, long id, BookingStatus status) throws SQLException {
        Jdbc.update(c, "UPDATE bookings SET status = ? WHERE id = ?", status.code(), id);
    }

    public static void reject(Connection c, long id, String reason) throws SQLException {
        Jdbc.update(c, "UPDATE bookings SET status = '3', rejection_reason = ? WHERE id = ?", reason, id);
    }

    /**
     * Inserts a new booking with PENDING_VERIFICATION status.
     *
     * @return the generated booking id
     */
    public static long insert(Connection c, long userId, String unit, int participants, String contact, String purpose) throws SQLException {
        return Jdbc.insert(c,
                "INSERT INTO bookings (user_id, organization_unit, participant_count, contact, purpose, status, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, '1', NOW())",
                userId, unit, participants, contact, purpose);
    }

    /**
     * Counts bookings waiting for verification (status = PENDING_VERIFICATION)
     * that have not passed their booking date.
     */
    public static int countPendingVerification(Connection c, LocalDate today) throws SQLException {
        return Jdbc.queryOne(c,
                "SELECT COUNT(*) FROM bookings b WHERE b.status = '1' AND NOT " + HAS_PAST_SLOT,
                r -> r.getInt(1), java.sql.Date.valueOf(today)).orElse(0);
    }

    /**
     * Counts bookings waiting for cancellation confirmation (status = PENDING_CANCELLATION)
     * that have not passed their booking date.
     */
    public static int countPendingCancellation(Connection c, LocalDate today) throws SQLException {
        return Jdbc.queryOne(c,
                "SELECT COUNT(*) FROM bookings b WHERE b.status = '4' AND NOT " + HAS_PAST_SLOT,
                r -> r.getInt(1), java.sql.Date.valueOf(today)).orElse(0);
    }

    /**
     * Finds all bookings for a user whose booking date is today or later.
     * Returns the most recently created first.
     */
    public static List<UpcomingBooking> findUpcomingByUser(Connection c, long userId, LocalDate today) throws SQLException {
        // HAS_FUTURE_SLOT: booking has at least one slot dated >= today
        String hasFutureSlot = "EXISTS (SELECT 1 FROM booking_time_slot bts JOIN time_slots ts ON ts.id = bts.time_slot_id "
                + "WHERE bts.booking_id = b.id AND ts.`date` >= ?)";

        return Jdbc.query(c,
                "SELECT b.*, b.created_at AS submitted_at FROM bookings b "
                        + "WHERE b.user_id = ? AND " + hasFutureSlot + " ORDER BY b.created_at DESC, b.id DESC",
                r -> new UpcomingBooking(map(r), r.getTimestamp("submitted_at").toLocalDateTime()),
                userId, java.sql.Date.valueOf(today));
    }

    public record UpcomingBooking(Booking booking, LocalDateTime createdAt) {
    }
}
