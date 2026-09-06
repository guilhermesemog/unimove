package com.guilhermesemog.unimove.dto.analytics;

import java.time.LocalDate;

public record AnalyticsTrendPointResponse(LocalDate date, long bookingsCreated, long bookingsCancelled, long tripsCreated) {
}
