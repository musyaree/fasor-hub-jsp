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

        List<StatusItem> items = BookingService.upcomingFor(user.id());

        req.setAttribute("items", items);
        req.setAttribute("noBookings", items.isEmpty());

        render(req, resp, "status.jsp");
    }
}
