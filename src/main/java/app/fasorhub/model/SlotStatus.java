package app.fasorhub.model;

// Whether a time slot can be booked
public enum SlotStatus implements Coded {
    AVAILABLE("1", "Available", "approved", "check_circle"),
    OCCUPIED("2", "Occupied", "rejected", "cancel"),
    CLOSED("3", "Closed", "closed", "block");

    private final String code;
    private final String label;
    private final String css;
    private final String icon;

    SlotStatus(String code, String label, String css, String icon) {
        this.code = code;
        this.label = label;
        this.css = css;
        this.icon = icon;
    }

    @Override
    public String code() {
        return code;
    }

    public String getLabel() {
        return app.fasorhub.util.Messages.get("slot_status." + code);
    }

    public String getCss() {
        return css;
    }

    public String getIcon() {
        return icon;
    }

    public static SlotStatus of(String code) {
        return Coded.fromCode(SlotStatus.class, code);
    }
}
