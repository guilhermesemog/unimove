package com.guilhermesemog.unimove.dto.student;

import jakarta.validation.constraints.NotNull;

public record StudentPreferredBoardingStopUpdate(
        @NotNull(message = "Boarding stop ID is required") Long boardingStopId
) {
}
