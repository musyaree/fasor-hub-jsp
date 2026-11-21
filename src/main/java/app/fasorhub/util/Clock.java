package app.fasorhub.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

// Today always means the Jakarta time zone and tests can fix the moment in time
public final class Clock {
    public static final ZoneId ZONE = ZoneId.of("Asia/Jakarta");
    private static java.time.Clock source = java.time.Clock.system(ZONE);

    private Clock() {
    }

    public static LocalDate today() {
        return LocalDate.now(source);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(source);
    }

    public static void fixAt(Instant moment) {
        source = java.time.Clock.fixed(moment, ZONE);
    }

    public static void reset() {
        source = java.time.Clock.system(ZONE);
    }
}
