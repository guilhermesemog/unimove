package com.guilhermesemog.unimove.dto.trip;

import java.util.UUID;
public record TripPatch(
        UUID conductorId,
        UUID vehicleId
) {
}
