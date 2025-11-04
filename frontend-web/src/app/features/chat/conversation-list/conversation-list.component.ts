import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatService } from '../../../core/services/chat.service';
import { Conversation } from '../../../core/models/chat.model';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-conversation-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './conversation-list.component.html',
  styleUrl: './conversation-list.component.scss'
})
export class ConversationListComponent implements OnInit, OnDestroy {
  private chatService = inject(ChatService);

  @Input() selectedConversationId?: string;
  @Output() conversationSelected = new EventEmitter<string>();

  conversations: Conversation[] = [];
  loading = false;
  unreadCount = 0;

  private refreshSubscription?: Subscription;

  ngOnInit(): void {
    this.loadConversations();
    this.loadUnreadCount();

    // Refresh conversations every 10 seconds
    this.refreshSubscription = interval(10000).subscribe(() => {
      this.loadConversations();
      this.loadUnreadCount();
    });
  }

  ngOnDestroy(): void {
    this.refreshSubscription?.unsubscribe();
  }

  loadConversations(): void {
    this.loading = true;
    this.chatService.getUserConversations().subscribe({
      next: (conversations) => {
        // Sort by most recent first
        this.conversations = conversations.sort((a, b) =>
          new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
        );
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load conversations:', err);
        this.loading = false;
      }
    });
  }

  loadUnreadCount(): void {
    this.chatService.getUnreadCount().subscribe({
      next: (count) => {
        this.unreadCount = count;
      },
      error: (err) => {
        console.error('Failed to load unread count:', err);
      }
    });
  }

  selectConversation(conversationId: string): void {
    this.selectedConversationId = conversationId;
    this.conversationSelected.emit(conversationId);
  }

  startNewChat(): void {
    this.chatService.startSupportChat().subscribe({
      next: (conversation) => {
        this.conversations.unshift(conversation);
        this.selectConversation(conversation.id);
      },
      error: (err) => {
        console.error('Failed to start chat:', err);
        alert('Failed to start new chat.');
      }
    });
  }

  hasUnread(conversation: Conversation): boolean {
    // Check if conversation has unread messages
    // Since we don't have unreadCount in Conversation, we'll check lastMessage
    return conversation.lastMessage ? !conversation.lastMessage.isRead : false;
  }

  getFormattedTime(date: Date): string {
    const now = new Date();
    const messageDate = new Date(date);
    const diffMs = now.getTime() - messageDate.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays}d ago`;

    // Format as date
    return messageDate.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  trackByConvId(index: number, conversation: Conversation): string {
    return conversation.id;
  }
}
