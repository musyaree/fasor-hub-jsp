package app.fasorhub.model;

// The five steps a booking can be in
public enum BookingStatus implements Coded {
    PENDING_VERIFICATION("1", "Pending Verification", "pending", "hourglass_top"),
    APPROVED("2", "Approved", "approved", "check_circle"),
    REJECTED("3", "Rejected", "rejected", "cancel"),
    PENDING_CANCELLATION("4", "Pending Cancellation", "cancelling", "event_busy"),
    CANCELLED("5", "Cancelled", "closed", "block");

    private final String code;
    private final String label;
    private final String css;
    private final String icon;

    BookingStatus(String code, String label, String css, String icon) {
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
        return app.fasorhub.util.Messages.get("booking_status." + code);
    }

    public String getCss() {
        return css;
    }

    public String getIcon() {
        return icon;
    }

    public static BookingStatus of(String code) {
        return Coded.fromCode(BookingStatus.class, code);
    }
}
