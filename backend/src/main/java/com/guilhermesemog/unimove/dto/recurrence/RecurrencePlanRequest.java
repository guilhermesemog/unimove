package com.guilhermesemog.unimove.dto.recurrence;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record RecurrencePlanRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull UUID destinationId,
        @NotEmpty Set<DayOfWeek> daysOfWeek,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull LocalTime closingTime,
        @NotNull LocalTime departureTime,
        @NotNull LocalTime arrivalTime,
        @NotNull LocalTime returnDepartureTime,
        @NotNull LocalTime returnArrivalTime,
        @Min(1) @Max(52) Integer horizonWeeks
) {
}
