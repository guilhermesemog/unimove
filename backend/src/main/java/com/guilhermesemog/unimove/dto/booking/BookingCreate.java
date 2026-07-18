package com.guilhermesemog.unimove.dto.booking;

import com.guilhermesemog.unimove.model.enums.TripType;
import jakarta.validation.constraints.NotNull;

public record BookingCreate(
        @NotNull(message = "Interest list ID is required") Long interestListId,
        @NotNull(message = "Trip type is required") TripType tripType,
        Long universityId,
        Long boardingStopId
) {
}
