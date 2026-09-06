package com.guilhermesemog.unimove.dto.analytics;

import java.time.Instant;
import java.time.LocalDate;

public record AnalyticsPeriodResponse(
        LocalDate from,
        LocalDate to,
        String timezone,
        Instant generatedAt,
        boolean complete,
        Instant completeSince
) {
}
