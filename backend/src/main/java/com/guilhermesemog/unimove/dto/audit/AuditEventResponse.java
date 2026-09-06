package com.guilhermesemog.unimove.dto.audit;

import com.guilhermesemog.unimove.model.enums.AuditAction;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AuditEventResponse(
        UUID id,
        UUID actorId,
        String actorName,
        String actorRole,
        AuditAction action,
        String actionLabel,
        String entityType,
        UUID entityId,
        Instant occurredAt,
        UUID correlationId,
        String description,
        List<AuditChangeResponse> changes
) {
}
