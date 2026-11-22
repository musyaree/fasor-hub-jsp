<%@ tag description="Empty state display" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="message" required="true" description="Message key from messages_en.properties (e.g. p05, p08)" %>
<%@ attribute name="icon" required="false" description="Material Symbol icon name (default: inbox)" %>
<%--
  Renders an empty state with an icon and a message from the catalog.

  Per DESIGN.md section 10:
  - Empty state shows a single sentence from the message catalog
  - No illustration, just icon and text

  Usage:
    <t:empty message="${msg.p05}" />
    <t:empty message="${msg.p08}" icon="history" />
--%>
<c:set var="iconName" value="${not empty icon ? icon : 'inbox'}" />
<div class="empty-state">
  <span class="material-symbols-outlined empty-state__icon" aria-hidden="true">${iconName}</span>
  <p class="empty-state__message">${message}</p>
</div>
