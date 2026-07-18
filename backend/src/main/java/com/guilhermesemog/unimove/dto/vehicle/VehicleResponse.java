package com.guilhermesemog.unimove.dto.vehicle;

public record VehicleResponse(
        Long id,
        String plate,
        Integer capacity
) {
}
