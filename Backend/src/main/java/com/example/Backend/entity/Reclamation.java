package com.example.Backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reclamations", indexes = {
        @Index(name = "idx_reclamations_user_id", columnList = "user_id"),
        @Index(name = "idx_reclamations_status", columnList = "status"),
        @Index(name = "idx_reclamations_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reclamation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Subject is required")
    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @NotBlank(message = "Description is required")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Status is required")
    @Column(name = "status", nullable = false, length = 50)
    private String status; // OPEN, IN_PROGRESS, RESOLVED, CLOSED

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @Column(name = "attachment_url")
    private String attachmentUrl; // Photo/video evidence

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // Reclamation status constants
    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_RESOLVED = "RESOLVED";
    public static final String STATUS_CLOSED = "CLOSED";

    // Helper methods
    public boolean isOpen() {
        return STATUS_OPEN.equals(status);
    }

    public boolean isResolved() {
        return STATUS_RESOLVED.equals(status);
    }

    public void markAsResolved(String responseMessage) {
        this.status = STATUS_RESOLVED;
        this.response = responseMessage;
        this.resolvedAt = LocalDateTime.now();
    }
}
