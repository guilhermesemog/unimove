package com.guilhermesemog.unimove.dto.booking;

import java.util.UUID;
import com.guilhermesemog.unimove.model.enums.TripType;
import jakarta.validation.constraints.NotNull;

public record BookingCreate(
        @NotNull(message = "Interest list ID is required") UUID interestListId,
        @NotNull(message = "Trip type is required") TripType tripType,
        UUID universityId,
        UUID boardingStopId
) {
}
