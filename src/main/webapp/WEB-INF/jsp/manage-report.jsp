<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout
        title="${msg.ui_nav_reports}"
        currentPage="reports">

  <c:set var="base" value="${pageContext.request.contextPath}/reports"/>

  <section class="card">
    <h2 class="card__title">${msg.ui_nav_reports}</h2>

    <form method="get" action="${base}" class="form">
      <input type="hidden" name="run" value="true">

      <div class="form__group">
        <label for="startDate">Tanggal awal</label>
        <input id="startDate"
               name="startDate"
               type="date"
               value="<c:out value='${startDate}'/>">
      </div>

      <div class="form__group">
        <label for="endDate">Tanggal akhir</label>
        <input id="endDate"
               name="endDate"
               type="date"
               value="<c:out value='${endDate}'/>">
      </div>

      <div class="form__group">
        <label for="facility">Fasilitas</label>
        <select id="facility" name="facility">
          <option value=""
                  <c:if test="${empty selectedFacilityId}">selected</c:if>>
            Semua Fasilitas
          </option>
          <c:forEach var="facility" items="${facilities}">
            <option value="${facility.id}"
                    <c:if test="${selectedFacilityId == facility.id}">selected</c:if>>
              <c:out value="${facility.name}"/>
            </option>
          </c:forEach>
        </select>
      </div>

      <button type="submit" class="btn btn--primary">Tampilkan Laporan</button>
    </form>

    <c:if test="${not empty reportMessage}">
      <p role="alert"><c:out value="${reportMessage}"/></p>
    </c:if>
  </section>

  <c:if test="${not empty report}">
    <section class="card">
      <h2 class="card__title">Ringkasan Pengajuan per Status</h2>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>Status</th>
              <th>Jumlah Pengajuan</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="item" items="${report.statusCounts}">
              <tr>
                <td><c:out value="${item.status.label}"/></td>
                <td><c:out value="${item.count}"/></td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </section>

    <section class="card">
      <h2 class="card__title">Penggunaan Fasilitas</h2>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>Fasilitas</th>
              <th>Peminjaman Disetujui</th>
              <th>Sesi Terisi</th>
              <th>Penggunaan</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="item" items="${report.facilityUsage}">
              <tr>
                <td><c:out value="${item.facilityName}"/></td>
                <td><c:out value="${item.bookingCount}"/></td>
                <td><c:out value="${item.occupiedSessions}"/></td>
                <td><c:out value="${item.usagePercent}"/>%</td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
      <p>Penggunaan = sesi terisi dibagi sesi yang dapat dipinjam; sesi Ditutup tidak dihitung.</p>
    </section>

    <section class="card">
      <h2 class="card__title">Rincian Pengajuan</h2>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>Peminjam</th>
              <th>Fasilitas</th>
              <th>Tanggal</th>
              <th>Status</th>
              <th>Jumlah Sesi</th>
              <th>Keperluan</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="item" items="${report.details}">
              <tr>
                <td><c:out value="${item.requesterName}"/></td>
                <td><c:out value="${item.facilityName}"/></td>
                <td><c:out value="${item.bookingDate}"/></td>
                <td><c:out value="${item.booking.status.label}"/></td>
                <td><c:out value="${item.sessionCount}"/></td>
                <td><c:out value="${item.booking.purpose}"/></td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </section>
  </c:if>
</t:layout>