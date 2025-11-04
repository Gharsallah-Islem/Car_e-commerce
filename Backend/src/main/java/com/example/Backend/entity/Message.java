package com.example.Backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_messages_conversation_id", columnList = "conversation_id"),
        @Index(name = "idx_messages_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    @JsonIgnore
    private Conversation conversation;

    @Column(name = "sender_id", columnDefinition = "UUID")
    private UUID senderId; // User ID of sender

    @NotBlank(message = "Sender type is required")
    @Column(name = "sender_type", nullable = false, length = 50)
    private String senderType; // USER, SUPPORT, ADMIN

    @NotBlank(message = "Content is required")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "attachment_url")
    private String attachmentUrl; // Photo/video attachment

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Sender type constants
    public static final String SENDER_USER = "USER";
    public static final String SENDER_SUPPORT = "SUPPORT";
    public static final String SENDER_ADMIN = "ADMIN";

    // Helper methods
    public void markAsRead() {
        this.isRead = true;
    }

    public boolean isFromUser() {
        return SENDER_USER.equals(senderType);
    }

    public boolean isFromSupport() {
        return SENDER_SUPPORT.equals(senderType);
    }
}
