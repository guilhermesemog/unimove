package com.guilhermesemog.unimove.dto.telemetry;

import com.guilhermesemog.unimove.model.enums.TelemetryEventName;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TelemetryEventRequest(
        @NotNull UUID id,
        @NotNull TelemetryEventName name,
        @NotNull UUID sessionId,
        @NotNull Instant occurredAt,
        @NotNull Map<String, Object> properties
) {
}
