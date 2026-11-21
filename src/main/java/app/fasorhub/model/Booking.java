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
}
