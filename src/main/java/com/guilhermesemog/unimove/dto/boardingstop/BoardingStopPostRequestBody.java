package com.guilhermesemog.unimove.dto.boardingstop;

import jakarta.validation.constraints.NotBlank;

public record BoardingStopPostRequestBody(
        @NotBlank(message = "Local is required") String local
) {
}
