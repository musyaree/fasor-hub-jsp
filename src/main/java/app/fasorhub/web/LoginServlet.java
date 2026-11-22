package app.fasorhub.web;

import app.fasorhub.exception.ValidationException;
import app.fasorhub.filter.SecurityFilter;
import app.fasorhub.model.Role;
import app.fasorhub.model.User;
import app.fasorhub.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Handles user login (S01).
 * GET displays the login form, POST processes login.
 */
@WebServlet("/login")
public class LoginServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // If already logged in, redirect to appropriate home
        HttpSession session = req.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute(SecurityFilter.USER_ATTR);
            if (user != null) {
                redirectToHome(req, resp, user);
                return;
            }
        }

        render(req, resp, "login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = param(req, "email");
        String password = req.getParameter("password"); // Don't trim password

        try {
            User user = AuthService.login(email, password);

            // Create new session (invalidate old one for security)
            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession newSession = req.getSession(true);
            req.changeSessionId(); // Additional protection against session fixation
            newSession.setAttribute(SecurityFilter.USER_ATTR, user);

            // Generate new CSRF token for new session
            // (SecurityFilter will do this automatically on next request)

            redirectToHome(req, resp, user);

        } catch (ValidationException e) {
            // Show error on form with email preserved
            req.setAttribute("error", msg(e.getMessageKey()));
            req.setAttribute("email", email);
            render(req, resp, "login.jsp");

        } catch (Exception e) {
            // Database or other error
            req.setAttribute("error", msg("p21"));
            req.setAttribute("email", email);
            render(req, resp, "login.jsp");
        }
    }

    private void redirectToHome(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException {
        if (user.role() == Role.MANAGER) {
            redirect(req, resp, "/manage/schedule");
        } else {
            redirect(req, resp, "/booking");
        }
    }
}
