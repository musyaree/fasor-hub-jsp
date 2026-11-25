package app.fasorhub.web.manage;

import app.fasorhub.dao.FacilityDao;
import app.fasorhub.exception.BookingStatusChangedException;
import app.fasorhub.model.Facility;
import app.fasorhub.model.QueueItem;
import app.fasorhub.service.BookingService;
import app.fasorhub.util.Tx;
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        show(req, resp, paramLong(req, "booking"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Long bookingId = paramLong(req, "bookingId");

        if (bookingId == null) {
            redirect(req, resp, cancellationUrl(req, null));
            return;
        }

        try {
            BookingService.confirmCancellation(bookingId);
            flashSuccess(req, msg("p17"));
            redirect(req, resp, cancellationUrl(req, null));
        } catch (BookingStatusChangedException e) {
            flashWarning(req, msg(e.getMessageKey()));
            redirect(req, resp, cancellationUrl(req, null));
        } catch (RuntimeException e) {
            flashError(req, msg("p18"));
            redirect(req, resp, cancellationUrl(req, bookingId));
        }
    }

    private void show(HttpServletRequest req, HttpServletResponse resp, Long selectedId)
            throws ServletException, IOException {
        List<Facility> facilities = Tx.run(c -> FacilityDao.findAll(c));
        Long requestedFacilityId = paramLong(req, "facility");
        Long facilityId = requestedFacilityId != null
                && requestedFacilityId > 0
                && facilities.stream().anyMatch(facility -> facility.id() == requestedFacilityId)
                ? requestedFacilityId
                : null;

        List<QueueItem> allQueue = BookingService.pendingCancellations();
        List<QueueItem> queue = facilityId == null
                ? allQueue
                : BookingService.pendingCancellations(facilityId);

        QueueItem selected = queue.stream()
                .filter(item -> selectedId != null && item.getId() == selectedId)
                .findFirst()
                .orElse(null);

        req.setAttribute("queue", queue);
        req.setAttribute("selected", selected);
        req.setAttribute("facilities", facilities);
        req.setAttribute("facilityFilter", facilityId == null ? "" : facilityId.toString());
        req.setAttribute("filterEmpty", facilityId != null && queue.isEmpty() && !allQueue.isEmpty());

        render(req, resp, "manage-cancellations.jsp");
    }

    private String cancellationUrl(HttpServletRequest req, Long bookingId) {
        StringBuilder url = new StringBuilder("/cancellation-requests");
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