<%@ tag description="Monthly calendar grid for schedule display" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="grid" required="true" type="app.fasorhub.model.MonthGrid" description="MonthGrid with cells" %>
<%@ attribute name="facilityId" required="true" type="java.lang.Long" description="Current facility ID" %>
<%@ attribute name="selectedDate" required="false" type="java.time.LocalDate" description="Currently selected date" %>
<%@ attribute name="baseUrl" required="true" description="Base URL for navigation links" %>
<c:set var="today" value="<%=app.fasorhub.util.Clock.today()%>"/>

<div class="calendar">
  <div class="calendar__header">
    <span class="calendar__title">${grid.title}</span>
    <div class="calendar__nav">
      <c:choose>
        <c:when test="${grid.hasPrevious}">
          <a href="${baseUrl}?facility=${facilityId}&amp;month=${grid.prevParam}" class="calendar__nav-btn" aria-label="Previous month">
            <span class="material-symbols-outlined">chevron_left</span>
          </a>
        </c:when>
        <c:otherwise>
          <span class="calendar__nav-btn calendar__nav-btn--disabled" aria-disabled="true">
            <span class="material-symbols-outlined">chevron_left</span>
          </span>
        </c:otherwise>
      </c:choose>
      <c:if test="${grid.hasNext}">
        <a href="${baseUrl}?facility=${facilityId}&amp;month=${grid.nextParam}" class="calendar__nav-btn" aria-label="Next month">
          <span class="material-symbols-outlined">chevron_right</span>
        </a>
      </c:if>
    </div>
  </div>

  <div class="calendar__grid">
    <div class="calendar__day-header">Mon</div>
    <div class="calendar__day-header">Tue</div>
    <div class="calendar__day-header">Wed</div>
    <div class="calendar__day-header">Thu</div>
    <div class="calendar__day-header">Fri</div>
    <div class="calendar__day-header">Sat</div>
    <div class="calendar__day-header">Sun</div>

    <c:forEach var="cell" items="${grid.cells}">
      <c:choose>
        <c:when test="${cell.padding}">
          <div class="calendar__cell calendar__cell--padding"></div>
        </c:when>
        <c:when test="${cell.selectable}">
          <c:set var="isSelected" value="${selectedDate != null && selectedDate == cell.date}"/>
          <a href="${baseUrl}?facility=${facilityId}&amp;month=${grid.param}&amp;date=${cell.date}"
             class="calendar__cell calendar__cell--${cell.css}<c:if test="${isSelected}"> calendar__cell--selected</c:if><c:if test="${cell.today}"> calendar__cell--today</c:if>"
             <c:if test="${isSelected}">aria-pressed="true"</c:if>>
            <span class="calendar__date">${cell.day}</span>
            <c:if test="${not empty cell.status.icon}">
              <span class="calendar__status">
                <span class="material-symbols-outlined calendar__status-icon">${cell.status.icon}</span>
                <span class="calendar__status-label">${cell.status.label}</span>
              </span>
            </c:if>
          </a>
        </c:when>
        <c:otherwise>
          <div class="calendar__cell calendar__cell--${cell.css}<c:if test="${cell.today}"> calendar__cell--today</c:if>">
            <span class="calendar__date">${cell.day}</span>
          </div>
        </c:otherwise>
      </c:choose>
    </c:forEach>
  </div>

  <div class="calendar__legend">
    <div class="calendar__legend-item">
      <span class="calendar__legend-color calendar__legend-color--available"></span>
      <span>Available</span>
    </div>
    <div class="calendar__legend-item">
      <span class="calendar__legend-color calendar__legend-color--partial"></span>
      <span>Partial</span>
    </div>
    <div class="calendar__legend-item">
      <span class="calendar__legend-color calendar__legend-color--full"></span>
      <span>Full</span>
    </div>
    <div class="calendar__legend-item">
      <span class="calendar__legend-color calendar__legend-color--closed"></span>
      <span>Closed</span>
    </div>
    <div class="calendar__legend-item">
      <span class="calendar__legend-color calendar__legend-color--past"></span>
      <span>Past / No session</span>
    </div>
  </div>
</div>
