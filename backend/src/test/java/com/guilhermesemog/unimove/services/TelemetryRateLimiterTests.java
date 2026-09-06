package com.guilhermesemog.unimove.services;

import com.guilhermesemog.unimove.service.TelemetryRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelemetryRateLimiterTests {
    @Test
    void shouldRejectEventsAboveTheMinuteLimit() {
        TelemetryRateLimiter limiter = new TelemetryRateLimiter(3,
                Clock.fixed(Instant.parse("2026-09-05T12:00:30Z"), ZoneOffset.UTC));

        limiter.consume("student", 2);

        assertThatThrownBy(() -> limiter.consume("student", 2))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("rate limit");
    }
}
