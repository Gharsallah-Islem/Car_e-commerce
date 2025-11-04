package com.example.Backend.service;

import com.example.Backend.dto.RecommendationDTO;
import com.example.Backend.entity.Recommendation;

import java.util.List;
import java.util.UUID;

public interface IAService {

    /**
     * Get AI recommendations for user
     * 
     * @param userId User ID
     * @return List of recommendations
     */
    List<Recommendation> getUserRecommendations(UUID userId);

    /**
     * Generate recommendations based on user's vehicles and order history
     * 
     * @param userId User ID
     * @return List of generated recommendations
     */
    List<Recommendation> generateRecommendations(UUID userId);

    /**
     * Create manual recommendation
     * 
     * @param userId            User ID
     * @param recommendationDTO Recommendation data
     * @return Created recommendation
     */
    Recommendation createRecommendation(UUID userId, RecommendationDTO recommendationDTO);

    /**
     * Get recommendation by ID
     * 
     * @param recommendationId Recommendation ID
     * @return Recommendation entity
     */
    Recommendation getRecommendationById(UUID recommendationId);

    /**
     * Get active recommendations for user
     * 
     * @param userId User ID
     * @return List of active recommendations
     */
    List<Recommendation> getActiveRecommendations(UUID userId);

    /**
     * Mark recommendation as viewed
     * 
     * @param recommendationId Recommendation ID
     * @return Updated recommendation
     */
    Recommendation markAsViewed(UUID recommendationId);

    /**
     * Analyze image for part recognition (virtual mechanic)
     * 
     * @param imageData Base64 encoded image
     * @param userId    User ID
     * @return AI analysis result with suggested products
     */
    String analyzePartImage(String imageData, UUID userId);

    /**
     * Virtual mechanic chat
     * 
     * @param userId   User ID
     * @param question User's question
     * @return AI-generated response
     */
    String virtualMechanicChat(UUID userId, String question);
}
