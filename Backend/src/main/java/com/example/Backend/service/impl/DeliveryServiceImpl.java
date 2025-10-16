package com.example.Backend.service.impl;

import com.example.Backend.dto.DeliveryDTO;
import com.example.Backend.entity.Delivery;
import com.example.Backend.entity.Order;
import com.example.Backend.repository.DeliveryRepository;
import com.example.Backend.repository.OrderRepository;
import com.example.Backend.service.DeliveryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Delivery createDelivery(UUID orderId, DeliveryDTO deliveryDTO) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setTrackingNumber(generateTrackingNumber());
        delivery.setStatus(Delivery.STATUS_PROCESSING);
        delivery.setDeliveryAddress(deliveryDTO.getDeliveryAddress());
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

        if (Delivery.STATUS_DELIVERED.equals(status)) {
            delivery.setActualDelivery(LocalDateTime.now());
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

        return deliveryRepository.save(delivery);
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
}
