package app.fasorhub.web;

import app.fasorhub.filter.SecurityFilter;
import app.fasorhub.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("")
public class HomeServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(SecurityFilter.USER_ATTR);
        if (user == null) {
            redirect(req, resp, "/login");
            return;
        }
        redirectHome(req, resp, user);
    }
}
