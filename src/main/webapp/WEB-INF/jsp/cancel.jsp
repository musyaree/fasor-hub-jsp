<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="${msg.ui_nav_cancellations}" subtitle="${msg.ui_cancel_subtitle}" currentPage="cancellations">
  <c:set var="base" value="${pageContext.request.contextPath}/cancellations"/>

  <c:choose>
    <%-- No bookings to cancel --%>
    <c:when test="${noBookings}">
      <section class="card">
        <t:empty message="${msg.p27}" icon="event_available"/>
      </section>
    </c:when>
    <c:otherwise>
      <div class="cancel-layout" data-async-region>
        <%-- Booking List --%>
        <section class="card">
          <h2 class="card__title">${msg.ui_cancel_list}</h2>

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
                      <a data-async-link class="btn <c:out value="${isSelected ? 'btn--primary' : 'btn--secondary'}"/>"
                         href="${base}?booking=${item.id}">${isSelected ? msg.ui_selected : msg.ui_select}</a>
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
                  <a data-async-link class="btn btn--secondary" href="${base}?booking=${item.id}">${msg.ui_select}</a>
                </div>
              </div>
            </c:forEach>
          </div>
        </section>

        <%-- Detail and Cancel Form --%>
        <c:choose>
          <c:when test="${not empty selected}">
            <section class="card" aria-labelledby="detail-title">
              <h2 id="detail-title" class="card__title card__title--divided" tabindex="-1">${msg.ui_details}</h2>

              <dl class="detail">
                <div class="detail__item"><dt>${msg.ui_facility}</dt><dd><c:out value="${selected.facilityName}"/></dd></div>
                <div class="detail__item"><dt>${msg.ui_date}</dt><dd><c:out value="${selected.dateLabel}"/></dd></div>
                <div class="detail__item"><dt>${msg.ui_sessions}</dt><dd><c:out value="${selected.slotRanges}"/></dd></div>
                <div class="detail__item"><dt>${msg.ui_unit}</dt><dd><c:out value="${selected.unit}"/></dd></div>
                <div class="detail__item"><dt>${msg.ui_participants}</dt><dd><c:out value="${selected.participants}"/></dd></div>
                <div class="detail__item"><dt>${msg.ui_contact}</dt><dd><c:out value="${selected.contact}"/></dd></div>
                <div class="detail__item detail__item--wide detail__item--text"><dt>${msg.ui_purpose}</dt><dd><c:out value="${selected.purpose}"/></dd></div>
                <div class="detail__item"><dt>${msg.ui_submitted}</dt><dd><c:out value="${selected.submittedLabel}"/></dd></div>
                <div class="detail__item"><dt>${msg.ui_status}</dt><dd><t:badge status="${selected.status}"/></dd></div>
              </dl>

              <%-- Cancel Form --%>
              <form method="post" action="${base}" class="cancel-form">
                <input type="hidden" name="_csrf" value="${sessionScope._csrf}">
                <input type="hidden" name="booking" value="${selected.id}">

                <div class="form-group">
                  <label for="reason" class="form-label">${msg.ui_cancel_reason}</label>
                  <textarea id="reason" name="reason" class="form-input form-textarea" maxlength="255" required
                            placeholder="${msg.ui_cancel_reason_placeholder}" rows="3"></textarea>
                  <span class="form-hint">${msg.ui_cancel_reason_hint}</span>
                </div>

                <div class="form-actions">
                  <button type="submit" class="btn btn--danger">${msg.ui_request_cancellation}</button>
                </div>
              </form>
            </section>
          </c:when>
          <c:otherwise>
            <section class="card cancel__placeholder">
              <div class="empty-state">
                <span class="material-symbols-outlined empty-state__icon" aria-hidden="true">touch_app</span>
                <p class="empty-state__message">${msg.ui_select_to_cancel}</p>
              </div>
            </section>
          </c:otherwise>
        </c:choose>
      </div>
    </c:otherwise>
  </c:choose>
</t:layout>
