package com.example.Backend.controller;

import com.example.Backend.dto.DeliveryDTO;
import com.example.Backend.entity.Delivery;
import com.example.Backend.security.UserPrincipal;
import com.example.Backend.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DeliveryController {

    private final DeliveryService deliveryService;

    /**
     * Create delivery for order (ADMIN only)
     * POST /api/delivery
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Delivery> createDelivery(
            @RequestParam UUID orderId,
            @Valid @RequestBody DeliveryDTO deliveryDTO) {

        Delivery delivery = deliveryService.createDelivery(orderId, deliveryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(delivery);
    }

    /**
     * Get delivery by ID
     * GET /api/delivery/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Delivery> getDelivery(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Delivery delivery = deliveryService.getDeliveryById(id);

        // Verify user owns the order or is staff
        boolean isOwner = delivery.getOrder().getUser().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().matches("ROLE_(ADMIN|SUPER_ADMIN)"));

        if (!isOwner && !isStaff) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(delivery);
    }

    /**
     * Get delivery by order ID
     * GET /api/delivery/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Delivery> getDeliveryByOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Delivery delivery = deliveryService.getDeliveryByOrderId(orderId);

        // Verify user owns the order or is staff
        boolean isOwner = delivery.getOrder().getUser().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().matches("ROLE_(ADMIN|SUPER_ADMIN)"));

        if (!isOwner && !isStaff) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(delivery);
    }

    /**
     * Track delivery by tracking number (public endpoint)
     * Auto-starts simulation for IN_TRANSIT deliveries (e.g., after backend
     * restart)
     * GET /api/delivery/track/{trackingNumber}
     */
    @GetMapping("/track/{trackingNumber}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Delivery> trackDelivery(@PathVariable String trackingNumber) {
        Delivery delivery = deliveryService.trackDelivery(trackingNumber);

        // Auto-start simulation for IN_TRANSIT deliveries if not already running
        if ("IN_TRANSIT".equals(delivery.getStatus()) || "OUT_FOR_DELIVERY".equals(delivery.getStatus())) {
            deliveryService.ensureSimulationRunning(delivery.getId());
        }

        return ResponseEntity.ok(delivery);
    }

    /**
     * Get delivery by tracking number
     * GET /api/delivery/tracking/{trackingNumber}
     */
    @GetMapping("/tracking/{trackingNumber}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Delivery> getDeliveryByTrackingNumber(@PathVariable String trackingNumber) {
        Delivery delivery = deliveryService.getDeliveryByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(delivery);
    }

    /**
     * Get all deliveries (ADMIN only)
     * GET /api/delivery
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Delivery>> getAllDeliveries(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<Delivery> deliveries = deliveryService.getAllDeliveries(pageable);
        return ResponseEntity.ok(deliveries);
    }

    /**
     * Get deliveries by status (ADMIN only)
     * GET /api/delivery/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Delivery>> getDeliveriesByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<Delivery> deliveries = deliveryService.getDeliveriesByStatus(status, pageable);
        return ResponseEntity.ok(deliveries);
    }

    /**
     * Get pending deliveries (ADMIN only)
     * GET /api/delivery/pending
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Delivery>> getPendingDeliveries(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<Delivery> deliveries = deliveryService.getPendingDeliveries(pageable);
        return ResponseEntity.ok(deliveries);
    }

    /**
     * Get active deliveries (in transit) (ADMIN only)
     * GET /api/delivery/active
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Delivery>> getActiveDeliveries(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<Delivery> deliveries = deliveryService.getActiveDeliveries(pageable);
        return ResponseEntity.ok(deliveries);
    }

    /**
     * Get deliveries by courier (ADMIN only)
     * GET /api/delivery/courier/{courierName}
     */
    @GetMapping("/courier/{courierName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Delivery>> getDeliveriesByCourier(
            @PathVariable String courierName,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<Delivery> deliveries = deliveryService.getDeliveriesByCourier(courierName, pageable);
        return ResponseEntity.ok(deliveries);
    }

    /**
     * Update delivery status (ADMIN only)
     * PATCH /api/delivery/{id}/status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Delivery> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> statusUpdate) {

        String status = statusUpdate.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Delivery delivery = deliveryService.updateStatus(id, status);
        return ResponseEntity.ok(delivery);
    }

    /**
     * Mark delivery as picked up (ADMIN only)
     * PATCH /api/delivery/{id}/picked-up
     */
    @PatchMapping("/{id}/picked-up")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Delivery> markAsPickedUp(
            @PathVariable UUID id,
            @RequestBody Map<String, String> courierData) {

        String courierName = courierData.get("courierName");
        if (courierName == null || courierName.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Delivery delivery = deliveryService.markAsPickedUp(id, courierName);
        return ResponseEntity.ok(delivery);
    }

    /**
     * Mark delivery as in transit (ADMIN only)
     * PATCH /api/delivery/{id}/in-transit
     */
    @PatchMapping("/{id}/in-transit")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Delivery> markAsInTransit(@PathVariable UUID id) {
        Delivery delivery = deliveryService.markAsInTransit(id);
        return ResponseEntity.ok(delivery);
    }

    /**
     * Mark delivery as delivered (ADMIN only)
     * PATCH /api/delivery/{id}/delivered
     */
    @PatchMapping("/{id}/delivered")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Delivery> markAsDelivered(@PathVariable UUID id) {
        Delivery delivery = deliveryService.markAsDelivered(id);
        return ResponseEntity.ok(delivery);
    }

    /**
     * Get delivery statistics (ADMIN only)
     * GET /api/delivery/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        Map<String, Long> statistics = deliveryService.getDeliveryStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get average delivery time (ADMIN only)
     * GET /api/delivery/average-time
     */
    @GetMapping("/average-time")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Double> getAverageDeliveryTime() {
        Double avgTime = deliveryService.getAverageDeliveryTime();
        return ResponseEntity.ok(avgTime);
    }

    /**
     * Migrate existing shipped orders - creates deliveries for orders that don't
     * have one
     * POST /api/delivery/migrate-shipped-orders
     */
    @PostMapping("/migrate-shipped-orders")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> migrateShippedOrders() {
        int count = deliveryService.createDeliveriesForShippedOrders();
        Map<String, Object> response = Map.of(
                "message", "Migration completed",
                "deliveriesCreated", count);
        return ResponseEntity.ok(response);
    }

    /**
     * Debug endpoint to check order statuses
     * GET /api/delivery/debug-orders
     */
    @GetMapping("/debug-orders")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> debugOrders() {
        Map<String, Object> debug = deliveryService.getDebugInfo();
        return ResponseEntity.ok(debug);
    }

    /**
     * Sync tracking numbers from deliveries to orders
     * POST /api/delivery/sync-tracking-numbers
     */
    @PostMapping("/sync-tracking-numbers")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> syncTrackingNumbers() {
        int count = deliveryService.syncTrackingNumbersToOrders();
        return ResponseEntity.ok(Map.of(
                "message", "Sync completed",
                "ordersSynced", count));
    }

    /**
     * Create deliveries for all confirmed orders without one
     * POST /api/delivery/sync-confirmed-orders
     */
    @PostMapping("/sync-confirmed-orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> syncConfirmedOrders() {
        int count = deliveryService.createDeliveriesForConfirmedOrders();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Créé " + count + " livraisons pour les commandes confirmées",
                "deliveriesCreated", count));
    }
}
