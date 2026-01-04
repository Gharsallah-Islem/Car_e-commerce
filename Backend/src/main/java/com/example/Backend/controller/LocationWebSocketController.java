package com.example.Backend.controller;

import com.example.Backend.dto.LocationUpdateDTO;
import com.example.Backend.entity.Driver;
import com.example.Backend.entity.DriverLocation;
import com.example.Backend.service.DriverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

/**
 * WebSocket controller for real-time location updates
 * Handles driver location streaming to customers tracking deliveries
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class LocationWebSocketController {

    private final DriverService driverService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle driver location update via WebSocket
     * Driver sends: /app/driver/{driverId}/location
     * Broadcast to: /topic/delivery/{deliveryId}/location
     */
    @MessageMapping("/driver/{driverId}/location")
    public void updateDriverLocation(
            @DestinationVariable String driverId,
            @Payload LocationUpdateDTO locationDTO) {

        try {
            UUID driverUuid = UUID.fromString(driverId);
            Driver driver = driverService.updateLocation(driverUuid, locationDTO);

            // If driver has active delivery, broadcast to that delivery's topic
            if (driver.getCurrentDelivery() != null) {
                String deliveryId = driver.getCurrentDelivery().getId().toString();

                // Create location message
                LocationBroadcast broadcast = new LocationBroadcast(
                        driver.getId().toString(),
                        locationDTO.getLatitude(),
                        locationDTO.getLongitude(),
                        locationDTO.getSpeed(),
                        locationDTO.getHeading(),
                        driver.getUser().getFullName());

                // Broadcast to all subscribers of this delivery
                messagingTemplate.convertAndSend(
                        "/topic/delivery/" + deliveryId + "/location",
                        broadcast);

                log.debug("Broadcast location for delivery {} from driver {}",
                        deliveryId, driverId);
            }
        } catch (Exception e) {
            log.error("Error updating driver location via WebSocket: {}", e.getMessage());
        }
    }

    /**
     * Handle delivery status update broadcast
     * Admin/Driver sends: /app/delivery/{deliveryId}/status
     * Broadcast to: /topic/delivery/{deliveryId}/status
     */
    @MessageMapping("/delivery/{deliveryId}/status")
    @SendTo("/topic/delivery/{deliveryId}/status")
    public DeliveryStatusBroadcast broadcastDeliveryStatus(
            @DestinationVariable String deliveryId,
            @Payload DeliveryStatusBroadcast statusUpdate) {

        log.info("Broadcasting status update for delivery {}: {}",
                deliveryId, statusUpdate.getStatus());
        return statusUpdate;
    }

    /**
     * Notify driver of new assignment
     * Call this from service when assigning delivery
     */
    public void notifyDriverAssignment(String driverId, String deliveryId, String orderDetails) {
        messagingTemplate.convertAndSend(
                "/topic/driver/" + driverId + "/assignment",
                new DriverAssignmentNotification(deliveryId, orderDetails));
    }

    // ==================== DTOs for WebSocket Messages ====================

    public record LocationBroadcast(
            String driverId,
            Double latitude,
            Double longitude,
            Double speed,
            Double heading,
            String driverName) {
    }

    public record DeliveryStatusBroadcast(
            String deliveryId,
            String status,
            String message,
            Long timestamp) {
        public String getStatus() {
            return status;
        }
    }

    public record DriverAssignmentNotification(
            String deliveryId,
            String orderDetails) {
    }
}
