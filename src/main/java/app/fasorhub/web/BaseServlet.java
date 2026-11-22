package app.fasorhub.web;

import app.fasorhub.util.Messages;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Base servlet providing common functionality for all page servlets.
 * <p>
 * Subclasses inherit:
 * <ul>
 *   <li>{@link #render} - forwards to a JSP and transfers flash messages</li>
 *   <li>{@link #flash} - stores a one-time message in the session</li>
 *   <li>{@link #param} - reads a request parameter, null-safe and trimmed</li>
 *   <li>{@link #paramLong} - reads a parameter as a Long, null if invalid</li>
 *   <li>{@link #paramInt} - reads a parameter as an Integer, null if invalid</li>
 *   <li>{@link #msg} - retrieves a message from the catalog</li>
 * </ul>
 */
public abstract class BaseServlet extends HttpServlet {

    // Session attribute keys for flash messages
    private static final String FLASH_SUCCESS = "_flash_success";
    private static final String FLASH_ERROR = "_flash_error";
    private static final String FLASH_WARNING = "_flash_warning";

    /**
     * Forwards the request to a JSP view.
     * <p>
     * Transfers any flash messages from the session to request attributes
     * and clears them so they only appear once.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @param jsp  the JSP path relative to /WEB-INF/jsp/ (e.g., "booking.jsp")
     */
    protected void render(HttpServletRequest req, HttpServletResponse resp, String jsp)
            throws ServletException, IOException {

        // Transfer flash messages from session to request (one-time display)
        HttpSession session = req.getSession(false);
        if (session != null) {
            transferFlash(session, req, FLASH_SUCCESS, "flashSuccess");
            transferFlash(session, req, FLASH_ERROR, "flashError");
            transferFlash(session, req, FLASH_WARNING, "flashWarning");
        }

        req.getRequestDispatcher("/WEB-INF/jsp/" + jsp).forward(req, resp);
    }

    /**
     * Transfers a flash attribute from session to request and removes it.
     */
    private void transferFlash(HttpSession session, HttpServletRequest req,
                               String sessionKey, String requestKey) {
        Object value = session.getAttribute(sessionKey);
        if (value != null) {
            req.setAttribute(requestKey, value);
            session.removeAttribute(sessionKey);
        }
    }

    /**
     * Stores a success message to be displayed once on the next page.
     *
     * @param req     the HTTP request (session will be created if needed)
     * @param message the message text (from catalog)
     */
    protected void flashSuccess(HttpServletRequest req, String message) {
        req.getSession().setAttribute(FLASH_SUCCESS, message);
    }

    /**
     * Stores an error message to be displayed once on the next page.
     *
     * @param req     the HTTP request (session will be created if needed)
     * @param message the message text (from catalog)
     */
    protected void flashError(HttpServletRequest req, String message) {
        req.getSession().setAttribute(FLASH_ERROR, message);
    }

    /**
     * Stores a warning message to be displayed once on the next page.
     *
     * @param req     the HTTP request (session will be created if needed)
     * @param message the message text (from catalog)
     */
    protected void flashWarning(HttpServletRequest req, String message) {
        req.getSession().setAttribute(FLASH_WARNING, message);
    }

    /**
     * Reads a request parameter, returning null if missing or blank.
     * <p>
     * Whitespace-only values are treated as missing (per wireframe decision 16).
     *
     * @param req  the HTTP request
     * @param name the parameter name
     * @return the trimmed value, or null if missing/blank
     */
    protected String param(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    /**
     * Reads a request parameter as a Long.
     *
     * @param req  the HTTP request
     * @param name the parameter name
     * @return the parsed Long, or null if missing, blank, or not a valid number
     */
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

    /**
     * Reads a request parameter as an Integer.
     *
     * @param req  the HTTP request
     * @param name the parameter name
     * @return the parsed Integer, or null if missing, blank, or not a valid number
     */
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

    /**
     * Reads a request parameter as a positive integer (1 or greater).
     *
     * @param req  the HTTP request
     * @param name the parameter name
     * @return the parsed Integer if positive, or null otherwise
     */
    protected Integer paramPositiveInt(HttpServletRequest req, String name) {
        Integer value = paramInt(req, name);
        return (value != null && value >= 1) ? value : null;
    }

    /**
     * Retrieves a message from the catalog.
     *
     * @param key the message key (e.g., "p01", "p02")
     * @return the message text
     */
    protected String msg(String key) {
        return Messages.get(key);
    }

    /**
     * Redirects to another URL within the application.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @param path the path to redirect to (e.g., "/booking")
     */
    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }
}
