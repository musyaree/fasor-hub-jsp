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
          <a href="${baseUrl}?facility=${facilityId}&amp;month=${grid.prevParam}" class="calendar__nav-btn" aria-label="${msg.ui_prev_month}">
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
        <a href="${baseUrl}?facility=${facilityId}&amp;month=${grid.nextParam}" class="calendar__nav-btn" aria-label="${msg.ui_next_month}">
          <span class="material-symbols-outlined">chevron_right</span>
        </a>
      </c:if>
    </div>
  </div>

  <div class="calendar__grid">
    <div class="calendar__day-header">${msg.ui_day_mon}</div>
    <div class="calendar__day-header">${msg.ui_day_tue}</div>
    <div class="calendar__day-header">${msg.ui_day_wed}</div>
    <div class="calendar__day-header">${msg.ui_day_thu}</div>
    <div class="calendar__day-header">${msg.ui_day_fri}</div>
    <div class="calendar__day-header">${msg.ui_day_sat}</div>
    <div class="calendar__day-header">${msg.ui_day_sun}</div>

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
      <span>${msg.ui_legend_available}</span>
    </div>
    <div class="calendar__legend-item">
      <span class="calendar__legend-color calendar__legend-color--partial"></span>
      <span>${msg.ui_legend_partial}</span>
    </div>
    <div class="calendar__legend-item">
      <span class="calendar__legend-color calendar__legend-color--full"></span>
      <span>${msg.ui_legend_full}</span>
    </div>
    <div class="calendar__legend-item">
      <span class="calendar__legend-color calendar__legend-color--closed"></span>
      <span>${msg.ui_legend_closed}</span>
    </div>
    <div class="calendar__legend-item">
      <span class="calendar__legend-color calendar__legend-color--past"></span>
      <span>${msg.ui_legend_past}</span>
    </div>
  </div>
</div>
