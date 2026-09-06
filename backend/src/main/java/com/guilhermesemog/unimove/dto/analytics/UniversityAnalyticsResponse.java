package com.guilhermesemog.unimove.dto.analytics;

import java.util.UUID;

public record UniversityAnalyticsResponse(UUID universityId, String universityName, long bookings, long trips) {
}
