<form method="get" action="${base}" class="filter-form">
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

    <button type="submit" class="btn btn--primary">${msg.ui_apply}</button>
    <a href="${base}" class="btn btn--secondary">${msg.ui_reset}</a>
</form>

<c:if test="${filterActive and noFilteredResults}">
    <t:empty message="${msg.p33}" icon="filter_alt_off"/>
</c:if>