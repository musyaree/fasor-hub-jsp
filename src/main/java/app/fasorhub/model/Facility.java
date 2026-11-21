package app.fasorhub.model;

// A sports facility that can be booked
public record Facility(long id, String name, FacilityType type, String description) {
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public FacilityType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
