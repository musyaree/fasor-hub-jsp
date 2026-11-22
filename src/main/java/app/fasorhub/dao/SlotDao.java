package app.fasorhub.dao;

import app.fasorhub.model.SlotStatus;
import app.fasorhub.model.TimeSlot;
import app.fasorhub.util.Jdbc;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

// Reads and writes the time slots table
public final class SlotDao {
    private static final String COLUMNS = "id, facility_id, `date`, start_time, end_time, status";

    private SlotDao() {
    }

    static TimeSlot map(ResultSet row) throws SQLException {
        return new TimeSlot(row.getLong("id"), row.getLong("facility_id"), row.getDate("date").toLocalDate(),
                row.getTime("start_time").toLocalTime(), row.getTime("end_time").toLocalTime(), SlotStatus.of(row.getString("status")));
    }

    public static List<TimeSlot> findByFacilityAndDate(Connection c, long facilityId, LocalDate date) throws SQLException {
        return Jdbc.query(c, "SELECT " + COLUMNS + " FROM time_slots WHERE facility_id = ? AND `date` = ? ORDER BY start_time",
                SlotDao::map, facilityId, java.sql.Date.valueOf(date));
    }

    public static Optional<TimeSlot> find(Connection c, long id) throws SQLException {
        return Jdbc.queryOne(c, "SELECT " + COLUMNS + " FROM time_slots WHERE id = ?", SlotDao::map, id);
    }

    public static List<TimeSlot> findByBooking(Connection c, long bookingId) throws SQLException {
        return Jdbc.query(c, "SELECT " + COLUMNS + " FROM time_slots WHERE id IN "
                + "(SELECT time_slot_id FROM booking_time_slot WHERE booking_id = ?) ORDER BY start_time", SlotDao::map, bookingId);
    }

    // Locks the slots of one booking in a fixed order so two approvals cannot deadlock
    public static List<TimeSlot> lockByBookingOrderedById(Connection c, long bookingId) throws SQLException {
        return Jdbc.query(c, "SELECT " + COLUMNS + " FROM time_slots WHERE id IN "
                + "(SELECT time_slot_id FROM booking_time_slot WHERE booking_id = ?) ORDER BY id FOR UPDATE", SlotDao::map, bookingId);
    }

    public static void setStatus(Connection c, Collection<Long> ids, SlotStatus status) throws SQLException {
        if (ids.isEmpty()) {
            return;
        }

        Object[] params = new Object[ids.size() + 1];
        params[0] = status.code();

        int i = 1;

        for (Long id : ids) {
            params[i++] = id;
        }

        Jdbc.update(c, "UPDATE time_slots SET status = ? WHERE id IN (" + Jdbc.marks(ids.size()) + ")", params);
    }

    // Adds a slot only when it does not exist yet so running twice is harmless
    public static int insertIfAbsent(Connection c, long facilityId, LocalDate date, java.time.LocalTime start, java.time.LocalTime end, SlotStatus status)
            throws SQLException {
        return Jdbc.update(c, "INSERT IGNORE INTO time_slots (facility_id, `date`, start_time, end_time, status) VALUES (?, ?, ?, ?, ?)",
                facilityId, java.sql.Date.valueOf(date), java.sql.Time.valueOf(start), java.sql.Time.valueOf(end), status.code());
    }

    public static long count(Connection c) throws SQLException {
        return Jdbc.queryOne(c, "SELECT COUNT(*) FROM time_slots", r -> r.getLong(1)).orElse(0L);
    }
}
