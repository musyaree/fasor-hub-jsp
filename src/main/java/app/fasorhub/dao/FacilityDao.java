package app.fasorhub.dao;

import app.fasorhub.model.Facility;
import app.fasorhub.model.FacilityType;
import app.fasorhub.util.Jdbc;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// Reads and writes the facilities table
public final class FacilityDao {
    private FacilityDao() {
    }

    static Facility map(ResultSet row) throws SQLException {
        return new Facility(row.getLong("id"), row.getString("name"), FacilityType.of(row.getString("type")), row.getString("description"));
    }

    public static List<Facility> findAll(Connection c) throws SQLException {
        return Jdbc.query(c, "SELECT id, name, type, description FROM facilities ORDER BY id", FacilityDao::map);
    }


    public static long insert(Connection c, String name, FacilityType type, String description) throws SQLException {
        return Jdbc.insert(c, "INSERT INTO facilities (name, type, description) VALUES (?, ?, ?)", name, type.code(), description);
    }




    public static long count(Connection c) throws SQLException {
        return Jdbc.queryOne(c, "SELECT COUNT(*) FROM facilities", r -> r.getLong(1)).orElse(0L);
    }
}
