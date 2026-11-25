<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="${msg.ui_nav_history}" currentPage="history">
  <c:choose>
    <c:when test="${historyLoadError}">
      <section class="card">
        <div class="empty-state">
          <span class="material-symbols-outlined empty-state__icon" aria-hidden="true">error</span>
          <p class="empty-state__message"><c:out value="${msg.p09}"/></p>
          <a class="btn btn--primary" href="${pageContext.request.contextPath}/history">
            ${msg.ui_try_again}
          </a>
        </div>
      </section>
    </c:when>

    <c:when test="${noBookings}">
      <section class="card">
        <t:empty message="${msg.p08}" icon="history"/>
      </section>
    </c:when>

    <c:otherwise>
      <div class="status-layout">
        <section class="card">
          <h2 class="card__title">${msg.ui_nav_history}</h2>
          <div class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <c:if test="${sessionScope.user.role.manager}">
                    <th>${msg.ui_requester}</th>
                  </c:if>
                  <th>${msg.ui_facility}</th>
                  <th>${msg.ui_date}</th>
                  <th>${msg.ui_sessions}</th>
                  <th>${msg.ui_status}</th>
                  <th><span class="sr-only">${msg.ui_select}</span></th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="item" items="${items}">
                  <tr id="row-${item.id}">
                    <c:if test="${sessionScope.user.role.manager}">
                      <td><c:out value="${item.requester}"/></td>
                    </c:if>
                    <td class="table__strong"><c:out value="${item.facilityName}"/></td>
                    <td><c:out value="${item.dateLabel}"/></td>
                    <td><c:out value="${item.slotRanges}"/></td>
                    <td><t:badge status="${item.status}"/></td>
                    <td class="text-right">
                      <button type="button" class="btn btn--secondary"
                              data-select-target="row-${item.id}" data-select-group="history"
                              aria-pressed="false">${msg.ui_select}</button>
                    </td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </section>

        <c:forEach var="item" items="${items}">
          <section class="card status__detail" id="detail-${item.id}"
                   data-detail-for="row-${item.id}" hidden>
            <h2 class="card__title card__title--divided" tabindex="-1">${msg.ui_details}</h2>
            <dl class="detail">
              <c:if test="${sessionScope.user.role.manager}">
                <div class="detail__item">
                  <dt>${msg.ui_requester}</dt>
                  <dd><c:out value="${item.requester}"/></dd>
                </div>
              </c:if>
              <div class="detail__item">
                <dt>${msg.ui_facility}</dt>
                <dd><c:out value="${item.facilityName}"/></dd>
              </div>
              <div class="detail__item">
                <dt>${msg.ui_date}</dt>
                <dd><c:out value="${item.dateLabel}"/></dd>
              </div>
              <div class="detail__item">
                <dt>${msg.ui_sessions}</dt>
                <dd><c:out value="${item.slotRanges}"/></dd>
              </div>
              <div class="detail__item detail__item--wide detail__item--text">
                <dt>${msg.ui_purpose}</dt>
                <dd><c:out value="${item.purpose}"/></dd>
              </div>
              <div class="detail__item">
                <dt>${msg.ui_submitted}</dt>
                <dd><c:out value="${item.submittedLabel}"/></dd>
              </div>
              <div class="detail__item">
                <dt>${msg.ui_status}</dt>
                <dd><t:badge status="${item.status}"/></dd>
              </div>
              <c:if test="${item.status.code == '3' and not empty item.rejectionReason}">
                <div class="detail__item detail__item--wide detail__item--text">
                  <dt>${msg.ui_rejection_reason}</dt>
                  <dd class="detail__rejection"><c:out value="${item.rejectionReason}"/></dd>
                </div>
              </c:if>
            </dl>
          </section>
        </c:forEach>

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