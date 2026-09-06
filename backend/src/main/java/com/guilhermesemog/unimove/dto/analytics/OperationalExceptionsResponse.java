package com.guilhermesemog.unimove.dto.analytics;

public record OperationalExceptionsResponse(long missingDriver, long missingVehicle, long insufficientCapacity) {
}
