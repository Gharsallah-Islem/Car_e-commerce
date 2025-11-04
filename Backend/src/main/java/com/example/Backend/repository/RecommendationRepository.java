package com.example.Backend.repository;

import com.example.Backend.entity.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {

    /**
     * Find all recommendations for a user
     */
    List<Recommendation> findByUserId(UUID userId);

    /**
     * Find recommendations by user with pagination (sorted by most recent)
     */
    Page<Recommendation> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find recommendations with high confidence (>= threshold)
     */
    @Query("SELECT r FROM Recommendation r WHERE r.confidenceScore >= :threshold ORDER BY r.createdAt DESC")
    List<Recommendation> findHighConfidenceRecommendations(@Param("threshold") Double threshold);

    /**
     * Find recommendations for a user with high confidence
     */
    @Query("SELECT r FROM Recommendation r WHERE r.user.id = :userId AND r.confidenceScore >= :threshold " +
            "ORDER BY r.createdAt DESC")
    List<Recommendation> findUserHighConfidenceRecommendations(@Param("userId") UUID userId,
            @Param("threshold") Double threshold);

    /**
     * Find recent recommendations (last N days)
     */
    @Query("SELECT r FROM Recommendation r WHERE r.createdAt >= :since ORDER BY r.createdAt DESC")
    List<Recommendation> findRecentRecommendations(@Param("since") LocalDateTime since);

    /**
     * Find recommendations with images
     */
    @Query("SELECT r FROM Recommendation r WHERE r.imageUrl IS NOT NULL")
    List<Recommendation> findRecommendationsWithImages();

    /**
     * Count recommendations by user
     */
    Long countByUserId(UUID userId);

    /**
     * Get average confidence score for user's recommendations
     */
    @Query("SELECT AVG(r.confidenceScore) FROM Recommendation r WHERE r.user.id = :userId")
    Double getAverageConfidenceScoreForUser(@Param("userId") UUID userId);

    /**
     * Find recommendations containing specific symptom keywords
     */
    @Query("SELECT r FROM Recommendation r WHERE LOWER(r.symptoms) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Recommendation> findBySymptomKeyword(@Param("keyword") String keyword);
}
