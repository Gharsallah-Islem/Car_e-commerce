package com.example.Backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recommendations", indexes = {
        @Index(name = "idx_recommendations_user_id", columnList = "user_id"),
        @Index(name = "idx_recommendations_created_at", columnList = "created_at")
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

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl; // Uploaded image for AI analysis

    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms; // User-described symptoms

    @Column(name = "ai_response", columnDefinition = "TEXT")
    private String aiResponse; // AI-generated recommendations

    @Column(name = "suggested_products", columnDefinition = "TEXT")
    private String suggestedProducts; // JSON array of product IDs

    @Column(name = "confidence_score")
    private Double confidenceScore; // AI confidence level (0.0 to 1.0)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Helper methods
    public boolean isHighConfidence() {
        return confidenceScore != null && confidenceScore >= 0.8;
    }
}
