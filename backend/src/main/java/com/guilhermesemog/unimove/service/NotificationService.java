package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.notification.NotificationResponse;
import com.guilhermesemog.unimove.exception.type.ResourceNotFoundException;
import com.guilhermesemog.unimove.model.Notification;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.repository.NotificationRepository;
import com.guilhermesemog.unimove.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final Clock businessClock;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository,
                               Clock businessClock) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.businessClock = businessClock;
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> mine(Authentication authentication, int page, int size) {
        User user = currentUser(authentication);
        return notificationRepository.findAllByRecipient_Id(user.getId(),
                PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)),
                        Sort.by("createdAt").descending())).map(this::response);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Authentication authentication) {
        return notificationRepository.countByRecipient_IdAndReadAtIsNull(currentUser(authentication).getId());
    }

    @Transactional
    public NotificationResponse markRead(Authentication authentication, UUID notificationId) {
        User user = currentUser(authentication);
        Notification notification = notificationRepository.findById(notificationId)
                .filter(item -> item.getRecipient().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.markRead(businessClock.instant());
        return response(notificationRepository.save(notification));
    }

    @Transactional
    public int markAllRead(Authentication authentication) {
        return notificationRepository.markAllRead(currentUser(authentication).getId(), businessClock.instant());
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByCpf(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private NotificationResponse response(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getType(), notification.getCategory(),
                notification.getContentKey(), notification.getTitle(), notification.getDescription(),
                notification.getRoute(), notification.getCreatedAt(), notification.getReadAt());
    }
}
