<form method="get" action="${base}" class="filter-form">
    <div class="form-group">
        <label class="form-label" for="status">${msg.ui_status}</label>
        <select id="status" name="status" class="form-input">
            <option value="">${msg.ui_all_statuses}</option>
            <option value="1" <c:if test="${statusFilter == '1'}">selected</c:if>>${msg.booking_status_1}</option>
            <option value="2" <c:if test="${statusFilter == '2'}">selected</c:if>>${msg.booking_status_2}</option>
            <option value="3" <c:if test="${statusFilter == '3'}">selected</c:if>>${msg.booking_status_3}</option>
            <option value="4" <c:if test="${statusFilter == '4'}">selected</c:if>>${msg.booking_status_4}</option>
            <option value="5" <c:if test="${statusFilter == '5'}">selected</c:if>>${msg.booking_status_5}</option>
        </select>
    </div>

    <c:if test="${isManager}">
        <div class="form-group">
            <label class="form-label" for="facility">${msg.ui_facility}</label>
            <select id="facility" name="facility" class="form-input">
                <option value="">${msg.ui_all_facilities}</option>
                <c:forEach var="facility" items="${facilities}">
                    <option value="${facility.id}"
                            <c:if test="${facilityFilter == facility.id}">selected</c:if>>
                        <c:out value="${facility.name}"/>
                    </option>
                </c:forEach>
            </select>
        </div>
    </c:if>

    <div class="form-group">
        <label class="form-label" for="startDate">${msg.ui_start_date}</label>
        <input id="startDate" type="date" name="startDate"
               value="<c:out value='${startDateFilter}'/>"
               class="form-input">
    </div>

    <div class="form-group">
        <label class="form-label" for="endDate">${msg.ui_end_date}</label>
        <input id="endDate" type="date" name="endDate"
               value="<c:out value='${endDateFilter}'/>"
               class="form-input">
    </div>

    <button type="submit" class="btn btn--primary">${msg.ui_apply}</button>
    <a href="${base}" class="btn btn--secondary">${msg.ui_reset}</a>
</form>

<c:if test="${not empty dateError}">
    <p class="field-error" role="alert"><c:out value="${dateError}"/></p>
</c:if>

<c:if test="${filterActive and noFilteredResults}">
    <t:empty message="${msg.p33}" icon="filter_alt_off"/>
</c:if>