package com.guilhermesemog.unimove.dto.student;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record StudentPreferredBoardingStopUpdate(
        @NotNull(message = "Boarding stop ID is required") UUID boardingStopId
) {
}
