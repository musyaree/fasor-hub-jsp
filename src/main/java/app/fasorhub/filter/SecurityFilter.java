package app.fasorhub.filter;

import app.fasorhub.dao.BookingDao;
import app.fasorhub.model.Role;
import app.fasorhub.model.User;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Db;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
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
import java.time.LocalDate;
import java.util.Base64;

/**
 * Security filter that handles authentication, authorization, and CSRF protection.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Sets UTF-8 encoding on all requests</li>
 *   <li>Redirects unauthenticated users to the login page (except for public paths)</li>
 *   <li>Restricts /manage/* paths to managers only (403 if wrong role)</li>
 *   <li>Validates CSRF tokens on POST requests using time-constant comparison</li>
 *   <li>Generates CSRF tokens for new sessions</li>
 *   <li>Populates navigation badge counts for managers on GET requests</li>
 * </ul>
 */
@WebFilter(urlPatterns = "/*")
public class SecurityFilter implements Filter {

    /** Session attribute for the logged-in user */
    public static final String USER_ATTR = "user";

    /** Session attribute for the CSRF token */
    public static final String CSRF_ATTR = "_csrf";

    /** Request parameter name for CSRF token in forms */
    private static final String CSRF_PARAM = "_csrf";

    private final SecureRandom random = new SecureRandom();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Set UTF-8 encoding for all requests and responses
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String path = req.getServletPath();

        // Skip filter for static resources
        if (isStaticResource(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Get or create session for CSRF handling
        HttpSession session = req.getSession(true);

        // Ensure CSRF token exists
        if (session.getAttribute(CSRF_ATTR) == null) {
            session.setAttribute(CSRF_ATTR, generateCsrfToken());
        }

        // Public paths don't require authentication
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Check authentication
        User user = (User) session.getAttribute(USER_ATTR);

        if (user == null) {
            // Not logged in - redirect to login
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Check role-based access for /manage/* paths
        if (isManagerOnlyPath(path) && user.role() != Role.MANAGER) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        // For POST requests, validate CSRF token using time-constant comparison
        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String sessionToken = (String) session.getAttribute(CSRF_ATTR);
            String requestToken = req.getParameter(CSRF_PARAM);

            if (sessionToken == null || !timeConstantEquals(sessionToken, requestToken)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
                return;
            }
        }

        // For manager GET requests, populate navigation badge counts
        if ("GET".equalsIgnoreCase(req.getMethod()) && user.role() == Role.MANAGER) {
            populateManagerNavCounts(req);
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }

    /**
     * Checks if a path is a static resource.
     */
    private boolean isStaticResource(String path) {
        return path.startsWith("/static/");
    }

    /**
     * Checks if a path is public (does not require authentication).
     * Public paths: /login, /register
     */
    private boolean isPublicPath(String path) {
        return "/login".equals(path) || "/register".equals(path);
    }

    /**
     * Checks if a path is restricted to managers only.
     * Manager-only paths: /manage/*
     */
    private boolean isManagerOnlyPath(String path) {
        return path.startsWith("/manage/") || path.equals("/manage");
    }

    /**
     * Generates a secure random CSRF token.
     */
    private String generateCsrfToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Compares two strings in constant time to prevent timing attacks.
     * Returns false if either string is null.
     */
    private boolean timeConstantEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Populates request attributes with navigation badge counts for managers.
     * <p>
     * Sets:
     * <ul>
     *   <li>navVerification - count of bookings pending verification</li>
     *   <li>navCancellations - count of bookings pending cancellation confirmation</li>
     * </ul>
     */
    private void populateManagerNavCounts(HttpServletRequest req) {
        try (Connection conn = Db.get()) {
            LocalDate today = Clock.today();

            int verificationCount = BookingDao.countPendingVerification(conn, today);
            int cancellationCount = BookingDao.countPendingCancellation(conn, today);

            req.setAttribute("navVerification", verificationCount);
            req.setAttribute("navCancellations", cancellationCount);
        } catch (Exception e) {
            // Log error but don't fail the request
            // Counts will show as 0 if database is unavailable
            req.setAttribute("navVerification", 0);
            req.setAttribute("navCancellations", 0);
        }
    }
}
