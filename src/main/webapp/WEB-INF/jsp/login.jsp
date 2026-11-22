<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:auth title="${msg.ui_login}" heading="${msg.ui_login}">
  <div class="auth__card">
    <c:if test="${not empty flashSuccess}">
      <div class="auth__notice" role="status">
        <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
        <c:out value="${flashSuccess}"/>
      </div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/login">
      <input type="hidden" name="_csrf" value="${sessionScope._csrf}">

      <div class="form-group">
        <label class="form-label" for="email">${msg.ui_email}</label>
        <input type="email" id="email" name="email" class="form-input" autocomplete="username"
               placeholder="${msg.ui_email_placeholder}" value="<c:out value="${email}"/>" required autofocus
               <c:if test="${not empty error}">aria-invalid="true" aria-describedby="form-error"</c:if>>
      </div>

      <div class="form-group">
        <label class="form-label" for="password">${msg.ui_password}</label>
        <div class="auth__password">
          <input type="password" id="password" name="password" class="form-input" autocomplete="current-password"
                 placeholder="${msg.ui_password_placeholder}" required
                 <c:if test="${not empty error}">aria-invalid="true" aria-describedby="form-error"</c:if>>
          <button type="button" class="auth__reveal" data-toggle-password="password"
                  data-label-show="${msg.ui_show_password}" data-label-hide="${msg.ui_hide_password}"
                  aria-label="${msg.ui_show_password}">
            <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
          </button>
        </div>
      </div>

      <c:if test="${not empty error}">
        <div id="form-error" class="auth__error" role="alert"><span class="material-symbols-outlined" aria-hidden="true">cancel</span><span><c:out value="${error}"/></span></div>
      </c:if>

      <button type="submit" class="btn btn--primary">${msg.ui_login}</button>
    </form>
  </div>

  <div class="auth__switch">
    ${msg.ui_no_account}<a href="${pageContext.request.contextPath}/register">${msg.ui_register}</a>
  </div>
</t:auth>
