<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:auth title="${msg.ui_register}" heading="${msg.ui_create_account}">
  <div class="auth__card">
    <form method="post" action="${pageContext.request.contextPath}/register">
      <input type="hidden" name="_csrf" value="${sessionScope._csrf}">

      <div class="form-group">
        <label class="form-label" for="name">${msg.ui_full_name}</label>
        <input type="text" id="name" name="name" class="form-input" autocomplete="name"
               value="<c:out value="${name}"/>" required autofocus>
      </div>

      <div class="form-group">
        <label class="form-label" for="email">${msg.ui_email}</label>
        <input type="email" id="email" name="email" class="form-input" autocomplete="username"
               placeholder="${msg.ui_email_placeholder}" value="<c:out value="${email}"/>" required
               <c:if test="${errorKey == 'p22'}">aria-invalid="true" aria-describedby="form-error"</c:if>>
      </div>

      <div class="form-group">
        <label class="form-label" for="password">${msg.ui_password}</label>
        <div class="auth__password">
          <input type="password" id="password" name="password" class="form-input" autocomplete="new-password" required>
          <button type="button" class="auth__reveal" data-toggle-password="password"
                  data-label-show="${msg.ui_show_password}" data-label-hide="${msg.ui_hide_password}"
                  aria-label="${msg.ui_show_password}">
            <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
          </button>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label" for="identity">${msg.ui_identity} <span class="form-hint">${msg.ui_identity_hint}</span></label>
        <input type="text" id="identity" name="identity" class="form-input"
               value="<c:out value="${identity}"/>" required>
      </div>

      <div class="form-group">
        <label class="form-label" for="phone">${msg.ui_phone}</label>
        <input type="tel" id="phone" name="phone" class="form-input" autocomplete="tel"
               value="<c:out value="${phone}"/>" required>
      </div>

      <c:if test="${not empty error}">
        <div id="form-error" class="auth__error" role="alert"><span class="material-symbols-outlined" aria-hidden="true">cancel</span><span><c:out value="${error}"/></span></div>
      </c:if>

      <button type="submit" class="btn btn--primary">${msg.ui_register}</button>
    </form>
  </div>

  <div class="auth__switch">
    ${msg.ui_have_account}<a href="${pageContext.request.contextPath}/login">${msg.ui_login}</a>
  </div>
</t:auth>
