package com.guilhermesemog.unimove.dto.trip;

import jakarta.validation.constraints.NotNull;

public record TripAssignmentUpdate(
        @NotNull(message = "Conductor ID is required") Long conductorId,
        @NotNull(message = "Vehicle ID is required") Long vehicleId
) {
}
