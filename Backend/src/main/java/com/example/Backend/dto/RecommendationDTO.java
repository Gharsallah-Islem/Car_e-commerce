package com.example.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDTO {

    private UUID productId;

    private String recommendationType; // AI_BASED, MANUAL, VEHICLE_BASED

    private String reason;

    private Double score; // Recommendation confidence score
}
