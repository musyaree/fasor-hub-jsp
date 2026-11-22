package app.fasorhub.model;

// A kind of facility such as Futsal that the manager can add and rename
public record FacilityType(long id, String name) {
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
