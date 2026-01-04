package com.example.Backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for AI-powered product recommendations.
 * Supports multiple recommendation types: personalized, similar products,
 * collaborative filtering ("also bought"), trending, and vehicle-based.
 */
@Entity
@Table(name = "recommendations", indexes = {
        @Index(name = "idx_recommendations_user_id", columnList = "user_id"),
        @Index(name = "idx_recommendations_created_at", columnList = "created_at"),
        @Index(name = "idx_recommendations_type", columnList = "recommendation_type"),
        @Index(name = "idx_recommendations_product_id", columnList = "product_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // NEW: Type of recommendation (personalized, similar, trending, etc.)
    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation_type", length = 30)
    private RecommendationType recommendationType;

    // NEW: Direct reference to recommended product
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties({ "cartItems", "orderItems", "images" })
    private Product recommendedProduct;

    // NEW: Relevance score for sorting (0.0 to 1.0)
    @Column(name = "score")
    private Double score;

    // NEW: Human-readable reason for the recommendation
    @Column(name = "reason", length = 500)
    private String reason;

    // NEW: Whether user has seen/interacted with recommendation
    @Column(name = "is_viewed")
    private Boolean isViewed = false;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl; // Uploaded image for AI analysis

    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms; // User-described symptoms

    @Column(name = "ai_response", columnDefinition = "TEXT")
    private String aiResponse; // AI-generated recommendations

    @Column(name = "suggested_products", columnDefinition = "TEXT")
    private String suggestedProducts; // JSON array of product IDs (legacy)

    @Column(name = "confidence_score")
    private Double confidenceScore; // AI confidence level (0.0 to 1.0)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Helper methods
    public boolean isHighConfidence() {
        return confidenceScore != null && confidenceScore >= 0.8;
    }

    public boolean isHighScore() {
        return score != null && score >= 0.7;
    }

    // Convenience constructor for product recommendations
    public Recommendation(User user, Product product, RecommendationType type, Double score, String reason) {
        this.user = user;
        this.recommendedProduct = product;
        this.recommendationType = type;
        this.score = score;
        this.reason = reason;
        this.confidenceScore = score;
    }
}
