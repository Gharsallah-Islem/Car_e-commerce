package com.example.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for storing admin notifications
 * Supports real-time broadcasting via WebSocket
 */
@Entity
@Table(name = "admin_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminNotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    /**
     * JSON data for additional context (e.g., orderId, productId, userId)
     */
    @Column(columnDefinition = "TEXT")
    private String data;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /**
     * Target admin user ID (null = broadcast to all admins)
     */
    private UUID targetUserId;

    /**
     * Reference ID for the related entity (order, product, user, etc.)
     */
    private String referenceId;

    /**
     * Icon to display in UI
     */
    private String icon;

    /**
     * Link to navigate when notification is clicked
     */
    private String actionUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
