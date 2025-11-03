package com.example.Backend.controller;

import com.example.Backend.dto.MessageDTO;
import com.example.Backend.entity.Conversation;
import com.example.Backend.entity.Message;
import com.example.Backend.repository.ConversationRepository;
import com.example.Backend.security.UserPrincipal;
import com.example.Backend.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ChatController {

    private final ChatService chatService;
    private final ConversationRepository conversationRepository;

    /**
     * Start or get conversation with support
     * For user-to-user chat, use: POST /api/chat/conversations/{userId}
     * For support chat, userId should be support staff ID
     * POST /api/chat/conversations/{userId}
     */
    @PostMapping("/conversations/{userId}")
    public ResponseEntity<Conversation> startConversation(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Conversation conversation = chatService.getOrCreateConversation(
                currentUser.getId(),
                userId);
        return ResponseEntity.ok(conversation);
    }

    /**
     * Get conversation by ID
     * GET /api/chat/conversations/{conversationId}
     */
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<Conversation> getConversation(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Conversation conversation = chatService.getConversationById(conversationId);

        // Verify user owns this conversation or is support/admin
        boolean isOwner = conversation.getUser().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().matches("ROLE_(SUPPORT|ADMIN|SUPER_ADMIN)"));

        if (!isOwner && !isStaff) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(conversation);
    }

    /**
     * Get all conversations for current user
     * GET /api/chat/conversations
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<Conversation>> getMyConversations(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<Conversation> conversations = chatService.getUserConversations(currentUser.getId());
        return ResponseEntity.ok(conversations);
    }

    /**
     * Get messages in a conversation (paginated)
     * GET /api/chat/conversations/{conversationId}/messages
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Page<Message>> getMessages(
            @PathVariable UUID conversationId,
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Verify access
        Conversation conversation = chatService.getConversationById(conversationId);
        boolean isOwner = conversation.getUser().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().matches("ROLE_(SUPPORT|ADMIN|SUPER_ADMIN)"));

        if (!isOwner && !isStaff) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Page<Message> messages = chatService.getConversationMessages(conversationId, pageable);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get recent messages after timestamp (for real-time polling)
     * GET /api/chat/conversations/{conversationId}/messages/recent
     */
    @GetMapping("/conversations/{conversationId}/messages/recent")
    public ResponseEntity<List<Message>> getRecentMessages(
            @PathVariable UUID conversationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Verify access
        Conversation conversation = chatService.getConversationById(conversationId);
        boolean isOwner = conversation.getUser().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().matches("ROLE_(SUPPORT|ADMIN|SUPER_ADMIN)"));

        if (!isOwner && !isStaff) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Message> messages = chatService.getRecentMessages(conversationId, since);
        return ResponseEntity.ok(messages);
    }

    /**
     * Send message in conversation
     * POST /api/chat/conversations/{conversationId}/messages
     */
    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Message> sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody MessageDTO messageDTO,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Verify access
        Conversation conversation = chatService.getConversationById(conversationId);
        boolean isOwner = conversation.getUser().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().matches("ROLE_(SUPPORT|ADMIN|SUPER_ADMIN)"));

        if (!isOwner && !isStaff) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Message message = chatService.sendMessage(
                conversationId,
                currentUser.getId(),
                messageDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    /**
     * Search messages in conversation
     * GET /api/chat/conversations/{conversationId}/messages/search
     */
    @GetMapping("/conversations/{conversationId}/messages/search")
    public ResponseEntity<Page<Message>> searchMessages(
            @PathVariable UUID conversationId,
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Verify access
        Conversation conversation = chatService.getConversationById(conversationId);
        boolean isOwner = conversation.getUser().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().matches("ROLE_(SUPPORT|ADMIN|SUPER_ADMIN)"));

        if (!isOwner && !isStaff) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Page<Message> messages = chatService.searchMessages(conversationId, query, pageable);
        return ResponseEntity.ok(messages);
    }

    /**
     * Mark message as read
     * PATCH /api/chat/messages/{messageId}/read
     */
    @PatchMapping("/messages/{messageId}/read")
    public ResponseEntity<Message> markMessageAsRead(
            @PathVariable UUID messageId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Message message = chatService.markAsRead(messageId, currentUser.getId());
        return ResponseEntity.ok(message);
    }

    /**
     * Mark all messages in conversation as read
     * PATCH /api/chat/conversations/{conversationId}/read-all
     */
    @PatchMapping("/conversations/{conversationId}/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Verify access
        Conversation conversation = chatService.getConversationById(conversationId);
        boolean isOwner = conversation.getUser().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().matches("ROLE_(SUPPORT|ADMIN|SUPER_ADMIN)"));

        if (!isOwner && !isStaff) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        chatService.markAllAsRead(conversationId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Get unread message count for current user
     * GET /api/chat/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Long count = chatService.countUnreadMessages(currentUser.getId());
        return ResponseEntity.ok(count);
    }

    /**
     * Delete message (sender only)
     * DELETE /api/chat/messages/{messageId}
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID messageId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        chatService.deleteMessage(messageId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Archive conversation
     * PATCH /api/chat/conversations/{conversationId}/archive
     */
    @PatchMapping("/conversations/{conversationId}/archive")
    public ResponseEntity<Void> archiveConversation(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Verify access
        Conversation conversation = chatService.getConversationById(conversationId);
        boolean isOwner = conversation.getUser().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().matches("ROLE_(SUPPORT|ADMIN|SUPER_ADMIN)"));

        if (!isOwner && !isStaff) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        chatService.archiveConversation(conversationId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * SUPPORT/ADMIN: Get all active conversations (for support dashboard)
     * GET /api/chat/support/conversations
     */
    @GetMapping("/support/conversations")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<Conversation>> getAllActiveConversations() {
        List<Conversation> activeConversations = conversationRepository.findAllActiveConversations();
        return ResponseEntity.ok(activeConversations);
    }

    /**
     * CLIENT: Start support conversation (shortcut endpoint)
     * POST /api/chat/support
     * 
     * Note: This creates a conversation for the current user to contact support.
     * Since Conversation entity is designed for User-to-Support chat,
     * we simply create/get conversation for the authenticated user.
     */
    @PostMapping("/support")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Conversation> startSupportConversation(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Create or get existing conversation for this user
        // The service will handle finding existing or creating new
        Conversation conversation = chatService.getOrCreateConversation(
                currentUser.getId(),
                currentUser.getId());

        return ResponseEntity.ok(conversation);
    }
}
