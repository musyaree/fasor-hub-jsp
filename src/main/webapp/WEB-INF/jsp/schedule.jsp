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
      <form id="booking-form" method="post" action="${base}" data-booking-form>
        <input type="hidden" name="_csrf" value="${sessionScope._csrf}">
        <input type="hidden" name="facility" value="${selectedFacility.id}">
        <input type="hidden" name="month" value="${grid.param}">
        <c:if test="${selectedDate != null}">
          <input type="hidden" name="date" value="${selectedDate}">
        </c:if>

        <div class="schedule-layout">
          <%-- Step 1: Select Schedule --%>
          <section class="card schedule__step">
            <h2 class="card__title">${msg.ui_step1}</h2>

            <%-- Facility selector --%>
            <div class="form-group">
              <label for="facility-select" class="form-label">${msg.ui_facility}</label>
              <select id="facility-select" class="form-select" onchange="window.location.href='${base}?facility='+this.value+'&amp;month=${grid.param}'">
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

            <%-- Sessions with checkboxes --%>
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
                  <div class="slot-list" data-slot-list>
                    <c:forEach var="slot" items="${slots}">
                      <label class="slot-item slot-item--${slot.status.css} <c:if test="${slot.status.css != 'approved'}">slot-item--disabled</c:if>">
                        <c:choose>
                          <c:when test="${slot.status.css == 'approved'}">
                            <input type="checkbox" name="slots" value="${slot.id}" class="slot-item__checkbox" data-slot-checkbox>
                          </c:when>
                          <c:otherwise>
                            <input type="checkbox" disabled class="slot-item__checkbox">
                          </c:otherwise>
                        </c:choose>
                        <span class="slot-item__time">${slot.range}</span>
                        <t:badge status="${slot.status}"/>
                      </label>
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
                    <div class="summary__item">
                      <dt>${msg.ui_sessions}</dt>
                      <dd data-selected-count>0 of 4 selected</dd>
                    </div>
                  </dl>
                </c:when>
                <c:otherwise>
                  <p class="text-muted">${msg.ui_no_session_selected}</p>
                </c:otherwise>
              </c:choose>
            </div>

            <%-- Applicant Data --%>
            <div class="schedule__section">
              <h3 class="schedule__section-title">${msg.ui_applicant_data}</h3>

              <div class="form-group">
                <label class="form-label">${msg.ui_name}</label>
                <input type="text" class="form-input" value="<c:out value="${sessionScope.user.name}"/>" disabled>
              </div>

              <div class="form-group">
                <label for="unit" class="form-label">${msg.ui_unit}</label>
                <input type="text" id="unit" name="unit" class="form-input" maxlength="50" required
                       placeholder="e.g. Informatics Student Association">
              </div>

              <div class="form-group">
                <label for="participants" class="form-label">${msg.ui_participants}</label>
                <input type="number" id="participants" name="participants" class="form-input" min="1" max="999" required
                       placeholder="e.g. 12">
              </div>

              <div class="form-group">
                <label for="contact" class="form-label">${msg.ui_contact}</label>
                <input type="text" id="contact" name="contact" class="form-input" maxlength="20" required
                       value="<c:out value="${sessionScope.user.phone}"/>" placeholder="e.g. 0812-3456-7890">
              </div>

              <div class="form-group">
                <label for="purpose" class="form-label">${msg.ui_purpose}</label>
                <textarea id="purpose" name="purpose" class="form-input form-textarea" maxlength="200" required
                          placeholder="e.g. Regular futsal practice" rows="3"></textarea>
              </div>
            </div>

            <%-- Submit button --%>
            <div class="schedule__actions">
              <button type="button" class="btn btn--primary" data-dialog-open="terms-dialog" data-submit-btn disabled>
                ${msg.ui_submit_booking}
              </button>
            </div>
          </section>
        </div>

        <%-- Terms Dialog --%>
        <div id="terms-dialog" class="dialog-backdrop" aria-hidden="true">
          <div class="dialog" role="dialog" aria-modal="true" aria-labelledby="terms-title">
            <div class="dialog__header">
              <h2 id="terms-title" class="dialog__title">${msg.ui_terms_title}</h2>
              <button type="button" class="dialog__close" data-dialog-close aria-label="${msg.ui_close}">
                <span class="material-symbols-outlined" aria-hidden="true">close</span>
              </button>
            </div>
            <div class="dialog__body">
              <p>${msg.ui_terms_intro}</p>
              <ul class="terms-list">
                <li>${msg.ui_terms_1}</li>
                <li>${msg.ui_terms_2}</li>
                <li>${msg.ui_terms_3}</li>
                <li>${msg.ui_terms_4}</li>
                <li>${msg.ui_terms_5}</li>
                <li>${msg.ui_terms_6}</li>
              </ul>
              <label class="checkbox-label">
                <input type="checkbox" id="agree-terms" data-agree-checkbox>
                <span>${msg.ui_terms_agree}</span>
              </label>
            </div>
            <div class="dialog__footer">
              <button type="submit" class="btn btn--primary" data-confirm-btn disabled>${msg.ui_terms_submit}</button>
            </div>
          </div>
        </div>
      </form>
    </c:otherwise>
  </c:choose>
</t:layout>
