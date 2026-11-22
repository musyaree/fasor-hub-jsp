package app.fasorhub.dao;

import app.fasorhub.model.FacilityType;
import app.fasorhub.model.FacilityTypeRow;
import app.fasorhub.util.Jdbc;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// Reads and writes the facility types table
public final class FacilityTypeDao {
    private FacilityTypeDao() {
    }

    static FacilityType map(ResultSet row) throws SQLException {
        return new FacilityType(row.getLong("id"), row.getString("name"));
    }

    public static List<FacilityType> findAll(Connection c) throws SQLException {
        return Jdbc.query(c, "SELECT id, name FROM facility_types ORDER BY name", FacilityTypeDao::map);
    }

    public static List<FacilityTypeRow> findAllWithCounts(Connection c) throws SQLException {
        return Jdbc.query(c, "SELECT t.id, t.name, (SELECT COUNT(*) FROM facilities f WHERE f.type_id = t.id) AS used "
                + "FROM facility_types t ORDER BY t.name", r -> new FacilityTypeRow(map(r), r.getLong("used")));
    }

    public static Optional<FacilityType> find(Connection c, long id) throws SQLException {
        return Jdbc.queryOne(c, "SELECT id, name FROM facility_types WHERE id = ?", FacilityTypeDao::map, id);
    }

    // Keeps the type from being deleted while a facility is being saved with it
    public static Optional<FacilityType> lockShared(Connection c, long id) throws SQLException {
        return Jdbc.queryOne(c, "SELECT id, name FROM facility_types WHERE id = ? FOR SHARE", FacilityTypeDao::map, id);
    }

    public static Optional<FacilityType> lockById(Connection c, long id) throws SQLException {
        return Jdbc.queryOne(c, "SELECT id, name FROM facility_types WHERE id = ? FOR UPDATE", FacilityTypeDao::map, id);
    }

    // The column collation ignores case so this also finds the same name written differently
    public static boolean nameTaken(Connection c, String name, long exceptId) throws SQLException {
        return Jdbc.queryOne(c, "SELECT COUNT(*) FROM facility_types WHERE name = ? AND id <> ?", r -> r.getLong(1), name, exceptId)
                .orElse(0L) > 0;
    }

    public static long insert(Connection c, String name) throws SQLException {
        return Jdbc.insert(c, "INSERT INTO facility_types (name) VALUES (?)", name);
    }

    public static void rename(Connection c, long id, String name) throws SQLException {
        Jdbc.update(c, "UPDATE facility_types SET name = ? WHERE id = ?", name, id);
    }

    public static long countFacilities(Connection c, long id) throws SQLException {
        return Jdbc.queryOne(c, "SELECT COUNT(*) FROM facilities WHERE type_id = ?", r -> r.getLong(1), id).orElse(0L);
    }

    public static void delete(Connection c, long id) throws SQLException {
        Jdbc.update(c, "DELETE FROM facility_types WHERE id = ?", id);
    }
}
