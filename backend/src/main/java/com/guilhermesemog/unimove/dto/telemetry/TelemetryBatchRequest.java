package com.guilhermesemog.unimove.dto.telemetry;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TelemetryBatchRequest(
        @NotEmpty @Size(max = 20) List<@Valid TelemetryEventRequest> events
) {
}
