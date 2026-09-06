package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    @Query(value = """
            SELECT * FROM outbox_events
            WHERE attempts < :maxAttempts AND (
                (status IN ('PENDING', 'FAILED') AND (next_attempt_at IS NULL OR next_attempt_at <= :now))
                OR (status = 'PROCESSING' AND locked_at < :staleBefore)
            )
            ORDER BY occurred_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> lockProcessable(Instant now, Instant staleBefore, int maxAttempts, int batchSize);
}
