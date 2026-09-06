package com.guilhermesemog.unimove.dto.analytics;

public record AnalyticsMetricResponse(
        Double value,
        Double previousValue,
        Double changePercent,
        String unit,
        String definition
) {
}
