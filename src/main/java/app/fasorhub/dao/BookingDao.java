package app.fasorhub.dao;

import app.fasorhub.model.Booking;
import app.fasorhub.model.BookingStatus;
import app.fasorhub.util.Jdbc;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
}
