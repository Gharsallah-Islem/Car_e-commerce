package com.example.Backend.service.impl;

import com.example.Backend.dto.DeliveryDTO;
import com.example.Backend.entity.Delivery;
import com.example.Backend.entity.Order;
import com.example.Backend.repository.DeliveryRepository;
import com.example.Backend.repository.OrderRepository;
import com.example.Backend.service.DeliveryService;
import com.example.Backend.service.DeliverySimulationService;
import com.example.Backend.service.EmailService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final DeliverySimulationService simulationService;

    public DeliveryServiceImpl(
            DeliveryRepository deliveryRepository,
            OrderRepository orderRepository,
            EmailService emailService,
            @Lazy DeliverySimulationService simulationService) {
        this.deliveryRepository = deliveryRepository;
        this.orderRepository = orderRepository;
        this.emailService = emailService;
        this.simulationService = simulationService;
    }

    @Override
    @Transactional
    public Delivery createDelivery(UUID orderId, DeliveryDTO deliveryDTO) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setTrackingNumber(generateTrackingNumber());
        delivery.setStatus(Delivery.STATUS_PROCESSING);
        delivery.setAddress(deliveryDTO.getDeliveryAddress());
        delivery.setDeliveryNotes(deliveryDTO.getRecipientName() + " - " + deliveryDTO.getRecipientPhone());
        delivery.setDriverName(deliveryDTO.getCourierName());
        delivery.setCreatedAt(LocalDateTime.now());

        // Set estimated delivery (e.g., 3 days from now)
        delivery.setEstimatedDelivery(LocalDateTime.now().plusDays(3));

        return deliveryRepository.save(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public Delivery getDeliveryById(UUID deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found with id: " + deliveryId));
    }

    @Override
    @Transactional(readOnly = true)
    public Delivery getDeliveryByOrderId(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found for order id: " + orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public Delivery getDeliveryByTrackingNumber(String trackingNumber) {
        return deliveryRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Delivery not found with tracking number: " + trackingNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Delivery> getAllDeliveries(Pageable pageable) {
        return deliveryRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Delivery> getDeliveriesByStatus(String status, Pageable pageable) {
        List<Delivery> deliveries = deliveryRepository.findByStatus(status);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), deliveries.size());
        return new PageImpl<>(deliveries.subList(start, end), pageable, deliveries.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Delivery> getPendingDeliveries(Pageable pageable) {
        return getDeliveriesByStatus(Delivery.STATUS_PROCESSING, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Delivery> getActiveDeliveries(Pageable pageable) {
        List<Delivery> deliveries = deliveryRepository.findActiveDeliveries();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), deliveries.size());
        return new PageImpl<>(deliveries.subList(start, end), pageable, deliveries.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Delivery> getDeliveriesByCourier(String courierName, Pageable pageable) {
        List<Delivery> deliveries = deliveryRepository.findByDriverName(courierName);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), deliveries.size());
        return new PageImpl<>(deliveries.subList(start, end), pageable, deliveries.size());
    }

    @Override
    @Transactional
    public Delivery updateStatus(UUID deliveryId, String status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found with id: " + deliveryId));

        delivery.setStatus(status);
        delivery.setUpdatedAt(LocalDateTime.now());

        // Sync Order status based on Delivery status
        Order order = delivery.getOrder();
        if (order != null) {
            if (Delivery.STATUS_DELIVERED.equals(status)) {
                delivery.setActualDelivery(LocalDateTime.now());
                // Auto-sync Order to DELIVERED
                order.markAsDelivered();
                orderRepository.save(order);
                log.info("Synced Order {} status to DELIVERED", order.getId());

                // Stop simulation if running
                simulationService.stopSimulation(deliveryId);
            } else if (Delivery.STATUS_FAILED.equals(status)) {
                // Auto-sync Order to DELIVERY_FAILED
                order.markAsDeliveryFailed();
                orderRepository.save(order);
                log.info("Synced Order {} status to DELIVERY_FAILED", order.getId());

                // Send notifications
                sendDeliveryFailedNotifications(delivery, order, null);

                // Stop simulation if running
                simulationService.stopSimulation(deliveryId);
            } else if (Delivery.STATUS_IN_TRANSIT.equals(status)) {
                // Start delivery simulation when status changes to IN_TRANSIT
                log.info("Starting delivery simulation for delivery {}", deliveryId);
                simulationService.startSimulation(deliveryId);
            }
        }

        return deliveryRepository.save(delivery);
    }

    @Override
    @Transactional
    public Delivery markAsPickedUp(UUID deliveryId, String courierName) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found with id: " + deliveryId));

        delivery.setStatus(Delivery.STATUS_IN_TRANSIT);
        delivery.setDriverName(courierName);
        delivery.setUpdatedAt(LocalDateTime.now());

        return deliveryRepository.save(delivery);
    }

    @Override
    @Transactional
    public Delivery markAsInTransit(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found with id: " + deliveryId));

        delivery.setStatus(Delivery.STATUS_IN_TRANSIT);
        delivery.setUpdatedAt(LocalDateTime.now());

        return deliveryRepository.save(delivery);
    }

    @Override
    @Transactional
    public Delivery markAsDelivered(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found with id: " + deliveryId));

        delivery.markAsDelivered();
        delivery.setUpdatedAt(LocalDateTime.now());

        // Auto-sync Order status to DELIVERED
        Order order = delivery.getOrder();
        if (order != null && !Order.STATUS_DELIVERED.equals(order.getStatus())) {
            order.markAsDelivered();
            orderRepository.save(order);
            log.info("Auto-synced Order {} to DELIVERED when Delivery {} was marked delivered",
                    order.getId(), deliveryId);
        }

        return deliveryRepository.save(delivery);
    }

    /**
     * Mark delivery as failed and sync Order status
     * Sends notifications to customer and admin
     */
    @Transactional
    public Delivery markAsFailed(UUID deliveryId, String failureReason) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found with id: " + deliveryId));

        delivery.setStatus(Delivery.STATUS_FAILED);
        delivery.setDeliveryNotes(failureReason);
        delivery.setUpdatedAt(LocalDateTime.now());

        // Sync Order status to DELIVERY_FAILED
        Order order = delivery.getOrder();
        if (order != null) {
            order.markAsDeliveryFailed();
            orderRepository.save(order);
            log.info("Order {} marked as DELIVERY_FAILED due to delivery failure", order.getId());
        }

        Delivery savedDelivery = deliveryRepository.save(delivery);

        // Send notifications
        sendDeliveryFailedNotifications(delivery, order, failureReason);

        return savedDelivery;
    }

    /**
     * Send notifications when delivery fails
     */
    private void sendDeliveryFailedNotifications(Delivery delivery, Order order, String failureReason) {
        try {
            // Send email to customer
            if (order != null && order.getUser() != null) {
                emailService.sendDeliveryFailedEmail(order, delivery, failureReason);
                log.info("Sent delivery failure email to customer for order {}", order.getId());
            }
        } catch (Exception e) {
            log.error("Failed to send delivery failure notification: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public Delivery trackDelivery(String trackingNumber) {
        Delivery delivery = deliveryRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Delivery not found with tracking number: " + trackingNumber));

        // TODO: Integrate with ONdelivery API to sync latest status
        // For now, just return the delivery

        return delivery;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getDeliveryStatistics() {
        Map<String, Long> statistics = new HashMap<>();

        statistics.put("PROCESSING", deliveryRepository.countByStatus(Delivery.STATUS_PROCESSING));
        statistics.put("IN_TRANSIT", deliveryRepository.countByStatus(Delivery.STATUS_IN_TRANSIT));
        statistics.put("OUT_FOR_DELIVERY", deliveryRepository.countByStatus(Delivery.STATUS_OUT_FOR_DELIVERY));
        statistics.put("DELIVERED", deliveryRepository.countByStatus(Delivery.STATUS_DELIVERED));
        statistics.put("FAILED", deliveryRepository.countByStatus(Delivery.STATUS_FAILED));
        statistics.put("TOTAL", deliveryRepository.count());

        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageDeliveryTime() {
        List<Delivery> deliveredOrders = deliveryRepository.findByStatus(Delivery.STATUS_DELIVERED);

        if (deliveredOrders.isEmpty()) {
            return 0.0;
        }

        double totalHours = 0.0;
        int count = 0;

        for (Delivery delivery : deliveredOrders) {
            if (delivery.getCreatedAt() != null && delivery.getActualDelivery() != null) {
                Duration duration = Duration.between(delivery.getCreatedAt(), delivery.getActualDelivery());
                totalHours += duration.toHours();
                count++;
            }
        }

        return count > 0 ? totalHours / count : 0.0;
    }

    /**
     * Generate a unique tracking number
     */
    private String generateTrackingNumber() {
        return "TRK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    @Transactional
    public int createDeliveriesForShippedOrders() {
        // Get all SHIPPED orders
        List<Order> shippedOrders = orderRepository.findByStatus("SHIPPED");
        int count = 0;

        for (Order order : shippedOrders) {
            // Check if delivery already exists for this order
            var existingDelivery = deliveryRepository.findByOrderId(order.getId());

            if (existingDelivery.isEmpty()) {
                // Create new delivery
                Delivery delivery = new Delivery();
                delivery.setOrder(order);

                // Use order's tracking number or generate one
                String trackingNumber = order.getTrackingNumber();
                if (trackingNumber == null || trackingNumber.isBlank()) {
                    trackingNumber = generateTrackingNumber();
                    // Also update the order's tracking number
                    order.setTrackingNumber(trackingNumber);
                    orderRepository.save(order);
                }

                delivery.setTrackingNumber(trackingNumber);
                delivery.setStatus(Delivery.STATUS_PROCESSING);
                delivery.setAddress(order.getDeliveryAddress());
                delivery.setEstimatedDelivery(LocalDateTime.now().plusDays(3));

                deliveryRepository.save(delivery);
                count++;
            } else {
                // Fix existing delivery with null tracking number
                Delivery delivery = existingDelivery.get();
                if (delivery.getTrackingNumber() == null || delivery.getTrackingNumber().isBlank()) {
                    String trackingNumber = generateTrackingNumber();
                    delivery.setTrackingNumber(trackingNumber);

                    // Also update the order's tracking number
                    order.setTrackingNumber(trackingNumber);
                    orderRepository.save(order);
                    deliveryRepository.save(delivery);
                    count++;
                }
            }
        }

        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDebugInfo() {
        Map<String, Object> info = new HashMap<>();

        // Get all orders and their statuses
        List<Order> allOrders = orderRepository.findAll();
        List<Map<String, String>> orderInfoList = new java.util.ArrayList<>();

        for (Order o : allOrders) {
            Map<String, String> orderInfo = new HashMap<>();
            orderInfo.put("id", o.getId().toString());
            orderInfo.put("status", o.getStatus() != null ? o.getStatus() : "null");
            orderInfo.put("orderTrackingNumber", o.getTrackingNumber() != null ? o.getTrackingNumber() : "null");

            var delivery = deliveryRepository.findByOrderId(o.getId());
            if (delivery.isPresent()) {
                orderInfo.put("hasDelivery", "yes");
                orderInfo.put("deliveryTrackingNumber",
                        delivery.get().getTrackingNumber() != null ? delivery.get().getTrackingNumber() : "null");
            } else {
                orderInfo.put("hasDelivery", "no");
                orderInfo.put("deliveryTrackingNumber", "N/A");
            }
            orderInfoList.add(orderInfo);
        }

        info.put("totalOrders", allOrders.size());
        info.put("shippedOrders", orderRepository.findByStatus("SHIPPED").size());
        info.put("totalDeliveries", deliveryRepository.count());
        info.put("orders", orderInfoList);

        return info;
    }

    @Override
    @Transactional
    public int syncTrackingNumbersToOrders() {
        int count = 0;
        List<Delivery> allDeliveries = deliveryRepository.findAll();

        for (Delivery delivery : allDeliveries) {
            Order order = delivery.getOrder();
            // If order has no tracking number but delivery does, sync it
            if (order != null && delivery.getTrackingNumber() != null && !delivery.getTrackingNumber().isBlank()) {
                if (order.getTrackingNumber() == null || order.getTrackingNumber().isBlank()) {
                    order.setTrackingNumber(delivery.getTrackingNumber());
                    orderRepository.save(order);
                    count++;
                }
            }
        }

        return count;
    }

    @Override
    @Transactional
    public Delivery createDeliveryFromOrder(Order order) {
        // Check if delivery already exists for this order
        if (deliveryRepository.existsByOrderId(order.getId())) {
            log.info("Delivery already exists for order {}", order.getId());
            return deliveryRepository.findByOrderId(order.getId()).orElse(null);
        }

        // Create new delivery from order
        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setTrackingNumber(generateTrackingNumber());
        delivery.setStatus(Delivery.STATUS_PROCESSING);

        // Use order's delivery address
        String address = order.getDeliveryAddress();
        if (address == null || address.isBlank()) {
            // Fallback to user address if available
            if (order.getUser() != null && order.getUser().getAddress() != null) {
                address = order.getUser().getAddress();
            } else {
                address = "Adresse à confirmer";
            }
        }
        delivery.setAddress(address);

        // Set recipient info from order/user
        String recipientInfo = "";
        if (order.getUser() != null) {
            String name = order.getUser().getFullName() != null ? order.getUser().getFullName()
                    : order.getUser().getUsername();
            String phone = order.getUser().getPhone() != null ? order.getUser().getPhone() : "";
            recipientInfo = name + " - " + phone;
        }
        delivery.setDeliveryNotes(recipientInfo);

        delivery.setCreatedAt(LocalDateTime.now());
        delivery.setEstimatedDelivery(LocalDateTime.now().plusDays(3));

        // Sync tracking number to order
        order.setTrackingNumber(delivery.getTrackingNumber());
        orderRepository.save(order);

        Delivery saved = deliveryRepository.save(delivery);
        log.info("Auto-created delivery {} for order {}", saved.getId(), order.getId());

        return saved;
    }

    @Override
    @Transactional
    public int createDeliveriesForConfirmedOrders() {
        int count = 0;

        // Get all orders with status CONFIRMED or SHIPPED that don't have a delivery
        List<Order> allOrders = orderRepository.findAll();

        for (Order order : allOrders) {
            String status = order.getStatus();
            // Process confirmed, processing, or shipped orders
            if ("CONFIRMED".equals(status) || "PROCESSING".equals(status) || "SHIPPED".equals(status)) {
                // Check if delivery already exists
                if (!deliveryRepository.existsByOrderId(order.getId())) {
                    try {
                        createDeliveryFromOrder(order);
                        count++;
                        log.info("Created delivery for existing {} order {}", status, order.getId());
                    } catch (Exception e) {
                        log.error("Failed to create delivery for order {}: {}", order.getId(), e.getMessage());
                    }
                }
            }
        }

        log.info("Created {} deliveries for confirmed orders", count);
        return count;
    }

    @Override
    public void ensureSimulationRunning(UUID deliveryId) {
        // Start simulation if not already running
        // This is used when user opens tracking page after backend restart
        log.info("Ensuring simulation running for delivery {}", deliveryId);
        simulationService.startSimulation(deliveryId);
    }
}
