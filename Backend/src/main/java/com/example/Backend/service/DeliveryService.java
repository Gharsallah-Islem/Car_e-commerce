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

    /**
     * Create deliveries for existing shipped orders that don't have one
     * 
     * @return Number of deliveries created
     */
    int createDeliveriesForShippedOrders();

    /**
     * Get debug info about orders and deliveries
     * 
     * @return Map with debug information
     */
    Map<String, Object> getDebugInfo();

    /**
     * Sync tracking numbers from deliveries to orders
     * 
     * @return Number of orders synced
     */
    int syncTrackingNumbersToOrders();

    /**
     * Auto-create delivery from confirmed order
     * Called automatically when order status changes to CONFIRMED
     * 
     * @param order The confirmed order
     * @return Created delivery
     */
    Delivery createDeliveryFromOrder(com.example.Backend.entity.Order order);

    /**
     * Create deliveries for all confirmed orders that don't have one
     * 
     * @return Number of deliveries created
     */
    int createDeliveriesForConfirmedOrders();

    /**
     * Ensure simulation is running for active deliveries
     * Used when user opens tracking page - restarts simulation if needed
     * 
     * @param deliveryId The delivery ID
     */
    void ensureSimulationRunning(java.util.UUID deliveryId);
}
