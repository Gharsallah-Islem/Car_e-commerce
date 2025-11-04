package com.example.Backend.repository;

import com.example.Backend.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    /**
     * Find delivery by order ID
     */
    Optional<Delivery> findByOrderId(UUID orderId);

    /**
     * Find delivery by tracking number
     */
    Optional<Delivery> findByTrackingNumber(String trackingNumber);

    /**
     * Find deliveries by status
     */
    List<Delivery> findByStatus(String status);

    /**
     * Find active deliveries (in transit or out for delivery)
     */
    @Query("SELECT d FROM Delivery d WHERE d.status IN ('IN_TRANSIT', 'OUT_FOR_DELIVERY')")
    List<Delivery> findActiveDeliveries();

    /**
     * Find deliveries by driver
     */
    List<Delivery> findByDriverName(String driverName);

    /**
     * Find deliveries with estimated delivery before a date
     */
    List<Delivery> findByEstimatedDeliveryBefore(LocalDateTime date);

    /**
     * Find overdue deliveries (estimated delivery passed but not delivered)
     */
    @Query("SELECT d FROM Delivery d WHERE d.status != 'DELIVERED' " +
            "AND d.estimatedDelivery < :now")
    List<Delivery> findOverdueDeliveries(@Param("now") LocalDateTime now);

    /**
     * Find deliveries for ONdelivery integration sync
     */
    @Query("SELECT d FROM Delivery d WHERE d.status NOT IN ('DELIVERED', 'FAILED') " +
            "ORDER BY d.createdAt DESC")
    List<Delivery> findPendingDeliveriesForSync();

    /**
     * Count deliveries by status
     */
    Long countByStatus(String status);
}
