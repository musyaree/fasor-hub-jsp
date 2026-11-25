package app.fasorhub.web;

import app.fasorhub.filter.SecurityFilter;
import app.fasorhub.model.HistoryItem;
import app.fasorhub.model.User;
import app.fasorhub.service.BookingService;
import app.fasorhub.util.DataAccessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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

        try {
            List<HistoryItem> items = BookingService.history(user);
            req.setAttribute("items", items);
            req.setAttribute("noBookings", items.isEmpty());
        } catch (DataAccessException e) {
            LOG.log(Level.WARNING, "Could not load booking history for user " + user.id(), e);
            req.setAttribute("historyLoadError", true);
        }

        render(req, resp, "history.jsp");
    }
}