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
  <title><c:out value="${title} - ${msg.ui_app_name}"/></title>
  <link rel="preload" href="${pageContext.request.contextPath}/static/fonts/ibm-plex-sans-latin.woff2" as="font" type="font/woff2" crossorigin>
  <link rel="preload" href="${pageContext.request.contextPath}/static/fonts/material-symbols-outlined-v2.woff2" as="font" type="font/woff2" crossorigin>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css?v=${applicationScope.assetVersion}">
</head>
<body>
<c:choose>
<c:when test="${sessionScope.user.role.manager}">
<div class="layout layout--manager">
  <header class="topbar topbar--manager">
    <div class="topbar__brand">
      <span class="topbar__title">${msg.ui_app_name}</span>
      <span class="topbar__subtitle">${msg.ui_role_manager}</span>
    </div>
    <button type="button" class="topbar__menu-toggle" aria-expanded="false" aria-controls="mobile-nav">
      <span class="material-symbols-outlined">menu</span>
      <span class="sr-only">${msg.ui_menu}</span>
    </button>
  </header>

  <nav id="mobile-nav" class="mobile-nav mobile-nav--manager">
  <a href="${pageContext.request.contextPath}/manage-schedule" class="mobile-nav__link"<c:if test="${currentPage == 'manage-schedule'}"> aria-current="page"</c:if>>
    <span class="material-symbols-outlined">calendar_month</span>
    ${msg.ui_nav_manage_schedule}
  </a>
  <a href="${pageContext.request.contextPath}/manage-facilities" class="mobile-nav__link"<c:if test="${currentPage == 'manage-facilities'}"> aria-current="page"</c:if>>
    <span class="material-symbols-outlined">sports_soccer</span>
    ${msg.ui_nav_facilities}
  </a>
  <a href="${pageContext.request.contextPath}/verification" class="mobile-nav__link"<c:if test="${currentPage == 'verification'}"> aria-current="page"</c:if>>
    <span class="material-symbols-outlined">fact_check</span>
    ${msg.ui_nav_verification}
    <c:if test="${navVerification > 0}"><span class="mobile-nav__badge">${navVerification}</span></c:if>
  </a>
  <a href="${pageContext.request.contextPath}/cancellation-requests" class="mobile-nav__link"<c:if test="${currentPage == 'cancellation-requests'}"> aria-current="page"</c:if>>
    <span class="material-symbols-outlined">event_busy</span>
    ${msg.ui_nav_cancellation_requests}
    <c:if test="${navCancellations > 0}"><span class="mobile-nav__badge">${navCancellations}</span></c:if>
  </a>
  <a href="${pageContext.request.contextPath}/reports" class="mobile-nav__link"<c:if test="${currentPage == 'reports'}"> aria-current="page"</c:if>>
    <span class="material-symbols-outlined">bar_chart</span>
    ${msg.ui_nav_reports}
  </a>
    <div class="mobile-nav__divider"></div>
    <div class="mobile-nav__user">
      <span class="mobile-nav__username"><c:out value="${sessionScope.user.name}"/></span>
      <form action="${pageContext.request.contextPath}/logout" method="post" style="display:inline;">
        <input type="hidden" name="_csrf" value="${sessionScope._csrf}">
        <button type="submit" class="btn btn--signout">${msg.ui_sign_out}</button>
      </form>
    </div>
  </nav>

  <aside class="layout__sidebar">
    <div class="sidebar">
      <div class="sidebar__brand">
        <h1 class="sidebar__title">${msg.ui_app_name}</h1>
        <span class="sidebar__role">${msg.ui_role_manager}</span>
      </div>
      <nav class="sidebar__nav">
      <a href="${pageContext.request.contextPath}/manage-schedule" class="sidebar__link"<c:if test="${currentPage == 'manage-schedule'}"> aria-current="page"</c:if>>
        <span class="material-symbols-outlined sidebar__icon">calendar_month</span>
        ${msg.ui_nav_manage_schedule}
      </a>
      <a href="${pageContext.request.contextPath}/manage-facilities" class="sidebar__link"<c:if test="${currentPage == 'manage-facilities'}"> aria-current="page"</c:if>>
        <span class="material-symbols-outlined sidebar__icon">sports_soccer</span>
        ${msg.ui_nav_facilities}
      </a>
      <a href="${pageContext.request.contextPath}/verification" class="sidebar__link"<c:if test="${currentPage == 'verification'}"> aria-current="page"</c:if>>
        <span class="material-symbols-outlined sidebar__icon">fact_check</span>
        ${msg.ui_nav_verification}
        <c:if test="${navVerification > 0}"><span class="sidebar__badge">${navVerification}</span></c:if>
      </a>
      <a href="${pageContext.request.contextPath}/cancellation-requests" class="sidebar__link"<c:if test="${currentPage == 'cancellation-requests'}"> aria-current="page"</c:if>>
        <span class="material-symbols-outlined sidebar__icon">event_busy</span>
        ${msg.ui_nav_cancellation_requests}
        <c:if test="${navCancellations > 0}"><span class="sidebar__badge">${navCancellations}</span></c:if>
      </a>
      <a href="${pageContext.request.contextPath}/reports" class="sidebar__link"<c:if test="${currentPage == 'reports'}"> aria-current="page"</c:if>>
        <span class="material-symbols-outlined sidebar__icon">bar_chart</span>
        ${msg.ui_nav_reports}
      </a>
      </nav>
      <div class="sidebar__user">
        <span class="sidebar__username"><c:out value="${sessionScope.user.name}"/></span>
        <form action="${pageContext.request.contextPath}/logout" method="post">
          <input type="hidden" name="_csrf" value="${sessionScope._csrf}">
          <button type="submit" class="btn btn--on-ink btn--signout">${msg.ui_sign_out}</button>
        </form>
      </div>
    </div>
  </aside>

  <main class="layout__main">
    <div class="band">
      <h1 class="band__title"><c:out value="${title}"/></h1>
      <c:if test="${not empty subtitle}">
        <p class="band__subtitle"><c:out value="${subtitle}"/></p>
      </c:if>
    </div>
    <%@ include file="/WEB-INF/tags/toasts.tagf" %>
    <div class="content">
      <jsp:doBody/>
    </div>
  </main>
