package com.example.Backend.service.impl;

import com.example.Backend.dto.DriverDTO;
import com.example.Backend.dto.LocationUpdateDTO;
import com.example.Backend.entity.*;
import com.example.Backend.repository.DeliveryRepository;
import com.example.Backend.repository.DriverLocationRepository;
import com.example.Backend.repository.DriverRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.DriverService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final DriverLocationRepository locationRepository;
    private final UserRepository userRepository;
    private final DeliveryRepository deliveryRepository;

    // ==================== DRIVER CRUD ====================

    @Override
    public Driver createDriver(DriverDTO driverDTO) {
        // Check if user exists
        User user = userRepository.findById(driverDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Check if already a driver
        if (driverRepository.existsByUserId(driverDTO.getUserId())) {
            throw new IllegalStateException("User is already registered as a driver");
        }

        Driver driver = new Driver();
        driver.setUser(user);
        driver.setVehicleType(driverDTO.getVehicleType());
        driver.setVehiclePlate(driverDTO.getVehiclePlate());
        driver.setVehicleModel(driverDTO.getVehicleModel());
        driver.setLicenseNumber(driverDTO.getLicenseNumber());
        driver.setIsAvailable(false);
        driver.setIsVerified(false);
        driver.setIsActive(true);
        driver.setRating(5.0);
        driver.setCompletedDeliveries(0);
        driver.setCancelledDeliveries(0);

        log.info("Creating driver profile for user: {}", user.getEmail());
        return driverRepository.save(driver);
    }

    @Override
    @Transactional(readOnly = true)
    public Driver getDriverById(UUID driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found with ID: " + driverId));
    }

    @Override
    @Transactional(readOnly = true)
    public Driver getDriverByUserId(UUID userId) {
        return driverRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found for user: " + userId));
    }

    @Override
    public Driver updateDriver(UUID driverId, DriverDTO driverDTO) {
        Driver driver = getDriverById(driverId);

        if (driverDTO.getVehicleType() != null) {
            driver.setVehicleType(driverDTO.getVehicleType());
        }
        if (driverDTO.getVehiclePlate() != null) {
            driver.setVehiclePlate(driverDTO.getVehiclePlate());
        }
        if (driverDTO.getVehicleModel() != null) {
            driver.setVehicleModel(driverDTO.getVehicleModel());
        }
        if (driverDTO.getLicenseNumber() != null) {
            driver.setLicenseNumber(driverDTO.getLicenseNumber());
        }

        return driverRepository.save(driver);
    }

    @Override
    public void deleteDriver(UUID driverId) {
        if (!driverRepository.existsById(driverId)) {
            throw new EntityNotFoundException("Driver not found");
        }
        driverRepository.deleteById(driverId);
    }

    // ==================== AVAILABILITY ====================

    @Override
    public Driver toggleAvailability(UUID driverId) {
        Driver driver = getDriverById(driverId);
        driver.setIsAvailable(!driver.getIsAvailable());
        log.info("Driver {} is now {}", driverId, driver.getIsAvailable() ? "ONLINE" : "OFFLINE");
        return driverRepository.save(driver);
    }

    @Override
    public Driver goOnline(UUID driverId) {
        Driver driver = getDriverById(driverId);
        if (!driver.getIsVerified()) {
            throw new IllegalStateException("Driver must be verified to go online");
        }
        driver.setIsAvailable(true);
        log.info("Driver {} is now ONLINE", driverId);
        return driverRepository.save(driver);
    }

    @Override
    public Driver goOffline(UUID driverId) {
        Driver driver = getDriverById(driverId);
        driver.setIsAvailable(false);
        log.info("Driver {} is now OFFLINE", driverId);
        return driverRepository.save(driver);
    }

    // ==================== LOCATION ====================

    @Override
    public Driver updateLocation(UUID driverId, LocationUpdateDTO locationDTO) {
        Driver driver = getDriverById(driverId);

        // Update driver's current location
        driver.updateLocation(
                locationDTO.getLatitude(),
                locationDTO.getLongitude(),
                locationDTO.getSpeed(),
                locationDTO.getHeading());

        // Save location history
        DriverLocation location = new DriverLocation();
        location.setDriver(driver);
        location.setLatitude(locationDTO.getLatitude());
        location.setLongitude(locationDTO.getLongitude());
        location.setSpeed(locationDTO.getSpeed());
        location.setHeading(locationDTO.getHeading());
        location.setAccuracy(locationDTO.getAccuracy());
        location.setAltitude(locationDTO.getAltitude());

        if (locationDTO.getDeliveryId() != null) {
            deliveryRepository.findById(locationDTO.getDeliveryId())
                    .ifPresent(location::setDelivery);
        } else if (driver.getCurrentDelivery() != null) {
            location.setDelivery(driver.getCurrentDelivery());
        }

        locationRepository.save(location);
        return driverRepository.save(driver);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverLocation> getLocationHistory(UUID driverId, int limit) {
        return locationRepository.findByDriverIdOrderByTimestampDesc(driverId)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverLocation> getDeliveryLocationHistory(UUID deliveryId) {
        return locationRepository.findByDeliveryIdOrderByTimestampAsc(deliveryId);
    }

    // ==================== DRIVER ASSIGNMENT ====================

    @Override
    @Transactional(readOnly = true)
    public Driver findNearestDriver(Double latitude, Double longitude) {
        List<Driver> availableDrivers = driverRepository.findAvailableDrivers();

        if (availableDrivers.isEmpty()) {
            throw new EntityNotFoundException("No available drivers found");
        }

        // Filter drivers with valid location and calculate distances
        return availableDrivers.stream()
                .filter(d -> d.getCurrentLatitude() != null && d.getCurrentLongitude() != null)
                .min(Comparator.comparingDouble(d -> d.distanceTo(latitude, longitude)))
                .orElseThrow(() -> new EntityNotFoundException("No drivers with valid location found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Driver> findDriversNearby(Double latitude, Double longitude, Double radiusKm) {
        // Calculate bounding box (approximate)
        double latDelta = radiusKm / 111.0; // 1 degree ≈ 111 km
        double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)));

        List<Driver> driversInArea = driverRepository.findDriversInArea(
                latitude - latDelta, latitude + latDelta,
                longitude - lonDelta, longitude + lonDelta);

        // Filter by actual distance and sort
        return driversInArea.stream()
                .filter(d -> d.distanceTo(latitude, longitude) <= radiusKm)
                .sorted(Comparator.comparingDouble(d -> d.distanceTo(latitude, longitude)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Driver> getAvailableDrivers() {
        return driverRepository.findAvailableDrivers();
    }

    @Override
    public Driver assignDelivery(UUID driverId, UUID deliveryId) {
        Driver driver = getDriverById(driverId);
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found"));

        if (driver.getCurrentDelivery() != null) {
            throw new IllegalStateException("Driver already has an active delivery");
        }

        driver.setCurrentDelivery(delivery);
        driver.setIsAvailable(false);

        // Update delivery with driver info
        delivery.setDriverName(driver.getUser().getFullName());
        delivery.setDriverPhone(driver.getUser().getPhone());
        delivery.setStatus(Delivery.STATUS_IN_TRANSIT);
        deliveryRepository.save(delivery);

        log.info("Assigned delivery {} to driver {}", deliveryId, driverId);
        return driverRepository.save(driver);
    }

    @Override
    public Driver unassignDelivery(UUID driverId) {
        Driver driver = getDriverById(driverId);
        driver.setCurrentDelivery(null);
        driver.setIsAvailable(true);
        return driverRepository.save(driver);
    }

    @Override
    public Driver completeDelivery(UUID driverId) {
        Driver driver = getDriverById(driverId);

        if (driver.getCurrentDelivery() != null) {
            Delivery delivery = driver.getCurrentDelivery();
            delivery.markAsDelivered();
            deliveryRepository.save(delivery);
        }

        driver.completeDelivery();
        driver.setIsAvailable(true);

        log.info("Driver {} completed delivery", driverId);
        return driverRepository.save(driver);
    }

    // ==================== ADMIN QUERIES ====================

    @Override
    @Transactional(readOnly = true)
    public Page<Driver> getAllDrivers(Pageable pageable) {
        return driverRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Driver> getUnverifiedDrivers(Pageable pageable) {
        return driverRepository.findByIsVerified(false, pageable);
    }

    @Override
    public Driver verifyDriver(UUID driverId) {
        Driver driver = getDriverById(driverId);
        driver.setIsVerified(true);
        log.info("Driver {} verified", driverId);
        return driverRepository.save(driver);
    }

    @Override
    public Driver suspendDriver(UUID driverId) {
        Driver driver = getDriverById(driverId);
        driver.setIsActive(false);
        driver.setIsAvailable(false);
        log.info("Driver {} suspended", driverId);
        return driverRepository.save(driver);
    }

    @Override
    public Driver reactivateDriver(UUID driverId) {
        Driver driver = getDriverById(driverId);
        driver.setIsActive(true);
        log.info("Driver {} reactivated", driverId);
        return driverRepository.save(driver);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Driver> searchDrivers(String query, Pageable pageable) {
        return driverRepository.searchDrivers(query, pageable);
    }

    // ==================== STATISTICS ====================

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDriverStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDrivers", driverRepository.count());
        stats.put("availableDrivers", driverRepository.countByIsAvailableTrue());
        stats.put("pendingVerification", driverRepository.countByIsVerifiedFalse());
        stats.put("activeDeliveries", driverRepository.countByCurrentDeliveryIsNotNull());
        return stats;
    }

    @Override
    public Driver updateRating(UUID driverId, Double newRating) {
        Driver driver = getDriverById(driverId);

        // Calculate weighted average with existing rating
        int deliveries = driver.getCompletedDeliveries();
        if (deliveries > 0) {
            double currentTotal = driver.getRating() * deliveries;
            driver.setRating((currentTotal + newRating) / (deliveries + 1));
        } else {
            driver.setRating(newRating);
        }

        return driverRepository.save(driver);
    }
}
