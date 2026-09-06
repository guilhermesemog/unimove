package com.guilhermesemog.unimove.model;

import com.guilhermesemog.unimove.model.enums.AuditAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Immutable
@NoArgsConstructor
@Table(name = "audit_events")
public class AuditEvent implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column
    private UUID actorId;

    @Column(length = 30)
    private String actorRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private AuditAction action;

    @Column(nullable = false, length = 80)
    private String entityType;

    @Column(nullable = false, length = 80)
    private UUID entityId;

    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(nullable = false, updatable = false)
    private UUID correlationId;

    @Column(columnDefinition = "text")
    private String previousState;

    @Column(columnDefinition = "text")
    private String resultingState;

    @Column(columnDefinition = "text")
    private String metadata;

    public AuditEvent(
            UUID actorId,
            String actorRole,
            AuditAction action,
            String entityType,
            UUID entityId,
            UUID correlationId,
            String previousState,
            String resultingState,
            String metadata
    ) {
        this.id = UUID.randomUUID();
        this.actorId = actorId;
        this.actorRole = actorRole;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.occurredAt = Instant.now();
        this.correlationId = correlationId;
        this.previousState = previousState;
        this.resultingState = resultingState;
        this.metadata = metadata;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
