package com.example.Backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverDTO {

    private UUID id;

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;

    private String vehiclePlate;
    private String vehicleModel;
    private String licenseNumber;

    // Read-only fields (from entity)
    private Boolean isAvailable;
    private Boolean isVerified;
    private Boolean isActive;
    private Double rating;
    private Integer completedDeliveries;
    private Double currentLatitude;
    private Double currentLongitude;

    // User info (populated from User entity)
    private String driverName;
    private String driverEmail;
    private String driverPhone;
    private String profilePicture;
}
