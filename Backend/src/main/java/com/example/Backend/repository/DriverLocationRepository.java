package com.example.Backend.repository;

import com.example.Backend.entity.Driver;
import com.example.Backend.entity.DriverLocation;
import com.example.Backend.entity.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverLocationRepository extends JpaRepository<DriverLocation, UUID> {

    // Find latest location for a driver
    Optional<DriverLocation> findFirstByDriverOrderByTimestampDesc(Driver driver);

    Optional<DriverLocation> findFirstByDriverIdOrderByTimestampDesc(UUID driverId);

    // Find location history for a driver
    Page<DriverLocation> findByDriverOrderByTimestampDesc(Driver driver, Pageable pageable);

    List<DriverLocation> findByDriverIdOrderByTimestampDesc(UUID driverId);

    // Find location history for a delivery
    List<DriverLocation> findByDeliveryOrderByTimestampAsc(Delivery delivery);

    List<DriverLocation> findByDeliveryIdOrderByTimestampAsc(UUID deliveryId);

    // Find locations within time range
    @Query("SELECT dl FROM DriverLocation dl WHERE dl.driver.id = :driverId " +
            "AND dl.timestamp BETWEEN :start AND :end ORDER BY dl.timestamp ASC")
    List<DriverLocation> findByDriverAndTimeRange(@Param("driverId") UUID driverId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Find locations for delivery tracking
    @Query("SELECT dl FROM DriverLocation dl WHERE dl.delivery.id = :deliveryId " +
            "ORDER BY dl.timestamp DESC")
    List<DriverLocation> findByDeliveryForTracking(@Param("deliveryId") UUID deliveryId);

    // Delete old location history (for data retention)
    void deleteByTimestampBefore(LocalDateTime cutoff);

    // Count locations for a driver
    long countByDriverId(UUID driverId);

    // Get average speed for a delivery
    @Query("SELECT AVG(dl.speed) FROM DriverLocation dl WHERE dl.delivery.id = :deliveryId AND dl.speed IS NOT NULL")
    Double getAverageSpeedForDelivery(@Param("deliveryId") UUID deliveryId);
}
