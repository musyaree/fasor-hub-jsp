package app.fasorhub.web;

import app.fasorhub.dao.BookingDao;
import app.fasorhub.exception.BookingStatusChangedException;
import app.fasorhub.exception.SlotUnavailableException;
import app.fasorhub.model.QueueItem;
import app.fasorhub.service.BookingService;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Tx;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet({"/verification", "/verification/approve", "/verification/reject"})
public class VerificationServlet extends BaseServlet {
    private static final int MAX_REASON = 255;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        show(req, resp, paramLong(req, "booking"), null, "1".equals(param(req, "reject")), null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = paramLong(req, "bookingId");
        if (id == null) {
            redirect(req, resp, "/verification");
            return;
        }

        if (req.getServletPath().endsWith("/approve")) {
            approve(req, resp, id);
        } else {
            reject(req, resp, id);
        }
    }

    private void approve(HttpServletRequest req, HttpServletResponse resp, long id) throws IOException {
        try {
            BookingService.approve(id);
            flashSuccess(req, msg("p12"));
            redirect(req, resp, "/verification");
        } catch (SlotUnavailableException e) {
            flashWarning(req, msg(e.getMessageKey()));
            redirect(req, resp, "/verification?booking=" + id);
        } catch (BookingStatusChangedException e) {
            flashWarning(req, msg(e.getMessageKey()));
            redirect(req, resp, "/verification");
        }
    }

    private void reject(HttpServletRequest req, HttpServletResponse resp, long id) throws ServletException, IOException {
        String reason = param(req, "reason");

        if (reason == null) {
            show(req, resp, id, null, true, msg("p14"));
            return;
        }

        if (reason.length() > MAX_REASON) {
            reason = reason.substring(0, MAX_REASON);
        }

        try {
            BookingService.reject(id, reason);
            flashError(req, msg("p13"));
            redirect(req, resp, "/verification");
        } catch (BookingStatusChangedException e) {
            flashWarning(req, msg(e.getMessageKey()));
            redirect(req, resp, "/verification");
        }
    }

    private void show(HttpServletRequest req, HttpServletResponse resp, Long selectedId, String reason, boolean rejectOpen, String reasonError)
            throws ServletException, IOException {
        List<QueueItem> queue = Tx.run(c -> BookingDao.findQueue(c, Clock.today()));

        QueueItem selected = queue.stream().filter(item -> selectedId != null && item.getId() == selectedId).findFirst().orElse(null);

        req.setAttribute("queue", queue);
        req.setAttribute("selected", selected);
        req.setAttribute("rejectOpen", rejectOpen && selected != null);
        req.setAttribute("reasonError", reasonError);
        req.setAttribute("reason", reason != null ? reason : param(req, "reason"));

        render(req, resp, "manage-verification.jsp");
    }
}
