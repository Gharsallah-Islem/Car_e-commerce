package com.example.Backend.repository;

import com.example.Backend.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

        /**
         * Find all messages in a conversation
         */
        List<Message> findByConversationId(UUID conversationId);

        /**
         * Find messages in conversation with pagination (sorted by creation time)
         */
        Page<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId, Pageable pageable);

        /**
         * Find messages by sender type
         */
        List<Message> findBySenderType(String senderType);

        /**
         * Find messages by sender ID and type
         */
        List<Message> findBySenderIdAndSenderType(UUID senderId, String senderType);

        /**
         * Find unread messages in a conversation
         */
        List<Message> findByConversationIdAndIsRead(UUID conversationId, Boolean isRead);

        /**
         * Find messages created after a specific time (for real-time updates)
         */
        List<Message> findByConversationIdAndCreatedAtAfter(UUID conversationId, LocalDateTime after);

        /**
         * Count unread messages in a conversation
         */
        Long countByConversationIdAndIsRead(UUID conversationId, Boolean isRead);

        /**
         * Count unread messages for a user (where sender is not USER)
         */
        @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.user.id = :userId " +
                        "AND m.isRead = false AND m.senderType != 'USER'")
        Long countUnreadMessagesForUser(@Param("userId") UUID userId);

        /**
         * Mark all messages as read in a conversation
         */
        @Modifying
        @Query("UPDATE Message m SET m.isRead = true WHERE m.conversation.id = :conversationId AND m.isRead = false")
        void markAllAsReadInConversation(@Param("conversationId") UUID conversationId);

        /**
         * Mark specific messages as read
         */
        @Modifying
        @Query("UPDATE Message m SET m.isRead = true WHERE m.id IN :messageIds")
        void markAsRead(@Param("messageIds") List<UUID> messageIds);

        /**
         * Get last message in conversation
         */
        @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
                        "ORDER BY m.createdAt DESC LIMIT 1")
        Message findLastMessageInConversation(@Param("conversationId") UUID conversationId);

        /**
         * Get last message in conversation using JPA naming convention
         */
        List<Message> findTop1ByConversationIdOrderByCreatedAtDesc(UUID conversationId);

        /**
         * Search messages by content
         */
        @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
                        "AND LOWER(m.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
        List<Message> searchInConversation(@Param("conversationId") UUID conversationId,
                        @Param("searchTerm") String searchTerm);
}
