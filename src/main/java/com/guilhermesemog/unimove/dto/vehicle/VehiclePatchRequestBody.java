package com.guilhermesemog.unimove.dto.vehicle;

import jakarta.validation.constraints.Size;

public record VehiclePatchRequestBody(
        @Size(min = 7, max = 7) String plate,
        Integer capacity
) {
}
