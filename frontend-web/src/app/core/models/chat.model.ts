// Chat models aligned with Backend API (Spring Boot)
// Backend uses UUID (string) for all IDs

export enum MessageType {
    TEXT = 'TEXT',
    IMAGE = 'IMAGE',
    VIDEO = 'VIDEO',
    FILE = 'FILE',
    SYSTEM = 'SYSTEM'
}

export enum SenderType {
    USER = 'USER',
    SUPPORT = 'SUPPORT',
    ADMIN = 'ADMIN'
}

export enum ChatStatus {
    ACTIVE = 'ACTIVE',
    CLOSED = 'CLOSED',
    PENDING = 'PENDING'
}

/**
 * ChatMessage - matches backend Message entity
 * Backend: c:\...\Backend\entity\Message.java
 */
export interface ChatMessage {
    id?: string;  // UUID from backend
    conversationId: string;  // UUID
    senderId: string;  // UUID
    senderType: SenderType;  // USER | SUPPORT | ADMIN
    content: string;
    attachmentUrl?: string;  // For photos/videos/files
    isRead: boolean;
    createdAt: Date;
}

/**
 * Conversation - matches backend Conversation entity
 * Backend: c:\...\Backend\entity\Conversation.java
 */
export interface Conversation {
    id: string;  // UUID from backend
    userId: string;  // UUID - owner of conversation
    title?: string;
    isActive: boolean;
    messages?: ChatMessage[];  // Populated when needed
    lastMessage?: ChatMessage;
    createdAt: Date;
    updatedAt: Date;
}

/**
 * DTOs for API requests
 */
export interface SendMessageRequest {
    content: string;
    attachmentUrl?: string;
}

export interface MessageDTO {
    content: string;
    attachmentUrl?: string;
}

export interface CreateConversationRequest {
    subject?: string;
}

/**
 * Pagination response from Spring Data
 */
export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;  // Current page number
    first: boolean;
    last: boolean;
}

/**
 * Optional: For future WebSocket implementation
 */
export interface TypingIndicator {
    conversationId: string;
    userId: string;
    isTyping: boolean;
}
