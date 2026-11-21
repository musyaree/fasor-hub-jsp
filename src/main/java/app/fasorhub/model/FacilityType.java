package app.fasorhub.model;

// The kinds of sports facilities
public enum FacilityType implements Coded {
    FUTSAL("1", "Futsal"),
    BASKETBALL("2", "Basketball"),
    BADMINTON("3", "Badminton"),
    TENNIS("4", "Tennis");

    private final String code;
    private final String label;

    FacilityType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    @Override
    public String code() {
        return code;
    }

    public String getLabel() {
        return app.fasorhub.util.Messages.get("facility_type." + code);
    }

    public String getName() {
        return name();
    }

    public static FacilityType of(String code) {
        return Coded.fromCode(FacilityType.class, code);
    }
}
