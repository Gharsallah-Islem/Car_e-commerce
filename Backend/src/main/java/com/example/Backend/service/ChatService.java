package com.example.Backend.service;

import com.example.Backend.dto.ConversationDTO;
import com.example.Backend.dto.MessageDTO;
import com.example.Backend.entity.Conversation;
import com.example.Backend.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ChatService {

    /**
     * Start or get conversation between users
     * 
     * @param user1Id First user ID
     * @param user2Id Second user ID
     * @return Conversation entity
     */
    Conversation getOrCreateConversation(UUID user1Id, UUID user2Id);

    /**
     * Get conversation by ID
     * 
     * @param conversationId Conversation ID
     * @return Conversation entity
     */
    Conversation getConversationById(UUID conversationId);

    /**
     * Get all conversations for a user
     * 
     * @param userId User ID
     * @return List of conversations
     */
    List<Conversation> getUserConversations(UUID userId);

    /**
     * Get all conversations for a user as DTOs (prevents serialization issues)
     * 
     * @param userId User ID
     * @return List of conversation DTOs
     */
    List<ConversationDTO> getUserConversationsDTO(UUID userId);

    /**
     * Send message in conversation
     * 
     * @param conversationId Conversation ID
     * @param senderId       Sender user ID
     * @param messageDTO     Message data
     * @return Created message
     */
    Message sendMessage(UUID conversationId, UUID senderId, MessageDTO messageDTO);

    /**
     * Get messages in conversation
     * 
     * @param conversationId Conversation ID
     * @param pageable       Pagination parameters
     * @return Page of messages
     */
    Page<Message> getConversationMessages(UUID conversationId, Pageable pageable);

    /**
     * Get recent messages after timestamp (for real-time updates)
     * 
     * @param conversationId Conversation ID
     * @param since          Timestamp
     * @return List of recent messages
     */
    List<Message> getRecentMessages(UUID conversationId, LocalDateTime since);

    /**
     * Mark message as read
     * 
     * @param messageId Message ID
     * @param userId    User ID
     * @return Updated message
     */
    Message markAsRead(UUID messageId, UUID userId);

    /**
     * Mark all messages in conversation as read
     * 
     * @param conversationId Conversation ID
     * @param userId         User ID
     */
    void markAllAsRead(UUID conversationId, UUID userId);

    /**
     * Get unread message count for user
     * 
     * @param userId User ID
     * @return Number of unread messages
     */
    Long countUnreadMessages(UUID userId);

    /**
     * Search messages in conversation
     * 
     * @param conversationId Conversation ID
     * @param searchTerm     Search term
     * @param pageable       Pagination parameters
     * @return Page of matching messages
     */
    Page<Message> searchMessages(UUID conversationId, String searchTerm, Pageable pageable);

    /**
     * Delete message
     * 
     * @param messageId Message ID
     * @param userId    User ID (must be sender)
     */
    void deleteMessage(UUID messageId, UUID userId);

    /**
     * Archive conversation
     * 
     * @param conversationId Conversation ID
     * @param userId         User ID
     */
    void archiveConversation(UUID conversationId, UUID userId);
}
