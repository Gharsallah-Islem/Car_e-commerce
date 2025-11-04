package com.example.Backend.service.impl;

import com.example.Backend.dto.VehicleDTO;
import com.example.Backend.entity.User;
import com.example.Backend.entity.Vehicle;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.repository.VehicleRepository;
import com.example.Backend.service.VehicleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Override
    public Vehicle addVehicle(UUID userId, VehicleDTO vehicleDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Vehicle vehicle = new Vehicle();
        vehicle.setUser(user);
        vehicle.setBrand(vehicleDTO.getBrand());
        vehicle.setModel(vehicleDTO.getModel());
        vehicle.setYear(vehicleDTO.getYear());

        return vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public Vehicle getVehicleById(UUID vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + vehicleId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> getUserVehicles(UUID userId) {
        return vehicleRepository.findByUserId(userId);
    }

    @Override
    public Vehicle updateVehicle(UUID vehicleId, VehicleDTO vehicleDTO) {
        Vehicle vehicle = getVehicleById(vehicleId);

        vehicle.setBrand(vehicleDTO.getBrand());
        vehicle.setModel(vehicleDTO.getModel());
        vehicle.setYear(vehicleDTO.getYear());

        return vehicleRepository.save(vehicle);
    }

    @Override
    public void deleteVehicle(UUID vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new EntityNotFoundException("Vehicle not found with id: " + vehicleId);
        }
        vehicleRepository.deleteById(vehicleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> getVehiclesByBrand(String brand) {
        return vehicleRepository.findByBrand(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> getVehiclesByModel(String model) {
        return vehicleRepository.findByBrandAndModel(null, model);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> searchVehicles(String brand, String model, Integer minYear, Integer maxYear) {
        // Use the search term method from repository
        if (brand != null && !brand.isEmpty()) {
            return vehicleRepository.searchVehicles(brand);
        } else if (model != null && !model.isEmpty()) {
            return vehicleRepository.searchVehicles(model);
        } else {
            return vehicleRepository.findAll();
        }
    }
}
