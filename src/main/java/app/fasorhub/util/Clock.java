package app.fasorhub.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

// Today always means the Jakarta time zone and tests can fix the date
public final class Clock {
    public static final ZoneId ZONE = ZoneId.of("Asia/Jakarta");
    private static LocalDate fixedToday;

    private Clock() {
    }

    public static LocalDate today() {
        return fixedToday != null ? fixedToday : LocalDate.now(ZONE);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }

    public static void fixToday(LocalDate date) {
        fixedToday = date;
    }
}
