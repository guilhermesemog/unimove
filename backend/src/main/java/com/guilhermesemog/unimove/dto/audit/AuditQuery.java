package com.guilhermesemog.unimove.dto.audit;

import com.guilhermesemog.unimove.model.enums.AuditAction;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AuditQuery(
        Instant from,
        Instant to,
        Set<UUID> actorIds,
        AuditAction action,
        String entityType,
        UUID entityId,
        Set<UUID> entityIds,
        UUID correlationId
) {
}
