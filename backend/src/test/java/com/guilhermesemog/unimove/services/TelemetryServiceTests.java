package com.guilhermesemog.unimove.services;

import com.guilhermesemog.unimove.dto.telemetry.TelemetryBatchRequest;
import com.guilhermesemog.unimove.dto.telemetry.TelemetryEventRequest;
import com.guilhermesemog.unimove.model.enums.Role;
import com.guilhermesemog.unimove.model.enums.TelemetryEventName;
import com.guilhermesemog.unimove.repository.TelemetryRepository;
import com.guilhermesemog.unimove.service.TelemetryRateLimiter;
import com.guilhermesemog.unimove.service.TelemetryService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class TelemetryServiceTests {
    @Test
    void shouldAcceptNothingWhenCollectionIsDisabled() {
        TelemetryRepository repository = mock(TelemetryRepository.class);
        TelemetryRateLimiter rateLimiter = mock(TelemetryRateLimiter.class);
        Instant now = Instant.parse("2026-09-05T12:00:00Z");
        TelemetryService service = new TelemetryService(repository, rateLimiter, new ObjectMapper(),
                Clock.fixed(now, ZoneOffset.UTC), false);
        TelemetryBatchRequest batch = new TelemetryBatchRequest(List.of(new TelemetryEventRequest(
                UUID.randomUUID(), TelemetryEventName.availability_viewed, UUID.randomUUID(), now,
                Map.of("screen", "availability"))));

        assertThat(service.ingest("student", Role.STUDENT, batch)).isZero();
        verifyNoInteractions(repository, rateLimiter);
    }
}
