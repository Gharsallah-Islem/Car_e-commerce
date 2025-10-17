package com.example.Backend.controller;

import com.example.Backend.dto.VehicleDTO;
import com.example.Backend.entity.Vehicle;
import com.example.Backend.security.UserPrincipal;
import com.example.Backend.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Vehicle management
 * Handles user's vehicle garage operations
 */
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class VehicleController {

    private final VehicleService vehicleService;

    /**
     * Get current user's vehicles
     * GET /api/vehicles
     * Security: CLIENT role required
     */
    @GetMapping
    public ResponseEntity<List<Vehicle>> getMyVehicles(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<Vehicle> vehicles = vehicleService.getUserVehicles(currentUser.getId());
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Get vehicle by ID
     * GET /api/vehicles/{id}
     * Security: CLIENT role required (own vehicles only)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id) {
        Vehicle vehicle = vehicleService.getVehicleById(id);

        // Verify vehicle belongs to current user
        if (!vehicle.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(vehicle);
    }

    /**
     * Add new vehicle to garage
     * POST /api/vehicles
     * Body: { "brand": "Toyota", "model": "Corolla", "year": 2020, "vin": "...",
     * "licensePlate": "..." }
     * Security: CLIENT role required
     */
    @PostMapping
    public ResponseEntity<Vehicle> addVehicle(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody VehicleDTO vehicleDTO) {
        Vehicle vehicle = vehicleService.addVehicle(currentUser.getId(), vehicleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicle);
    }

    /**
     * Update vehicle
     * PUT /api/vehicles/{id}
     * Security: CLIENT role required (own vehicles only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody VehicleDTO vehicleDTO) {

        // Verify vehicle belongs to current user
        Vehicle existingVehicle = vehicleService.getVehicleById(id);
        if (!existingVehicle.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Vehicle vehicle = vehicleService.updateVehicle(id, vehicleDTO);
        return ResponseEntity.ok(vehicle);
    }

    /**
     * Delete vehicle from garage
     * DELETE /api/vehicles/{id}
     * Security: CLIENT role required (own vehicles only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id) {

        // Verify vehicle belongs to current user
        Vehicle vehicle = vehicleService.getVehicleById(id);
        if (!vehicle.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(Map.of("message", "Vehicle deleted successfully"));
    }

    /**
     * Search compatible vehicles (for product filtering)
     * GET /api/vehicles/search?brand=Toyota&model=Corolla&minYear=2015&maxYear=2023
     * Security: CLIENT role required
     */
    @GetMapping("/search")
    public ResponseEntity<List<Vehicle>> searchVehicles(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) Integer maxYear) {

        List<Vehicle> vehicles = vehicleService.searchVehicles(brand, model, minYear, maxYear);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Get vehicles by brand
     * GET /api/vehicles/brand/{brand}
     * Security: CLIENT role required
     */
    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<Vehicle>> getVehiclesByBrand(@PathVariable String brand) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByBrand(brand);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Get vehicles by model
     * GET /api/vehicles/model/{model}
     * Security: CLIENT role required
     */
    @GetMapping("/model/{model}")
    public ResponseEntity<List<Vehicle>> getVehiclesByModel(@PathVariable String model) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByModel(model);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Get vehicle count for current user
     * GET /api/vehicles/count
     * Security: CLIENT role required
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getVehicleCount(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<Vehicle> vehicles = vehicleService.getUserVehicles(currentUser.getId());
        return ResponseEntity.ok(Map.of("count", vehicles.size()));
    }
}
