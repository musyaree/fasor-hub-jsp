<%@ tag description="Two panel frame for Login and Register" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="heading" required="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><c:out value="${title} - ${msg.ui_app_name}"/></title>
  <link rel="preload" href="${pageContext.request.contextPath}/static/fonts/ibm-plex-sans-latin.woff2" as="font" type="font/woff2" crossorigin>
  <link rel="preload" href="${pageContext.request.contextPath}/static/fonts/material-symbols-outlined.woff2" as="font" type="font/woff2" crossorigin>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css?v=${applicationScope.assetVersion}">
</head>
<body>
  <main class="auth">
    <section class="auth__brand" aria-label="${msg.ui_app_name}">
      <div class="auth__wordmark">${msg.ui_app_name}</div>
      <div>
        <p class="auth__headline">${msg.ui_auth_headline}</p>
        <p class="auth__subhead">${msg.ui_auth_subhead}</p>
      </div>
      <ul class="auth__facilities">
        <li><span class="material-symbols-outlined" aria-hidden="true">sports_soccer</span>${msg['facility_type.1']}</li>
        <li><span class="material-symbols-outlined" aria-hidden="true">sports_basketball</span>${msg['facility_type.2']}</li>
        <li>${msg['facility_type.3']}</li>
        <li><span class="material-symbols-outlined" aria-hidden="true">sports_tennis</span>${msg['facility_type.4']}</li>
      </ul>
    </section>

    <section class="auth__panel" aria-label="${heading}">
      <div class="auth__panel-inner">
        <h1 class="auth__title"><c:out value="${heading}"/></h1>
        <jsp:doBody/>
      </div>
    </section>
  </main>
  <script src="${pageContext.request.contextPath}/static/js/app.js?v=${applicationScope.assetVersion}"></script>
</body>
</html>
