<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="${msg.ui_nav_schedule}" subtitle="${msg.ui_schedule_subtitle}" currentPage="schedule">
  <c:set var="base" value="${pageContext.request.contextPath}/schedule"/>

  <c:choose>
    <%-- No facilities at all --%>
    <c:when test="${noFacilities}">
      <section class="card">
        <t:empty message="${msg.p01}" icon="sports_soccer"/>
      </section>
    </c:when>
    <c:otherwise>
      <div class="schedule-layout">
        <%-- Step 1: Select Schedule --%>
        <section class="card schedule__step">
          <h2 class="card__title">${msg.ui_step1}</h2>

          <%-- Facility selector --%>
          <div class="form-group">
            <label for="facility" class="form-label">${msg.ui_facility}</label>
            <select id="facility" name="facility" class="form-select" onchange="window.location.href='${base}?facility='+this.value+'&amp;month=${grid.param}'">
              <c:forEach var="f" items="${facilities}">
                <option value="${f.id}" <c:if test="${f.id == selectedFacility.id}">selected</c:if>><c:out value="${f.name}"/></option>
              </c:forEach>
            </select>
            <span class="form-hint">${msg.ui_facility_type}: <c:out value="${selectedFacility.type.name}"/></span>
          </div>

          <%-- Calendar --%>
          <t:calendar grid="${grid}" facilityId="${selectedFacility.id}" selectedDate="${selectedDate}" baseUrl="${base}"/>

          <%-- No selectable dates message --%>
          <c:if test="${noSelectableDates}">
            <p class="notice notice--info">
              <span class="material-symbols-outlined" aria-hidden="true">info</span>
              ${msg.p01}
            </p>
          </c:if>
        </section>

        <%-- Step 2: Booking Details --%>
        <section class="card schedule__step">
          <h2 class="card__title">${msg.ui_step2}</h2>

          <%-- Sessions --%>
          <div class="schedule__section">
            <h3 class="schedule__section-title">${msg.ui_sessions_title}</h3>
            <c:choose>
              <c:when test="${selectedDate == null}">
                <p class="text-muted">${msg.p25}</p>
              </c:when>
              <c:when test="${empty slots}">
                <p class="text-muted">${msg.p01}</p>
              </c:when>
              <c:otherwise>
                <div class="slot-list">
                  <c:forEach var="slot" items="${slots}">
                    <div class="slot-item slot-item--${slot.status.css}">
                      <span class="slot-item__time">${slot.range}</span>
                      <t:badge status="${slot.status}"/>
                    </div>
                  </c:forEach>
                </div>
              </c:otherwise>
            </c:choose>
          </div>

          <%-- Summary --%>
          <div class="schedule__section">
            <h3 class="schedule__section-title">${msg.ui_summary}</h3>
            <c:choose>
              <c:when test="${selectedDate != null and not empty slots}">
                <dl class="summary">
                  <div class="summary__item">
                    <dt>${msg.ui_facility}</dt>
                    <dd><c:out value="${selectedFacility.name}"/></dd>
                  </div>
                  <div class="summary__item">
                    <dt>${msg.ui_date}</dt>
                    <dd>${selectedDateFormatted}</dd>
                  </div>
                </dl>
                <p class="text-muted">${msg.ui_no_session_selected}</p>
              </c:when>
              <c:otherwise>
                <p class="text-muted">${msg.ui_no_session_selected}</p>
              </c:otherwise>
            </c:choose>
          </div>

          <%-- Applicant Data --%>
          <div class="schedule__section">
            <h3 class="schedule__section-title">${msg.ui_applicant_data}</h3>
            <dl class="summary">
              <div class="summary__item">
                <dt>${msg.ui_name}</dt>
                <dd><c:out value="${sessionScope.user.name}"/></dd>
              </div>
            </dl>
            <p class="text-muted form-hint">
              <span class="material-symbols-outlined" aria-hidden="true">info</span>
              Complete the booking form after selecting sessions (coming soon).
            </p>
          </div>

          <%-- Submit button (disabled for now - UC02 not implemented yet) --%>
          <div class="schedule__actions">
            <button type="button" class="btn btn--primary" disabled>${msg.ui_submit_booking}</button>
          </div>
        </section>
      </div>
    </c:otherwise>
  </c:choose>
</t:layout>
