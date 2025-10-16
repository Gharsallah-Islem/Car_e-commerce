package com.example.Backend.service;

import com.example.Backend.dto.DeliveryDTO;
import com.example.Backend.entity.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface DeliveryService {

    /**
     * Create delivery for order
     * 
     * @param orderId     Order ID
     * @param deliveryDTO Delivery data
     * @return Created delivery
     */
    Delivery createDelivery(UUID orderId, DeliveryDTO deliveryDTO);

    /**
     * Get delivery by ID
     * 
     * @param deliveryId Delivery ID
     * @return Delivery entity
     */
    Delivery getDeliveryById(UUID deliveryId);

    /**
     * Get delivery by order ID
     * 
     * @param orderId Order ID
     * @return Delivery entity
     */
    Delivery getDeliveryByOrderId(UUID orderId);

    /**
     * Get delivery by tracking number
     * 
     * @param trackingNumber ONdelivery tracking number
     * @return Delivery entity
     */
    Delivery getDeliveryByTrackingNumber(String trackingNumber);

    /**
     * Get all deliveries
     * 
     * @param pageable Pagination parameters
     * @return Page of deliveries
     */
    Page<Delivery> getAllDeliveries(Pageable pageable);

    /**
     * Get deliveries by status
     * 
     * @param status   Delivery status
     * @param pageable Pagination parameters
     * @return Page of deliveries
     */
    Page<Delivery> getDeliveriesByStatus(String status, Pageable pageable);

    /**
     * Get pending deliveries
     * 
     * @param pageable Pagination parameters
     * @return Page of pending deliveries
     */
    Page<Delivery> getPendingDeliveries(Pageable pageable);

    /**
     * Get active deliveries (in transit)
     * 
     * @param pageable Pagination parameters
     * @return Page of active deliveries
     */
    Page<Delivery> getActiveDeliveries(Pageable pageable);

    /**
     * Get deliveries by courier
     * 
     * @param courierName Courier name
     * @param pageable    Pagination parameters
     * @return Page of deliveries
     */
    Page<Delivery> getDeliveriesByCourier(String courierName, Pageable pageable);

    /**
     * Update delivery status
     * 
     * @param deliveryId Delivery ID
     * @param status     New status
     * @return Updated delivery
     */
    Delivery updateStatus(UUID deliveryId, String status);

    /**
     * Mark as picked up
     * 
     * @param deliveryId  Delivery ID
     * @param courierName Courier name
     * @return Updated delivery
     */
    Delivery markAsPickedUp(UUID deliveryId, String courierName);

    /**
     * Mark as in transit
     * 
     * @param deliveryId Delivery ID
     * @return Updated delivery
     */
    Delivery markAsInTransit(UUID deliveryId);

    /**
     * Mark as delivered
     * 
     * @param deliveryId Delivery ID
     * @return Updated delivery
     */
    Delivery markAsDelivered(UUID deliveryId);

    /**
     * Track delivery (sync with ONdelivery API)
     * 
     * @param trackingNumber Tracking number
     * @return Updated delivery with tracking info
     */
    Delivery trackDelivery(String trackingNumber);

    /**
     * Get delivery statistics
     * 
     * @return Map of counts by status
     */
    Map<String, Long> getDeliveryStatistics();

    /**
     * Get average delivery time
     * 
     * @return Average time in hours
     */
    Double getAverageDeliveryTime();
}
