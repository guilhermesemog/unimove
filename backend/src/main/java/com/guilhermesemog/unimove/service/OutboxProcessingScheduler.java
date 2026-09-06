package com.guilhermesemog.unimove.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "unimove.phase-five.notifications", name = "processor-enabled", havingValue = "true")
public class OutboxProcessingScheduler {
    private final OutboxClaimService claimService;
    private final OutboxEventProcessor processor;

    public OutboxProcessingScheduler(OutboxClaimService claimService, OutboxEventProcessor processor) {
        this.claimService = claimService;
        this.processor = processor;
    }

    @Scheduled(fixedDelayString = "${unimove.phase-five.notifications.processing-interval-ms:15000}")
    public void processBatch() {
        claimService.claim().forEach(processor::process);
    }
}
