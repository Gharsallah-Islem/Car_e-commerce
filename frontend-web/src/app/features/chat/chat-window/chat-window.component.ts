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
      this.stopPolling();
      this._conversationId = id;
      if (id) {
        this.loadMessages();
        this.startPolling();
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

  // 🔑 Polling variables
  private pollingInterval?: any;
  private lastPollTimestamp?: Date;

  // Get current user ID from AuthService
  currentUserId = computed(() => {
    const user = this.authService.currentUser();
    // Convert user ID to string to match backend UUID strings
    if (!user?.id) return undefined;
    return user.id.toString();
  });

  ngOnInit(): void {
    // AuthService already provides current user via signals
    // The currentUserId computed will automatically update when user changes
  }

  ngOnDestroy(): void {
    this.stopPolling();
  }

  loadMessages(): void {
    if (!this._conversationId) return;

    this.loading.set(true);
    this.chatService.getConversationMessages(this._conversationId, 0, 50)
      .subscribe({
        next: (page) => {
          const parsedMessages = page.content.map(msg => this.parseMessage(msg));
          this.messages.set(parsedMessages);
          
          // Set last poll timestamp to the most recent message time
          if (parsedMessages.length > 0) {
            const lastMessage = parsedMessages[parsedMessages.length - 1];
            this.lastPollTimestamp = new Date(lastMessage.createdAt);
          } else {
            this.lastPollTimestamp = new Date();
          }
          
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

  // 🔑 POLLING IMPLEMENTATION - CHECK FOR NEW MESSAGES EVERY 3 SECONDS
  startPolling(): void {
    this.stopPolling(); // Clear any existing interval

    this.pollingInterval = setInterval(() => {
      if (!this._conversationId || !this.lastPollTimestamp) return;

      this.chatService.getRecentMessages(
        this._conversationId,
        this.lastPollTimestamp
      ).subscribe({
        next: (newMessages) => {
          if (newMessages && newMessages.length > 0) {
            console.log(`📨 Received ${newMessages.length} new message(s)`);
            const parsedMessages = newMessages.map(msg => this.parseMessage(msg));
            this.messages.update(current => [...current, ...parsedMessages]);
            
            // Update timestamp to the most recent message
            const lastMessage = parsedMessages[parsedMessages.length - 1];
            this.lastPollTimestamp = new Date(lastMessage.createdAt);
            
            this.scrollToBottom();
            this.markAllAsRead();
          }
        },
        error: (err) => {
          // Silently handle polling errors to avoid console spam
          if (err.status !== 0) { // Don't log network errors (CORS, etc)
            console.error('Polling error:', err);
          }
        }
      });
    }, 3000); // Poll every 3 seconds (like WhatsApp)
  }

  stopPolling(): void {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
      this.pollingInterval = null;
    }
  }

  onSendMessage(content: string): void {
    if (!this._conversationId || !content.trim()) return;

    this.sending.set(true);
    const request = { content: content.trim() };

    this.chatService.sendMessage(this._conversationId, request)
      .subscribe({
        next: (message) => {
          const parsedMessage = this.parseMessage(message);
          this.messages.update(current => [...current, parsedMessage]);
          this.lastPollTimestamp = new Date(parsedMessage.createdAt);
          this.sending.set(false);
          this.scrollToBottom();
        },
        error: (err) => {
          console.error('Failed to send message:', err);
          alert('Failed to send message. Please try again.');
          this.sending.set(false);
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
          // Update read status in messages
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
    const userId = this.currentUserId();
    return userId !== undefined && message.senderId === userId;
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
          this.stopPolling();
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

  /**
   * Parse message from backend - handles date conversion from LocalDateTime string
   */
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
