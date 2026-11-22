package app.fasorhub.web;

import app.fasorhub.model.Role;
import app.fasorhub.model.User;
import app.fasorhub.util.Messages;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {
    private static final String FLASH_SUCCESS = "_flash_success";
    private static final String FLASH_ERROR = "_flash_error";
    private static final String FLASH_WARNING = "_flash_warning";

    protected void render(HttpServletRequest req, HttpServletResponse resp, String jsp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            transferFlash(session, req, FLASH_SUCCESS, "flashSuccess");
            transferFlash(session, req, FLASH_ERROR, "flashError");
            transferFlash(session, req, FLASH_WARNING, "flashWarning");
        }

        req.setAttribute("msg", Messages.all());
        req.getRequestDispatcher("/WEB-INF/jsp/" + jsp).forward(req, resp);
    }

    private void transferFlash(HttpSession session, HttpServletRequest req,
                               String sessionKey, String requestKey) {
        Object value = session.getAttribute(sessionKey);
        if (value != null) {
            req.setAttribute(requestKey, value);
            session.removeAttribute(sessionKey);
        }
    }

    protected void flashSuccess(HttpServletRequest req, String message) {
        req.getSession().setAttribute(FLASH_SUCCESS, message);
    }

    protected void flashError(HttpServletRequest req, String message) {
        req.getSession().setAttribute(FLASH_ERROR, message);
    }

    protected void flashWarning(HttpServletRequest req, String message) {
        req.getSession().setAttribute(FLASH_WARNING, message);
    }

    protected String param(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    protected Long paramLong(HttpServletRequest req, String name) {
        String value = param(req, name);
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Integer paramInt(HttpServletRequest req, String name) {
        String value = param(req, name);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Integer paramPositiveInt(HttpServletRequest req, String name) {
        Integer value = paramInt(req, name);
        return (value != null && value >= 1) ? value : null;
    }

    protected String msg(String key) {
        return Messages.get(key);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    protected void redirectHome(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        redirect(req, resp, user.role() == Role.MANAGER ? "/manage-schedule" : "/schedule");
    }
}
