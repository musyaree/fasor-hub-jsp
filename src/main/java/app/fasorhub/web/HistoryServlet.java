package app.fasorhub.web;

import app.fasorhub.filter.SecurityFilter;
import app.fasorhub.model.HistoryItem;
import app.fasorhub.model.User;
import app.fasorhub.service.BookingService;
import app.fasorhub.util.DataAccessException;
import app.fasorhub.dao.FacilityDao;
import app.fasorhub.exception.ValidationException;
import app.fasorhub.model.BookingStatus;
import app.fasorhub.model.Facility;
import app.fasorhub.util.Tx;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/history")
public class HistoryServlet extends BaseServlet {
    private static final Logger LOG = Logger.getLogger(HistoryServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute(SecurityFilter.USER_ATTR);

        String statusValue = param(req, "status");
        String facilityValue = param(req, "facility");
        String startValue = param(req, "startDate");
        String endValue = param(req, "endDate");

        BookingStatus status = Arrays.stream(BookingStatus.values())
                .filter(value -> value.code().equals(statusValue))
                .findFirst()
                .orElse(null);

        LocalDate startDate = null;
        LocalDate endDate = null;
        boolean invalidDate = false;

        try {
            if (startValue != null) {
                startDate = LocalDate.parse(startValue);
            }
            if (endValue != null) {
                endDate = LocalDate.parse(endValue);
            }
        } catch (DateTimeParseException e) {
            invalidDate = true;
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            invalidDate = true;
        }

        try {
            List<Facility> facilities = user.role().isManager()
                    ? Tx.run(FacilityDao::findAll)
                    : new ArrayList<>();
            Long facilityId = null;

            if (user.role().isManager() && facilityValue != null) {
                try {
                    long requestedId = Long.parseLong(facilityValue);
                    if (requestedId > 0 && facilities.stream().anyMatch(f -> f.id() == requestedId)) {
                        facilityId = requestedId;
                    }
                } catch (NumberFormatException ignored) {
                    facilityId = null;
                }
            }

            List<HistoryItem> allItems = BookingService.history(user);
            List<HistoryItem> items;

            if (invalidDate) {
                items = allItems;
                req.setAttribute("filterError", msg("p19"));
            } else {
                items = BookingService.history(user, status, facilityId, startDate, endDate);
            }

            boolean hasFilter = status != null || facilityId != null || startDate != null || endDate != null;

            req.setAttribute("items", items);
            req.setAttribute("noBookings", allItems.isEmpty());
            req.setAttribute("filterEmpty", !invalidDate && hasFilter && items.isEmpty() && !allItems.isEmpty());
            req.setAttribute("statuses", BookingStatus.values());
            req.setAttribute("statusFilter", status == null ? "" : status.code());
            req.setAttribute("facilities", facilities);
            req.setAttribute("facilityFilter", facilityId == null ? "" : facilityId.toString());
            req.setAttribute("startDateFilter", startValue == null ? "" : startValue);
            req.setAttribute("endDateFilter", endValue == null ? "" : endValue);
        } catch (ValidationException e) {
            List<HistoryItem> allItems = BookingService.history(user);
            req.setAttribute("items", allItems);
            req.setAttribute("noBookings", allItems.isEmpty());
            req.setAttribute("filterEmpty", false);
            req.setAttribute("filterError", msg(e.getMessageKey()));
        } catch (DataAccessException e) {
            LOG.log(Level.WARNING, "Could not load booking history for user " + user.id(), e);
            req.setAttribute("historyLoadError", true);
        }

        render(req, resp, "history.jsp");
    }
}