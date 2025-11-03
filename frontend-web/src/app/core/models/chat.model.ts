export enum MessageType {
    TEXT = 'TEXT',
    IMAGE = 'IMAGE',
    VIDEO = 'VIDEO',
    FILE = 'FILE',
    SYSTEM = 'SYSTEM'
}

export enum ChatStatus {
    ACTIVE = 'ACTIVE',
    CLOSED = 'CLOSED',
    PENDING = 'PENDING'
}

export interface ChatMessage {
    id?: number;
    conversationId: number;
    senderId: number;
    senderName: string;
    senderRole: 'CLIENT' | 'ADMIN';
    content: string;
    type: MessageType;
    fileUrl?: string;
    timestamp: Date;
    isRead: boolean;
}

export interface Conversation {
    id: number;
    clientId: number;
    clientName: string;
    adminId?: number;
    adminName?: string;
    status: ChatStatus;
    subject?: string;
    lastMessage?: ChatMessage;
    unreadCount: number;
    createdAt: Date;
    updatedAt: Date;
}

export interface SendMessageRequest {
    conversationId?: number;
    content: string;
    type: MessageType;
    file?: File;
}

export interface CreateConversationRequest {
    subject?: string;
    initialMessage: string;
}

export interface TypingIndicator {
    conversationId: number;
    userId: number;
    userName: string;
    isTyping: boolean;
}
