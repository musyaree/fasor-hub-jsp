package app.fasorhub.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

// A booking with the information displayed in booking history
public record HistoryItem(Booking booking, String requesterName, String requesterIdentity, Facility facility,
                          LocalDate date, List<TimeSlot> slots, LocalDateTime createdAt) {
    private static final DateTimeFormatter SUBMITTED =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter DAY =
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    public long getId() {
        return booking.id();
    }

    public String getRequester() {
        return requesterIdentity == null || requesterIdentity.isBlank() ? requesterName : requesterIdentity;
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

    public String getPurpose() {
        return booking.purpose();
    }

    public String getRejectionReason() {
        return booking.rejectionReason();
    }
}