package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.analytics.*;
import com.guilhermesemog.unimove.repository.OperationalAnalyticsRepository;
import com.guilhermesemog.unimove.repository.OperationalAnalyticsRepository.MetricSnapshot;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.temporal.ChronoUnit;

@Service
public class OperationalAnalyticsService {
    private final OperationalAnalyticsRepository repository;
    private final Clock businessClock;

    public OperationalAnalyticsService(OperationalAnalyticsRepository repository, Clock businessClock) {
        this.repository = repository;
        this.businessClock = businessClock;
    }

    @Transactional(readOnly = true)
    public OperationalAnalyticsResponse get(Integer days, LocalDate requestedFrom, LocalDate requestedTo) {
        DateRange range = resolveRange(days, requestedFrom, requestedTo);
        DateRange previous = range.previous();
        MetricSnapshot current = repository.snapshot(range.fromInstant(), range.toInstant(), range.from(),
                range.to().plusDays(1), range.today());
        MetricSnapshot prior = repository.snapshot(previous.fromInstant(), previous.toInstant(), previous.from(),
                previous.to().plusDays(1), range.today());
        Instant completeSince = repository.metricsCompleteSince();

        AnalyticsSummaryResponse summary = new AnalyticsSummaryResponse(
                metric(current.confirmedBookings(), prior.confirmedBookings(), "bookings",
                        "Active bookings created during the selected period."),
                metric(percent(current.cancelledBookings(), current.createdBookings()),
                        percent(prior.cancelledBookings(), prior.createdBookings()), "percent",
                        "Cancelled bookings divided by all bookings created during the period."),
                metric(percent(current.passengers(), current.seats()), percent(prior.passengers(), prior.seats()),
                        "percent", "Active passengers divided by seats assigned to trips created during the period."),
                metric(current.planningHours(), prior.planningHours(), "hours",
                        "Average time between demand creation and trip creation."),
                metric(current.assignmentHours(), prior.assignmentHours(), "hours",
                        "Average time between trip creation and its complete assignment."),
                metric(current.tripsAtRisk(), prior.tripsAtRisk(), "trips",
                        "Upcoming trips in the selected operational dates missing a driver or vehicle."));

        RecurrenceAnalyticsResponse recurrence = new RecurrenceAnalyticsResponse(
                repository.activeRecurrencePlans(),
                metric(current.generatedOccurrences(), prior.generatedOccurrences(), "demands",
                        "Recurring demands generated during the selected period."),
                metric(percent(current.recurringDemands(), current.demands()),
                        percent(prior.recurringDemands(), prior.demands()), "percent",
                        "Future demands published during the period that originated from a recurring plan."),
                metric(current.conflictsAvoided(), prior.conflictsAvoided(), "conflicts",
                        "Conflicting dates detected and left unchanged during recurring generation."));

        return new OperationalAnalyticsResponse(
                new AnalyticsPeriodResponse(range.from(), range.to(), businessClock.getZone().getId(),
                        businessClock.instant(), completeSince != null && !range.fromInstant().isBefore(completeSince),
                        completeSince),
                summary,
                repository.trend(range.fromInstant(), range.toInstant(), range.from(), range.to(),
                        businessClock.getZone().getId()),
                repository.universities(range.fromInstant(), range.toInstant()),
                recurrence,
                repository.exceptions(range.from(), range.to().plusDays(1), range.today())
        );
    }

    private DateRange resolveRange(Integer days, LocalDate requestedFrom, LocalDate requestedTo) {
        LocalDate today = LocalDate.now(businessClock);
        LocalDate from;
        LocalDate to;
        if (requestedFrom != null || requestedTo != null) {
            if (requestedFrom == null || requestedTo == null) badRequest("Custom periods require both from and to");
            from = requestedFrom;
            to = requestedTo;
        } else {
            int selectedDays = days == null ? 30 : days;
            if (selectedDays != 7 && selectedDays != 30 && selectedDays != 90)
                badRequest("Preset days must be 7, 30, or 90");
            to = today;
            from = today.minusDays(selectedDays - 1L);
        }
        if (from.isAfter(to)) badRequest("Analytics start must not be after end");
        if (ChronoUnit.DAYS.between(from, to) >= 366) badRequest("Analytics periods cannot exceed 366 days");
        ZoneId zone = businessClock.getZone();
        return new DateRange(from, to, today, from.atStartOfDay(zone).toInstant(),
                to.plusDays(1).atStartOfDay(zone).toInstant(), zone);
    }

    private void badRequest(String message) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private AnalyticsMetricResponse metric(Number current, Number previous, String unit, String definition) {
        Double value = current == null ? null : round(current.doubleValue());
        Double prior = previous == null ? null : round(previous.doubleValue());
        Double change = value == null || prior == null || prior == 0
                ? null : round(((value - prior) / Math.abs(prior)) * 100.0);
        return new AnalyticsMetricResponse(value, prior, change, unit, definition);
    }

    private Double percent(long numerator, long denominator) {
        return denominator == 0 ? null : numerator * 100.0 / denominator;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record DateRange(LocalDate from, LocalDate to, LocalDate today, Instant fromInstant,
                             Instant toInstant, ZoneId zone) {
        DateRange previous() {
            long length = ChronoUnit.DAYS.between(from, to) + 1;
            LocalDate previousTo = from.minusDays(1);
            LocalDate previousFrom = previousTo.minusDays(length - 1);
            return new DateRange(previousFrom, previousTo, today,
                    previousFrom.atStartOfDay(zone).toInstant(),
                    previousTo.plusDays(1).atStartOfDay(zone).toInstant(), zone);
        }
    }
}
