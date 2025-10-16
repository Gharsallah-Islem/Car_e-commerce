package com.example.Backend.repository;

import com.example.Backend.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    /**
     * Find all conversations for a user
     */
    List<Conversation> findByUserId(UUID userId);

    /**
     * Find conversations by user with pagination (sorted by most recent)
     */
    @Query("SELECT c FROM Conversation c WHERE c.user.id = :userId ORDER BY c.updatedAt DESC")
    Page<Conversation> findByUserIdOrderByUpdatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find active conversations for a user
     */
    List<Conversation> findByUserIdAndIsActive(UUID userId, Boolean isActive);

    /**
     * Find conversation with messages (fetch join to avoid N+1)
     */
    @Query("SELECT c FROM Conversation c LEFT JOIN FETCH c.messages WHERE c.id = :conversationId")
    Optional<Conversation> findByIdWithMessages(@Param("conversationId") UUID conversationId);

    /**
     * Find all active conversations (for support team)
     */
    @Query("SELECT c FROM Conversation c WHERE c.isActive = true ORDER BY c.updatedAt DESC")
    List<Conversation> findAllActiveConversations();

    /**
     * Find active conversations with pagination
     */
    Page<Conversation> findByIsActive(Boolean isActive, Pageable pageable);

    /**
     * Count active conversations
     */
    Long countByIsActive(Boolean isActive);

    /**
     * Count user's active conversations
     */
    Long countByUserIdAndIsActive(UUID userId, Boolean isActive);

    /**
     * Find conversations with unread messages for a user
     */
    @Query("SELECT DISTINCT c FROM Conversation c JOIN c.messages m " +
            "WHERE c.user.id = :userId AND m.isRead = false AND m.senderType != 'USER'")
    List<Conversation> findConversationsWithUnreadMessages(@Param("userId") UUID userId);
}
