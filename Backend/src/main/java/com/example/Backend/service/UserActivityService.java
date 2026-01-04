package com.example.Backend.service;

import com.example.Backend.dto.UserActivityDTO;
import com.example.Backend.entity.ActivityType;
import com.example.Backend.entity.UserActivity;

import java.util.List;
import java.util.UUID;

/**
 * Service for tracking and querying user activities for recommendations
 */
public interface UserActivityService {

    /**
     * Track a user activity (view, cart add, purchase, search)
     * 
     * @param userId      User ID
     * @param activityDTO Activity details
     * @return Created activity
     */
    UserActivity trackActivity(UUID userId, UserActivityDTO activityDTO);

    /**
     * Track product view
     * 
     * @param userId    User ID
     * @param productId Product ID
     * @param sessionId Optional session ID
     * @return Created activity
     */
    UserActivity trackProductView(UUID userId, UUID productId, String sessionId);

    /**
     * Track add to cart action
     * 
     * @param userId    User ID
     * @param productId Product ID
     * @return Created activity
     */
    UserActivity trackAddToCart(UUID userId, UUID productId);

    /**
     * Track purchase
     * 
     * @param userId     User ID
     * @param productIds List of purchased product IDs
     * @return List of created activities
     */
    List<UserActivity> trackPurchase(UUID userId, List<UUID> productIds);

    /**
     * Track search query
     * 
     * @param userId      User ID
     * @param searchQuery Search query
     * @return Created activity
     */
    UserActivity trackSearch(UUID userId, String searchQuery);

    /**
     * Get user's recently viewed products
     * 
     * @param userId User ID
     * @param limit  Max results
     * @return List of product IDs
     */
    List<UUID> getRecentlyViewedProducts(UUID userId, int limit);

    /**
     * Get user's category preferences
     * 
     * @param userId User ID
     * @return Map of category ID to activity count
     */
    List<Object[]> getUserCategoryPreferences(UUID userId);

    /**
     * Get trending product IDs
     * 
     * @param days  Number of days to look back
     * @param limit Max results
     * @return List of {productId, viewCount} pairs
     */
    List<Object[]> getTrendingProducts(int days, int limit);

    /**
     * Get products frequently bought together
     * 
     * @param productId Product ID
     * @param limit     Max results
     * @return List of {productId, count} pairs
     */
    List<Object[]> getAlsoBoughtProducts(UUID productId, int limit);

    /**
     * Get user's activity count by type
     * 
     * @param userId       User ID
     * @param activityType Activity type
     * @return Count
     */
    Long getActivityCount(UUID userId, ActivityType activityType);
}
