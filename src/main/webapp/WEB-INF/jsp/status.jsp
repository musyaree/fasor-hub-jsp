<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="${msg.ui_nav_booking_status}" subtitle="${msg.ui_status_subtitle}" currentPage="booking-status">
  <c:set var="base" value="${pageContext.request.contextPath}/booking-status"/>

  <c:choose>
    <%-- No bookings to show --%>
    <c:when test="${noBookings}">
      <section class="card">
        <t:empty message="${msg.p05}" icon="event_available"/>
      </section>
    </c:when>
    <c:otherwise>
      <div class="status-layout">
        <%-- Booking List --%>
        <section class="card">
          <h2 class="card__title">${msg.ui_status_list}</h2>

          <%-- Desktop Table --%>
          <div class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>${msg.ui_facility}</th>
                  <th>${msg.ui_date}</th>
                  <th>${msg.ui_sessions}</th>
                  <th>${msg.ui_status}</th>
                  <th><span class="sr-only">${msg.ui_select}</span></th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="item" items="${items}">
                  <c:set var="isSelected" value="${selected.id == item.id}"/>
                  <tr id="row-${item.id}" <c:if test="${isSelected}">class="table__row--selected" aria-current="true"</c:if>>
                    <td class="table__strong"><c:out value="${item.facilityName}"/></td>
                    <td><c:out value="${item.dateLabel}"/></td>
                    <td><c:out value="${item.slotRanges}"/></td>
                    <td><t:badge status="${item.status}"/></td>
                    <td class="text-right">
                      <button type="button" class="btn <c:out value="${isSelected ? 'btn--primary' : 'btn--secondary'}"/>"
                              data-select-target="row-${item.id}" data-select-group="bookings"
                              data-label="${msg.ui_select}" data-label-selected="${msg.ui_selected}"
                              aria-pressed="${isSelected}">${isSelected ? msg.ui_selected : msg.ui_select}</button>
                    </td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>

          <%-- Mobile Card List --%>
          <div class="card-list">
            <c:forEach var="item" items="${items}">
              <c:set var="isSelected" value="${selected.id == item.id}"/>
              <div id="card-${item.id}" class="card-list__item <c:if test="${isSelected}">card-list__item--selected</c:if>">
                <div class="card-list__header">
                  <span class="card-list__title"><c:out value="${item.facilityName}"/></span>
                  <t:badge status="${item.status}"/>
                </div>
                <div class="card-list__meta"><c:out value="${item.dateLabel}"/> &middot; <c:out value="${item.slotRanges}"/></div>
                <div class="card-list__actions">
                  <button type="button" class="btn btn--secondary"
                          data-select-target="card-${item.id}" data-select-group="bookings-mobile"
                          data-label="${msg.ui_select}" data-label-selected="${msg.ui_selected}"
                          aria-pressed="${isSelected}">${msg.ui_select}</button>
                </div>
              </div>
            </c:forEach>
          </div>
        </section>

        <%-- Booking Details (shown when selected via JS) --%>
        <c:forEach var="item" items="${items}">
          <section class="card status__detail" id="detail-${item.id}" data-detail-for="row-${item.id}" hidden>
            <h2 id="detail-title-${item.id}" class="card__title card__title--divided" tabindex="-1">${msg.ui_details}</h2>

            <dl class="detail">
              <div class="detail__item"><dt>${msg.ui_facility}</dt><dd><c:out value="${item.facilityName}"/></dd></div>
              <div class="detail__item"><dt>${msg.ui_date}</dt><dd><c:out value="${item.dateLabel}"/></dd></div>
              <div class="detail__item"><dt>${msg.ui_sessions}</dt><dd><c:out value="${item.slotRanges}"/></dd></div>
              <div class="detail__item"><dt>${msg.ui_unit}</dt><dd><c:out value="${item.unit}"/></dd></div>
              <div class="detail__item"><dt>${msg.ui_participants}</dt><dd><c:out value="${item.participants}"/></dd></div>
              <div class="detail__item"><dt>${msg.ui_contact}</dt><dd><c:out value="${item.contact}"/></dd></div>
              <div class="detail__item detail__item--wide detail__item--text"><dt>${msg.ui_purpose}</dt><dd><c:out value="${item.purpose}"/></dd></div>
              <div class="detail__item"><dt>${msg.ui_submitted}</dt><dd><c:out value="${item.submittedLabel}"/></dd></div>
              <div class="detail__item"><dt>${msg.ui_status}</dt><dd><t:badge status="${item.status}"/></dd></div>
              <c:if test="${item.status.css == 'rejected' and not empty item.rejectionReason}">
                <div class="detail__item detail__item--wide detail__item--text">
                  <dt>${msg.ui_rejection_reason}</dt>
                  <dd class="detail__rejection"><c:out value="${item.rejectionReason}"/></dd>
                </div>
              </c:if>
            </dl>
          </section>
        </c:forEach>

        <%-- Placeholder when nothing is selected --%>
        <section class="card status__detail status__placeholder" id="detail-placeholder">
          <div class="empty-state">
            <span class="material-symbols-outlined empty-state__icon" aria-hidden="true">touch_app</span>
            <p class="empty-state__message">${msg.ui_select_booking}</p>
          </div>
        </section>
      </div>
    </c:otherwise>
  </c:choose>
</t:layout>
