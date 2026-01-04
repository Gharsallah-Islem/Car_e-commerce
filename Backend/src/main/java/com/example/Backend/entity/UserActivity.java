package com.example.Backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity to track user activities for AI-powered recommendations.
 * Records product views, cart additions, purchases, and searches.
 */
@Entity
@Table(name = "user_activities", indexes = {
        @Index(name = "idx_user_activities_user_id", columnList = "user_id"),
        @Index(name = "idx_user_activities_product_id", columnList = "product_id"),
        @Index(name = "idx_user_activities_type", columnList = "activity_type"),
        @Index(name = "idx_user_activities_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 30)
    private ActivityType activityType;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "search_query", columnDefinition = "TEXT")
    private String searchQuery;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON for additional context

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Convenience constructor for product activities
    public UserActivity(User user, Product product, ActivityType activityType) {
        this.user = user;
        this.product = product;
        this.activityType = activityType;
    }

    // Convenience constructor for search activities
    public UserActivity(User user, String searchQuery, ActivityType activityType) {
        this.user = user;
        this.searchQuery = searchQuery;
        this.activityType = activityType;
    }
}
