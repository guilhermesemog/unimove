package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.notification.NotificationResponse;
import com.guilhermesemog.unimove.dto.notification.UnreadCountResponse;
import com.guilhermesemog.unimove.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/me")
    public ResponseEntity<Page<NotificationResponse>> mine(Authentication authentication,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.mine(authentication, page, size));
    }

    @GetMapping("/me/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount(Authentication authentication) {
        return ResponseEntity.ok(new UnreadCountResponse(notificationService.unreadCount(authentication)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markRead(authentication, id));
    }

    @PatchMapping("/me/read-all")
    public ResponseEntity<Void> markAllRead(Authentication authentication) {
        notificationService.markAllRead(authentication);
        return ResponseEntity.noContent().build();
    }
}
