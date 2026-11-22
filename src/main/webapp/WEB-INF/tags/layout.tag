<%@ tag description="Main page layout" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="subtitle" required="false" %>
<%@ attribute name="currentPage" required="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${title} - Fasor Hub</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=IBM+Plex+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
  <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css">
</head>
<body>
  <c:choose>
    <%-- Manager layout with sidebar --%>
    <c:when test="${sessionScope.user.role.manager}">
      <div class="layout layout--manager">
        <%-- Mobile top bar for manager --%>
        <header class="topbar topbar--manager">
          <div class="topbar__brand">
            <span class="topbar__title">Fasor Hub</span>
            <span class="topbar__subtitle">Manager</span>
          </div>
          <button type="button" class="topbar__menu-toggle" aria-expanded="false" aria-controls="mobile-nav">
            <span class="material-symbols-outlined">menu</span>
            <span class="sr-only">Menu</span>
          </button>
        </header>

        <%-- Mobile navigation dropdown for manager --%>
        <nav id="mobile-nav" class="mobile-nav mobile-nav--manager">
          <a href="${pageContext.request.contextPath}/schedule"
             class="mobile-nav__link"
             <c:if test="${currentPage == 'schedule'}">aria-current="page"</c:if>>
            <span class="material-symbols-outlined">calendar_month</span>
            Manage Schedule
          </a>
          <a href="${pageContext.request.contextPath}/verification"
             class="mobile-nav__link"
             <c:if test="${currentPage == 'verification'}">aria-current="page"</c:if>>
            <span class="material-symbols-outlined">task_alt</span>
            Verification
            <c:if test="${navVerification > 0}">
              <span class="mobile-nav__badge">${navVerification}</span>
            </c:if>
          </a>
          <a href="${pageContext.request.contextPath}/cancellations"
             class="mobile-nav__link"
             <c:if test="${currentPage == 'cancellations'}">aria-current="page"</c:if>>
            <span class="material-symbols-outlined">event_busy</span>
            Cancellation Requests
            <c:if test="${navCancellations > 0}">
              <span class="mobile-nav__badge">${navCancellations}</span>
            </c:if>
          </a>
          <a href="${pageContext.request.contextPath}/history"
             class="mobile-nav__link"
             <c:if test="${currentPage == 'history'}">aria-current="page"</c:if>>
            <span class="material-symbols-outlined">history</span>
            History
          </a>
          <a href="${pageContext.request.contextPath}/reports"
             class="mobile-nav__link"
             <c:if test="${currentPage == 'reports'}">aria-current="page"</c:if>>
            <span class="material-symbols-outlined">bar_chart</span>
            Reports
          </a>
          <div class="mobile-nav__divider"></div>
          <div class="mobile-nav__user">
            <span class="mobile-nav__username">${sessionScope.user.name}</span>
            <form action="${pageContext.request.contextPath}/logout" method="post" style="display:inline;">
              <input type="hidden" name="_csrf" value="${sessionScope._csrf}">
              <button type="submit" class="btn btn--signout">Sign out</button>
            </form>
          </div>
        </nav>

        <%-- Desktop sidebar --%>
        <aside class="layout__sidebar">
          <div class="sidebar">
            <div class="sidebar__brand">
              <h1 class="sidebar__title">Fasor Hub</h1>
              <p class="sidebar__subtitle">Sports Facility Booking</p>
              <span class="sidebar__role">Manager</span>
            </div>

            <nav class="sidebar__nav">
              <a href="${pageContext.request.contextPath}/schedule"
                 class="sidebar__link"
                 <c:if test="${currentPage == 'schedule'}">aria-current="page"</c:if>>
                <span class="material-symbols-outlined sidebar__icon">calendar_month</span>
                Manage Schedule
              </a>
              <a href="${pageContext.request.contextPath}/verification"
                 class="sidebar__link"
                 <c:if test="${currentPage == 'verification'}">aria-current="page"</c:if>>
                <span class="material-symbols-outlined sidebar__icon">task_alt</span>
                Verification
                <c:if test="${navVerification > 0}">
                  <span class="sidebar__badge">${navVerification}</span>
                </c:if>
              </a>
              <a href="${pageContext.request.contextPath}/cancellations"
                 class="sidebar__link"
                 <c:if test="${currentPage == 'cancellations'}">aria-current="page"</c:if>>
                <span class="material-symbols-outlined sidebar__icon">event_busy</span>
                Cancellation Requests
                <c:if test="${navCancellations > 0}">
                  <span class="sidebar__badge">${navCancellations}</span>
                </c:if>
              </a>
              <a href="${pageContext.request.contextPath}/history"
                 class="sidebar__link"
                 <c:if test="${currentPage == 'history'}">aria-current="page"</c:if>>
                <span class="material-symbols-outlined sidebar__icon">history</span>
                History
              </a>
              <a href="${pageContext.request.contextPath}/reports"
                 class="sidebar__link"
                 <c:if test="${currentPage == 'reports'}">aria-current="page"</c:if>>
                <span class="material-symbols-outlined sidebar__icon">bar_chart</span>
                Reports
              </a>
            </nav>

            <div class="sidebar__user">
              <span class="sidebar__username">${sessionScope.user.name}</span>
              <form action="${pageContext.request.contextPath}/logout" method="post">
                <input type="hidden" name="_csrf" value="${sessionScope._csrf}">
                <button type="submit" class="btn btn--on-ink btn--signout">Sign out</button>
              </form>
            </div>
          </div>
        </aside>

        <main class="layout__main">
          <%-- Band header --%>
          <div class="band">
            <h1 class="band__title">${title}</h1>
            <c:if test="${not empty subtitle}">
              <p class="band__subtitle">${subtitle}</p>
            </c:if>
          </div>

          <%-- Toast container --%>
          <div class="toast-container" role="status" aria-live="polite">
            <c:if test="${not empty flashSuccess}">
              <div class="toast toast--success">
                <span class="material-symbols-outlined toast__icon">check_circle</span>
                <span class="toast__message">${flashSuccess}</span>
                <button type="button" class="toast__close" aria-label="Close">
                  <span class="material-symbols-outlined">close</span>
                </button>
              </div>
            </c:if>
            <c:if test="${not empty flashError}">
              <div class="toast toast--error">
                <span class="material-symbols-outlined toast__icon">cancel</span>
                <span class="toast__message">${flashError}</span>
                <button type="button" class="toast__close" aria-label="Close">
                  <span class="material-symbols-outlined">close</span>
                </button>
              </div>
            </c:if>
            <c:if test="${not empty flashWarning}">
              <div class="toast toast--warning">
                <span class="material-symbols-outlined toast__icon">warning</span>
                <span class="toast__message">${flashWarning}</span>
                <button type="button" class="toast__close" aria-label="Close">
                  <span class="material-symbols-outlined">close</span>
                </button>
              </div>
            </c:if>
          </div>

          <div class="content">
            <jsp:doBody/>
          </div>
        </main>
      </div>
    </c:when>

    <%-- Borrower layout with top bar --%>
    <c:otherwise>
      <div class="layout layout--borrower">
        <header class="topbar">
          <div class="topbar__brand">
            <span class="topbar__title">Fasor Hub</span>
            <span class="topbar__subtitle">Sports Facility Booking</span>
          </div>

          <button type="button" class="topbar__menu-toggle" aria-expanded="false" aria-controls="mobile-nav">
            <span class="material-symbols-outlined">menu</span>
            <span class="sr-only">Menu</span>
          </button>

          <nav class="topbar__nav">
            <a href="${pageContext.request.contextPath}/booking"
               class="topbar__link"
               <c:if test="${currentPage == 'booking'}">aria-current="page"</c:if>>
              Schedule & Booking
            </a>
            <a href="${pageContext.request.contextPath}/status"
               class="topbar__link"
               <c:if test="${currentPage == 'status'}">aria-current="page"</c:if>>
              Booking Status
            </a>
            <a href="${pageContext.request.contextPath}/cancellation"
               class="topbar__link"
               <c:if test="${currentPage == 'cancellation'}">aria-current="page"</c:if>>
              Cancellation
            </a>
            <a href="${pageContext.request.contextPath}/history"
               class="topbar__link"
               <c:if test="${currentPage == 'history'}">aria-current="page"</c:if>>
              History
            </a>
          </nav>

          <div class="topbar__user">
            <span class="topbar__username">${sessionScope.user.name}</span>
            <form action="${pageContext.request.contextPath}/logout" method="post">
              <input type="hidden" name="_csrf" value="${sessionScope._csrf}">
              <button type="submit" class="btn btn--signout">Sign out</button>
            </form>
          </div>
        </header>

        <%-- Mobile navigation dropdown for borrower --%>
        <nav id="mobile-nav" class="mobile-nav">
          <a href="${pageContext.request.contextPath}/booking"
             class="mobile-nav__link"
             <c:if test="${currentPage == 'booking'}">aria-current="page"</c:if>>
            <span class="material-symbols-outlined">calendar_month</span>
            Schedule & Booking
          </a>
          <a href="${pageContext.request.contextPath}/status"
             class="mobile-nav__link"
             <c:if test="${currentPage == 'status'}">aria-current="page"</c:if>>
            <span class="material-symbols-outlined">pending_actions</span>
            Booking Status
          </a>
          <a href="${pageContext.request.contextPath}/cancellation"
             class="mobile-nav__link"
             <c:if test="${currentPage == 'cancellation'}">aria-current="page"</c:if>>
            <span class="material-symbols-outlined">event_busy</span>
            Cancellation
          </a>
          <a href="${pageContext.request.contextPath}/history"
             class="mobile-nav__link"
             <c:if test="${currentPage == 'history'}">aria-current="page"</c:if>>
            <span class="material-symbols-outlined">history</span>
            History
          </a>
          <div class="mobile-nav__divider"></div>
          <div class="mobile-nav__user">
            <span class="mobile-nav__username">${sessionScope.user.name}</span>
            <form action="${pageContext.request.contextPath}/logout" method="post" style="display:inline;">
              <input type="hidden" name="_csrf" value="${sessionScope._csrf}">
              <button type="submit" class="btn btn--signout">Sign out</button>
            </form>
          </div>
        </nav>

        <main class="layout__main">
          <%-- Band header --%>
          <div class="band">
            <h1 class="band__title">${title}</h1>
            <c:if test="${not empty subtitle}">
              <p class="band__subtitle">${subtitle}</p>
            </c:if>
          </div>

          <%-- Toast container --%>
          <div class="toast-container" role="status" aria-live="polite">
            <c:if test="${not empty flashSuccess}">
              <div class="toast toast--success">
                <span class="material-symbols-outlined toast__icon">check_circle</span>
                <span class="toast__message">${flashSuccess}</span>
                <button type="button" class="toast__close" aria-label="Close">
                  <span class="material-symbols-outlined">close</span>
                </button>
              </div>
            </c:if>
            <c:if test="${not empty flashError}">
              <div class="toast toast--error">
                <span class="material-symbols-outlined toast__icon">cancel</span>
                <span class="toast__message">${flashError}</span>
                <button type="button" class="toast__close" aria-label="Close">
                  <span class="material-symbols-outlined">close</span>
                </button>
              </div>
            </c:if>
            <c:if test="${not empty flashWarning}">
              <div class="toast toast--warning">
                <span class="material-symbols-outlined toast__icon">warning</span>
                <span class="toast__message">${flashWarning}</span>
                <button type="button" class="toast__close" aria-label="Close">
                  <span class="material-symbols-outlined">close</span>
                </button>
              </div>
            </c:if>
          </div>

          <div class="content">
            <jsp:doBody/>
          </div>
        </main>
      </div>
    </c:otherwise>
  </c:choose>

  <script src="${pageContext.request.contextPath}/static/js/app.js"></script>
</body>
</html>
