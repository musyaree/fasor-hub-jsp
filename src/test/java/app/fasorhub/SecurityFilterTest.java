package app.fasorhub;

import app.fasorhub.filter.SecurityFilter;
import app.fasorhub.model.Role;
import app.fasorhub.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityFilterTest {

    private final SecurityFilter filter = new SecurityFilter();
    private Map<String, Object> sessionAttrs;

    @BeforeEach
    void setUp() {
        TestDb.fresh(true);
        sessionAttrs = new HashMap<>();
    }

    private record Outcome(boolean passed, int status, String redirect) {
    }

    private Outcome call(String method, String path, Map<String, String> params) throws Exception {
        HttpSession session = (HttpSession) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {HttpSession.class}, (p, m, a) -> switch (m.getName()) {
                    case "getAttribute" -> sessionAttrs.get(a[0]);
                    case "setAttribute" -> sessionAttrs.put((String) a[0], a[1]);
                    default -> null;
                });
        Map<String, Object> reqAttrs = new HashMap<>();
        HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {HttpServletRequest.class}, (p, m, a) -> switch (m.getName()) {
                    case "getServletPath" -> path;
                    case "getMethod" -> method;
                    case "getContextPath" -> "";
                    case "getSession" -> session;
                    case "getParameter" -> params.get(a[0]);
                    case "setAttribute" -> reqAttrs.put((String) a[0], a[1]);
                    default -> null;
                });
        int[] status = {200};
        String[] redirect = {null};
        HttpServletResponse resp = (HttpServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {HttpServletResponse.class}, (p, m, a) -> {
                    if (m.getName().equals("sendError")) {
                        status[0] = (Integer) a[0];
                    } else if (m.getName().equals("sendRedirect")) {
                        redirect[0] = (String) a[0];
                    }
                    return null;
                });
        AtomicBoolean passed = new AtomicBoolean();
        filter.doFilter(req, resp, (rq, rs) -> passed.set(true));
        return new Outcome(passed.get(), status[0], redirect[0]);
    }

    private void loginAs(Role role) {
        sessionAttrs.put(SecurityFilter.USER_ATTR,
                new User(1L, "Test", "t@example.com", null, role, "1", "08"));
    }

    private Map<String, String> token() {
        return Map.of("_csrf", (String) sessionAttrs.get(SecurityFilter.CSRF_ATTR));
    }

    @Test
    void aut07ProtectedPagesRedirectToLoginWithoutSession() throws Exception {
        for (String path : new String[] {"/schedule", "/history", "/verification", "/"}) {
            Outcome o = call("GET", path, Map.of());
            assertFalse(o.passed(), path);
            assertEquals("/login", o.redirect(), path);
        }
    }

    @Test
    void publicPagesAndStaticFilesAreOpen() throws Exception {
        assertTrue(call("GET", "/login", Map.of()).passed());
        assertTrue(call("GET", "/register", Map.of()).passed());
        assertTrue(call("GET", "/static/css/app.css", Map.of()).passed());
    }

    @Test
    void kem04PostWithoutOrWithWrongTokenIsRejectedEverywhere() throws Exception {
        for (String path : new String[] {"/login", "/register", "/logout", "/bookings"}) {
            assertEquals(403, call("POST", path, Map.of()).status(), path);
            assertEquals(403, call("POST", path, Map.of("_csrf", "wrong")).status(), path);
        }
    }

    @Test
    void postWithValidTokenPassesOnPublicPaths() throws Exception {
        call("GET", "/login", Map.of());
        assertTrue(call("POST", "/login", token()).passed());
        assertTrue(call("POST", "/register", token()).passed());
    }

    @Test
    void kem01BorrowerCannotOpenManagerAreas() throws Exception {
        call("GET", "/login", Map.of());
        loginAs(Role.BORROWER);
        for (String path : new String[] {"/verification", "/manage-schedule", "/reports", "/cancellation-requests",
                "/verification/1/approve"}) {
            assertEquals(403, call("GET", path, Map.of()).status(), path);
            assertEquals(403, call("POST", path, token()).status(), path);
        }
        assertTrue(call("GET", "/schedule", Map.of()).passed());
    }

    @Test
    void kem02ManagerCannotOpenBorrowerAreas() throws Exception {
        call("GET", "/login", Map.of());
        loginAs(Role.MANAGER);
        for (String path : new String[] {"/schedule", "/booking-status", "/cancellations", "/bookings"}) {
            assertEquals(403, call("GET", path, Map.of()).status(), path);
        }
        assertEquals(403, call("POST", "/bookings", token()).status());
        assertTrue(call("GET", "/manage-schedule", Map.of()).passed());
        assertTrue(call("GET", "/verification", Map.of()).passed());
    }
}
