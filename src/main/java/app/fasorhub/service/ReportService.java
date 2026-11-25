package app.fasorhub.service;

import app.fasorhub.dao.BookingDao;
import app.fasorhub.dao.FacilityDao;
import app.fasorhub.exception.ValidationException;
import app.fasorhub.model.BookingStatus;
import app.fasorhub.model.Facility;
import app.fasorhub.util.Tx;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ReportService {
    private ReportService() {
    }

    public record StatusCount(BookingStatus status, long count) {
        public BookingStatus getStatus() {
            return status;
        }

        public long getCount() {
            return count;
        }
    }

    public record Report(
            List<BookingDao.ReportBooking> details,
            List<StatusCount> statusCounts,
            List<BookingDao.FacilityUsage> facilityUsage) {
        public List<BookingDao.ReportBooking> getDetails() {
            return details;
        }

        public List<StatusCount> getStatusCounts() {
            return statusCounts;
        }

        public List<BookingDao.FacilityUsage> getFacilityUsage() {
            return facilityUsage;
        }
    }

    public static List<Facility> facilities() {
        return Tx.run(FacilityDao::findAll);
    }

    // Builds status totals, booking details, and approved-only facility utilization.
    public static Report generate(LocalDate startDate, LocalDate endDate, Long facilityId)
            throws ValidationException {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new ValidationException("p19");
        }

        List<BookingDao.ReportBooking> details = Tx.run(
                c -> BookingDao.findReportBookings(c, startDate, endDate, facilityId));

        if (details.isEmpty()) {
            throw new ValidationException("p20");
        }

        List<BookingDao.FacilityUsage> usage = Tx.run(
                c -> BookingDao.findFacilityUsage(c, startDate, endDate, facilityId));

        Map<BookingStatus, Long> counts = new EnumMap<>(BookingStatus.class);
        for (BookingStatus status : BookingStatus.values()) {
            counts.put(status, 0L);
        }
        for (BookingDao.ReportBooking detail : details) {
            counts.merge(detail.booking().status(), 1L, Long::sum);
        }

        List<StatusCount> statusCounts = new ArrayList<>();
        for (BookingStatus status : BookingStatus.values()) {
            statusCounts.add(new StatusCount(status, counts.get(status)));
        }

        return new Report(List.copyOf(details), List.copyOf(statusCounts), List.copyOf(usage));
    }
}