package com.guilhermesemog.unimove.model;

import com.guilhermesemog.unimove.model.enums.BusinessEventType;
import com.guilhermesemog.unimove.model.enums.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private BusinessEventType eventType;

    @Column(nullable = false, length = 80)
    private String aggregateType;

    @Column(nullable = false, length = 80)
    private UUID aggregateId;

    @Column
    private UUID actorId;

    @Column(length = 30)
    private String actorRole;

    @Column(nullable = false, updatable = false)
    private UUID correlationId;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Column(nullable = false, unique = true, length = 180)
    private String deduplicationKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OutboxStatus status;

    @Column(nullable = false)
    private Integer attempts;

    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

    @Column
    private Instant nextAttemptAt;

    private Instant lockedAt;

    @Column
    private Instant processedAt;

    @Column(columnDefinition = "text")
    private String lastError;

    public OutboxEvent(
            BusinessEventType eventType,
            String aggregateType,
            UUID aggregateId,
            UUID actorId,
            String actorRole,
            UUID correlationId,
            String payload,
            String deduplicationKey
    ) {
        this.id = UUID.randomUUID();
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.actorId = actorId;
        this.actorRole = actorRole;
        this.correlationId = correlationId;
        this.payload = payload;
        this.deduplicationKey = deduplicationKey;
        this.status = OutboxStatus.PENDING;
        this.attempts = 0;
        this.occurredAt = Instant.now();
    }

    public void markProcessing(Instant now) {
        status = OutboxStatus.PROCESSING;
        attempts++;
        lockedAt = now;
        lastError = null;
    }

    public void markPublished(Instant now) {
        status = OutboxStatus.PUBLISHED;
        processedAt = now;
        nextAttemptAt = null;
        lockedAt = null;
        lastError = null;
    }

    public void markFailed(Instant retryAt, String error) {
        status = OutboxStatus.FAILED;
        nextAttemptAt = retryAt;
        lockedAt = null;
        lastError = error == null ? "Unknown processing error" : error.substring(0, Math.min(error.length(), 2000));
    }
}
