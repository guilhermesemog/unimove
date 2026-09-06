package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.audit.*;
import com.guilhermesemog.unimove.model.enums.AuditAction;
import com.guilhermesemog.unimove.service.AuditQueryService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/admin/audit-events")
@PreAuthorize("hasRole('ADMIN')")
public class AuditEventController {
    private final AuditQueryService auditQueryService;

    public AuditEventController(AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    @GetMapping
    public ResponseEntity<Page<AuditEventResponse>> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) Set<UUID> entityIds,
            @RequestParam(required = false) UUID correlationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Set<UUID> actorIds = auditQueryService.findActorIds(actor);
        if (actor != null && !actor.isBlank() && actorIds.isEmpty()) return ResponseEntity.ok(Page.empty());
        return ResponseEntity.ok(auditQueryService.search(new AuditQuery(from, to, actorIds, action,
                entityType, entityId, entityIds, correlationId), page, size, sortDirection));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditEventDetailResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(auditQueryService.getById(id));
    }
}
