<%@ tag description="Status badge with icon and label" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="status" required="true" type="java.lang.Object"
    description="A BookingStatus or SlotStatus enum value with getCss(), getIcon(), getLabel()" %>
<%--
  Renders a status badge with icon, label, and appropriate styling.

  The badge uses triple signifiers for accessibility (not color alone):
  - Icon (Material Symbol)
  - Label text
  - Border style (solid, dashed for pending, double for cancelling)

  Usage:
    <t:badge status="${booking.status}" />
    <t:badge status="${slot.status}" />
--%>
<span class="badge badge--${status.css}">
  <span class="material-symbols-outlined badge__icon" aria-hidden="true">${status.icon}</span>
  <span class="badge__label">${status.label}</span>
</span>
