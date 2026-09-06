package com.guilhermesemog.unimove.dto.telemetry;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record TelemetrySummaryResponse(
        LocalDate from,
        LocalDate to,
        String timezone,
        Instant generatedAt,
        long bookingStarted,
        long bookingCompleted,
        Double bookingCompletionRate,
        Double medianBookingDurationMs,
        List<TelemetryBreakdownResponse> bookingAbandonments,
        List<TelemetryBreakdownResponse> errorsByScreen,
        long recurrencePreviewed,
        long recurrencePublished,
        long tripsGenerated,
        long assignmentsCompleted,
        long operationsOpened,
        long manifestsViewed,
        long operationLoadFailures,
        Double medianOperationOpenDurationMs
) {
}
