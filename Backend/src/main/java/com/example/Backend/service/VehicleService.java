package com.example.Backend.service;

import com.example.Backend.dto.VehicleDTO;
import com.example.Backend.entity.Vehicle;

import java.util.List;
import java.util.UUID;

public interface VehicleService {

    /**
     * Add vehicle for user
     * 
     * @param userId     User ID
     * @param vehicleDTO Vehicle data
     * @return Created vehicle
     */
    Vehicle addVehicle(UUID userId, VehicleDTO vehicleDTO);

    /**
     * Get vehicle by ID
     * 
     * @param vehicleId Vehicle ID
     * @return Vehicle entity
     */
    Vehicle getVehicleById(UUID vehicleId);

    /**
     * Get all vehicles for user
     * 
     * @param userId User ID
     * @return List of user's vehicles
     */
    List<Vehicle> getUserVehicles(UUID userId);

    /**
     * Update vehicle
     * 
     * @param vehicleId  Vehicle ID
     * @param vehicleDTO Updated vehicle data
     * @return Updated vehicle
     */
    Vehicle updateVehicle(UUID vehicleId, VehicleDTO vehicleDTO);

    /**
     * Delete vehicle
     * 
     * @param vehicleId Vehicle ID
     */
    void deleteVehicle(UUID vehicleId);

    /**
     * Get vehicles by brand
     * 
     * @param brand Vehicle brand
     * @return List of vehicles
     */
    List<Vehicle> getVehiclesByBrand(String brand);

    /**
     * Get vehicles by model
     * 
     * @param model Vehicle model
     * @return List of vehicles
     */
    List<Vehicle> getVehiclesByModel(String model);

    /**
     * Search vehicles
     * 
     * @param brand   Brand (optional)
     * @param model   Model (optional)
     * @param minYear Minimum year (optional)
     * @param maxYear Maximum year (optional)
     * @return List of matching vehicles
     */
    List<Vehicle> searchVehicles(String brand, String model, Integer minYear, Integer maxYear);
}
