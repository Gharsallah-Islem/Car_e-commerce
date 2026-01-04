import { Component, Input, OnInit, OnDestroy, ViewChild, ElementRef, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatService } from '../../../core/services/chat.service';
import { AuthService } from '../../../core/services/auth.service';
import { ChatMessage, Conversation } from '../../../core/models/chat.model';
import { MessageBubbleComponent } from '../message-bubble/message-bubble.component';
import { MessageInputComponent } from '../message-input/message-input.component';

@Component({
  selector: 'app-chat-window',
  standalone: true,
  imports: [CommonModule, MessageBubbleComponent, MessageInputComponent],
  templateUrl: './chat-window.component.html',
  styleUrl: './chat-window.component.scss'
})
export class ChatWindowComponent implements OnInit, OnDestroy {
  private chatService = inject(ChatService);
  private authService = inject(AuthService);

  @Input() set conversationId(id: string | undefined) {
    if (id !== this._conversationId) {
      this._conversationId = id;
      if (id) {
        this.loadMessages();
      } else {
        this.messages.set([]);
      }
    }
  }

  get conversationId(): string | undefined {
    return this._conversationId;
  }

  @ViewChild('messagesContainer') messagesContainer!: ElementRef<HTMLDivElement>;

  private _conversationId?: string;
  messages = signal<ChatMessage[]>([]);
  loading = signal(false);
  sending = signal(false);
  refreshing = signal(false);

  // Typing indicator - shows when waiting for AI response
  aiTyping = signal(false);

  // Get current user ID from AuthService
  currentUserId = computed(() => {
    const user = this.authService.currentUser();
    if (!user?.id) return undefined;
    return user.id.toString();
  });

  ngOnInit(): void {
    // Component initialization
  }

  ngOnDestroy(): void {
    // Cleanup
  }

  loadMessages(): void {
    if (!this._conversationId) return;

    this.loading.set(true);
    this.chatService.getConversationMessages(this._conversationId, 0, 50)
      .subscribe({
        next: (page) => {
          const parsedMessages = page.content.map(msg => this.parseMessage(msg));
          this.messages.set(parsedMessages);
          this.loading.set(false);
          this.scrollToBottom();
          this.markAllAsRead();
        },
        error: (err) => {
          console.error('Failed to load messages:', err);
          this.loading.set(false);
        }
      });
  }

  // Manual refresh - replaces polling to eliminate blinking
  refreshMessages(): void {
    if (!this._conversationId || this.refreshing()) return;

    this.refreshing.set(true);

    // Get the timestamp of the last message
    const currentMessages = this.messages();
    const since = currentMessages.length > 0
      ? new Date(currentMessages[currentMessages.length - 1].createdAt)
      : new Date(Date.now() - 60000); // Last minute if no messages

    this.chatService.getRecentMessages(this._conversationId, since)
      .subscribe({
        next: (newMessages) => {
          if (newMessages && newMessages.length > 0) {
            const parsedMessages = newMessages.map(msg => this.parseMessage(msg));

            // Deduplicate
            const existingIds = new Set(this.messages().map(m => m.id));
            const uniqueNewMessages = parsedMessages.filter(m => !existingIds.has(m.id));

            if (uniqueNewMessages.length > 0) {
              this.messages.update(current => [...current, ...uniqueNewMessages]);
              this.scrollToBottom();
              this.markAllAsRead();
            }
          }
          this.refreshing.set(false);
          this.aiTyping.set(false);
        },
        error: (err) => {
          console.error('Refresh error:', err);
          this.refreshing.set(false);
        }
      });
  }

  onSendMessage(content: string): void {
    if (!this._conversationId || !content.trim()) return;

    this.sending.set(true);
    this.aiTyping.set(true); // Show typing indicator
    const request = { content: content.trim() };

    this.chatService.sendMessage(this._conversationId, request)
      .subscribe({
        next: (message) => {
          const parsedMessage = this.parseMessage(message);
          this.messages.update(current => [...current, parsedMessage]);
          this.sending.set(false);
          this.scrollToBottom();

          // Auto-check for AI response after 2 seconds
          setTimeout(() => this.checkForAiResponse(), 2000);
        },
        error: (err) => {
          console.error('Failed to send message:', err);
          alert('Failed to send message. Please try again.');
          this.sending.set(false);
          this.aiTyping.set(false);
        }
      });
  }

