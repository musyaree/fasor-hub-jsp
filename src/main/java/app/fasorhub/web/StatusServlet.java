package app.fasorhub.web;

import app.fasorhub.filter.SecurityFilter;
import app.fasorhub.model.StatusItem;
import app.fasorhub.model.User;
import app.fasorhub.service.BookingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Booking status page (S04).
 * <p>
 * GET /booking-status - displays user's bookings dated today or later.
 */
@WebServlet("/booking-status")
public class StatusServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute(SecurityFilter.USER_ATTR);
        BookingStatus status = null;
        String statusCode = param(req, "status");
        if (statusCode != null) {
            try {
                status = BookingStatus.of(statusCode);
            } catch (IllegalArgumentException ignored) {
                status = null;
            }
        }
        LocalDate startDate = parseDate(req, "startDate");
        LocalDate endDate = parseDate(req, "endDate");
        BookingDao.BookingFilter filter =
                new BookingDao.BookingFilter(
                        status,
                        paramLong(req, "facility"),
                        startDate,
                        endDate
                );
        // Invalid periods show P19 and do not execute the query.
        if (BookingDao.invalidDateRange(filter)) {
            req.setAttribute("dateError", msg("p19"));
            req.setAttribute("items", List.of());
            req.setAttribute("noBookings", false);
            req.setAttribute("filterActive", filter.isActive());
            req.setAttribute("noFilteredResults", true);
            render(req, resp, "history.jsp");
            return;
        }
        Long userId = user.role() == Role.MANAGER ? null : user.id();
        List<StatusItem> items = Tx.run(c ->
                loadHistory(c, userId, filter));
        req.setAttribute("items", items);
        req.setAttribute("noBookings", items.isEmpty()
                && !filter.isActive());
        req.setAttribute("filterActive", filter.isActive());
        req.setAttribute("noFilteredResults",
                filter.isActive() && items.isEmpty());
        req.setAttribute("statusFilter", statusCode);
        req.setAttribute("facilityFilter", filter.facilityId());
        req.setAttribute("startDateFilter",
                param(req, "startDate"));
        req.setAttribute("endDateFilter",
                param(req, "endDate"));
        req.setAttribute("isManager",
                user.role() == Role.MANAGER);
        req.setAttribute("facilities",
                Tx.run(FacilityDao::findAll));
        render(req, resp, "history.jsp");
    }
    // Converts DAO history rows into the JSP display model.
    private LocalDate parseDate(HttpServletRequest req, String name) {
        try {
            String value = param(req, name);
            return value == null ? null : LocalDate.parse(value);
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }
}


