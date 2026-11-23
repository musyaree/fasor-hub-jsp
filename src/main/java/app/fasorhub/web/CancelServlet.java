package app.fasorhub.web;

import app.fasorhub.exception.BookingStatusChangedException;
import app.fasorhub.exception.ValidationException;
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
 * Cancellation request page (S05).
 * <p>
 * GET /cancellations - displays user's bookings that can be cancelled.
 * POST /cancellations - submits a cancellation request.
 */
@WebServlet("/cancellations")
public class CancelServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute(SecurityFilter.USER_ATTR);

        List<StatusItem> items = BookingService.cancellableFor(user.id());

        // Find selected booking if specified
        Long selectedId = paramLong(req, "booking");
        StatusItem selected = null;
        if (selectedId != null) {
            selected = items.stream()
                    .filter(i -> i.getId() == selectedId)
                    .findFirst()
                    .orElse(null);
        }

        req.setAttribute("items", items);
        req.setAttribute("selected", selected);
        req.setAttribute("noBookings", items.isEmpty());

        render(req, resp, "cancel.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute(SecurityFilter.USER_ATTR);

        Long bookingId = paramLong(req, "booking");
        String reason = param(req, "reason");

        if (bookingId == null) {
            flashError(req, msg("p31"));
            redirect(req, resp, "/cancellations");
            return;
        }

        try {
            BookingService.requestCancellation(user.id(), bookingId, reason);

            flashSuccess(req, msg("p06"));
            redirect(req, resp, "/cancellations");

        } catch (ValidationException e) {
            // Reason is blank
            flashError(req, msg(e.getMessageKey()));
            redirect(req, resp, "/cancellations?booking=" + bookingId);

        } catch (BookingStatusChangedException e) {
            // Booking can no longer be cancelled
            flashError(req, msg(e.getMessageKey()));
            redirect(req, resp, "/cancellations");
        }
    }
}
