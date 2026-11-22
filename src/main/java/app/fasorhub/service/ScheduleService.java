package app.fasorhub.service;

import app.fasorhub.dao.SlotDao;
import app.fasorhub.model.SlotStatus;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

// Opening and closing of time slots will live here
public final class ScheduleService {
    // Every day has four sessions
    public static final List<LocalTime[]> SESSIONS = List.of(
            new LocalTime[] {LocalTime.of(8, 0), LocalTime.of(10, 0)},
            new LocalTime[] {LocalTime.of(10, 0), LocalTime.of(12, 0)},
            new LocalTime[] {LocalTime.of(13, 0), LocalTime.of(15, 0)},
            new LocalTime[] {LocalTime.of(15, 0), LocalTime.of(17, 0)});

    private ScheduleService() {
    }

    // Creates the four daily sessions of one facility for a number of days and skips sessions that already exist
    public static void generateSlots(Connection c, long facilityId, LocalDate from, int days) throws SQLException {
        for (int day = 0; day < days; day++) {
            for (LocalTime[] session : SESSIONS) {
                SlotDao.insertIfAbsent(c, facilityId, from.plusDays(day), session[0], session[1], SlotStatus.AVAILABLE);
            }
        }
    }

    public static boolean save(Map<Long, SlotStatus> changes) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
