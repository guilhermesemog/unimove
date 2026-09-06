package com.guilhermesemog.unimove.dto.analytics;

public record RecurrenceAnalyticsResponse(
        long activePlans,
        AnalyticsMetricResponse generatedOccurrences,
        AnalyticsMetricResponse coverage,
        AnalyticsMetricResponse conflictsAvoided
) {
}
