package com.guilhermesemog.unimove.dto.recurrence;

import com.guilhermesemog.unimove.dto.university.UniversityResponse;
import com.guilhermesemog.unimove.model.enums.RecurrencePlanStatus;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record RecurrencePlanResponse(
        UUID id,
        String name,
        UniversityResponse destination,
        Set<DayOfWeek> daysOfWeek,
        LocalDate startDate,
        LocalDate endDate,
        LocalTime closingTime,
        LocalTime departureTime,
        LocalTime arrivalTime,
        LocalTime returnDepartureTime,
        LocalTime returnArrivalTime,
        Integer horizonWeeks,
        RecurrencePlanStatus status,
        LocalDate nextEligibleDate,
        UUID createdById,
        long futureOccurrences,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
