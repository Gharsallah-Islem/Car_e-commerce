import { Component, Input, OnInit, OnDestroy, ViewChild, ElementRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatService } from '../../../core/services/chat.service';
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

  @Input() set conversationId(id: string | undefined) {
    if (id !== this._conversationId) {
      this.stopPolling();
      this._conversationId = id;
      if (id) {
        this.loadMessages();
        this.startPolling();
      } else {
        this.messages = [];
      }
    }
  }

  get conversationId(): string | undefined {
    return this._conversationId;
  }

  @ViewChild('messagesContainer') messagesContainer!: ElementRef<HTMLDivElement>;

  private _conversationId?: string;
  messages: ChatMessage[] = [];
  loading = false;
  sending = false;

  // 🔑 Polling variables
  private pollingInterval?: any;
  private lastPollTimestamp?: Date;

  // Get current user ID (you'll need to inject AuthService)
  private currentUserId?: string;

  ngOnInit(): void {
    // TODO: Get current user ID from AuthService
    // this.currentUserId = this.authService.getCurrentUser()?.id;
    this.currentUserId = 'temp-user-id'; // Placeholder
  }

  ngOnDestroy(): void {
    this.stopPolling();
  }

  loadMessages(): void {
    if (!this._conversationId) return;

    this.loading = true;
    this.chatService.getConversationMessages(this._conversationId, 0, 50)
      .subscribe({
        next: (page) => {
          this.messages = page.content;
          this.lastPollTimestamp = new Date();
          this.loading = false;
          this.scrollToBottom();
          this.markAllAsRead();
        },
        error: (err) => {
          console.error('Failed to load messages:', err);
          this.loading = false;
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
            this.messages.push(...newMessages);
            this.lastPollTimestamp = new Date();
            this.scrollToBottom();
            this.markAllAsRead();
          }
        },
        error: (err) => {
          console.error('Polling error:', err);
        }
      });
    }, 3000); // Poll every 3 seconds
  }

  stopPolling(): void {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
      this.pollingInterval = null;
    }
  }

  onSendMessage(content: string): void {
    if (!this._conversationId || !content.trim()) return;

    this.sending = true;
    const request = { content: content.trim() };

    this.chatService.sendMessage(this._conversationId, request)
      .subscribe({
        next: (message) => {
          this.messages.push(message);
          this.lastPollTimestamp = new Date();
          this.sending = false;
          this.scrollToBottom();
        },
        error: (err) => {
          console.error('Failed to send message:', err);
          alert('Failed to send message. Please try again.');
          this.sending = false;
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

    const unreadMessages = this.messages.filter(m =>
      !m.isRead && !this.isOwnMessage(m)
    );

    if (unreadMessages.length > 0) {
      this.chatService.markAllAsRead(this._conversationId).subscribe({
        next: () => {
          unreadMessages.forEach(m => m.isRead = true);
        },
        error: (err) => {
          console.error('Failed to mark messages as read:', err);
        }
      });
    }
  }

  isOwnMessage(message: ChatMessage): boolean {
    return message.senderId === this.currentUserId;
  }

  onDeleteMessage(messageId: string): void {
    this.chatService.deleteMessage(messageId).subscribe({
      next: () => {
        this.messages = this.messages.filter(m => m.id !== messageId);
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
          this.messages = [];
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
}
