package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.model.OutboxEvent;
import com.guilhermesemog.unimove.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OutboxClaimService {
    private final OutboxEventRepository repository;
    private final Clock businessClock;
    private final int batchSize;
    private final int maxAttempts;
    private final Duration lockTimeout;

    public OutboxClaimService(OutboxEventRepository repository, Clock businessClock,
                              @Value("${unimove.phase-five.notifications.batch-size:50}") int batchSize,
                              @Value("${unimove.phase-five.notifications.max-attempts:5}") int maxAttempts,
                              @Value("${unimove.phase-five.notifications.lock-timeout:PT5M}") Duration lockTimeout) {
        this.repository = repository;
        this.businessClock = businessClock;
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
        this.lockTimeout = lockTimeout;
    }

    @Transactional
    public List<UUID> claim() {
        Instant now = businessClock.instant();
        List<OutboxEvent> claimed = repository.lockProcessable(now, now.minus(lockTimeout), maxAttempts, batchSize);
        claimed.forEach(event -> event.markProcessing(now));
        repository.saveAll(claimed);
        return claimed.stream().map(OutboxEvent::getId).toList();
    }
}
