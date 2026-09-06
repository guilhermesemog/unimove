package com.guilhermesemog.unimove.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TelemetryRateLimiter {
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final int maxEventsPerMinute;
    private final Clock clock;

    public TelemetryRateLimiter(@Value("${unimove.phase-five.telemetry.max-events-per-minute:120}") int maxEventsPerMinute,
                                Clock businessClock) {
        this.maxEventsPerMinute = maxEventsPerMinute;
        this.clock = businessClock;
    }

    public void consume(String authenticationName, int eventCount) {
        Instant minute = clock.instant().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
        Window result = windows.compute(authenticationName, (key, current) ->
                current == null || !current.minute().equals(minute)
                        ? new Window(minute, eventCount)
                        : new Window(minute, current.count() + eventCount));
        if (result.count() > maxEventsPerMinute) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Telemetry rate limit exceeded");
        }
        if (windows.size() > 10_000) windows.entrySet().removeIf(entry -> entry.getValue().minute().isBefore(minute));
    }

    private record Window(Instant minute, int count) {
    }
}
