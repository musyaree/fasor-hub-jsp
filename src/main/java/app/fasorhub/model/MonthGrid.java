package app.fasorhub.model;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * Monthly calendar grid for a facility.
 * <p>
 * Contains the cells for one month, plus navigation metadata.
 */
public record MonthGrid(
        YearMonth month,
        List<Cell> cells,
        boolean hasPrevious,
        boolean hasNext
) {
    /** Month and year for display, e.g. "October 2026" */
    public String getTitle() {
        return month.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + month.getYear();
    }

    /** Query parameter value for this month */
    public String getParam() {
        return month.toString();
    }

    /** Query parameter value for the previous month */
    public String getPrevParam() {
        return month.minusMonths(1).toString();
    }

    /** Query parameter value for the next month */
    public String getNextParam() {
        return month.plusMonths(1).toString();
    }

    public YearMonth getMonth() {
        return month;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public boolean isHasNext() {
        return hasNext;
    }
}
