package app.fasorhub.model;

/**
 * Derived status for a calendar date cell.
 * <p>
 * The status is computed from the sessions on that date:
 * <ul>
 *   <li>AVAILABLE - all sessions are available</li>
 *   <li>PARTIAL - some sessions available, some not</li>
 *   <li>FULL - no sessions available, at least one occupied</li>
 *   <li>CLOSED - all sessions closed</li>
 *   <li>PAST - date is before today</li>
 *   <li>EMPTY - no sessions exist for this date</li>
 * </ul>
 */
public enum DateStatus {
    AVAILABLE("available", "Available", "check_circle"),
    PARTIAL("partial", "Partial", "pending"),
    FULL("full", "Full", "cancel"),
    CLOSED("closed", "Closed", "block"),
    PAST("past", "", ""),
    EMPTY("empty", "", "");

    private final String css;
    private final String label;
    private final String icon;

    DateStatus(String css, String label, String icon) {
        this.css = css;
        this.label = label;
        this.icon = icon;
    }

    public String getCss() {
        return css;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }

    /** Whether the cell can be clicked to select the date */
    public boolean isSelectable() {
        return this == AVAILABLE || this == PARTIAL || this == FULL || this == CLOSED;
    }
}
