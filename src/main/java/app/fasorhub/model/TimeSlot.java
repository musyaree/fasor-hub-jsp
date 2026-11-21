package app.fasorhub.model;

import java.time.LocalDate;
import java.time.LocalTime;

// A slot is available only when its status is available and its date is not in the past
public record TimeSlot(long id, long facilityId, LocalDate date, LocalTime start, LocalTime end, SlotStatus status) {

    public boolean isAvailable(LocalDate today) {
        return status == SlotStatus.AVAILABLE && !date.isBefore(today);
    }

    public long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public String getRange() {
        return start.toString().substring(0, 5) + "-" + end.toString().substring(0, 5);
    }
}
