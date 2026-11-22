<%@ tag description="Empty state display" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="message" required="true" description="Message text taken from the catalog" %>
<%@ attribute name="icon" required="false" description="Material Symbol icon name (default: inbox)" %>
<c:set var="iconName" value="${not empty icon ? icon : 'inbox'}" />
<div class="empty-state">
  <span class="material-symbols-outlined empty-state__icon" aria-hidden="true">${iconName}</span>
  <p class="empty-state__message"><c:out value="${message}"/></p>
</div>
