package com.example.Backend.repository;

import com.example.Backend.entity.ActivityType;
import com.example.Backend.entity.UserActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for UserActivity entity.
 * Provides queries for recommendation engine analytics.
 */
@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, UUID> {

        /**
         * Find recent activities by user
         */
        List<UserActivity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

        /**
         * Find activities by user and type
         */
        List<UserActivity> findByUserIdAndActivityTypeOrderByCreatedAtDesc(
                        UUID userId, ActivityType activityType, Pageable pageable);

        /**
         * Get recently viewed products by user (unique products, most recent first)
         */
        @Query("SELECT ua.product.id FROM UserActivity ua " +
                        "WHERE ua.user.id = :userId AND ua.activityType = 'VIEW' AND ua.product IS NOT NULL " +
                        "GROUP BY ua.product.id " +
                        "ORDER BY MAX(ua.createdAt) DESC")
        List<UUID> findRecentlyViewedProductIds(@Param("userId") UUID userId, Pageable pageable);

        /**
         * Get recently viewed products by user with full activity
         */
        @Query("SELECT ua FROM UserActivity ua " +
                        "WHERE ua.user.id = :userId AND ua.activityType = 'VIEW' AND ua.product IS NOT NULL " +
                        "ORDER BY ua.createdAt DESC")
        Page<UserActivity> findRecentViews(@Param("userId") UUID userId, Pageable pageable);

        /**
         * Get trending products - most viewed in the last N days
         */
        @Query("SELECT ua.product.id, COUNT(ua) as viewCount FROM UserActivity ua " +
                        "WHERE ua.activityType = 'VIEW' AND ua.createdAt >= :since AND ua.product IS NOT NULL " +
                        "GROUP BY ua.product.id ORDER BY viewCount DESC")
        List<Object[]> findTrendingProductIds(@Param("since") LocalDateTime since, Pageable pageable);

        /**
         * Get products frequently bought together (collaborative filtering)
         * Finds products purchased by users who also purchased the given product
         */
        @Query("SELECT ua2.product.id, COUNT(ua2) as purchaseCount FROM UserActivity ua1 " +
                        "JOIN UserActivity ua2 ON ua1.user.id = ua2.user.id " +
                        "WHERE ua1.product.id = :productId AND ua2.product.id != :productId " +
                        "AND ua1.activityType = 'PURCHASE' AND ua2.activityType = 'PURCHASE' " +
                        "GROUP BY ua2.product.id ORDER BY purchaseCount DESC")
        List<Object[]> findFrequentlyBoughtTogether(@Param("productId") UUID productId, Pageable pageable);

        /**
         * Get user's category preferences based on activity
         */
        @Query("SELECT ua.product.category.id, COUNT(ua) as activityCount FROM UserActivity ua " +
                        "WHERE ua.user.id = :userId AND ua.product IS NOT NULL AND ua.product.category IS NOT NULL " +
                        "GROUP BY ua.product.category.id ORDER BY activityCount DESC")
        List<Object[]> findUserCategoryPreferences(@Param("userId") UUID userId);

        /**
         * Get user's brand preferences based on activity
         */
        @Query("SELECT ua.product.brand.id, COUNT(ua) as activityCount FROM UserActivity ua " +
                        "WHERE ua.user.id = :userId AND ua.product IS NOT NULL AND ua.product.brand IS NOT NULL " +
                        "GROUP BY ua.product.brand.id ORDER BY activityCount DESC")
        List<Object[]> findUserBrandPreferences(@Param("userId") UUID userId);

        /**
         * Count activities by type for a user
         */
        Long countByUserIdAndActivityType(UUID userId, ActivityType activityType);

        /**
         * Find users who purchased a specific product
         */
        @Query("SELECT DISTINCT ua.user.id FROM UserActivity ua " +
                        "WHERE ua.product.id = :productId AND ua.activityType = 'PURCHASE'")
        List<UUID> findUsersByPurchasedProduct(@Param("productId") UUID productId);

        /**
         * Get products purchased by users who also purchased the target product
         * Used for "Customers also bought" feature
         */
        @Query("SELECT ua.product.id, COUNT(ua) as freq FROM UserActivity ua " +
                        "WHERE ua.user.id IN (" +
                        "  SELECT DISTINCT ua2.user.id FROM UserActivity ua2 " +
                        "  WHERE ua2.product.id = :productId AND ua2.activityType = 'PURCHASE'" +
                        ") " +
                        "AND ua.product.id != :productId AND ua.activityType = 'PURCHASE' " +
                        "GROUP BY ua.product.id ORDER BY freq DESC")
        List<Object[]> findAlsoBoughtProductIds(@Param("productId") UUID productId, Pageable pageable);

        /**
         * Check if user has already viewed a product recently (within timeframe)
         */
        @Query("SELECT COUNT(ua) > 0 FROM UserActivity ua " +
                        "WHERE ua.user.id = :userId AND ua.product.id = :productId " +
                        "AND ua.activityType = 'VIEW' AND ua.createdAt >= :since")
        boolean hasRecentView(@Param("userId") UUID userId,
                        @Param("productId") UUID productId,
                        @Param("since") LocalDateTime since);

        /**
         * Delete old activities (for data retention)
         */
        void deleteByCreatedAtBefore(LocalDateTime before);

        /**
         * Get most searched terms
         */
        @Query("SELECT ua.searchQuery, COUNT(ua) as searchCount FROM UserActivity ua " +
                        "WHERE ua.activityType = 'SEARCH' AND ua.searchQuery IS NOT NULL " +
                        "GROUP BY ua.searchQuery ORDER BY searchCount DESC")
        List<Object[]> findPopularSearchTerms(Pageable pageable);
}
