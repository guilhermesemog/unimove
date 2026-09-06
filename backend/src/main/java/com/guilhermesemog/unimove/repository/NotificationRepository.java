package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findAllByRecipient_Id(UUID recipientId, Pageable pageable);
    long countByRecipient_IdAndReadAtIsNull(UUID recipientId);
    boolean existsByDeduplicationKey(String deduplicationKey);

    @Modifying
    @Query(value = """
            INSERT INTO notifications (
                id, recipient_id, notification_type, category, content_key, payload,
                title, description, route, created_at, deduplication_key
            ) VALUES (
                :id, :recipientId, :type, :category, :contentKey, :payload,
                :title, :description, :route, :createdAt, :deduplicationKey
            ) ON CONFLICT (deduplication_key) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(UUID id, UUID recipientId, String type, String category, String contentKey,
                       String payload, String title, String description, String route,
                       Instant createdAt, String deduplicationKey);

    @Modifying
    @Query("update Notification n set n.readAt = :now where n.recipient.id = :recipientId and n.readAt is null")
    int markAllRead(UUID recipientId, Instant now);
}
