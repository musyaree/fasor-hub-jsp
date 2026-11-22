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

@WebServlet("/register")
public class RegisterServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(SecurityFilter.USER_ATTR);
        if (user != null) {
            redirectHome(req, resp, user);
            return;
        }
        render(req, resp, "register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = param(req, "name");
        String email = param(req, "email");
        String identity = param(req, "identity");
        String phone = param(req, "phone");
        try {
            AuthService.register(name, email, req.getParameter("password"), identity, phone);
            flashSuccess(req, msg("p24"));
            redirect(req, resp, "/login");
        } catch (ValidationException e) {
            req.setAttribute("error", msg(e.getMessageKey()));
            req.setAttribute("errorKey", e.getMessageKey());
            req.setAttribute("name", name);
            req.setAttribute("email", email);
            req.setAttribute("identity", identity);
            req.setAttribute("phone", phone);
            render(req, resp, "register.jsp");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
