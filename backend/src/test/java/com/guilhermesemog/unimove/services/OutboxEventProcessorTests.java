package com.guilhermesemog.unimove.services;

import com.guilhermesemog.unimove.model.OutboxEvent;
import com.guilhermesemog.unimove.model.enums.BusinessEventType;
import com.guilhermesemog.unimove.model.enums.OutboxStatus;
import com.guilhermesemog.unimove.repository.OutboxEventRepository;
import com.guilhermesemog.unimove.service.NotificationDispatchService;
import com.guilhermesemog.unimove.service.OutboxEventProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventProcessorTests {
    @Mock private OutboxEventRepository repository;
    @Mock private NotificationDispatchService dispatchService;

    @Test
    void shouldUseExponentialBackoffAndStopAfterMaximumAttempts() {
        Instant now = Instant.parse("2026-09-05T12:00:00Z");
        OutboxEvent event = event();
        event.markProcessing(now);
        when(repository.findById(event.getId())).thenReturn(Optional.of(event));
        doThrow(new IllegalStateException("temporary failure")).when(dispatchService).dispatch(event);
        OutboxEventProcessor processor = new OutboxEventProcessor(
                repository, dispatchService, Clock.fixed(now, ZoneOffset.UTC), 2, Duration.ofSeconds(30));

        processor.process(event.getId());
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(event.getNextAttemptAt()).isEqualTo(now.plusSeconds(30));

        event.markProcessing(now.plusSeconds(30));
        processor.process(event.getId());
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(event.getAttempts()).isEqualTo(2);
        assertThat(event.getNextAttemptAt()).isNull();
    }

    private OutboxEvent event() {
        return new OutboxEvent(BusinessEventType.BOOKING_CREATED, "Booking", UUID.randomUUID(),
                null, "SYSTEM", UUID.randomUUID(), "{}", "test:" + UUID.randomUUID());
    }
}
