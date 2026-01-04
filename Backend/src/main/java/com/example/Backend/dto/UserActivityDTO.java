package com.example.Backend.dto;

import com.example.Backend.entity.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for tracking user activity events
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityDTO {

    private UUID productId;

    private ActivityType activityType;

    private String searchQuery;

    private Long categoryId;

    private String sessionId;

    private String metadata; // JSON for additional context
}
