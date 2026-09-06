package com.guilhermesemog.unimove.dto.analytics;

public record AnalyticsSummaryResponse(
        AnalyticsMetricResponse confirmedBookings,
        AnalyticsMetricResponse cancellationRate,
        AnalyticsMetricResponse capacityUtilization,
        AnalyticsMetricResponse planningLeadTime,
        AnalyticsMetricResponse assignmentLeadTime,
        AnalyticsMetricResponse tripsAtRisk
) {
}
