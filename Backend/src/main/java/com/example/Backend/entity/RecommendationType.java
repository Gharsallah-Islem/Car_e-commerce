package com.example.Backend.entity;

/**
 * Enum representing types of recommendations
 */
public enum RecommendationType {
    /** Personalized based on user behavior */
    PERSONALIZED,

    /** Similar products (content-based filtering) */
    SIMILAR,

    /** Frequently bought together (collaborative filtering) */
    ALSO_BOUGHT,

    /** Trending/popular products */
    TRENDING,

    /** Based on user's vehicle */
    VEHICLE_BASED,

    /** Manually created by admin */
    MANUAL
}
