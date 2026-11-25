package app.fasorhub.web.manage;

import app.fasorhub.exception.BookingStatusChangedException;
import app.fasorhub.model.QueueItem;
import app.fasorhub.service.BookingService;
import app.fasorhub.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet({
        "/cancellation-requests",
        "/cancellation-requests/confirm"
})
public class CancellationsServlet extends BaseServlet {

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {

        show(req, resp, paramLong(req, "booking"));
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp) throws IOException {

        Long bookingId = paramLong(req, "bookingId");

        if (bookingId == null) {
            redirect(req, resp, "/cancellation-requests");
            return;
        }

        try {
            BookingService.confirmCancellation(bookingId);

            // P17: Cancellation confirmed.
            flashSuccess(req, msg("p17"));
            redirect(req, resp, "/cancellation-requests");

        } catch (BookingStatusChangedException e) {
            // P32: The cancellation request was already processed.
            flashWarning(req, msg(e.getMessageKey()));
            redirect(req, resp, "/cancellation-requests");

        } catch (RuntimeException e) {
            // P18: Processing failed. The transaction rolls back all changes.
            flashError(req, msg("p18"));
            redirect(req, resp,
                    "/cancellation-requests?booking=" + bookingId);
        }
    }

    private void show(HttpServletRequest req, HttpServletResponse resp, Long selectedId) throws ServletException, IOException {
        List<QueueItem> queue = BookingService.pendingCancellations();
        QueueItem selected = queue.stream()
                .filter(item -> selectedId != null && item.getId() == selectedId)
                .findFirst()
                .orElse(null);
        req.setAttribute("queue", queue);
        req.setAttribute("selected", selected);
        render(req, resp, "manage-cancellations.jsp");
    }
}