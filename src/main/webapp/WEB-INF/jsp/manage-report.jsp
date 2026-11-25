<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout
        title="${msg.ui_nav_reports}"
        subtitle="${msg.ui_report_subtitle}"
        currentPage="reports">

  <c:set var="base" value="${pageContext.request.contextPath}/reports"/>

  <section class="card">
    <h2 class="card__title card__title--divided">${msg.ui_report_filters}</h2>

    <form method="get" action="${base}" class="report-filters">
      <input type="hidden" name="run" value="true">

      <div class="form-group">
        <label for="startDate" class="form-label">${msg.ui_start_date}</label>
        <input id="startDate" name="startDate" type="date" class="form-input"
               value="<c:out value='${startDate}'/>">
      </div>

      <div class="form-group">
        <label for="endDate" class="form-label">${msg.ui_end_date}</label>
        <input id="endDate" name="endDate" type="date" class="form-input"
               value="<c:out value='${endDate}'/>">
      </div>

      <div class="form-group">
        <label for="facility" class="form-label">${msg.ui_facility}</label>
        <select id="facility" name="facility" class="form-input form-select">
          <option value="" <c:if test="${empty selectedFacilityId}">selected</c:if>>${msg.ui_all_facilities}</option>
          <c:forEach var="facility" items="${facilities}">
            <option value="${facility.id}" <c:if test="${selectedFacilityId == facility.id}">selected</c:if>><c:out value="${facility.name}"/></option>
          </c:forEach>
        </select>
      </div>

      <div class="form-group report-filters__submit">
        <button type="submit" class="btn btn--primary">${msg.ui_show_report}</button>
      </div>
    </form>

    <c:if test="${not empty reportMessage}">
      <p class="notice notice--warning" role="alert">
        <span class="material-symbols-outlined" aria-hidden="true">warning</span>
        <c:out value="${reportMessage}"/>
      </p>
    </c:if>
  </section>

  <c:if test="${not empty report}">
    <section class="card">
      <h2 class="card__title">${msg.ui_report_status_summary}</h2>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>${msg.ui_status}</th>
              <th class="text-right">${msg.ui_report_count}</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="item" items="${report.statusCounts}">
              <tr>
                <td><t:badge status="${item.status}"/></td>
                <td class="text-right table__strong"><c:out value="${item.count}"/></td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </section>

    <section class="card">
      <h2 class="card__title">${msg.ui_report_usage}</h2>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>${msg.ui_facility}</th>
              <th class="text-right">${msg.ui_report_approved_bookings}</th>
              <th class="text-right">${msg.ui_report_occupied}</th>
              <th class="text-right">${msg.ui_report_usage_pct}</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="item" items="${report.facilityUsage}">
              <tr>
                <td class="table__strong"><c:out value="${item.facilityName}"/></td>
                <td class="text-right"><c:out value="${item.bookingCount}"/></td>
                <td class="text-right"><c:out value="${item.occupiedSessions}"/></td>
                <td class="text-right"><c:out value="${item.usagePercent}"/>%</td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
      <p class="form-hint">${msg.ui_report_usage_note}</p>
    </section>

    <section class="card">
      <h2 class="card__title">${msg.ui_report_details}</h2>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>${msg.ui_requester}</th>
              <th>${msg.ui_facility}</th>
              <th>${msg.ui_date}</th>
              <th>${msg.ui_status}</th>
              <th class="text-right">${msg.ui_report_sessions_count}</th>
              <th>${msg.ui_purpose}</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="item" items="${report.details}">
              <tr>
                <td class="table__strong table__nowrap"><c:out value="${item.requesterName}"/></td>
                <td><c:out value="${item.facilityName}"/></td>
                <td class="table__nowrap"><c:out value="${item.bookingDate}"/></td>
                <td><t:badge status="${item.booking.status}"/></td>
                <td class="text-right"><c:out value="${item.sessionCount}"/></td>
                <td><c:out value="${item.booking.purpose}"/></td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </section>
  </c:if>
</t:layout>
