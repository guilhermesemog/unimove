package com.guilhermesemog.unimove.model;

import com.guilhermesemog.unimove.model.enums.NotificationCategory;
import com.guilhermesemog.unimove.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "notifications")
@NoArgsConstructor
public class Notification {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 40)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationCategory category;

    @Column(nullable = false, length = 100)
    private String contentKey;

    @Column(columnDefinition = "text")
    private String payload;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(length = 300)
    private String route;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant readAt;

    @Column(nullable = false, unique = true, length = 220)
    private String deduplicationKey;

    public Notification(User recipient, NotificationType type, NotificationCategory category,
                        String contentKey, String payload, String title, String description,
                        String route, String deduplicationKey, Instant now) {
        this.id = UUID.randomUUID();
        this.recipient = recipient;
        this.type = type;
        this.category = category;
        this.contentKey = contentKey;
        this.payload = payload;
        this.title = title;
        this.description = description;
        this.route = route;
        this.deduplicationKey = deduplicationKey;
        this.createdAt = now;
    }

    public void markRead(Instant now) {
        if (readAt == null) readAt = now;
    }
}
