package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.model.OutboxEvent;
import com.guilhermesemog.unimove.model.enums.OutboxStatus;
import com.guilhermesemog.unimove.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class OutboxEventProcessor {
    private final OutboxEventRepository repository;
    private final NotificationDispatchService dispatchService;
    private final Clock businessClock;
    private final int maxAttempts;
    private final Duration initialBackoff;

    public OutboxEventProcessor(OutboxEventRepository repository, NotificationDispatchService dispatchService,
                                Clock businessClock,
                                @Value("${unimove.phase-five.notifications.max-attempts:5}") int maxAttempts,
                                @Value("${unimove.phase-five.notifications.initial-backoff:PT30S}") Duration initialBackoff) {
        this.repository = repository;
        this.dispatchService = dispatchService;
        this.businessClock = businessClock;
        this.maxAttempts = maxAttempts;
        this.initialBackoff = initialBackoff;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(UUID eventId) {
        OutboxEvent event = repository.findById(eventId).orElse(null);
        if (event == null || event.getStatus() != OutboxStatus.PROCESSING) return;
        Instant now = businessClock.instant();
        try {
            dispatchService.dispatch(event);
            event.markPublished(now);
        } catch (RuntimeException failure) {
            Instant retryAt = event.getAttempts() >= maxAttempts
                    ? null
                    : now.plus(initialBackoff.multipliedBy(1L << Math.min(event.getAttempts() - 1, 10)));
            event.markFailed(retryAt, failure.getMessage());
        }
        repository.save(event);
    }
}
