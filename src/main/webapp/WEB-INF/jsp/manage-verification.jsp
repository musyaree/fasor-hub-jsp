<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="${msg.ui_nav_verification}" subtitle="${msg.ui_verification_subtitle}" currentPage="verification">
  <c:set var="base" value="${pageContext.request.contextPath}/verification"/>

  <c:choose>
    <c:when test="${empty queue}">
      <t:empty message="${msg.p28}" icon="task_alt"/>
    </c:when>
    <c:otherwise>
      <section class="card">
        <h2 class="card__title">${msg.ui_queue}</h2>

        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>${msg.ui_submitted}</th>
                <th>${msg.ui_requester}</th>
                <th>${msg.ui_facility}</th>
                <th>${msg.ui_date}</th>
                <th><span class="sr-only">${msg.ui_select}</span></th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="item" items="${queue}">
                <c:set var="isSelected" value="${selected.id == item.id}"/>
                <tr <c:if test="${isSelected}">class="table__row--selected" aria-current="true"</c:if>>
                  <td><c:out value="${item.submittedLabel}"/></td>
                  <td class="table__strong"><c:out value="${item.requesterName}"/> (<c:out value="${item.requesterIdentity}"/>)</td>
                  <td><c:out value="${item.facilityName}"/></td>
                  <td><c:out value="${item.dateLabel}"/></td>
                  <td class="text-right"><a class="btn <c:out value="${isSelected ? 'btn--primary' : 'btn--secondary'}"/>" href="${base}?booking=${item.id}">${isSelected ? msg.ui_selected : msg.ui_select}</a></td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>

        <div class="card-list">
          <c:forEach var="item" items="${queue}">
            <div class="card-list__item <c:if test="${selected.id == item.id}">card-list__item--selected</c:if>">
              <div class="card-list__header">
                <span class="card-list__title"><c:out value="${item.requesterName}"/></span>
                <span class="card-list__meta"><c:out value="${item.submittedLabel}"/></span>
              </div>
              <div class="card-list__meta"><c:out value="${item.facilityName}"/>, <c:out value="${item.dateLabel}"/></div>
              <div class="card-list__actions"><a class="btn btn--secondary" href="${base}?booking=${item.id}">${msg.ui_select}</a></div>
            </div>
          </c:forEach>
        </div>
      </section>

      <c:if test="${not empty selected}">
        <section class="card" aria-labelledby="detail-title">
          <h2 id="detail-title" class="card__title card__title--divided">${msg.ui_details}</h2>

          <dl class="detail">
            <div class="detail__item"><dt>${msg.ui_requester}</dt><dd><c:out value="${selected.requesterName}"/> (<c:out value="${selected.requesterIdentity}"/>)</dd></div>
            <div class="detail__item"><dt>${msg.ui_unit}</dt><dd><c:out value="${selected.unit}"/></dd></div>
            <div class="detail__item"><dt>${msg.ui_contact}</dt><dd><c:out value="${selected.contact}"/></dd></div>
            <div class="detail__item"><dt>${msg.ui_participants}</dt><dd><c:out value="${selected.participants}"/></dd></div>
            <div class="detail__item"><dt>${msg.ui_facility}</dt><dd><c:out value="${selected.facilityName}"/></dd></div>
            <div class="detail__item"><dt>${msg.ui_date}</dt><dd><c:out value="${selected.dateLabel}"/></dd></div>
            <div class="detail__item"><dt>${msg.ui_sessions}</dt><dd><c:out value="${selected.slotRanges}"/></dd></div>
            <div class="detail__item detail__item--wide"><dt>${msg.ui_purpose}</dt><dd><c:out value="${selected.purpose}"/></dd></div>
          </dl>

          <c:if test="${not selected.allAvailable}">
            <p class="notice notice--warning" role="alert">
              <span class="material-symbols-outlined" aria-hidden="true">warning</span>
              ${msg.p15}
            </p>
          </c:if>

          <div class="actions">
            <button type="button" class="btn btn--danger" data-dialog-open="reject-dialog">${msg.ui_reject}</button>
            <form method="post" action="${base}/approve">
              <input type="hidden" name="_csrf" value="${sessionScope._csrf}">
              <input type="hidden" name="bookingId" value="${selected.id}">
              <button type="submit" class="btn btn--primary" <c:if test="${not selected.allAvailable}">disabled</c:if>>${msg.ui_approve}</button>
            </form>
          </div>
        </section>

        <div id="reject-dialog" class="dialog-backdrop <c:if test="${rejectOpen}">is-open</c:if>" aria-hidden="${not rejectOpen}">
          <div class="dialog" role="dialog" aria-modal="true" aria-labelledby="reject-title">
            <form method="post" action="${base}/reject">
              <input type="hidden" name="_csrf" value="${sessionScope._csrf}">
              <input type="hidden" name="bookingId" value="${selected.id}">

              <div class="dialog__header">
                <h2 id="reject-title" class="dialog__title">${msg.ui_rejection_reason}</h2>
                <button type="button" class="dialog__close" data-dialog-close aria-label="${msg.ui_close}">
                  <span class="material-symbols-outlined" aria-hidden="true">close</span>
                </button>
              </div>

              <div class="dialog__body">
                <label class="form-label" for="reason">${msg.ui_rejection_reason}</label>
                <textarea id="reason" name="reason" class="form-input" maxlength="255" rows="4"
                          <c:if test="${not empty reasonError}">aria-invalid="true" aria-describedby="reason-error"</c:if>><c:choose><c:when test="${not empty reasonError}"></c:when><c:when test="${not empty reason}"><c:out value="${reason}"/></c:when><c:when test="${not selected.allAvailable}"><c:out value="${msg.p16}"/></c:when></c:choose></textarea>
                <c:if test="${not empty reasonError}">
                  <p id="reason-error" class="field-error" role="alert"><c:out value="${reasonError}"/></p>
                </c:if>
              </div>

              <div class="dialog__footer">
                <button type="button" class="btn btn--secondary" data-dialog-close>${msg.ui_cancel}</button>
                <button type="submit" class="btn btn--danger">${msg.ui_confirm_rejection}</button>
              </div>
            </form>
          </div>
        </div>
      </c:if>
    </c:otherwise>
  </c:choose>
</t:layout>
