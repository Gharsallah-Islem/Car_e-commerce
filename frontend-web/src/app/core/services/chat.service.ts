import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ChatMessage,
  Conversation,
  SendMessageRequest,
  Page
} from '../models/chat.model';

/**
 * ChatService - Handles all chat-related API calls
 * Connects to Spring Boot backend /api/chat endpoints
 * Supports polling-based real-time updates
 */
@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private readonly apiUrl = `${environment.apiUrl}/chat`;

  constructor(private http: HttpClient) { }

  // ==================== CONVERSATION MANAGEMENT ====================

  /**
   * Start a new support conversation (or get existing one)
   * POST /api/chat/support
   */
  startSupportChat(): Observable<Conversation> {
    return this.http.post<Conversation>(`${this.apiUrl}/support`, {});
  }

  /**
   * Start or get conversation with a specific user
   * POST /api/chat/conversations/{userId}
   */
  startConversation(userId: string): Observable<Conversation> {
    return this.http.post<Conversation>(
      `${this.apiUrl}/conversations/${userId}`,
      {}
    );
  }

  /**
   * Get all conversations for current user
   * GET /api/chat/conversations
   */
  getUserConversations(): Observable<Conversation[]> {
    return this.http.get<Conversation[]>(`${this.apiUrl}/conversations`);
  }

  /**
   * Get specific conversation by ID
   * GET /api/chat/conversations/{conversationId}
   */
  getConversationById(conversationId: string): Observable<Conversation> {
    return this.http.get<Conversation>(
      `${this.apiUrl}/conversations/${conversationId}`
    );
  }

  // ==================== MESSAGE OPERATIONS ====================

  /**
   * Get paginated messages in a conversation
   * GET /api/chat/conversations/{conversationId}/messages
   * @param page - Page number (0-indexed)
   * @param size - Number of messages per page (default: 50)
   */
  getConversationMessages(
    conversationId: string,
    page: number = 0,
    size: number = 50
  ): Observable<Page<ChatMessage>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'createdAt,asc');  // Oldest first for chat

    return this.http.get<Page<ChatMessage>>(
      `${this.apiUrl}/conversations/${conversationId}/messages`,
      { params }
    );
  }

  /**
   * 🔑 CRITICAL FOR POLLING: Get messages created after a specific timestamp
   * GET /api/chat/conversations/{conversationId}/messages/recent
   * This is used to poll for new messages every 3 seconds
   * @param since - Only return messages created after this date
   */
  getRecentMessages(
    conversationId: string,
    since: Date
  ): Observable<ChatMessage[]> {
    const timestamp = since.toISOString();
    const params = new HttpParams().set('since', timestamp);

    return this.http.get<ChatMessage[]>(
      `${this.apiUrl}/conversations/${conversationId}/messages/recent`,
      { params }
    );
  }

  /**
   * Send a new message in a conversation
   * POST /api/chat/conversations/{conversationId}/messages
   */
  sendMessage(
    conversationId: string,
    request: SendMessageRequest
  ): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(
      `${this.apiUrl}/conversations/${conversationId}/messages`,
      request
    );
  }

  /**
   * Search messages in a conversation
   * GET /api/chat/conversations/{conversationId}/messages/search
   */
  searchMessages(
    conversationId: string,
    query: string,
    page: number = 0,
    size: number = 20
  ): Observable<Page<ChatMessage>> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<ChatMessage>>(
      `${this.apiUrl}/conversations/${conversationId}/messages/search`,
      { params }
    );
  }

  // ==================== READ STATUS ====================

  /**
   * Mark a single message as read
   * PATCH /api/chat/messages/{messageId}/read
   */
  markMessageAsRead(messageId: string): Observable<ChatMessage> {
    return this.http.patch<ChatMessage>(
      `${this.apiUrl}/messages/${messageId}/read`,
      {}
    );
  }

  /**
   * Mark all messages in a conversation as read
   * PATCH /api/chat/conversations/{conversationId}/read-all
   */
  markAllAsRead(conversationId: string): Observable<void> {
    return this.http.patch<void>(
      `${this.apiUrl}/conversations/${conversationId}/read-all`,
      {}
    );
  }

  /**
   * Get total unread message count for current user
   * GET /api/chat/unread-count
   * Used for navbar badge
   */
  getUnreadCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/unread-count`);
  }

  // ==================== ADMIN OPERATIONS ====================

  /**
   * Delete a message (sender only)
   * DELETE /api/chat/messages/{messageId}
   */
  deleteMessage(messageId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/messages/${messageId}`);
  }

  /**
   * Archive/close a conversation
   * PATCH /api/chat/conversations/{conversationId}/archive
   */
  archiveConversation(conversationId: string): Observable<void> {
    return this.http.patch<void>(
      `${this.apiUrl}/conversations/${conversationId}/archive`,
      {}
    );
  }

  // ==================== SUPPORT DASHBOARD ====================

  /**
   * Get all active conversations (SUPPORT/ADMIN only)
   * GET /api/chat/support/conversations
   */
  getAllActiveConversations(): Observable<Conversation[]> {
    return this.http.get<Conversation[]>(
      `${this.apiUrl}/support/conversations`
    );
  }
}
