package com.example.Backend.repository;

import com.example.Backend.entity.AdminNotification;
import com.example.Backend.entity.AdminNotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for admin notifications
 */
@Repository
public interface AdminNotificationRepository extends JpaRepository<AdminNotification, UUID> {

    /**
     * Find all unread notifications
     */
    List<AdminNotification> findByIsReadFalseOrderByCreatedAtDesc();

    /**
     * Find notifications by type
     */
    List<AdminNotification> findByTypeOrderByCreatedAtDesc(AdminNotificationType type);

    /**
     * Find all notifications ordered by creation date
     */
    List<AdminNotification> findAllByOrderByCreatedAtDesc();

    /**
     * Find notifications with pagination
     */
    Page<AdminNotification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Count unread notifications
     */
    long countByIsReadFalse();

    /**
     * Find notifications created after a specific date
     */
    List<AdminNotification> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime date);

    /**
     * Mark notification as read
     */
    @Modifying
    @Query("UPDATE AdminNotification n SET n.isRead = true WHERE n.id = :id")
    void markAsRead(@Param("id") UUID id);

    /**
     * Mark all notifications as read
     */
    @Modifying
    @Query("UPDATE AdminNotification n SET n.isRead = true WHERE n.isRead = false")
    void markAllAsRead();

    /**
     * Delete old notifications (older than specified date)
     */
    @Modifying
    @Query("DELETE FROM AdminNotification n WHERE n.createdAt < :date")
    void deleteOldNotifications(@Param("date") LocalDateTime date);
}
