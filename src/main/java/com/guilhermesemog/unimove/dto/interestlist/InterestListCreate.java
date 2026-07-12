package com.guilhermesemog.unimove.dto.interestlist;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record InterestListCreate(
        @NotNull(message = "Reference date is required") LocalDate referenceDate,
        @NotNull(message = "Closing time is required") LocalTime closingTime,
        @NotNull(message = "Departure time is required") LocalTime departureTime,
        @NotNull(message = "Arrival time is required") LocalTime arrivalTime,
        @NotNull(message = "Return departure time is required") LocalTime returnDepartureTime,
        @NotNull(message = "Return arrival time is required") LocalTime returnArrivalTime,
        @NotNull(message = "Destination ID is required") Long destinationId
) {
}
