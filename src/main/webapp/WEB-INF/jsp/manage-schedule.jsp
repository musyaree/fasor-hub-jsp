<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout
    title="${msg.ui_nav_manage_schedule}"
    currentPage="manage-schedule">

  <c:set
      var="base"
      value="${pageContext.request.contextPath}/manage-schedule"/>

  <c:choose>
    <c:when test="${noFacilities}">
      <section class="card">
        <t:empty message="${msg.p01}" icon="sports_soccer"/>
      </section>
    </c:when>

    <c:otherwise>
      <form
          method="post"
          action="${base}/save"
          data-manage-schedule-form
          data-stage-url="${base}/stage">

        <input
            type="hidden"
            name="_csrf"
            value="${sessionScope._csrf}">

        <input
            type="hidden"
            name="facility"
            value="${selectedFacility.id}">

        <input
            type="hidden"
            name="month"
            value="${grid.param}">

        <c:if test="${selectedDate != null}">
          <input
              type="hidden"
              name="date"
              value="${selectedDate}">
        </c:if>

        <div class="schedule-layout">
          <section class="card schedule__step">
            <h2 class="card__title">${msg.ui_step1}</h2>

            <div class="form-group">
              <label
                  for="facility-select"
                  class="form-label">
                ${msg.ui_facility}
              </label>

              <select
                  id="facility-select"
                  class="form-select"
                  onchange="window.location.href='${base}?facility='+this.value+'&amp;month=${grid.param}'">
                <c:forEach var="facility" items="${facilities}">
                  <option
                      value="${facility.id}"
                      <c:if test="${facility.id == selectedFacility.id}">
                        selected
                      </c:if>>
                    <c:out value="${facility.name}"/>
                  </option>
                </c:forEach>
              </select>

              <span class="form-hint">
                ${msg.ui_facility_type}:
                <c:out value="${selectedFacility.type.name}"/>
              </span>
            </div>

            <%-- Reuse the shared calendar to keep date navigation consistent. --%>
            <t:calendar
                grid="${grid}"
                facilityId="${selectedFacility.id}"
                selectedDate="${selectedDate}"
                baseUrl="${base}"/>

            <c:if test="${noSelectableDates}">
              <p class="notice notice--info">
                <span
                    class="material-symbols-outlined"
                    aria-hidden="true">info</span>
                ${msg.p01}
              </p>
            </c:if>
          </section>

          <section class="card schedule__step">
            <h2 class="card__title">${msg.ui_sessions}</h2>

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
                    <%-- Prefer the pending status when the manager has staged a change. --%>
                    <c:set
                        var="pendingStatus"
                        value="${scheduleChanges[slot.id]}"/>

                    <c:set
                        var="effectiveStatus"
                        value="${pendingStatus != null
                            ? pendingStatus
                            : slot.status}"/>

                    <label
                        class="slot-item slot-item--${effectiveStatus.css}
                        <c:if test="${slot.status.css == 'rejected'}">
                          slot-item--disabled
                        </c:if>"
                        data-manage-slot
                        data-slot-id="${slot.id}"
                        data-original-status="${slot.status.code}"
                        data-effective-status="${effectiveStatus.code}">

                      <c:choose>
                        <c:when test="${slot.status.css != 'rejected'}">
                          <input
                              type="checkbox"
                              class="slot-item__checkbox"
                              data-manage-slot-checkbox
                              <c:if test="${effectiveStatus.code == '1'}">
                                checked
                              </c:if>>
                        </c:when>

                        <%-- Occupied sessions cannot be edited. --%>
                        <c:otherwise>
                          <input
                              type="checkbox"
                              class="slot-item__checkbox"
                              disabled>
                        </c:otherwise>
                      </c:choose>

                      <span class="slot-item__time">
                        ${slot.range}
                      </span>
                      <t:badge status="${effectiveStatus}"/>
                    </label>
                  </c:forEach>
                </div>
              </c:otherwise>
            </c:choose>

            <div class="schedule__actions">
              <button
                  type="submit"
                  class="btn btn--primary"
                  <c:if test="${empty scheduleChanges}">
                    disabled
                  </c:if>>
                ${msg.ui_save}
              </button>
            </div>
          </section>
        </div>
      </form>

      <script>
        (function () {
          var form =
              document.querySelector('[data-manage-schedule-form]');

          if (!form) {
            return;
          }

          var csrfToken =
              form.querySelector('[name="_csrf"]').value;

          form.querySelectorAll('[data-manage-slot-checkbox]')
              .forEach(function (checkbox) {
                checkbox.addEventListener('change', function () {
                  var label = checkbox.closest('[data-manage-slot]');
                  var targetStatus = checkbox.checked ? '1' : '3';
                  var data = new URLSearchParams();

                  data.append('_csrf', csrfToken);
                  data.append(
                      'facility',
                      form.querySelector('[name="facility"]').value);
                  data.append(
                      'slotId',
                      label.getAttribute('data-slot-id'));
                  data.append(
                      'originalStatus',
                      label.getAttribute('data-original-status'));
                  data.append('targetStatus', targetStatus);

                  // Stage the change in the session so it survives calendar navigation.
                  fetch(form.getAttribute('data-stage-url'), {
                    method: 'POST',
                    headers: {
                      'Content-Type':
                          'application/x-www-form-urlencoded;charset=UTF-8'
                    },
                    body: data.toString()
                  }).then(function (response) {
                    if (!response.ok) {
                      throw new Error('Could not stage schedule change.');
                    }

                    window.location.reload();
                  }).catch(function () {
                    window.alert(
                        'The schedule change could not be saved. '
                        + 'The page will be refreshed.');
                    window.location.reload();
                  });
                });
              });
        })();
      </script>
    </c:otherwise>
  </c:choose>
</t:layout>