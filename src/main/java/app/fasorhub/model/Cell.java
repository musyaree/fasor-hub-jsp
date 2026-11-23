package app.fasorhub.model;

import java.time.LocalDate;

/**
 * A single cell in the monthly calendar grid.
 * <p>
 * Each cell represents one day and carries its derived status.
 * Null date means padding (empty cell at start/end of month).
 */
public record Cell(LocalDate date, DateStatus status, boolean today) {

    /** Creates a padding cell with no date */
    public static Cell padding() {
        return new Cell(null, null, false);
    }

    /** Whether this is a padding cell */
    public boolean isPadding() {
        return date == null;
    }

    /** Day of month for display, or empty if padding */
    public String getDay() {
        return date == null ? "" : String.valueOf(date.getDayOfMonth());
    }

    public LocalDate getDate() {
        return date;
    }

    public DateStatus getStatus() {
        return status;
    }

    public boolean isToday() {
        return today;
    }

    public boolean isSelectable() {
        return status != null && status.isSelectable();
    }

    /** CSS class based on status */
    public String getCss() {
        if (isPadding()) {
            return "";
        }
        return status.getCss();
    }
}
