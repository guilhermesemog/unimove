package com.guilhermesemog.unimove.dto.interestlist;

import com.guilhermesemog.unimove.model.enums.ListStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record InterestListPatch(
        LocalDate referenceDate,
        LocalTime closingTime,
        LocalTime departureTime,
        LocalTime arrivalTime,
        LocalTime returnDepartureTime,
        LocalTime returnArrivalTime,
        Long destinationId,
        ListStatus listStatus
) {
}
