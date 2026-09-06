package com.guilhermesemog.unimove.dto.notification;

import com.guilhermesemog.unimove.model.enums.NotificationCategory;
import com.guilhermesemog.unimove.model.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        NotificationCategory category,
        String contentKey,
        String title,
        String description,
        String route,
        Instant createdAt,
        Instant readAt
) {
}
