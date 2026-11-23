package app.fasorhub.web;

import app.fasorhub.dao.FacilityDao;
import app.fasorhub.model.Facility;
import app.fasorhub.model.MonthGrid;
import app.fasorhub.model.TimeSlot;
import app.fasorhub.service.ScheduleService;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Db;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

/**
 * Schedule and booking page (S03).
 * <p>
 * GET /schedule - displays facility calendar and sessions.
 * Parameters: facility (id), month (YYYY-MM), date (YYYY-MM-DD).
 * Invalid parameters fall back to defaults.
 */
@WebServlet("/schedule")
public class ScheduleServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (Connection c = Db.get()) {
            // Load all facilities
            List<Facility> facilities = FacilityDao.findAll(c);
            if (facilities.isEmpty()) {
                // No facilities: show empty state
                req.setAttribute("facilities", facilities);
                req.setAttribute("noFacilities", true);
                render(req, resp, "schedule.jsp");
                return;
            }

            // Parse facility parameter, default to first
            Long facilityId = paramLong(req, "facility");
            Facility selectedFacility = null;
            if (facilityId != null) {
                for (Facility f : facilities) {
                    if (f.id() == facilityId) {
                        selectedFacility = f;
                        break;
                    }
                }
            }
            if (selectedFacility == null) {
                selectedFacility = facilities.get(0);
            }

            // Parse month parameter, default to current month
            YearMonth month = parseMonth(param(req, "month"));

            // Build the calendar grid
            MonthGrid grid = ScheduleService.monthGrid(selectedFacility.id(), month);
            boolean noSelectableDates = !ScheduleService.hasSelectableDates(grid);

            // Parse date parameter for session display
            LocalDate selectedDate = parseDate(param(req, "date"));
            List<TimeSlot> slots = null;

            // Validate selected date is in the displayed month and selectable
            if (selectedDate != null) {
                YearMonth selectedMonth = YearMonth.from(selectedDate);
                if (!selectedMonth.equals(month)) {
                    selectedDate = null; // Date not in displayed month
                } else {
                    // Check if date is selectable
                    LocalDate today = Clock.today();
                    if (selectedDate.isBefore(today)) {
                        selectedDate = null; // Past date
                    }
                }
            }

            // Fetch slots for selected date
            if (selectedDate != null) {
                slots = ScheduleService.slotsOn(selectedFacility.id(), selectedDate);
                if (slots.isEmpty()) {
                    selectedDate = null; // No sessions for this date
                    slots = null;
                }
            }

            // Set attributes for JSP
            req.setAttribute("facilities", facilities);
            req.setAttribute("selectedFacility", selectedFacility);
            req.setAttribute("grid", grid);
            req.setAttribute("noSelectableDates", noSelectableDates);
            req.setAttribute("selectedDate", selectedDate);
            req.setAttribute("slots", slots);

            // Format the selected date for display
            if (selectedDate != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
                req.setAttribute("selectedDateFormatted", selectedDate.format(formatter));
            }

            render(req, resp, "schedule.jsp");

        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    private YearMonth parseMonth(String value) {
        if (value == null) {
            return YearMonth.from(Clock.today());
        }
        try {
            return YearMonth.parse(value);
        } catch (DateTimeParseException e) {
            return YearMonth.from(Clock.today());
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
