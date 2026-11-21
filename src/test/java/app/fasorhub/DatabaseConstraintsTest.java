package app.fasorhub;

import static org.junit.jupiter.api.Assertions.assertThrows;

import app.fasorhub.util.Jdbc;
import app.fasorhub.util.Passwords;
import app.fasorhub.util.Tx;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DatabaseConstraintsTest {
    @BeforeEach
    void setUp() {
        TestDb.fresh(false);
    }

    private static void insertUser(java.sql.Connection c, String email, String role) throws SQLException {
        Jdbc.update(c, "INSERT INTO users (name, email, password, role) VALUES ('X', ?, ?, ?)", email, Passwords.hash("x"), role);
    }

    private static long insertFacility(java.sql.Connection c) throws SQLException {
        return Jdbc.insert(c, "INSERT INTO facilities (name, type) VALUES ('Court A1', '1')");
    }

    @Test
    void duplicateEmailIsRejected() {
        Tx.runVoid(c -> {
            insertUser(c, "same@example.test", "1");
            return null;
        });
        assertThrows(RuntimeException.class, () -> Tx.runVoid(c -> {
            insertUser(c, "same@example.test", "1");
            return null;
        }));
    }

    @Test
    void duplicateTimeSlotIsRejected() {
        long facility = Tx.run(TestDbHelpers::facility);
        String sql = "INSERT INTO time_slots (facility_id, `date`, start_time, end_time) VALUES (?, '2030-01-01', '08:00', '10:00')";
        Tx.runVoid(c -> {
            Jdbc.update(c, sql, facility);
            return null;
        });
        assertThrows(RuntimeException.class, () -> Tx.runVoid(c -> {
            Jdbc.update(c, sql, facility);
            return null;
        }));
    }

    @Test
    void outOfRangeRoleIsRejected() {
        assertThrows(RuntimeException.class, () -> Tx.runVoid(c -> {
            insertUser(c, "x@example.test", "9");
            return null;
        }));
    }

    @Test
    void outOfRangeFacilityTypeIsRejected() {
        assertThrows(RuntimeException.class, () -> Tx.runVoid(c -> {
            Jdbc.update(c, "INSERT INTO facilities (name, type) VALUES ('X', '9')");
            return null;
        }));
    }

    @Test
    void outOfRangeSlotStatusIsRejected() {
        long facility = Tx.run(TestDbHelpers::facility);
        assertThrows(RuntimeException.class, () -> Tx.runVoid(c -> {
            Jdbc.update(c, "INSERT INTO time_slots (facility_id, `date`, start_time, end_time, status) VALUES (?, '2030-01-01', '08:00', '10:00', '9')", facility);
            return null;
        }));
    }

    @Test
    void outOfRangeBookingStatusIsRejected() {
        long user = Tx.run(c -> {
            insertUser(c, "u@example.test", "1");
            return Jdbc.queryOne(c, "SELECT id FROM users", r -> r.getLong(1)).orElseThrow();
        });
        assertThrows(RuntimeException.class, () -> Tx.runVoid(c -> {
            Jdbc.update(c, "INSERT INTO bookings (user_id, organization_unit, participant_count, contact, purpose, status) VALUES (?, 'X', 1, '0', 'X', '9')", user);
            return null;
        }));
    }

    static final class TestDbHelpers {
        static long facility(java.sql.Connection c) throws SQLException {
            return insertFacility(c);
        }
    }
}
