package com.compassed.compassed_api.api.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.repository.NotificationRepository;
import com.compassed.compassed_api.security.CurrentUserService;

@RestController
@Profile("mysql")
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final CurrentUserService currentUserService;

    public NotificationController(NotificationRepository notificationRepository, CurrentUserService currentUserService) {
        this.notificationRepository = notificationRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<Map<String, Object>> myNotifications() {
        Long userId = currentUserService.requireCurrentUserId();
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream().limit(200).map(n -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", n.getId());
            item.put("title", n.getTitle());
            item.put("message", n.getMessage());
            item.put("type", n.getType());
            item.put("read", n.isReadFlag());
            item.put("createdAt", n.getCreatedAt());
            item.put("readAt", n.getReadAt());
            return item;
        }).toList();
    }

    @GetMapping("/unread-count")
    public Map<String, Object> unreadCount() {
        Long userId = currentUserService.requireCurrentUserId();
        long count = notificationRepository.countByUser_IdAndReadFlagFalse(userId);
        return Map.of("unreadCount", count);
    }

    @PostMapping("/{id}/read")
    public Map<String, Object> markRead(@PathVariable Long id) {
        Long userId = currentUserService.requireCurrentUserId();
        var notification = notificationRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setReadFlag(true);
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
        }
        notificationRepository.save(notification);
        return Map.of("id", notification.getId(), "read", true);
    }
}
