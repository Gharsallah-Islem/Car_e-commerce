package com.example.Backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Driver entity - represents delivery drivers in the system
 * Linked to User for authentication, contains vehicle and location info
 */
@Entity
@Table(name = "drivers", indexes = {
        @Index(name = "idx_drivers_user_id", columnList = "user_id"),
        @Index(name = "idx_drivers_is_available", columnList = "is_available"),
        @Index(name = "idx_drivers_location", columnList = "current_latitude, current_longitude")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnoreProperties({ "password", "vehicles", "cart", "orders", "reclamations",
            "emailVerificationToken", "passwordResetToken", "conversations", "recommendations" })
    private User user;

    // Vehicle Information
    @NotNull(message = "Vehicle type is required")
    @Column(name = "vehicle_type", nullable = false, length = 50)
    private String vehicleType; // CAR, MOTORCYCLE, BICYCLE, VAN

    @Column(name = "vehicle_plate", length = 20)
    private String vehiclePlate;

    @Column(name = "vehicle_model", length = 100)
    private String vehicleModel;

    @Column(name = "license_number", length = 50)
    private String licenseNumber;

    // Availability Status
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = false;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Performance Metrics
    @Column(name = "rating", precision = 3)
    private Double rating = 5.0;

    @Column(name = "completed_deliveries")
    private Integer completedDeliveries = 0;

    @Column(name = "cancelled_deliveries")
    private Integer cancelledDeliveries = 0;

    // Current Location (GPS)
    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "current_speed")
    private Double currentSpeed;

    @Column(name = "current_heading")
    private Double currentHeading;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    // Current Assignment
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_delivery_id")
    @JsonIgnore
    private Delivery currentDelivery;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Vehicle type constants
    public static final String VEHICLE_CAR = "CAR";
    public static final String VEHICLE_MOTORCYCLE = "MOTORCYCLE";
    public static final String VEHICLE_BICYCLE = "BICYCLE";
    public static final String VEHICLE_VAN = "VAN";

    // Helper methods
    public boolean isOnline() {
        return isAvailable && isActive && isVerified;
    }

    public boolean hasCurrentDelivery() {
        return currentDelivery != null;
    }

    public void updateLocation(Double latitude, Double longitude, Double speed, Double heading) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        this.currentSpeed = speed;
        this.currentHeading = heading;
        this.lastLocationUpdate = LocalDateTime.now();
    }

    public void goOnline() {
        this.isAvailable = true;
    }

    public void goOffline() {
        this.isAvailable = false;
    }

    public void completeDelivery() {
        this.completedDeliveries++;
        this.currentDelivery = null;
    }

    /**
     * Calculate distance to a point in kilometers using Haversine formula
     */
    public Double distanceTo(Double latitude, Double longitude) {
        if (currentLatitude == null || currentLongitude == null) {
            return null;
        }

        final int EARTH_RADIUS = 6371; // km

        double latDistance = Math.toRadians(latitude - currentLatitude);
        double lonDistance = Math.toRadians(longitude - currentLongitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(currentLatitude)) * Math.cos(Math.toRadians(latitude))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