  // Check for AI response after sending a message
  private checkForAiResponse(): void {
    if (!this._conversationId) return;

    const currentMessages = this.messages();
    const since = currentMessages.length > 0
      ? new Date(currentMessages[currentMessages.length - 1].createdAt)
      : new Date(Date.now() - 5000);

    this.chatService.getRecentMessages(this._conversationId, since)
      .subscribe({
        next: (newMessages) => {
          if (newMessages && newMessages.length > 0) {
            const parsedMessages = newMessages.map(msg => this.parseMessage(msg));

            const existingIds = new Set(this.messages().map(m => m.id));
            const uniqueNewMessages = parsedMessages.filter(m => !existingIds.has(m.id));

            if (uniqueNewMessages.length > 0) {
              this.messages.update(current => [...current, ...uniqueNewMessages]);
              this.scrollToBottom();
              this.aiTyping.set(false);
            } else {
              // No response yet, try again in 2 seconds (max 3 attempts)
              setTimeout(() => this.checkForAiResponse(), 2000);
            }
          } else {
            // Keep trying
            setTimeout(() => this.checkForAiResponse(), 2000);
          }
        },
        error: () => {
          this.aiTyping.set(false);
        }
      });
  }

  scrollToBottom(): void {
    setTimeout(() => {
      if (this.messagesContainer) {
        const container = this.messagesContainer.nativeElement;
        container.scrollTop = container.scrollHeight;
      }
    }, 100);
  }

  markAllAsRead(): void {
    if (!this._conversationId) return;

    const currentMessages = this.messages();
    const unreadMessages = currentMessages.filter(m =>
      !m.isRead && !this.isOwnMessage(m)
    );

    if (unreadMessages.length > 0) {
      this.chatService.markAllAsRead(this._conversationId).subscribe({
        next: () => {
          this.messages.update(msgs =>
            msgs.map(msg =>
              !msg.isRead && !this.isOwnMessage(msg)
                ? { ...msg, isRead: true }
                : msg
            )
          );
        },
        error: (err) => {
          console.error('Failed to mark messages as read:', err);
        }
      });
    }
  }

  isOwnMessage(message: ChatMessage): boolean {
    // USER messages → RIGHT side
    // SUPPORT/ADMIN messages → LEFT side
    return message.senderType === 'USER';
  }

  onDeleteMessage(messageId: string): void {
    if (!messageId) return;

    this.chatService.deleteMessage(messageId).subscribe({
      next: () => {
        this.messages.update(msgs => msgs.filter(m => m.id !== messageId));
      },
      error: (err) => {
        console.error('Failed to delete message:', err);
        alert('Failed to delete message.');
      }
    });
  }

  archiveChat(): void {
    if (!this._conversationId) return;

    if (confirm('Archive this conversation?')) {
      this.chatService.archiveConversation(this._conversationId).subscribe({
        next: () => {
          this._conversationId = undefined;
          this.messages.set([]);
          alert('Conversation archived.');
        },
        error: (err) => {
          console.error('Failed to archive conversation:', err);
          alert('Failed to archive conversation.');
        }
      });
    }
  }

  startNewChat(): void {
    this.chatService.startSupportChat().subscribe({
      next: (conversation) => {
        this.conversationId = conversation.id;
      },
      error: (err) => {
        console.error('Failed to start chat:', err);
        alert('Failed to start new chat.');
      }
    });
  }

  trackByMessageId(index: number, message: ChatMessage): string | undefined {
    return message.id;
  }

  private parseMessage(message: any): ChatMessage {
    return {
      ...message,
      createdAt: typeof message.createdAt === 'string'
        ? new Date(message.createdAt)
        : message.createdAt,
      id: message.id?.toString() || message.id,
      conversationId: message.conversationId?.toString() || message.conversationId,
      senderId: message.senderId?.toString() || message.senderId
    };
  }
}
