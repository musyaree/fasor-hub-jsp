<%@ tag description="Status badge with icon and label" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="status" required="true" type="java.lang.Object"
    description="A BookingStatus or SlotStatus enum value with getCss(), getIcon(), getLabel()" %>
<span class="badge badge--${status.css}">
  <span class="material-symbols-outlined badge__icon" aria-hidden="true">${status.icon}</span>
  <span class="badge__label"><c:out value="${status.label}"/></span>
</span>
