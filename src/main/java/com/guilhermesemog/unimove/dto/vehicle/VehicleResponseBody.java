package com.guilhermesemog.unimove.dto.vehicle;

public record VehicleResponseBody(
        Long id,
        String plate,
        Integer capacity
) {
}
