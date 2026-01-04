package com.example.Backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateDTO {

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private Double speed;
    private Double heading;
    private Double accuracy;
    private Double altitude;

    // Optional: the delivery being tracked
    private UUID deliveryId;

    // Client timestamp (for latency calculation)
    private LocalDateTime clientTimestamp;
}
