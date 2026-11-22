package app.fasorhub.web;

import app.fasorhub.filter.SecurityFilter;
import app.fasorhub.model.Role;
import app.fasorhub.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Root path handler that redirects to appropriate page based on role.
 * Borrowers go to /booking (S03), Managers go to /manage/schedule (S07).
 */
@WebServlet("")
public class HomeServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null) {
            redirect(req, resp, "/login");
            return;
        }

        User user = (User) session.getAttribute(SecurityFilter.USER_ATTR);
        if (user == null) {
            redirect(req, resp, "/login");
            return;
        }

        if (user.role() == Role.MANAGER) {
            redirect(req, resp, "/manage/schedule");
        } else {
            redirect(req, resp, "/booking");
        }
    }
}
