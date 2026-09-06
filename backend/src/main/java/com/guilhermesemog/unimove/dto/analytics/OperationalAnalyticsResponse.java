package com.guilhermesemog.unimove.dto.analytics;

import java.util.List;

public record OperationalAnalyticsResponse(
        AnalyticsPeriodResponse period,
        AnalyticsSummaryResponse summary,
        List<AnalyticsTrendPointResponse> trend,
        List<UniversityAnalyticsResponse> universities,
        RecurrenceAnalyticsResponse recurrence,
        OperationalExceptionsResponse exceptions
) {
}
