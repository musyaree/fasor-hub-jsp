<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout
        title="${msg.ui_nav_cancellation_requests}"
        subtitle="${msg.ui_cancellation_requests_subtitle}"
        currentPage="cancellation-requests">

  <c:set var="base"
         value="${pageContext.request.contextPath}/cancellation-requests"/>

  <c:choose>

    <%-- P29: Show an empty state when no cancellation is waiting. --%>
    <c:when test="${empty queue}">
      <section class="card">
        <h2 class="card__title card__title--divided">
          ${msg.ui_nav_cancellation_requests}
        </h2>

        <t:empty message="${msg.p29}" icon="event_busy"/>
      </section>
    </c:when>

    <c:otherwise>
      <div data-async-region>

        <section class="card">
          <h2 class="card__title">
            ${msg.ui_nav_cancellation_requests}
          </h2>

          <div class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>${msg.ui_requested}</th>
                  <th>${msg.ui_requester}</th>
                  <th>${msg.ui_facility}</th>
                  <th>${msg.ui_date}</th>
                  <th>
                    <span class="sr-only">${msg.ui_select}</span>
                  </th>
                </tr>
              </thead>

              <tbody>
                <c:forEach var="item" items="${queue}">
                  <c:set var="isSelected"
                         value="${selected.id == item.id}"/>

                  <tr
                    <c:if test="${isSelected}">
                      class="table__row--selected"
                      aria-current="true"
                    </c:if>>

                    <td>
                      <c:out value="${item.submittedLabel}"/>
                    </td>

                    <td class="table__strong">
                      <c:out value="${item.requesterName}"/>
                      (<c:out value="${item.requesterIdentity}"/>)
                    </td>

                    <td>
                      <c:out value="${item.facilityName}"/>
                    </td>

                    <td>
                      <c:out value="${item.dateLabel}"/>
                    </td>

                    <td class="text-right">
                      <a data-async-link
                         class="btn
                         <c:out value="${isSelected ? 'btn--primary' : 'btn--secondary'}"/>"
                         href="${base}?booking=${item.id}">
                        ${isSelected ? msg.ui_selected : msg.ui_select}
                      </a>
                    </td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>

          <div class="card-list">
            <c:forEach var="item" items="${queue}">
              <div class="card-list__item
              <c:if test="${selected.id == item.id}">
                card-list__item--selected
              </c:if>">

                <div class="card-list__header">
                  <span class="card-list__title">
                    <c:out value="${item.requesterName}"/>
                  </span>

                  <span class="card-list__meta">
                    <c:out value="${item.submittedLabel}"/>
                  </span>
                </div>

                <div class="card-list__meta">
                  <c:out value="${item.facilityName}"/>,
                  <c:out value="${item.dateLabel}"/>
                </div>

                <div class="card-list__actions">
                  <a data-async-link
                     class="btn btn--secondary"
                     href="${base}?booking=${item.id}">
                    ${msg.ui_select}
                  </a>
                </div>
              </div>
            </c:forEach>
          </div>
        </section>

        <%-- Selected request details and confirmation button. --%>
        <c:if test="${not empty selected}">
          <section class="card" aria-labelledby="detail-title">

            <h2 id="detail-title"
                class="card__title card__title--divided">
              ${msg.ui_details}
            </h2>

            <dl class="detail">
              <div class="detail__item">
                <dt>${msg.ui_requester}</dt>
                <dd>
                  <c:out value="${selected.requesterName}"/>
                  (<c:out value="${selected.requesterIdentity}"/>)
                </dd>
              </div>

              <div class="detail__item">
                <dt>${msg.ui_unit}</dt>
                <dd>
                  <c:out value="${selected.unit}"/>
                </dd>
              </div>

              <div class="detail__item">
                <dt>${msg.ui_contact}</dt>
                <dd>
                  <c:out value="${selected.contact}"/>
                </dd>
              </div>

              <div class="detail__item">
                <dt>${msg.ui_participants}</dt>
                <dd>
                  <c:out value="${selected.participants}"/>
                </dd>
              </div>

              <div class="detail__item">
                <dt>${msg.ui_facility}</dt>
                <dd>
                  <c:out value="${selected.facilityName}"/>
                </dd>
              </div>

              <div class="detail__item">
                <dt>${msg.ui_date}</dt>
                <dd>
                  <c:out value="${selected.dateLabel}"/>
                </dd>
              </div>

              <div class="detail__item">
                <dt>${msg.ui_sessions}</dt>
                <dd>
                  <c:out value="${selected.slotRanges}"/>
                </dd>
              </div>

              <%-- Previous status is saved by requestCancellation(). --%>
              <div class="detail__item">
                <dt>${msg.ui_previous_status}</dt>
                <dd>
                  <c:out value="${selected.booking.previousStatus.label}"/>
                </dd>
              </div>

              <div class="detail__item detail__item--wide detail__item--text">
                <dt>${msg.ui_cancel_reason}</dt>
                <dd>
                  <c:out value="${selected.booking.cancellationReason}"/>
                </dd>
              </div>

              <div class="detail__item detail__item--wide detail__item--text">
                <dt>${msg.ui_purpose}</dt>
                <dd>
                  <c:out value="${selected.purpose}"/>
                </dd>
              </div>
            </dl>

            <div class="actions">
              <form method="post" action="${base}/confirm">
                <input type="hidden"
                       name="_csrf"
                       value="${sessionScope._csrf}">

                <input type="hidden"
                       name="bookingId"
                       value="${selected.id}">

                <button type="submit" class="btn btn--primary">
                  ${msg.ui_confirm_cancellation}
                </button>
              </form>
            </div>
          </section>
        </c:if>

      </div>
    </c:otherwise>
  </c:choose>
</t:layout>