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

/**
 * Handles user registration (S02).
 * GET displays the registration form, POST processes registration.
 * Registration is for borrowers only.
 */
@WebServlet("/register")
public class RegisterServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // If already logged in, redirect to home
        HttpSession session = req.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute(SecurityFilter.USER_ATTR);
            if (user != null) {
                redirect(req, resp, "/booking");
                return;
            }
        }

        render(req, resp, "register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String name = param(req, "name");
        String email = param(req, "email");
        String password = req.getParameter("password"); // Don't trim password
        String identity = param(req, "identity");
        String phone = param(req, "phone");

        try {
            AuthService.register(name, email, password, identity, phone);

            // Registration successful - redirect to login with success message
            flashSuccess(req, msg("p24"));
            redirect(req, resp, "/login");

        } catch (ValidationException e) {
            // Show error on form with fields preserved
            req.setAttribute("error", msg(e.getMessageKey()));
            preserveFormFields(req, name, email, identity, phone);
            render(req, resp, "register.jsp");

        } catch (Exception e) {
            // Database or other error
            req.setAttribute("error", msg("p23"));
            preserveFormFields(req, name, email, identity, phone);
            render(req, resp, "register.jsp");
        }
    }

    private void preserveFormFields(HttpServletRequest req, String name, String email,
                                    String identity, String phone) {
        req.setAttribute("name", name);
        req.setAttribute("email", email);
        req.setAttribute("identity", identity);
        req.setAttribute("phone", phone);
        // Password is intentionally not preserved for security
    }
}
