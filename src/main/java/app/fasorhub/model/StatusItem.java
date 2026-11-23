package app.fasorhub.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * A booking with everything the status page shows.
 * Only includes bookings whose date is today or later.
 */
public record StatusItem(Booking booking, Facility facility, LocalDate date,
                         List<TimeSlot> slots, LocalDateTime createdAt) {
    private static final DateTimeFormatter SUBMITTED = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    public long getId() {
        return booking.id();
    }

    public String getFacilityName() {
        return facility.name();
    }

    public String getDateLabel() {
        return date.format(DAY);
    }

    public String getSubmittedLabel() {
        return createdAt.format(SUBMITTED);
    }

    public String getSlotRanges() {
        return slots.stream().map(TimeSlot::getRange).collect(Collectors.joining(", "));
    }

    public BookingStatus getStatus() {
        return booking.status();
    }

    public String getRejectionReason() {
        return booking.rejectionReason();
    }

    public String getUnit() {
        return booking.organizationUnit();
    }

    public int getParticipants() {
        return booking.participantCount();
    }

    public String getContact() {
        return booking.contact();
    }

    public String getPurpose() {
        return booking.purpose();
    }
}
