package app.fasorhub.service;

import app.fasorhub.dao.BookingDao;
import app.fasorhub.dao.SlotDao;
import app.fasorhub.exception.BookingStatusChangedException;
import app.fasorhub.exception.SlotUnavailableException;
import app.fasorhub.exception.ValidationException;
import app.fasorhub.model.Booking;
import app.fasorhub.model.BookingStatus;
import app.fasorhub.model.SlotStatus;
import app.fasorhub.model.TimeSlot;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Messages;
import app.fasorhub.util.Tx;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Every booking change happens here in one transaction and the booking is always locked before its slots
public final class BookingService {
    private BookingService() {
    }

    /**
     * Submits a new booking request.
     * <p>
     * Validates that:
     * <ul>
     *   <li>1 to 4 slots are selected</li>
     *   <li>All slots belong to the same facility and date</li>
     *   <li>All slots are available (not past, occupied, or closed)</li>
     * </ul>
     * Locks slots by id before checking to prevent race conditions.
     *
     * @return the new booking id
     * @throws SlotUnavailableException if any slot is not available (p04)
     * @throws ValidationException if input is invalid (p03)
     */
    public static long submit(long userId, String unit, int participants, String contact, String purpose, List<Long> slotIds)
            throws ValidationException {
        // Validate input data (outside transaction)
        if (unit == null || unit.isBlank() || participants < 1 || contact == null || contact.isBlank()
                || purpose == null || purpose.isBlank()) {
            throw new ValidationException("p03");
        }

        // Validate slot count (1-4)
        if (slotIds == null || slotIds.isEmpty() || slotIds.size() > 4) {
            throw new ValidationException("p03");
        }

        LocalDate today = Clock.today();

        return Tx.run(c -> {
            // Lock slots in id order to prevent deadlock
            List<TimeSlot> slots = SlotDao.lockByIdsOrderedById(c, slotIds);

            // Check all requested slots were found
            if (slots.size() != slotIds.size()) {
                throw new SlotUnavailableException("p04");
            }

            // Validate all slots are from same facility and same date
            long facilityId = slots.get(0).facilityId();
            LocalDate date = slots.get(0).date();

            for (TimeSlot slot : slots) {
                if (slot.facilityId() != facilityId || !slot.date().equals(date)) {
                    // Slots from different facilities/dates - this is a programming error or tampering
                    throw new SlotUnavailableException("p04");
                }
            }

            // Check all slots are available
            for (TimeSlot slot : slots) {
                if (!slot.isAvailable(today)) {
                    throw new SlotUnavailableException("p04");
                }
            }

            // Create the booking
            long bookingId = BookingDao.insert(c, userId, unit, participants, contact, purpose);

            // Attach slots to booking
            BookingDao.attachSlots(c, bookingId, slotIds);

            return bookingId;
        });
    }

    // Approval needs every slot to be available and then occupies them all or none
    public static void approve(long bookingId) {
        LocalDate today = Clock.today();

        Tx.runVoid(c -> {
            Locked locked = lockBookingThenSlots(c, bookingId);
            Booking booking = locked.booking();
            List<TimeSlot> slots = locked.slots();

            if (slots.isEmpty() || slots.stream().anyMatch(s -> !s.isAvailable(today))) {
                throw new SlotUnavailableException("p15");
            }

            SlotDao.setStatus(c, slots.stream().map(TimeSlot::id).toList(), SlotStatus.OCCUPIED);
            BookingDao.setStatus(c, booking.id(), BookingStatus.APPROVED);

            return null;
        });
    }

    // Rejection only records the reason because a pending booking never held its slots
    public static void reject(long bookingId, String reason) {
        Tx.runVoid(c -> {
            Booking booking = lockPendingBooking(c, bookingId);

            BookingDao.reject(c, booking.id(), reason);

            return null;
        });
    }

    public static void requestCancellation(long bookingId, String reason) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static void confirmCancellation(long bookingId) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    // Runs every overdue booking through expireOverdue and returns how many were changed
    public static int expireAllOverdue() {
        LocalDate today = Clock.today();
        List<Long> ids = Tx.run(c -> BookingDao.findOverdueIds(c, today));

        int changed = 0;

        for (Long id : ids) {
            if (expireOverdue(id)) {
                changed++;
            }
        }

        return changed;
    }

    // One booking per transaction so a failure never rolls back the others and running it twice changes nothing
    public static boolean expireOverdue(long bookingId) {
        LocalDate today = Clock.today();

        return Tx.run(c -> {
            Optional<Booking> locked = BookingDao.lockById(c, bookingId);

            if (locked.isEmpty() || !BookingDao.isOverdue(c, bookingId, today)) {
                return false;
            }

            Booking booking = locked.get();

            if (booking.status() == BookingStatus.PENDING_VERIFICATION) {
                BookingDao.reject(c, booking.id(), Messages.get("p26"));

                return true;
            }

            if (booking.status() == BookingStatus.PENDING_CANCELLATION) {
                releaseIfApproved(c, booking);
                BookingDao.setStatus(c, booking.id(), BookingStatus.CANCELLED);

                return true;
            }

            return false;
        });
    }

    // Only a booking that was approved before the cancellation request holds occupied slots
    static void releaseIfApproved(Connection c, Booking booking) throws SQLException {
        if (booking.previousStatus() != BookingStatus.APPROVED) {
            return;
        }

        List<TimeSlot> slots = SlotDao.lockByBookingOrderedById(c, booking.id());

        SlotDao.setStatus(c, slots.stream().filter(s -> s.status() == SlotStatus.OCCUPIED).map(TimeSlot::id).toList(), SlotStatus.AVAILABLE);
    }

    private record Locked(Booking booking, List<TimeSlot> slots) {
    }

    // Lock order matters so the booking comes first and then its slots
    private static Locked lockBookingThenSlots(Connection c, long bookingId) throws SQLException {
        Booking booking = lockPendingBooking(c, bookingId);

        return new Locked(booking, SlotDao.lockByBookingOrderedById(c, booking.id()));
    }

    // Fails when the booking is no longer waiting for verification
    private static Booking lockPendingBooking(Connection c, long bookingId) throws SQLException {
        Booking booking = BookingDao.lockById(c, bookingId).orElseThrow(() -> new BookingStatusChangedException("p30"));

        if (booking.status() != BookingStatus.PENDING_VERIFICATION) {
            throw new BookingStatusChangedException("p30");
        }

        return booking;
    }
}
