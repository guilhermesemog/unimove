package com.guilhermesemog.unimove.dto.boardingstop;

import jakarta.validation.constraints.NotBlank;

public record BoardingStopCreate(
        @NotBlank(message = "Local is required") String local
) {
}
