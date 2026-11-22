package app.fasorhub.model;

// A facility with the counts the manager needs to decide whether it can be deleted
public record FacilityRow(Facility facility, long sessions, long bookings) {
    public Facility getFacility() {
        return facility;
    }

    public long getSessions() {
        return sessions;
    }

    public long getBookings() {
        return bookings;
    }

    public boolean isDeletable() {
        return bookings == 0;
    }
}
