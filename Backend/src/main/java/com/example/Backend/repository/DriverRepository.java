package com.example.Backend.repository;

import com.example.Backend.entity.Driver;
import com.example.Backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {

    // Find by user
    Optional<Driver> findByUser(User user);

    Optional<Driver> findByUserId(UUID userId);

    // Find available drivers
    List<Driver> findByIsAvailableTrueAndIsVerifiedTrueAndIsActiveTrue();

    // Find available drivers without current delivery
    @Query("SELECT d FROM Driver d WHERE d.isAvailable = true AND d.isVerified = true " +
            "AND d.isActive = true AND d.currentDelivery IS NULL")
    List<Driver> findAvailableDrivers();

    // Find by verification status
    Page<Driver> findByIsVerified(Boolean isVerified, Pageable pageable);

    // Find by vehicle type
    List<Driver> findByVehicleTypeAndIsAvailableTrueAndIsVerifiedTrue(String vehicleType);

    // Find drivers near a location (basic query - for advanced geo queries use
    // PostGIS)
    @Query("SELECT d FROM Driver d WHERE d.isAvailable = true AND d.isVerified = true " +
            "AND d.isActive = true AND d.currentDelivery IS NULL " +
            "AND d.currentLatitude IS NOT NULL AND d.currentLongitude IS NOT NULL " +
            "AND d.currentLatitude BETWEEN :minLat AND :maxLat " +
            "AND d.currentLongitude BETWEEN :minLon AND :maxLon")
    List<Driver> findDriversInArea(@Param("minLat") Double minLat, @Param("maxLat") Double maxLat,
            @Param("minLon") Double minLon, @Param("maxLon") Double maxLon);

    // Find by rating
    Page<Driver> findByRatingGreaterThanEqual(Double minRating, Pageable pageable);

    // Count by status
    long countByIsAvailableTrue();

    long countByIsVerifiedFalse();

    long countByCurrentDeliveryIsNotNull();

    // Check if user is already a driver
    boolean existsByUserId(UUID userId);

    // Search drivers
    @Query("SELECT d FROM Driver d WHERE " +
            "LOWER(d.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.vehiclePlate) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Driver> searchDrivers(@Param("search") String search, Pageable pageable);
}
