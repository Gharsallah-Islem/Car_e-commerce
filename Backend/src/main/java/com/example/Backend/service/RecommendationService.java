package com.example.Backend.service;

import com.example.Backend.entity.Product;
import com.example.Backend.entity.Recommendation;
import com.example.Backend.entity.RecommendationType;

import java.util.List;
import java.util.UUID;

/**
 * Service for AI-powered product recommendations.
 * Provides personalized, similar, collaborative, and trending recommendations.
 */
public interface RecommendationService {

    /**
     * Get personalized recommendations for a user based on their behavior
     *
     * @param userId User's UUID
     * @param limit  Maximum recommendations to return
     * @return List of recommended products
     */
    List<Product> getPersonalizedRecommendations(UUID userId, int limit);

    /**
     * Get products similar to the given product (content-based filtering)
     *
     * @param productId Source product UUID
     * @param limit     Maximum recommendations to return
     * @return List of similar products
     */
    List<Product> getSimilarProducts(UUID productId, int limit);

    /**
     * Get products frequently bought together (collaborative filtering)
     *
     * @param productId Source product UUID
     * @param limit     Maximum recommendations to return
     * @return List of products bought together
     */
    List<Product> getAlsoBoughtProducts(UUID productId, int limit);

    /**
     * Get currently trending products
     *
     * @param days  Number of days to look back
     * @param limit Maximum recommendations to return
     * @return List of trending products
     */
    List<Product> getTrendingProducts(int days, int limit);

    /**
     * Save a recommendation for a user
     *
     * @param userId    User UUID
     * @param productId Product UUID
     * @param type      Recommendation type
     * @param score     Relevance score
     * @param reason    Human-readable reason
     * @return Created recommendation
     */
    Recommendation saveRecommendation(UUID userId, UUID productId,
            RecommendationType type, Double score, String reason);

    /**
     * Get user's saved recommendations
     *
     * @param userId User UUID
     * @param limit  Maximum to return
     * @return List of recommendations
     */
    List<Recommendation> getUserRecommendations(UUID userId, int limit);

    /**
     * Mark a recommendation as viewed
     *
     * @param recommendationId Recommendation UUID
     */
    void markAsViewed(UUID recommendationId);
}
