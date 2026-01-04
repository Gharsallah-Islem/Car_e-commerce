package com.example.Backend.controller;

import com.example.Backend.dto.DriverDTO;
import com.example.Backend.dto.LocationUpdateDTO;
import com.example.Backend.entity.Driver;
import com.example.Backend.entity.DriverLocation;
import com.example.Backend.security.UserPrincipal;
import com.example.Backend.service.DriverService;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    // ==================== DRIVER REGISTRATION ====================

    /**
     * Register as a driver
     * POST /api/drivers
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Driver> registerAsDriver(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody DriverDTO driverDTO) {

        driverDTO.setUserId(currentUser.getId());
        Driver driver = driverService.createDriver(driverDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(driver);
    }

    /**
     * Get current driver profile
     * GET /api/drivers/me
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Driver> getCurrentDriver(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Driver driver = driverService.getDriverByUserId(currentUser.getId());
        return ResponseEntity.ok(driver);
    }

    /**
     * Update current driver profile
     * PUT /api/drivers/me
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Driver> updateCurrentDriver(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody DriverDTO driverDTO) {

        Driver driver = driverService.getDriverByUserId(currentUser.getId());
        Driver updated = driverService.updateDriver(driver.getId(), driverDTO);
        return ResponseEntity.ok(updated);
    }

    // ==================== AVAILABILITY ====================

    /**
     * Toggle driver availability
     * POST /api/drivers/me/toggle-availability
     */
    @PostMapping("/me/toggle-availability")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Driver> toggleAvailability(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Driver driver = driverService.getDriverByUserId(currentUser.getId());
        Driver updated = driverService.toggleAvailability(driver.getId());
        return ResponseEntity.ok(updated);
    }

    /**
     * Go online
     * POST /api/drivers/me/online
     */
    @PostMapping("/me/online")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Driver> goOnline(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Driver driver = driverService.getDriverByUserId(currentUser.getId());
        Driver updated = driverService.goOnline(driver.getId());
        return ResponseEntity.ok(updated);
    }

    /**
     * Go offline
     * POST /api/drivers/me/offline
     */
    @PostMapping("/me/offline")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Driver> goOffline(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Driver driver = driverService.getDriverByUserId(currentUser.getId());
        Driver updated = driverService.goOffline(driver.getId());
        return ResponseEntity.ok(updated);
    }

    // ==================== LOCATION ====================

    /**
     * Update driver location
     * POST /api/drivers/me/location
     */
    @PostMapping("/me/location")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Driver> updateLocation(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody LocationUpdateDTO locationDTO) {

        Driver driver = driverService.getDriverByUserId(currentUser.getId());
        Driver updated = driverService.updateLocation(driver.getId(), locationDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get location history
     * GET /api/drivers/me/locations
     */
    @GetMapping("/me/locations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DriverLocation>> getLocationHistory(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "100") int limit) {

        Driver driver = driverService.getDriverByUserId(currentUser.getId());
        List<DriverLocation> history = driverService.getLocationHistory(driver.getId(), limit);
        return ResponseEntity.ok(history);
    }

    // ==================== DELIVERY ACTIONS ====================

    /**
     * Complete current delivery
     * POST /api/drivers/me/complete-delivery
     */
    @PostMapping("/me/complete-delivery")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Driver> completeDelivery(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Driver driver = driverService.getDriverByUserId(currentUser.getId());
        Driver updated = driverService.completeDelivery(driver.getId());
        return ResponseEntity.ok(updated);
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Get all drivers (ADMIN)
     * GET /api/drivers
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Driver>> getAllDrivers(
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Driver> drivers = driverService.getAllDrivers(pageable);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get driver by ID (ADMIN)
     * GET /api/drivers/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Driver> getDriverById(@PathVariable UUID id) {
        Driver driver = driverService.getDriverById(id);
        return ResponseEntity.ok(driver);
    }

    /**
     * Get available drivers (ADMIN)
     * GET /api/drivers/available
     */
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<Driver>> getAvailableDrivers() {
        List<Driver> drivers = driverService.getAvailableDrivers();
        return ResponseEntity.ok(drivers);
    }

    /**
     * Find nearest driver (ADMIN)
     * GET /api/drivers/nearest
     */
    @GetMapping("/nearest")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Driver> findNearestDriver(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {

        Driver driver = driverService.findNearestDriver(latitude, longitude);
        return ResponseEntity.ok(driver);
    }

    /**
     * Find drivers nearby (ADMIN)
     * GET /api/drivers/nearby
     */
    @GetMapping("/nearby")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<Driver>> findDriversNearby(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") Double radiusKm) {

        List<Driver> drivers = driverService.findDriversNearby(latitude, longitude, radiusKm);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Assign delivery to driver (ADMIN)
     * POST /api/drivers/{id}/assign
     */
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Driver> assignDelivery(
            @PathVariable UUID id,
            @RequestBody Map<String, UUID> body) {

        UUID deliveryId = body.get("deliveryId");
        if (deliveryId == null) {
            return ResponseEntity.badRequest().build();
        }

        Driver driver = driverService.assignDelivery(id, deliveryId);
        return ResponseEntity.ok(driver);
    }

    /**
     * Unassign delivery from driver (ADMIN)
     * POST /api/drivers/{id}/unassign
     */
    @PostMapping("/{id}/unassign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Driver> unassignDelivery(@PathVariable UUID id) {
        Driver driver = driverService.unassignDelivery(id);
        return ResponseEntity.ok(driver);
    }

    /**
     * Verify driver (ADMIN)
     * POST /api/drivers/{id}/verify
     */
    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Driver> verifyDriver(@PathVariable UUID id) {
        Driver driver = driverService.verifyDriver(id);
        return ResponseEntity.ok(driver);
    }

    /**
     * Suspend driver (ADMIN)
     * POST /api/drivers/{id}/suspend
     */
    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Driver> suspendDriver(@PathVariable UUID id) {
        Driver driver = driverService.suspendDriver(id);
        return ResponseEntity.ok(driver);
    }

    /**
     * Reactivate driver (ADMIN)
     * POST /api/drivers/{id}/reactivate
     */
    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Driver> reactivateDriver(@PathVariable UUID id) {
        Driver driver = driverService.reactivateDriver(id);
        return ResponseEntity.ok(driver);
    }

    /**
     * Get unverified drivers (ADMIN)
     * GET /api/drivers/unverified
     */
    @GetMapping("/unverified")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Driver>> getUnverifiedDrivers(
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Driver> drivers = driverService.getUnverifiedDrivers(pageable);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Search drivers (ADMIN)
     * GET /api/drivers/search
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Driver>> searchDrivers(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Driver> drivers = driverService.searchDrivers(q, pageable);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get driver statistics (ADMIN)
     * GET /api/drivers/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = driverService.getDriverStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get delivery location history (ADMIN)
     * GET /api/drivers/delivery/{deliveryId}/locations
     */
    @GetMapping("/delivery/{deliveryId}/locations")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<DriverLocation>> getDeliveryLocations(
            @PathVariable UUID deliveryId) {

        List<DriverLocation> history = driverService.getDeliveryLocationHistory(deliveryId);
        return ResponseEntity.ok(history);
    }
}
