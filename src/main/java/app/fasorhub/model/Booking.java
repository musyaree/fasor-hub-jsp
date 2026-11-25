package app.fasorhub.model;

import java.time.LocalDateTime;

// One row of the bookings table
public record Booking(
        long id,
        long userId,
        String organizationUnit,
        int participantCount,
        String contact,
        String purpose,
        BookingStatus status,
        String rejectionReason,
        String cancellationReason,
        BookingStatus previousStatus,
        LocalDateTime cancellationRequestedAt) {
    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getOrganizationUnit() {
        return organizationUnit;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public String getContact() {
        return contact;
    }

    public String getPurpose() {
        return purpose;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public BookingStatus getPreviousStatus() {
        return previousStatus;
    }

    public LocalDateTime getCancellationRequestedAt() {
        return cancellationRequestedAt;
    }
}
