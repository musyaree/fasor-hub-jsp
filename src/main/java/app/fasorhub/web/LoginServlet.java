package app.fasorhub.web;

import app.fasorhub.exception.ValidationException;
import app.fasorhub.filter.SecurityFilter;
import app.fasorhub.model.User;
import app.fasorhub.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(SecurityFilter.USER_ATTR);
        if (user != null) {
            redirectHome(req, resp, user);
            return;
        }
        render(req, resp, "login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = param(req, "email");
        try {
            User user = AuthService.login(email, req.getParameter("password"));

            HttpSession old = req.getSession(false);
            if (old != null) {
                old.invalidate();
            }
            req.getSession(true).setAttribute(SecurityFilter.USER_ATTR, user);
            req.changeSessionId();

            redirectHome(req, resp, user);
        } catch (ValidationException e) {
            req.setAttribute("error", msg(e.getMessageKey()));
            req.setAttribute("email", email);
            render(req, resp, "login.jsp");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
