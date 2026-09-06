package com.guilhermesemog.unimove.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(
        prefix = "unimove.phase-five.recurrence",
        name = {"enabled", "automatic-generation-enabled"},
        havingValue = "true"
)
public class RecurrenceGenerationScheduler {
    private static final Logger log = LoggerFactory.getLogger(RecurrenceGenerationScheduler.class);
    private final RecurrencePlanService recurrencePlanService;
    private final RecurrenceGenerationFailureReporter failureReporter;

    public RecurrenceGenerationScheduler(
            RecurrencePlanService recurrencePlanService,
            RecurrenceGenerationFailureReporter failureReporter
    ) {
        this.recurrencePlanService = recurrencePlanService;
        this.failureReporter = failureReporter;
    }

    @Scheduled(cron = "${unimove.phase-five.recurrence.generation-cron:0 15 1 * * *}",
            zone = "${unimove.business-time-zone:America/Sao_Paulo}")
    public void generateActivePlans() {
        for (UUID planId : recurrencePlanService.activePlans().stream().map(plan -> plan.getId()).toList()) {
            try {
                recurrencePlanService.generate(planId);
            } catch (RuntimeException failure) {
                log.error("Recurring demand generation failed for plan {}", planId, failure);
                try {
                    failureReporter.report(planId, failure);
                } catch (RuntimeException reportingFailure) {
                    log.error("Could not persist recurring generation failure for plan {}", planId, reportingFailure);
                }
            }
        }
    }
}
