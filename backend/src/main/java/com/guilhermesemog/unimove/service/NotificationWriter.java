package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.model.enums.NotificationCategory;
import com.guilhermesemog.unimove.model.enums.NotificationType;
import com.guilhermesemog.unimove.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.UUID;

@Service
public class NotificationWriter {
    private final NotificationRepository notificationRepository;
    private final Clock businessClock;

    public NotificationWriter(NotificationRepository notificationRepository, Clock businessClock) {
        this.notificationRepository = notificationRepository;
        this.businessClock = businessClock;
    }

    public boolean create(User recipient, NotificationType type, NotificationCategory category,
                          String contentKey, String payload, String title, String description,
                          String route, String deduplicationKey) {
        if (recipient == null || !Boolean.TRUE.equals(recipient.getActive())) {
            return false;
        }
        return notificationRepository.insertIfAbsent(UUID.randomUUID(), recipient.getId(), type.name(),
                category.name(), contentKey, payload, title, description, route,
                businessClock.instant(), deduplicationKey) == 1;
    }
}
