package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.model.enums.BusinessEventType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
public class RecurrenceGenerationFailureReporter {
    private final BusinessEventPublisher eventPublisher;
    private final Clock businessClock;

    public RecurrenceGenerationFailureReporter(BusinessEventPublisher eventPublisher, Clock businessClock) {
        this.eventPublisher = eventPublisher;
        this.businessClock = businessClock;
    }

    @Transactional
    public void report(UUID planId, RuntimeException failure) {
        LocalDate date = LocalDate.now(businessClock);
        eventPublisher.publish(
                BusinessEventType.RECURRENCE_GENERATION_FAILED,
                "RecurrencePlan",
                planId,
                Map.of("planId", planId, "errorType", failure.getClass().getSimpleName()),
                BusinessEventType.RECURRENCE_GENERATION_FAILED + ":" + planId + ":" + date
        );
    }
}
