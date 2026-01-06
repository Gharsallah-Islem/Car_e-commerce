package com.example.Backend.service;

import com.example.Backend.entity.AdminNotification;
import com.example.Backend.entity.AdminNotificationType;
import com.example.Backend.repository.AdminNotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing admin notifications with real-time WebSocket
 * broadcasting
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationService {

    private final AdminNotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    private static final String ADMIN_NOTIFICATIONS_TOPIC = "/topic/admin/notifications";

    /**
     * Create and broadcast a new notification
     */
    @Transactional
    public AdminNotification createNotification(
            AdminNotificationType type,
            String title,
            String message,
            String referenceId,
            String actionUrl,
            Map<String, Object> additionalData) {
        try {
            String dataJson = additionalData != null ? objectMapper.writeValueAsString(additionalData) : null;

            AdminNotification notification = AdminNotification.builder()
                    .type(type)
                    .title(title)
                    .message(message)
                    .referenceId(referenceId)
                    .actionUrl(actionUrl)
                    .icon(getIconForType(type))
                    .data(dataJson)
                    .isRead(false)
                    .build();

            notification = notificationRepository.save(notification);

            // Broadcast to all connected admins
            broadcastNotification(notification);

            log.info("Created and broadcasted notification: {} - {}", type, title);
            return notification;

        } catch (Exception e) {
            log.error("Error creating notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create notification", e);
        }
    }

    /**
     * Simplified method to create notification
     */
    @Transactional
    public AdminNotification createNotification(
            AdminNotificationType type,
            String title,
            String message,
            String referenceId,
            String actionUrl) {
        return createNotification(type, title, message, referenceId, actionUrl, null);
    }

    /**
     * Broadcast notification to WebSocket topic
     */
    private void broadcastNotification(AdminNotification notification) {
        try {
            messagingTemplate.convertAndSend(ADMIN_NOTIFICATIONS_TOPIC, notification);
            log.debug("Broadcasted notification to {}", ADMIN_NOTIFICATIONS_TOPIC);
        } catch (Exception e) {
            log.error("Error broadcasting notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Get all notifications with pagination
     */
    public Page<AdminNotification> getAllNotifications(int page, int size) {
        return notificationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    /**
     * Get all notifications (for initial load)
     */
    public List<AdminNotification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get recent notifications (last 50)
     */
    public List<AdminNotification> getRecentNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 50)).getContent();
    }

    /**
     * Get unread notifications
     */
    public List<AdminNotification> getUnreadNotifications() {
        return notificationRepository.findByIsReadFalseOrderByCreatedAtDesc();
    }

    /**
     * Get unread count
     */
    public long getUnreadCount() {
        return notificationRepository.countByIsReadFalse();
    }

    /**
     * Mark single notification as read
     */
    @Transactional
    public void markAsRead(UUID notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    /**
     * Mark all notifications as read
     */
    @Transactional
    public void markAllAsRead() {
        notificationRepository.markAllAsRead();
    }

    /**
     * Delete old notifications (cleanup task)
     */
    @Transactional
    public void deleteOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        notificationRepository.deleteOldNotifications(cutoffDate);
        log.info("Deleted notifications older than {} days", daysOld);
    }

    /**
     * Get appropriate icon for notification type
     */
    private String getIconForType(AdminNotificationType type) {
        return switch (type) {
            case NEW_ORDER -> "shopping_cart";
            case ORDER_CANCELLED -> "cancel";
            case LOW_STOCK -> "inventory";
            case OUT_OF_STOCK -> "remove_shopping_cart";
            case NEW_USER -> "person_add";
            case DELIVERY_COMPLETED -> "local_shipping";
            case DELIVERY_FAILED -> "error";
            case PAYMENT_RECEIVED -> "payments";
            case SYSTEM_ALERT -> "warning";
        };
    }

    // ==================== CONVENIENCE METHODS ====================

    /**
     * Notify about new order
     */
    public void notifyNewOrder(String orderId, String customerName, double totalAmount) {
        createNotification(
                AdminNotificationType.NEW_ORDER,
                "Nouvelle commande",
                String.format("Commande de %s pour %.2f TND", customerName, totalAmount),
                orderId,
                "/admin/orders/" + orderId,
                Map.of("customerId", customerName, "total", totalAmount));
    }

    /**
     * Notify about low stock
     */
    public void notifyLowStock(String productId, String productName, int currentStock) {
        createNotification(
                AdminNotificationType.LOW_STOCK,
                "Stock bas",
                String.format("Le produit \"%s\" n'a plus que %d unités", productName, currentStock),
                productId,
                "/admin/inventory",
                Map.of("productName", productName, "stock", currentStock));
    }

    /**
     * Notify about new user registration
     */
    public void notifyNewUser(String userId, String userName, String email) {
        createNotification(
                AdminNotificationType.NEW_USER,
                "Nouvel utilisateur",
                String.format("%s (%s) vient de s'inscrire", userName, email),
                userId,
                "/admin/users",
                Map.of("userName", userName, "email", email));
    }

    /**
     * Notify about order cancellation
     */
    public void notifyOrderCancelled(String orderId, String customerName) {
        createNotification(
                AdminNotificationType.ORDER_CANCELLED,
                "Commande annulée",
                String.format("La commande de %s a été annulée", customerName),
                orderId,
                "/admin/orders/" + orderId);
    }

    /**
     * Notify about completed delivery
     */
    public void notifyDeliveryCompleted(String deliveryId, String orderId) {
        createNotification(
                AdminNotificationType.DELIVERY_COMPLETED,
                "Livraison terminée",
                String.format("La commande #%s a été livrée avec succès", orderId.substring(0, 8)),
                deliveryId,
                "/admin/delivery");
    }
}
