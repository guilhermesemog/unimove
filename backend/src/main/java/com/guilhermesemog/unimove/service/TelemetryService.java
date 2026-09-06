package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.telemetry.*;
import com.guilhermesemog.unimove.model.enums.Role;
import com.guilhermesemog.unimove.model.enums.TelemetryEventName;
import com.guilhermesemog.unimove.repository.TelemetryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.*;
import java.util.*;

@Service
public class TelemetryService {
    private static final Set<String> SCREENS = Set.of("availability", "booking_checkout", "my_trips",
            "recurrence_editor", "recurrence_list", "admin_operation", "operation_schedule", "operation_detail");
    private static final Set<String> STEPS = Set.of("trip", "trip_type", "boarding_stop", "review",
            "schedule", "dates", "times", "preview", "publish", "manifest");
    private static final Set<String> RESULTS = Set.of("success", "failure", "abandoned", "conflict");
    private static final Set<String> ERROR_CATEGORIES = Set.of("validation", "network", "authorization",
            "not_found", "server", "unknown");
    private static final Map<TelemetryEventName, Set<String>> ALLOWED_PROPERTIES = Map.ofEntries(
            entry(TelemetryEventName.availability_viewed, "screen"),
            entry(TelemetryEventName.booking_started, "screen"),
            entry(TelemetryEventName.booking_step_completed, "screen", "step", "durationMs"),
            entry(TelemetryEventName.booking_validation_failed, "screen", "step", "errorCategory"),
            entry(TelemetryEventName.booking_completed, "screen", "durationMs", "result"),
            entry(TelemetryEventName.booking_abandoned, "screen", "step", "durationMs", "result"),
            entry(TelemetryEventName.booking_cancelled, "screen", "result"),
            entry(TelemetryEventName.recurrence_started, "screen"),
            entry(TelemetryEventName.recurrence_previewed, "screen", "durationMs"),
            entry(TelemetryEventName.recurrence_conflict_found, "screen", "result"),
            entry(TelemetryEventName.recurrence_published, "screen", "durationMs", "result"),
            entry(TelemetryEventName.trip_generated, "screen", "result"),
            entry(TelemetryEventName.assignment_completed, "screen", "durationMs", "result"),
            entry(TelemetryEventName.operation_opened, "screen", "durationMs"),
            entry(TelemetryEventName.manifest_viewed, "screen", "step"),
            entry(TelemetryEventName.operation_load_failed, "screen", "errorCategory")
    );
    private static final Map<Role, Set<TelemetryEventName>> EVENTS_BY_ROLE = Map.of(
            Role.STUDENT, EnumSet.range(TelemetryEventName.availability_viewed, TelemetryEventName.booking_cancelled),
            Role.ADMIN, EnumSet.range(TelemetryEventName.recurrence_started, TelemetryEventName.assignment_completed),
            Role.CONDUCTOR, EnumSet.range(TelemetryEventName.operation_opened, TelemetryEventName.operation_load_failed)
    );

    private final TelemetryRepository repository;
    private final TelemetryRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;
    private final Clock businessClock;
    private final boolean enabled;

    public TelemetryService(TelemetryRepository repository, TelemetryRateLimiter rateLimiter, ObjectMapper objectMapper,
                            Clock businessClock,
                            @Value("${unimove.phase-five.telemetry.enabled:false}") boolean enabled) {
        this.repository = repository;
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
        this.businessClock = businessClock;
        this.enabled = enabled;
    }

    @Transactional
    public int ingest(String authenticationName, Role role, TelemetryBatchRequest batch) {
        if (!enabled) return 0;
        rateLimiter.consume(authenticationName, batch.events().size());
        Instant receivedAt = businessClock.instant();
        batch.events().forEach(event -> validate(event, role, receivedAt));
        int accepted = 0;
        for (TelemetryEventRequest event : batch.events()) {
            accepted += repository.insertIfAbsent(event, role, serialize(event.properties()), receivedAt);
        }
        return accepted;
    }

    @Transactional(readOnly = true)
    public TelemetrySummaryResponse summary(int days) {
        if (days != 7 && days != 30 && days != 90)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Telemetry days must be 7, 30, or 90");
        LocalDate toDate = LocalDate.now(businessClock);
        LocalDate fromDate = toDate.minusDays(days - 1L);
        Instant from = fromDate.atStartOfDay(businessClock.getZone()).toInstant();
        Instant to = toDate.plusDays(1).atStartOfDay(businessClock.getZone()).toInstant();
        TelemetryRepository.TelemetryTotals totals = repository.totals(from, to);
        Double completion = totals.bookingStarted() == 0 ? null
                : round(totals.bookingCompleted() * 100.0 / totals.bookingStarted());
        return new TelemetrySummaryResponse(fromDate, toDate, businessClock.getZone().getId(), businessClock.instant(),
                totals.bookingStarted(), totals.bookingCompleted(), completion, totals.medianBookingDurationMs(),
                repository.breakdown(from, to, "booking_abandoned", "step"),
                repository.breakdown(from, to, "booking_validation_failed", "screen"),
                totals.recurrencePreviewed(), totals.recurrencePublished(), totals.tripsGenerated(),
                totals.assignmentsCompleted(), totals.operationsOpened(), totals.manifestsViewed(),
                totals.operationLoadFailures(), totals.medianOperationOpenDurationMs());
    }

    @Transactional
    public int deleteExpired(int retentionDays) {
        if (retentionDays < 1) throw new IllegalArgumentException("Telemetry retention must be positive");
        return repository.deleteReceivedBefore(businessClock.instant().minus(Duration.ofDays(retentionDays)));
    }

    private void validate(TelemetryEventRequest event, Role role, Instant receivedAt) {
        if (!EVENTS_BY_ROLE.getOrDefault(role, Set.of()).contains(event.name()))
            badRequest("Telemetry event is not allowed for the authenticated role");
        if (event.occurredAt().isAfter(receivedAt.plus(Duration.ofMinutes(5)))
                || event.occurredAt().isBefore(receivedAt.minus(Duration.ofDays(7))))
            badRequest("Telemetry event timestamp is outside the accepted window");
        Set<String> allowed = ALLOWED_PROPERTIES.get(event.name());
        if (!allowed.containsAll(event.properties().keySet())) badRequest("Telemetry contains an unsupported property");
        event.properties().forEach(this::validateProperty);
    }

    private void validateProperty(String key, Object value) {
        if (key.equals("durationMs")) {
            if (!(value instanceof Number number) || number.longValue() < 0 || number.longValue() > 3_600_000)
                badRequest("Telemetry durationMs is invalid");
            return;
        }
        if (!(value instanceof String)) badRequest("Telemetry property values must use the documented type");
        String text = (String) value;
        Set<String> allowed = switch (key) {
            case "screen" -> SCREENS;
            case "step" -> STEPS;
            case "result" -> RESULTS;
            case "errorCategory" -> ERROR_CATEGORIES;
            default -> Set.of();
        };
        if (!allowed.contains(text)) badRequest("Telemetry property value is not allowed");
    }

    private String serialize(Map<String, Object> properties) {
        try { return objectMapper.writeValueAsString(properties); }
        catch (JacksonException exception) { throw new IllegalStateException("Could not serialize telemetry", exception); }
    }

    private void badRequest(String message) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private static double round(double value) { return Math.round(value * 100.0) / 100.0; }
    private static Map.Entry<TelemetryEventName, Set<String>> entry(TelemetryEventName name, String... properties) {
        return Map.entry(name, Set.of(properties));
    }
}
