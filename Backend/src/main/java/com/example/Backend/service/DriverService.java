package com.example.Backend.service;

import com.example.Backend.dto.DriverDTO;
import com.example.Backend.dto.LocationUpdateDTO;
import com.example.Backend.entity.Driver;
import com.example.Backend.entity.DriverLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DriverService {

    // ==================== DRIVER CRUD ====================

    /**
     * Create a new driver profile for a user
     */
    Driver createDriver(DriverDTO driverDTO);

    /**
     * Get driver by ID
     */
    Driver getDriverById(UUID driverId);

    /**
     * Get driver by user ID
     */
    Driver getDriverByUserId(UUID userId);

    /**
     * Update driver profile
     */
    Driver updateDriver(UUID driverId, DriverDTO driverDTO);

    /**
     * Delete driver profile
     */
    void deleteDriver(UUID driverId);

    // ==================== AVAILABILITY ====================

    /**
     * Toggle driver availability (online/offline)
     */
    Driver toggleAvailability(UUID driverId);

    /**
     * Set driver as available
     */
    Driver goOnline(UUID driverId);

    /**
     * Set driver as unavailable
     */
    Driver goOffline(UUID driverId);

    // ==================== LOCATION ====================

    /**
     * Update driver's current location
     */
    Driver updateLocation(UUID driverId, LocationUpdateDTO locationDTO);

    /**
     * Get driver's location history
     */
    List<DriverLocation> getLocationHistory(UUID driverId, int limit);

    /**
     * Get location history for a delivery
     */
    List<DriverLocation> getDeliveryLocationHistory(UUID deliveryId);

    // ==================== DRIVER ASSIGNMENT ====================

    /**
     * Find nearest available driver to a location
     */
    Driver findNearestDriver(Double latitude, Double longitude);

    /**
     * Find available drivers within a radius (km)
     */
    List<Driver> findDriversNearby(Double latitude, Double longitude, Double radiusKm);

    /**
     * Get all available drivers
     */
    List<Driver> getAvailableDrivers();

    /**
     * Assign delivery to driver
     */
    Driver assignDelivery(UUID driverId, UUID deliveryId);

    /**
     * Unassign delivery from driver
     */
    Driver unassignDelivery(UUID driverId);

    /**
     * Complete current delivery
     */
    Driver completeDelivery(UUID driverId);

    // ==================== ADMIN QUERIES ====================

    /**
     * Get all drivers (paginated)
     */
    Page<Driver> getAllDrivers(Pageable pageable);

    /**
     * Get unverified drivers
     */
    Page<Driver> getUnverifiedDrivers(Pageable pageable);

    /**
     * Verify driver
     */
    Driver verifyDriver(UUID driverId);

    /**
     * Suspend driver
     */
    Driver suspendDriver(UUID driverId);

    /**
     * Reactivate driver
     */
    Driver reactivateDriver(UUID driverId);

    /**
     * Search drivers
     */
    Page<Driver> searchDrivers(String query, Pageable pageable);

    // ==================== STATISTICS ====================

    /**
     * Get driver statistics
     */
    Map<String, Object> getDriverStatistics();

    /**
     * Update driver rating
     */
    Driver updateRating(UUID driverId, Double newRating);
}
