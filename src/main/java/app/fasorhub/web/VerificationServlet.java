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
            redirect(req, resp, verificationUrl(req, null));
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
            redirect(req, resp, verificationUrl(req, null));
        } catch (SlotUnavailableException e) {
            flashWarning(req, msg(e.getMessageKey()));
            redirect(req, resp, verificationUrl(req, id));
        } catch (BookingStatusChangedException e) {
            flashWarning(req, msg(e.getMessageKey()));
            redirect(req, resp, verificationUrl(req, null));
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
            redirect(req, resp, verificationUrl(req, null));
        } catch (BookingStatusChangedException e) {
            flashWarning(req, msg(e.getMessageKey()));
            redirect(req, resp, verificationUrl(req, null));
        }
    }

    private void show(HttpServletRequest req, HttpServletResponse resp, Long selectedId, String reason, boolean rejectOpen, String reasonError) throws ServletException, IOException {
        // The selected facility is kept as a GET parameter.
        Long facilityId = paramLong(req, "facility");
        BookingDao.BookingFilter filter =
                new BookingDao.BookingFilter(null, facilityId, null, null);
        List<QueueItem> queue = Tx.run(c ->
                BookingDao.findQueue(c, Clock.today(), filter));
        QueueItem selected = queue.stream()
                .filter(item -> selectedId != null && item.getId() == selectedId)
                .findFirst()
                .orElse(null);
        req.setAttribute("facilities",
                Tx.run(app.fasorhub.dao.FacilityDao::findAll));
        req.setAttribute("facilityFilter", facilityId);
        req.setAttribute("filterActive", facilityId != null);

        // Distinguishes an empty queue from a queue empty only by filter.
        req.setAttribute("noFilteredResults", queue.isEmpty());
        req.setAttribute("queue", queue);
        req.setAttribute("selected", selected);
        req.setAttribute("rejectOpen", rejectOpen && selected != null);
        req.setAttribute("reasonError", reasonError);
        req.setAttribute("reason",
                reason != null ? reason : param(req, "reason"));
        render(req, resp, "manage-verification.jsp");
    }
    // Preserves the active facility filter after a decision.
    private String verificationUrl(HttpServletRequest req, Long bookingId) {
        Long facilityId = paramLong(req, "facility");
        StringBuilder url = new StringBuilder("/verification");

        if (facilityId != null || bookingId != null) {
            url.append('?');

            if (facilityId != null) {
                url.append("facility=").append(facilityId);
            }
            
            if (bookingId != null) {
                if (facilityId != null) {
                    url.append('&');
                }
                url.append("booking=").append(bookingId);
            }
        }
        return url.toString();
    }
}
