package app.fasorhub.dao;

import app.fasorhub.model.Facility;
import app.fasorhub.model.FacilityRow;
import app.fasorhub.model.FacilityType;
import app.fasorhub.util.Jdbc;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// Reads and writes the facilities table
public final class FacilityDao {
    private static final String COLUMNS = "f.id, f.name, f.description, t.id AS type_id, t.name AS type_name";
    private static final String FROM = " FROM facilities f JOIN facility_types t ON t.id = f.type_id";

    private FacilityDao() {
    }

    static Facility map(ResultSet row) throws SQLException {
        return new Facility(row.getLong("id"), row.getString("name"), new FacilityType(row.getLong("type_id"), row.getString("type_name")),
                row.getString("description"));
    }

    public static List<Facility> findAll(Connection c) throws SQLException {
        return Jdbc.query(c, "SELECT " + COLUMNS + FROM + " ORDER BY f.id", FacilityDao::map);
    }

    // Adds how many sessions and how many different bookings each facility has
    public static List<FacilityRow> findAllWithCounts(Connection c) throws SQLException {
        return Jdbc.query(c, "SELECT " + COLUMNS + ", "
                + "(SELECT COUNT(*) FROM time_slots s WHERE s.facility_id = f.id) AS sessions, "
                + "(SELECT COUNT(DISTINCT bts.booking_id) FROM booking_time_slot bts JOIN time_slots s ON s.id = bts.time_slot_id "
                + "WHERE s.facility_id = f.id) AS bookings" + FROM + " ORDER BY f.id",
                r -> new FacilityRow(map(r), r.getLong("sessions"), r.getLong("bookings")));
    }

    public static Optional<Facility> find(Connection c, long id) throws SQLException {
        return Jdbc.queryOne(c, "SELECT " + COLUMNS + FROM + " WHERE f.id = ?", FacilityDao::map, id);
    }

    // Locks the facility row so an edit and a delete cannot run together
    public static Optional<Facility> lockById(Connection c, long id) throws SQLException {
        return Jdbc.queryOne(c, "SELECT " + COLUMNS + FROM + " WHERE f.id = ? FOR UPDATE", FacilityDao::map, id);
    }

    public static long insert(Connection c, String name, long typeId, String description) throws SQLException {
        return Jdbc.insert(c, "INSERT INTO facilities (name, type_id, description) VALUES (?, ?, ?)", name, typeId, description);
    }

    public static void update(Connection c, long id, String name, long typeId, String description) throws SQLException {
        Jdbc.update(c, "UPDATE facilities SET name = ?, type_id = ?, description = ? WHERE id = ?", name, typeId, description, id);
    }

    public static void delete(Connection c, long id) throws SQLException {
        Jdbc.update(c, "DELETE FROM facilities WHERE id = ?", id);
    }

    public static long count(Connection c) throws SQLException {
        return Jdbc.queryOne(c, "SELECT COUNT(*) FROM facilities", r -> r.getLong(1)).orElse(0L);
    }
}
