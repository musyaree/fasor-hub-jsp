package app.fasorhub.web.manage;

import app.fasorhub.exception.ValidationException;
import app.fasorhub.model.Facility;
import app.fasorhub.service.ReportService;
import app.fasorhub.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet("/reports")
public class ReportServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Facility> facilities = ReportService.facilities();
        req.setAttribute("facilities", facilities);

        String startValue = param(req, "startDate");
        String endValue = param(req, "endDate");
        String facilityValue = param(req, "facility");

        req.setAttribute("startDate", startValue == null ? "" : startValue);
        req.setAttribute("endDate", endValue == null ? "" : endValue);

        Long facilityId = paramLong(req, "facility");
        if (facilityValue != null && facilityId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (facilityId != null && facilities.stream().noneMatch(f -> f.id() == facilityId)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        req.setAttribute("selectedFacilityId", facilityId);

        if ("true".equals(param(req, "run"))) {
            LocalDate startDate = parseDate(startValue);
            LocalDate endDate = parseDate(endValue);

            if (startDate == null || endDate == null) {
                req.setAttribute("reportMessage", msg("p19"));
            } else {
                try {
                    req.setAttribute("report",
                            ReportService.generate(startDate, endDate, facilityId));
                } catch (ValidationException e) {
                    req.setAttribute("reportMessage", msg(e.getMessageKey()));
                }
            }
        }

        render(req, resp, "manage-report.jsp");
    }

    // Invalid or missing date input is handled with the report's P19 message.
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