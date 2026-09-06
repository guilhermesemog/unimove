package com.guilhermesemog.unimove.dto.trip;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record TripAssignmentUpdate(
        @NotNull(message = "Conductor ID is required") UUID conductorId,
        @NotNull(message = "Vehicle ID is required") UUID vehicleId
) {
}
