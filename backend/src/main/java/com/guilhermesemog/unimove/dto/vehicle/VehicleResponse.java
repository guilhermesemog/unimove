package com.guilhermesemog.unimove.dto.vehicle;

import java.util.UUID;
public record VehicleResponse(
        UUID id,
        String plate,
        Integer capacity
) {
}
