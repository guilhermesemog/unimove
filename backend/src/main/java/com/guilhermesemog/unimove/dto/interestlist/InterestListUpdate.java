package com.guilhermesemog.unimove.dto.interestlist;

import java.util.UUID;
import com.guilhermesemog.unimove.model.enums.ListStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record InterestListUpdate(
        @NotNull(message = "Reference date is required") LocalDate referenceDate,
        @NotNull(message = "Closing time is required") LocalTime closingTime,
        @NotNull(message = "Departure time is required") LocalTime departureTime,
        @NotNull(message = "Arrival time is required") LocalTime arrivalTime,
        @NotNull(message = "Return departure time is required") LocalTime returnDepartureTime,
        @NotNull(message = "Return arrival time is required") LocalTime returnArrivalTime,
        @NotNull(message = "Destination ID is required") UUID destinationId,
        @NotNull(message = "List status is required") ListStatus listStatus
) {
}
