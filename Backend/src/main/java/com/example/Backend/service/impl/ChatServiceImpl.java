package com.example.Backend.service.impl;

import com.example.Backend.dto.ConversationDTO;
import com.example.Backend.dto.MessageDTO;
import com.example.Backend.entity.Conversation;
import com.example.Backend.entity.Message;
import com.example.Backend.entity.User;
import com.example.Backend.repository.ConversationRepository;
import com.example.Backend.repository.MessageRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.entity.Product;
import com.example.Backend.repository.ProductRepository;
import com.example.Backend.service.GeminiService;
import lombok.extern.slf4j.Slf4j;
import com.example.Backend.service.ChatService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Conversation getOrCreateConversation(UUID user1Id, UUID user2Id) {
        // Since Conversation entity is designed for User-to-Support chat (one user),
        // we'll create/find conversation for user1 (the customer)
        User user = userRepository.findById(user1Id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + user1Id));

        // Find existing active conversation for this user
        List<Conversation> existingConversations = conversationRepository.findByUserIdAndIsActive(user1Id, true);
        if (!existingConversations.isEmpty()) {
            return existingConversations.get(0);
        }

        // Create new conversation
        Conversation conversation = new Conversation();
        conversation.setUser(user);
        conversation.setTitle("Support Chat");
        conversation.setIsActive(true);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());

        return conversationRepository.save(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public Conversation getConversationById(UUID conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found with id: " + conversationId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Conversation> getUserConversations(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        return conversationRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDTO> getUserConversationsDTO(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        List<Conversation> conversations = conversationRepository.findByUserId(userId);
        return conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Message sendMessage(UUID conversationId, UUID senderId, MessageDTO messageDTO) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found with id: " + conversationId));
        if (!userRepository.existsById(senderId)) {
            throw new EntityNotFoundException("Sender not found with id: " + senderId);
        }
        // Save user's message
        Message userMessage = new Message();
        userMessage.setConversation(conversation);
        userMessage.setSenderId(senderId);
        userMessage.setSenderType(determineSenderType(senderId));
        userMessage.setContent(messageDTO.getContent());
        userMessage.setAttachmentUrl(messageDTO.getAttachmentUrl());
        userMessage.setIsRead(false);
        userMessage.setCreatedAt(LocalDateTime.now());
        // Update conversation timestamp
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        Message savedUserMessage = messageRepository.save(userMessage);
        // Generate AI response if sender is a regular user (not admin/support)
        String senderType = determineSenderType(senderId);
        if (Message.SENDER_USER.equals(senderType)) {
            try {
                // Get conversation history
                String conversationHistory = buildConversationHistory(conversationId);

                // Search for relevant products
                List<Product> relevantProducts = findRelevantProducts(messageDTO.getContent());

                // Generate AI response
                String aiResponse;
                if (!relevantProducts.isEmpty()) {
                    aiResponse = geminiService.generateResponseWithProducts(
                            messageDTO.getContent(),
                            relevantProducts,
                            conversationHistory);
                } else {
                    aiResponse = geminiService.generateResponse(
                            messageDTO.getContent(),
                            conversationHistory);
                }
                // Save AI response as a message
                Message aiMessage = new Message();
                aiMessage.setConversation(conversation);
                aiMessage.setSenderId(senderId);
                aiMessage.setSenderType(Message.SENDER_SUPPORT); // Mark as AI/support
                aiMessage.setContent(aiResponse);
                aiMessage.setIsRead(false);
                aiMessage.setCreatedAt(LocalDateTime.now().plusSeconds(1));

                messageRepository.save(aiMessage);

                log.info("AI response generated for conversation: {}", conversationId);

            } catch (Exception e) {
                log.error("Error generating AI response for conversation: {}", conversationId, e);
                // Don't fail if AI fails
            }
        }
        return savedUserMessage;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Message> getConversationMessages(UUID conversationId, Pageable pageable) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new EntityNotFoundException("Conversation not found with id: " + conversationId);
        }
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getRecentMessages(UUID conversationId, LocalDateTime since) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new EntityNotFoundException("Conversation not found with id: " + conversationId);
        }
        return messageRepository.findByConversationIdAndCreatedAtAfter(conversationId, since);
    }

    @Override
    @Transactional
    public Message markAsRead(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + messageId));

        // Only mark as read if user is not the sender
        if (!message.getSenderId().equals(userId)) {
            message.setIsRead(true);
            return messageRepository.save(message);
        }

        return message;
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID conversationId, UUID userId) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new EntityNotFoundException("Conversation not found with id: " + conversationId);
        }

        List<Message> unreadMessages = messageRepository.findByConversationIdAndIsRead(conversationId, false);
        unreadMessages.stream()
                .filter(msg -> !msg.getSenderId().equals(userId))
                .forEach(msg -> msg.setIsRead(true));
        messageRepository.saveAll(unreadMessages);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUnreadMessages(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        return messageRepository.countUnreadMessagesForUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Message> searchMessages(UUID conversationId, String searchTerm, Pageable pageable) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new EntityNotFoundException("Conversation not found with id: " + conversationId);
        }

        List<Message> results = messageRepository.searchInConversation(conversationId, searchTerm);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), results.size());
        return new PageImpl<>(results.subList(start, end), pageable, results.size());
    }

    @Override
    @Transactional
    public void deleteMessage(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + messageId));

        // Verify user is the sender
        if (!message.getSenderId().equals(userId)) {
            throw new IllegalArgumentException("Only sender can delete the message");
        }

        messageRepository.delete(message);
    }

    @Override
    @Transactional
    public void archiveConversation(UUID conversationId, UUID userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found with id: " + conversationId));

        // Verify user owns the conversation
        if (!conversation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not the owner of this conversation");
        }

        // Close the conversation instead of deleting
        conversation.closeConversation();
        conversationRepository.save(conversation);
    }

    /**
     * Determine sender type based on user role
     */
    private String determineSenderType(UUID senderId) {
        User user = userRepository.findById(senderId).orElse(null);
        if (user == null) {
            return Message.SENDER_USER;
        }

        if (user.isAdmin() || user.isSuperAdmin()) {
            return Message.SENDER_ADMIN;
        } else if (user.isSupport()) {
            return Message.SENDER_SUPPORT;
        } else {
            return Message.SENDER_USER;
        }
    }

    /**
     * Convert Conversation entity to ConversationDTO to avoid serialization issues
     */
    private ConversationDTO convertToDTO(Conversation conversation) {
        // Count unread messages for this conversation
        Long unreadCount = messageRepository.countByConversationIdAndIsRead(conversation.getId(), false);

        // Get last message if exists
        List<Message> messages = messageRepository.findTop1ByConversationIdOrderByCreatedAtDesc(conversation.getId());
        MessageDTO lastMessageDTO = null;
        if (!messages.isEmpty()) {
            Message lastMessage = messages.get(0);
            lastMessageDTO = MessageDTO.builder()
                    .id(lastMessage.getId())
                    .content(lastMessage.getContent())
                    .senderId(lastMessage.getSenderId())
                    .senderType(lastMessage.getSenderType())
                    .isRead(lastMessage.getIsRead())
                    .createdAt(lastMessage.getCreatedAt())
                    .attachmentUrl(lastMessage.getAttachmentUrl())
                    .build();
        }

        return ConversationDTO.builder()
                .id(conversation.getId())
                .userId(conversation.getUser().getId())
                .title(conversation.getTitle())
                .isActive(conversation.getIsActive())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .unreadCount(unreadCount.intValue())
                .lastMessage(lastMessageDTO)
                .build();
    }

    private String buildConversationHistory(UUID conversationId) {
        List<Message> recentMessages = messageRepository.findTop10ByConversationIdOrderByCreatedAtDesc(conversationId);

        if (recentMessages.isEmpty()) {
            return "";
        }
        StringBuilder history = new StringBuilder();
        // Reverse to get chronological order
        for (int i = recentMessages.size() - 1; i >= 0; i--) {
            Message msg = recentMessages.get(i);
            String role = Message.SENDER_USER.equals(msg.getSenderType()) ? "User" : "Assistant";
            history.append(role).append(": ").append(msg.getContent()).append("\\n");
        }

        return history.toString();
    }

    /**
     * Find relevant products based on user message keywords
     */
    private List<Product> findRelevantProducts(String userMessage) {
        try {
            String searchTerm = userMessage.toLowerCase();

            // Search products by name
            List<Product> products = productRepository.findByNameContaining(searchTerm);

            // Limit to top 5 most relevant in-stock products
            return products.stream()
                    .filter(p -> p.getStock() > 0)
                    .limit(5)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error finding relevant products", e);
            return List.of();
        }
    }
}
