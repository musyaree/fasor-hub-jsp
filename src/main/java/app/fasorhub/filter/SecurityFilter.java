package app.fasorhub.filter;

import app.fasorhub.dao.BookingDao;
import app.fasorhub.model.Role;
import app.fasorhub.model.User;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Db;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// One filter forces login, checks the role per area and checks the token on every post
@WebFilter(urlPatterns = "/*")
public class SecurityFilter implements Filter {

    public static final String USER_ATTR = "user";
    public static final String CSRF_ATTR = "_csrf";

    private static final Logger LOG = Logger.getLogger(SecurityFilter.class.getName());
    private static final String CSRF_PARAM = "_csrf";
    private static final List<String> PUBLIC_PATHS = List.of("/login", "/register");
    private static final List<String> MANAGER_AREAS =
            List.of("/manage-schedule", "/verification", "/cancellation-requests", "/reports", "/manage-facilities");
    private static final List<String> BORROWER_AREAS =
            List.of("/schedule", "/bookings", "/booking-status", "/cancellations");

    private final SecureRandom random = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String path = req.getServletPath();
        if (path.startsWith("/static/")) {
            if (path.startsWith("/static/fonts/")) {
                resp.setHeader("Cache-Control", "public, max-age=604800, immutable");
            }
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(true);
        if (session.getAttribute(CSRF_ATTR) == null) {
            session.setAttribute(CSRF_ATTR, newToken());
        }

        if ("POST".equalsIgnoreCase(req.getMethod())
                && !sameToken((String) session.getAttribute(CSRF_ATTR), req.getParameter(CSRF_PARAM))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
            return;
        }

        if (PUBLIC_PATHS.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        User user = (User) session.getAttribute(USER_ATTR);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        boolean wrongArea = user.role() == Role.MANAGER ? inAreas(path, BORROWER_AREAS) : inAreas(path, MANAGER_AREAS);
        if (wrongArea) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        if ("GET".equalsIgnoreCase(req.getMethod()) && user.role() == Role.MANAGER) {
            fillManagerNavCounts(req);
        }

        chain.doFilter(request, response);
    }

    private static boolean inAreas(String path, List<String> areas) {
        return areas.stream().anyMatch(area -> path.equals(area) || path.startsWith(area + "/"));
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static boolean sameToken(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }

    private void fillManagerNavCounts(HttpServletRequest req) {
        int verification = 0;
        int cancellations = 0;
        try (Connection conn = Db.get()) {
            verification = BookingDao.countPendingVerification(conn, Clock.today());
            cancellations = BookingDao.countPendingCancellation(conn, Clock.today());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Could not load menu counts", e);
        }
        req.setAttribute("navVerification", verification);
        req.setAttribute("navCancellations", cancellations);
    }
}
