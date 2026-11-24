package app.fasorhub.web.manage;

import app.fasorhub.dao.FacilityDao;
import app.fasorhub.dao.SlotDao;
import app.fasorhub.model.Facility;
import app.fasorhub.model.MonthGrid;
import app.fasorhub.model.SlotStatus;
import app.fasorhub.model.TimeSlot;
import app.fasorhub.service.ScheduleService;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Tx;
import app.fasorhub.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet({
        "/manage-schedule",
        "/manage-schedule/stage",
        "/manage-schedule/save"
})
public class ManageScheduleServlet extends BaseServlet {
    private static final String CHANGES_ATTRIBUTE = "manageScheduleChanges";
    private static final String FACILITY_ATTRIBUTE = "manageScheduleFacility";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Facility> facilities = Tx.run(FacilityDao::findAll);

        if (facilities.isEmpty()) {
            req.setAttribute("noFacilities", true);
            render(req, resp, "manage-schedule.jsp");
            return;
        }

        Long requestedFacilityId = paramLong(req, "facility");
        Facility selectedFacility = facilities.stream()
                .filter(facility ->
                        requestedFacilityId != null
                                && facility.getId() == requestedFacilityId)
                .findFirst()
                .orElse(facilities.get(0));

        clearChangesIfFacilityChanged(req, selectedFacility.getId());

        YearMonth month = parseMonth(param(req, "month"));
        MonthGrid grid;
        try {
            grid = ScheduleService.monthGrid(
                    selectedFacility.getId(), month);
        } catch (SQLException e) {
            throw new ServletException("Could not load the schedule", e);
        }

        LocalDate selectedDate = parseDate(param(req, "date"));
        if (selectedDate != null
                && (!YearMonth.from(selectedDate).equals(month)
                || selectedDate.isBefore(Clock.today()))) {
            selectedDate = null;
        }

        List<TimeSlot> slots = List.of();
        if (selectedDate != null) {
            LocalDate dateToLoad = selectedDate;
            slots = Tx.run(connection ->
                    SlotDao.findByFacilityAndDate(
                            connection,
                            selectedFacility.getId(),
                            dateToLoad));

            // A date with no sessions cannot be selected for editing.
            if (slots.isEmpty()) {
                selectedDate = null;
            }
        }

        req.setAttribute("facilities", facilities);
        req.setAttribute("selectedFacility", selectedFacility);
        req.setAttribute("grid", grid);
        req.setAttribute(
                "noSelectableDates",
                !ScheduleService.hasSelectableDates(grid));
        req.setAttribute("selectedDate", selectedDate);
        req.setAttribute("slots", slots);
        req.setAttribute("scheduleChanges", changes(req));

        render(req, resp, "manage-schedule.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String path = req.getServletPath();

        if (path.endsWith("/stage")) {
            stageChange(req, resp);
        } else if (path.endsWith("/save")) {
            saveChanges(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void stageChange(
            HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        Long facilityId = paramLong(req, "facility");
        Long slotId = paramLong(req, "slotId");
        String originalCode = param(req, "originalStatus");
        String targetCode = param(req, "targetStatus");

        if (facilityId == null || slotId == null
                || originalCode == null || targetCode == null
                || !isCurrentFacility(req, facilityId)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        SlotStatus originalStatus;
        SlotStatus targetStatus;
        try {
            originalStatus = SlotStatus.of(originalCode);
            targetStatus = SlotStatus.of(targetCode);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Managers can only toggle between Available and Closed.
        if (!isEditableStatus(originalStatus)
                || !isEditableStatus(targetStatus)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        TimeSlot slot = Tx.run(connection ->
                SlotDao.find(connection, slotId).orElse(null));

        // Do not accept changes for another facility, past date, or occupied slot.
        if (slot == null
                || slot.facilityId() != facilityId
                || slot.date().isBefore(Clock.today())
                || slot.status() == SlotStatus.OCCUPIED
                || slot.status() != originalStatus) {
            resp.sendError(HttpServletResponse.SC_CONFLICT);
            return;
        }

        Map<Long, SlotStatus> changes = changes(req);
        synchronized (changes) {
            if (targetStatus == originalStatus) {
                changes.remove(slotId);
            } else {
                changes.put(slotId, targetStatus);
            }
        }

        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private void saveChanges(
            HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        Long facilityId = paramLong(req, "facility");

        if (facilityId == null || !isCurrentFacility(req, facilityId)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Map<Long, SlotStatus> pendingChanges = changes(req);
        Map<Long, SlotStatus> changesToSave;
        synchronized (pendingChanges) {
            changesToSave = new HashMap<>(pendingChanges);
        }

        if (changesToSave.isEmpty()) {
            redirect(req, resp, buildPageUrl(req));
            return;
        }

        boolean conflict = ScheduleService.save(changesToSave);

        // A save attempt ends this batch, including when some slots conflicted.
        req.getSession().removeAttribute(CHANGES_ATTRIBUTE);

        if (conflict) {
            flashWarning(req, msg("p11"));
        } else {
            flashSuccess(req, msg("p10"));
        }

        redirect(req, resp, buildPageUrl(req));
    }

    private boolean isCurrentFacility(
            HttpServletRequest req,
            long requestedFacilityId) {
        Object currentFacilityId =
                req.getSession().getAttribute(FACILITY_ATTRIBUTE);

        return currentFacilityId instanceof Long
                && ((Long) currentFacilityId) == requestedFacilityId;
    }

    private boolean isEditableStatus(SlotStatus status) {
        return status == SlotStatus.AVAILABLE
                || status == SlotStatus.CLOSED;
    }

    private void clearChangesIfFacilityChanged(
            HttpServletRequest req,
            long facilityId) {
        HttpSession session = req.getSession();
        Object previousFacilityId =
                session.getAttribute(FACILITY_ATTRIBUTE);

        if (previousFacilityId instanceof Long
                && ((Long) previousFacilityId) != facilityId) {
            session.removeAttribute(CHANGES_ATTRIBUTE);
        }

        session.setAttribute(FACILITY_ATTRIBUTE, facilityId);
    }

    @SuppressWarnings("unchecked")
    private Map<Long, SlotStatus> changes(HttpServletRequest req) {
        HttpSession session = req.getSession();
        Object storedChanges = session.getAttribute(CHANGES_ATTRIBUTE);

        if (storedChanges instanceof Map<?, ?>) {
            return (Map<Long, SlotStatus>) storedChanges;
        }

        Map<Long, SlotStatus> newChanges = new HashMap<>();
        session.setAttribute(CHANGES_ATTRIBUTE, newChanges);
        return newChanges;
    }

    private String buildPageUrl(HttpServletRequest req) {
        StringBuilder url = new StringBuilder("/manage-schedule");
        String separator = "?";

        for (String parameter : List.of("facility", "month", "date")) {
            String value = param(req, parameter);
            if (value != null) {
                url.append(separator)
                        .append(parameter)
                        .append('=')
                        .append(value);
                separator = "&";
            }
        }

        return url.toString();
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