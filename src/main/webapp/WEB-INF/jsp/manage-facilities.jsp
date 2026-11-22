<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="${msg.ui_nav_facilities}" subtitle="${msg.ui_facilities_subtitle}" currentPage="manage-facilities">
  <c:set var="base" value="${pageContext.request.contextPath}/manage-facilities"/>

  <section class="card">
    <div class="card__header">
      <h2 class="card__title">${msg.ui_facility_list}</h2>
      <div class="card__tools">
        <a class="btn btn--secondary" href="${base}?types">${msg.ui_manage_types}</a>
        <a class="btn btn--primary" href="${base}?add">${msg.ui_add_facility}</a>
      </div>
    </div>

    <c:choose>
      <c:when test="${empty facilities}">
        <t:empty message="${msg.ui_no_facilities}" icon="sports_soccer"/>
      </c:when>
      <c:otherwise>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>${msg.ui_facility_name}</th>
                <th>${msg.ui_facility_type}</th>
                <th>${msg.ui_facility_description}</th>
                <th><span class="sr-only">${msg.ui_actions}</span></th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="row" items="${facilities}">
                <c:set var="f" value="${row.facility}"/>
                <tr id="facility-${f.id}">
                  <td>
                    <span class="table__strong"><c:out value="${f.name}"/></span>
                    <span class="row-meta">${msg.ui_meta_sessions}: ${row.sessions}, ${msg.ui_meta_bookings}: ${row.bookings}</span>
                  </td>
                  <td><c:out value="${f.type.name}"/></td>
                  <td class="table__truncate"><c:out value="${f.description}"/></td>
                  <td class="text-right">
                    <div class="row-actions">
                      <a class="btn btn--secondary" href="${base}?edit=${f.id}" aria-label="${msg.ui_edit} ${f.name}">${msg.ui_edit}</a>
                      <c:choose>
                        <c:when test="${row.deletable}">
                          <a class="btn btn--danger" href="${base}?delete=${f.id}" aria-label="${msg.ui_delete} ${f.name}">${msg.ui_delete}</a>
                        </c:when>
                        <c:otherwise>
                          <button type="button" class="btn btn--danger" disabled aria-label="${msg.ui_delete} ${f.name}">${msg.ui_delete}</button>
                        </c:otherwise>
                      </c:choose>
                    </div>
                    <c:if test="${not row.deletable}"><span class="row-meta">${msg.ui_has_bookings}</span></c:if>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>

        <div class="card-list">
          <c:forEach var="row" items="${facilities}">
            <c:set var="f" value="${row.facility}"/>
            <div class="card-list__item">
              <div class="card-list__header">
                <span class="card-list__title"><c:out value="${f.name}"/></span>
                <span class="card-list__meta"><c:out value="${f.type.name}"/></span>
              </div>
              <c:if test="${not empty f.description}"><div class="card-list__meta"><c:out value="${f.description}"/></div></c:if>
              <div class="card-list__meta">${msg.ui_meta_sessions}: ${row.sessions}, ${msg.ui_meta_bookings}: ${row.bookings}</div>
              <div class="card-list__actions">
                <a class="btn btn--secondary" href="${base}?edit=${f.id}" aria-label="${msg.ui_edit} ${f.name}">${msg.ui_edit}</a>
                <c:choose>
                  <c:when test="${row.deletable}">
                    <a class="btn btn--danger" href="${base}?delete=${f.id}" aria-label="${msg.ui_delete} ${f.name}">${msg.ui_delete}</a>
                  </c:when>
                  <c:otherwise>
                    <button type="button" class="btn btn--danger" disabled aria-label="${msg.ui_delete} ${f.name}">${msg.ui_delete}</button>
                  </c:otherwise>
                </c:choose>
              </div>
              <c:if test="${not row.deletable}"><div class="card-list__meta">${msg.ui_has_bookings}</div></c:if>
            </div>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
  </section>

  <c:if test="${dialog == 'form'}">
    <div id="facility-dialog" class="dialog-backdrop is-open" aria-hidden="false">
      <div class="dialog" role="dialog" aria-modal="true" aria-labelledby="form-title">
        <form method="post" action="${base}/save">
          <input type="hidden" name="_csrf" value="${sessionScope._csrf}">
          <c:if test="${not empty formId}"><input type="hidden" name="id" value="${formId}"></c:if>

          <div class="dialog__header">
            <h2 id="form-title" class="dialog__title">
              <c:choose>
                <c:when test="${not empty formId}">${msg.ui_edit_facility}</c:when>
                <c:otherwise>${msg.ui_add_facility}</c:otherwise>
              </c:choose>
            </h2>
            <a class="dialog__close" href="${base}" aria-label="${msg.ui_close}">
              <span class="material-symbols-outlined" aria-hidden="true">close</span>
            </a>
          </div>

          <div class="dialog__body">
            <div class="form-group">
              <label class="form-label" for="name">${msg.ui_facility_name} <span class="form-hint">${msg.ui_helper_name}</span></label>
              <input type="text" id="name" name="name" class="form-input" maxlength="50" required autofocus
                     value="<c:out value="${formName}"/>"
                     <c:if test="${errorKey == 'p38'}">aria-invalid="true" aria-describedby="name-error"</c:if>>
              <c:if test="${errorKey == 'p38'}"><p id="name-error" class="field-error" role="alert">${msg.p38}</p></c:if>
            </div>

            <div class="form-group">
              <label class="form-label" for="type">${msg.ui_facility_type}</label>
              <select id="type" name="type" class="form-input" required <c:if test="${empty types}">disabled</c:if>
                      <c:if test="${errorKey == 'p39'}">aria-invalid="true" aria-describedby="type-error"</c:if>>
                <option value="">${msg.ui_select_type}</option>
                <c:forEach var="row" items="${types}">
                  <option value="${row.type.id}" <c:if test="${formType == row.type.id}">selected</c:if>><c:out value="${row.type.name}"/></option>
                </c:forEach>
              </select>
              <c:if test="${empty types}"><p class="form-hint">${msg.ui_no_types}</p></c:if>
              <c:if test="${errorKey == 'p39'}"><p id="type-error" class="field-error" role="alert">${msg.p39}</p></c:if>
            </div>

            <div class="form-group">
              <label class="form-label" for="description">${msg.ui_facility_description} <span class="form-hint">${msg.ui_helper_description}</span></label>
              <textarea id="description" name="description" class="form-input" rows="3" maxlength="200"
                        <c:if test="${errorKey == 'p40'}">aria-invalid="true" aria-describedby="description-error"</c:if>><c:out value="${formDescription}"/></textarea>
              <c:if test="${errorKey == 'p40'}"><p id="description-error" class="field-error" role="alert">${msg.p40}</p></c:if>
            </div>

            <c:if test="${empty formId}"><p class="form-hint">${msg.ui_helper_new}</p></c:if>
          </div>

          <div class="dialog__footer">
            <a class="btn btn--secondary" href="${base}">${msg.ui_cancel}</a>
            <button type="submit" class="btn btn--primary" <c:if test="${empty types}">disabled</c:if>>${msg.ui_save}</button>
          </div>
        </form>
      </div>
    </div>
  </c:if>

  <c:if test="${dialog == 'delete' and not empty toDelete}">
    <div id="delete-dialog" class="dialog-backdrop is-open" aria-hidden="false">
      <div class="dialog" role="dialog" aria-modal="true" aria-labelledby="delete-title">
        <form method="post" action="${base}/delete">
          <input type="hidden" name="_csrf" value="${sessionScope._csrf}">
          <input type="hidden" name="id" value="${toDelete.facility.id}">

          <div class="dialog__header">
            <h2 id="delete-title" class="dialog__title">${msg.ui_delete_facility}</h2>
            <a class="dialog__close" href="${base}" aria-label="${msg.ui_close}">
              <span class="material-symbols-outlined" aria-hidden="true">close</span>
            </a>
          </div>

          <div class="dialog__body">
            <p><strong><c:out value="${toDelete.facility.name}"/></strong></p>
            <p>${msg.ui_delete_question}</p>
          </div>

          <div class="dialog__footer">
            <a class="btn btn--secondary" href="${base}" autofocus>${msg.ui_cancel}</a>
            <button type="submit" class="btn btn--danger">${msg.ui_delete}</button>
          </div>
        </form>
      </div>
    </div>
  </c:if>

  <c:if test="${dialog == 'types'}">
    <div id="types-dialog" class="dialog-backdrop is-open" aria-hidden="false">
      <div class="dialog" role="dialog" aria-modal="true" aria-labelledby="types-title">
        <div class="dialog__header">
          <h2 id="types-title" class="dialog__title">${msg.ui_manage_types}</h2>
          <a class="dialog__close" href="${base}" aria-label="${msg.ui_close}">
            <span class="material-symbols-outlined" aria-hidden="true">close</span>
          </a>
        </div>

        <div class="dialog__body">
          <c:choose>
            <c:when test="${empty types}">
              <p class="form-hint">${msg.ui_no_types}</p>
            </c:when>
            <c:otherwise>
              <ul class="type-list">
                <c:forEach var="row" items="${types}">
                  <li class="type-list__item">
                    <div class="type-list__text">
                      <span class="type-list__name"><c:out value="${row.type.name}"/></span>
                      <span class="row-meta">
                        <c:choose>
                          <c:when test="${row.used}">${msg.ui_used_by} ${row.facilities}</c:when>
                          <c:otherwise>${msg.ui_not_used}</c:otherwise>
                        </c:choose>
                      </span>
                    </div>
                    <div class="row-actions">
                      <a class="btn btn--secondary" href="${base}?editType=${row.type.id}" aria-label="${msg.ui_rename} ${row.type.name}">${msg.ui_rename}</a>
                      <c:choose>
                        <c:when test="${row.used}">
                          <button type="button" class="btn btn--danger" disabled aria-label="${msg.ui_remove} ${row.type.name}">${msg.ui_remove}</button>
                        </c:when>
                        <c:otherwise>
                          <form method="post" action="${base}/types/delete">
                            <input type="hidden" name="_csrf" value="${sessionScope._csrf}">
                            <input type="hidden" name="id" value="${row.type.id}">
                            <button type="submit" class="btn btn--danger" aria-label="${msg.ui_remove} ${row.type.name}">${msg.ui_remove}</button>
                          </form>
                        </c:otherwise>
                      </c:choose>
                    </div>
                  </li>
                </c:forEach>
              </ul>
            </c:otherwise>
          </c:choose>

          <form method="post" action="${base}/types/save" class="type-form">
            <input type="hidden" name="_csrf" value="${sessionScope._csrf}">
            <c:if test="${not empty typeId}"><input type="hidden" name="id" value="${typeId}"></c:if>

            <div class="form-group">
              <label class="form-label" for="type-name">${msg.ui_type_name} <span class="form-hint">${msg.ui_helper_type_name}</span></label>
              <input type="text" id="type-name" name="name" class="form-input" maxlength="30" required
                     <c:if test="${empty typeId}">autofocus</c:if>
                     value="<c:out value="${typeName}"/>"
                     <c:if test="${not empty typeErrorKey}">aria-invalid="true" aria-describedby="type-name-error"</c:if>>
              <c:if test="${not empty typeErrorKey}"><p id="type-name-error" class="field-error" role="alert"><c:out value="${msg[typeErrorKey]}"/></p></c:if>
            </div>

            <div class="actions">
              <c:if test="${not empty typeId}"><a class="btn btn--secondary" href="${base}?types">${msg.ui_cancel}</a></c:if>
              <button type="submit" class="btn btn--primary">
                <c:choose>
                  <c:when test="${not empty typeId}">${msg.ui_save}</c:when>
                  <c:otherwise>${msg.ui_add_type}</c:otherwise>
                </c:choose>
              </button>
            </div>
          </form>
        </div>

        <div class="dialog__footer">
          <a class="btn btn--secondary" href="${base}">${msg.ui_close}</a>
        </div>
      </div>
    </div>
  </c:if>
</t:layout>
