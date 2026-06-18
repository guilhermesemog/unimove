package com.guilhermesemog.unimove.dto.vehicle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehiclePutRequestBody(
        @NotBlank(message = "Plate is required") @Size(min = 7, max = 7) String plate,
        @NotNull(message = "Capacity is required") Integer capacity
) {
}