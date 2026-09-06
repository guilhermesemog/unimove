package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.AuditEvent;
import org.springframework.data.repository.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface AuditEventRepository extends Repository<AuditEvent, UUID>, JpaSpecificationExecutor<AuditEvent> {
    AuditEvent save(AuditEvent auditEvent);

    Optional<AuditEvent> findById(UUID id);

    long count();
}
