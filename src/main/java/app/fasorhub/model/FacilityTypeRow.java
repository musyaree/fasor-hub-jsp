package app.fasorhub.model;

// A type together with the number of facilities that use it
public record FacilityTypeRow(FacilityType type, long facilities) {
    public FacilityType getType() {
        return type;
    }

    public long getFacilities() {
        return facilities;
    }

    public boolean isUsed() {
        return facilities > 0;
    }
}
