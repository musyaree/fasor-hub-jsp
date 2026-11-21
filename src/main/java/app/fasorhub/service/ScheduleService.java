package app.fasorhub.service;

import app.fasorhub.model.SlotStatus;
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

    public static boolean save(Map<Long, SlotStatus> changes) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
