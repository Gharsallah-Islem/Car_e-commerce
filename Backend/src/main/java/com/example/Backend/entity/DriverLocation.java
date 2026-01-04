package com.example.Backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DriverLocation entity - stores GPS location history for drivers
 * Used for tracking delivery routes and analytics
 */
@Entity
@Table(name = "driver_locations", indexes = {
        @Index(name = "idx_driver_locations_driver_id", columnList = "driver_id"),
        @Index(name = "idx_driver_locations_delivery_id", columnList = "delivery_id"),
        @Index(name = "idx_driver_locations_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "user", "currentDelivery" })
    private Driver driver;

    @NotNull(message = "Latitude is required")
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "speed")
    private Double speed; // km/h

    @Column(name = "heading")
    private Double heading; // degrees from north (0-360)

    @Column(name = "accuracy")
    private Double accuracy; // meters

    @Column(name = "altitude")
    private Double altitude; // meters

    // Optional: link to specific delivery being tracked
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "order" })
    private Delivery delivery;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // Constructor for quick creation
    public DriverLocation(Driver driver, Double latitude, Double longitude) {
        this.driver = driver;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor with full GPS data
    public DriverLocation(Driver driver, Double latitude, Double longitude,
            Double speed, Double heading, Delivery delivery) {
        this.driver = driver;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.heading = heading;
        this.delivery = delivery;
        this.timestamp = LocalDateTime.now();
    }
}
