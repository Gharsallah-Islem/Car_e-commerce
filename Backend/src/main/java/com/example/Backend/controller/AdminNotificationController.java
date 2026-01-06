package com.example.Backend.controller;

import com.example.Backend.entity.AdminNotification;
import com.example.Backend.service.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for admin notifications
 */
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationService notificationService;

    /**
     * Get all notifications with optional pagination
     */
    @GetMapping
    public ResponseEntity<List<AdminNotification>> getAllNotifications(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            Page<AdminNotification> notifications = notificationService.getAllNotifications(page, size);
            return ResponseEntity.ok(notifications.getContent());
        }
        return ResponseEntity.ok(notificationService.getRecentNotifications());
    }

    /**
     * Get unread notifications only
     */
    @GetMapping("/unread")
    public ResponseEntity<List<AdminNotification>> getUnreadNotifications() {
        return ResponseEntity.ok(notificationService.getUnreadNotifications());
    }

    /**
     * Get unread count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Mark a notification as read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
        // For now, we'll just mark as read. Actual delete can be implemented if needed
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }
}
