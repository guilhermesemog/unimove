package com.guilhermesemog.unimove.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "unimove.phase-five.telemetry", name = "enabled", havingValue = "true")
public class TelemetryRetentionScheduler {
    private static final Logger log = LoggerFactory.getLogger(TelemetryRetentionScheduler.class);
    private final TelemetryService telemetryService;
    private final int retentionDays;

    public TelemetryRetentionScheduler(TelemetryService telemetryService,
                                       @Value("${unimove.phase-five.telemetry.retention-days:180}") int retentionDays) {
        this.telemetryService = telemetryService;
        this.retentionDays = retentionDays;
    }

    @Scheduled(cron = "${unimove.phase-five.telemetry.retention-cron:0 30 2 * * *}",
            zone = "${unimove.business-time-zone:America/Sao_Paulo}")
    public void deleteExpiredEvents() {
        int deleted = telemetryService.deleteExpired(retentionDays);
        if (deleted > 0) log.info("Deleted {} expired product telemetry events", deleted);
    }
}