</div>
</c:when>
<c:otherwise>
<div class="layout layout--borrower">
  <header class="topbar">
    <div class="topbar__brand">
      <span class="topbar__title">${msg.ui_app_name}</span>
      <span class="topbar__subtitle">${msg.ui_app_tagline}</span>
    </div>
    <button type="button" class="topbar__menu-toggle" aria-expanded="false" aria-controls="mobile-nav">
      <span class="material-symbols-outlined">menu</span>
      <span class="sr-only">${msg.ui_menu}</span>
    </button>
    <nav class="topbar__nav">
    <a href="${pageContext.request.contextPath}/schedule" class="topbar__link"<c:if test="${currentPage == 'schedule'}"> aria-current="page"</c:if>>
      ${msg.ui_nav_schedule}
    </a>
    <a href="${pageContext.request.contextPath}/booking-status" class="topbar__link"<c:if test="${currentPage == 'booking-status'}"> aria-current="page"</c:if>>
      ${msg.ui_nav_booking_status}
    </a>
    <a href="${pageContext.request.contextPath}/cancellations" class="topbar__link"<c:if test="${currentPage == 'cancellations'}"> aria-current="page"</c:if>>
      ${msg.ui_nav_cancellations}
    </a>
    <a href="${pageContext.request.contextPath}/history" class="topbar__link"<c:if test="${currentPage == 'history'}"> aria-current="page"</c:if>>
      ${msg.ui_nav_history}
    </a>
    </nav>
    <div class="topbar__user">
      <span class="topbar__username"><c:out value="${sessionScope.user.name}"/></span>
      <form action="${pageContext.request.contextPath}/logout" method="post">
        <input type="hidden" name="_csrf" value="${sessionScope._csrf}">
        <button type="submit" class="btn btn--signout">${msg.ui_sign_out}</button>
      </form>
    </div>
  </header>

  <nav id="mobile-nav" class="mobile-nav">
  <a href="${pageContext.request.contextPath}/schedule" class="mobile-nav__link"<c:if test="${currentPage == 'schedule'}"> aria-current="page"</c:if>>
    <span class="material-symbols-outlined">calendar_month</span>
    ${msg.ui_nav_schedule}
  </a>
  <a href="${pageContext.request.contextPath}/booking-status" class="mobile-nav__link"<c:if test="${currentPage == 'booking-status'}"> aria-current="page"</c:if>>
    <span class="material-symbols-outlined">pending_actions</span>
    ${msg.ui_nav_booking_status}
  </a>
  <a href="${pageContext.request.contextPath}/cancellations" class="mobile-nav__link"<c:if test="${currentPage == 'cancellations'}"> aria-current="page"</c:if>>
    <span class="material-symbols-outlined">event_busy</span>
    ${msg.ui_nav_cancellations}
  </a>
  <a href="${pageContext.request.contextPath}/history" class="mobile-nav__link"<c:if test="${currentPage == 'history'}"> aria-current="page"</c:if>>
    <span class="material-symbols-outlined">history</span>
    ${msg.ui_nav_history}
  </a>
    <div class="mobile-nav__divider"></div>
    <div class="mobile-nav__user">
      <span class="mobile-nav__username"><c:out value="${sessionScope.user.name}"/></span>
      <form action="${pageContext.request.contextPath}/logout" method="post" style="display:inline;">
        <input type="hidden" name="_csrf" value="${sessionScope._csrf}">
        <button type="submit" class="btn btn--signout">${msg.ui_sign_out}</button>
      </form>
    </div>
  </nav>

  <main class="layout__main">
    <div class="band">
      <h1 class="band__title"><c:out value="${title}"/></h1>
      <c:if test="${not empty subtitle}">
        <p class="band__subtitle"><c:out value="${subtitle}"/></p>
      </c:if>
    </div>
    <%@ include file="/WEB-INF/tags/toasts.tagf" %>
    <div class="content">
      <jsp:doBody/>
    </div>
  </main>
</div>
</c:otherwise>
</c:choose>
<script src="${pageContext.request.contextPath}/static/js/app.js?v=${applicationScope.assetVersion}"></script>
</body>
</html>
