<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="${msg.ui_nav_verification}" subtitle="${msg.ui_verification_subtitle}" currentPage="verification">
  <c:set var="base" value="${pageContext.request.contextPath}/verification"/>
    <section class="card filter-card">
    <form method="get" action="${base}" class="filter-bar">
      <div class="form-group">
      <label class="form-label" for="facility">${msg.ui_facility}</label>
      <select class="form-input form-select" id="facility" name="facility" onchange="this.form.submit()">
        <option value="">${msg.ui_all_facilities}</option>
        <c:forEach var="option" items="${facilities}">
          <option value="${option.id}"
                  <c:if test="${facilityFilter == option.id}">selected</c:if>>
            <c:out value="${option.name}"/>
          </option>
        </c:forEach>
      </select>
      </div>
      <div class="filter-bar__actions">
        <button class="btn btn--primary" type="submit">${msg.ui_apply}</button>
        <a class="btn btn--secondary" href="${base}">${msg.ui_reset}</a>
      </div>
    </form>
  </section>
  <c:choose>
      <c:when test="${filterEmpty}">
      <section class="card">
        <h2 class="card__title card__title--divided">${msg.ui_queue}</h2>
        <t:empty message="${msg.p33}" icon="search"/>
      </section>
    </c:when>
    <c:when test="${empty queue}">
      <section class="card">
        <h2 class="card__title card__title--divided">${msg.ui_queue}</h2>
        <t:empty message="${msg.p28}" icon="fact_check"/>
      </section>
    </c:when>
    <c:otherwise>
      <div data-async-region>
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
                  <td class="text-right">
                  <a data-async-link
                    class="btn <c:out value="${isSelected ? 'btn--primary' : 'btn--secondary'}"/>"
                    href="${base}?booking=${item.id}<c:if test='${not empty facilityFilter}'>&amp;facility=${facilityFilter}</c:if>">
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
            <div class="card-list__item <c:if test="${selected.id == item.id}">card-list__item--selected</c:if>">
              <div class="card-list__header">
                <span class="card-list__title"><c:out value="${item.requesterName}"/></span>
                <span class="card-list__meta"><c:out value="${item.submittedLabel}"/></span>
              </div>
              <div class="card-list__meta"><c:out value="${item.facilityName}"/>, <c:out value="${item.dateLabel}"/></div>
              <div class="card-list__actions"><a data-async-link class="btn btn--secondary" href="${base}?booking=${item.id}<c:if test='${not empty facilityFilter}'>&amp;facility=${facilityFilter}</c:if>">${msg.ui_select}</a></div>
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
            <div class="detail__item detail__item--wide detail__item--text"><dt>${msg.ui_purpose}</dt><dd><c:out value="${selected.purpose}"/></dd></div>
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
              <input type="hidden" name="facility" value="${facilityFilter}">
              <button type="submit" class="btn btn--primary" <c:if test="${not selected.allAvailable}">disabled</c:if>>${msg.ui_approve}</button>
            </form>
          </div>
        </section>

        <div id="reject-dialog" class="dialog-backdrop <c:if test="${rejectOpen}">is-open</c:if>" aria-hidden="${not rejectOpen}">
          <div class="dialog" role="dialog" aria-modal="true" aria-labelledby="reject-title">
            <form method="post" action="${base}/reject">
              <input type="hidden" name="_csrf" value="${sessionScope._csrf}">
              <input type="hidden" name="bookingId" value="${selected.id}">
              <input type="hidden" name="facility" value="${facilityFilter}">
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
      </div>

      <template id="async-skeleton">
        <section class="card" aria-hidden="true">
          <h2 class="card__title">${msg.ui_queue}</h2>
          <div class="skeleton-list">
            <c:forEach begin="1" end="3">
              <div class="skeleton-row">
                <span class="skeleton skeleton--w3"></span>
                <span class="skeleton skeleton--w5"></span>
                <span class="skeleton skeleton--w4"></span>
                <span class="skeleton skeleton--w3"></span>
                <span class="skeleton skeleton--button"></span>
              </div>
            </c:forEach>
          </div>
        </section>
        <section class="card" aria-hidden="true">
          <h2 class="card__title card__title--divided">${msg.ui_details}</h2>
          <div class="skeleton-detail">
            <c:forEach begin="1" end="6">
              <div class="skeleton-detail__item">
                <span class="skeleton skeleton--label"></span>
                <span class="skeleton skeleton--value"></span>
              </div>
            </c:forEach>
            <div class="skeleton-detail__item skeleton-detail__item--wide">
              <span class="skeleton skeleton--label"></span>
              <span class="skeleton skeleton--value"></span>
            </div>
          </div>
          <div class="actions">
            <span class="skeleton skeleton--button"></span>
            <span class="skeleton skeleton--button"></span>
          </div>
        </section>
      </template>
    </c:otherwise>
  </c:choose>
</t:layout>
