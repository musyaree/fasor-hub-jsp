package app.fasorhub.web;

import app.fasorhub.dao.BookingDao;
import app.fasorhub.dao.FacilityDao;
import app.fasorhub.exception.BookingStatusChangedException;
import app.fasorhub.exception.SlotUnavailableException;
import app.fasorhub.model.Facility;
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        show(req, resp, paramLong(req, "booking"), null,
                "1".equals(param(req, "reject")), null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
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

    private void approve(HttpServletRequest req, HttpServletResponse resp, long id)
            throws IOException {
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

    private void reject(HttpServletRequest req, HttpServletResponse resp, long id)
            throws ServletException, IOException {
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

    private void show(HttpServletRequest req, HttpServletResponse resp, Long selectedId,
                      String reason, boolean rejectOpen, String reasonError)
            throws ServletException, IOException {
        List<Facility> facilities = Tx.run(c -> FacilityDao.findAll(c));
        Long requestedFacilityId = paramLong(req, "facility");
        Long facilityId = requestedFacilityId != null
                && requestedFacilityId > 0
                && facilities.stream().anyMatch(facility -> facility.id() == requestedFacilityId)
                ? requestedFacilityId
                : null;

        List<QueueItem> allQueue = Tx.run(c -> BookingDao.findQueue(c, Clock.today()));
        List<QueueItem> queue = facilityId == null
                ? allQueue
                : Tx.run(c -> BookingDao.findQueue(c, Clock.today(), facilityId));

        QueueItem selected = queue.stream()
                .filter(item -> selectedId != null && item.getId() == selectedId)
                .findFirst()
                .orElse(null);

        req.setAttribute("queue", queue);
        req.setAttribute("selected", selected);
        req.setAttribute("rejectOpen", rejectOpen && selected != null);
        req.setAttribute("reasonError", reasonError);
        req.setAttribute("reason", reason != null ? reason : param(req, "reason"));
        req.setAttribute("facilities", facilities);
        req.setAttribute("facilityFilter", facilityId == null ? "" : facilityId.toString());
        req.setAttribute("filterEmpty", facilityId != null && queue.isEmpty() && !allQueue.isEmpty());

        render(req, resp, "manage-verification.jsp");
    }

    private String verificationUrl(HttpServletRequest req, Long bookingId) {
        StringBuilder url = new StringBuilder("/verification");
        boolean hasQuery = false;

        if (bookingId != null) {
            url.append("?booking=").append(bookingId);
            hasQuery = true;
        }

        Long facilityId = paramLong(req, "facility");
        if (facilityId != null && facilityId > 0) {
            url.append(hasQuery ? "&" : "?").append("facility=").append(facilityId);
        }

        return url.toString();
    }
}