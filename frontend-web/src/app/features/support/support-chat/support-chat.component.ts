import { Component, OnInit, OnDestroy, signal, inject, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatListModule } from '@angular/material/list';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { interval, Subscription } from 'rxjs';

import { ChatService } from '../../../core/services/chat.service';
import { AuthService } from '../../../core/services/auth.service';
import { Conversation, ChatMessage } from '../../../core/models/chat.model';

@Component({
    selector: 'app-support-chat',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatCardModule,
        MatIconModule,
        MatButtonModule,
        MatInputModule,
        MatFormFieldModule,
        MatListModule,
        MatBadgeModule,
        MatProgressSpinnerModule,
        MatDividerModule
    ],
    templateUrl: './support-chat.component.html',
    styleUrls: ['./support-chat.component.scss']
})
export class SupportChatComponent implements OnInit, OnDestroy, AfterViewChecked {
    private chatService = inject(ChatService);
    private authService = inject(AuthService);
    private currentUser = this.authService.getCurrentUser();

    @ViewChild('messagesContainer') messagesContainer!: ElementRef;

    loading = signal<boolean>(true);
    sending = signal<boolean>(false);
    conversations = signal<Conversation[]>([]);
    selectedConversation = signal<Conversation | null>(null);
    messages = signal<ChatMessage[]>([]);
    newMessage = '';

    private pollingSubscription?: Subscription;
    private shouldScrollToBottom = false;

    ngOnInit(): void {
        this.loadConversations();
    }

    ngOnDestroy(): void {
        this.pollingSubscription?.unsubscribe();
    }

    ngAfterViewChecked(): void {
        if (this.shouldScrollToBottom) {
            this.scrollToBottom();
            this.shouldScrollToBottom = false;
        }
    }

    private loadConversations(): void {
        this.loading.set(true);
        // Use getAllActiveConversations for support staff
        this.chatService.getAllActiveConversations().subscribe({
            next: (convs: Conversation[]) => {
                this.conversations.set(convs || []);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }

    selectConversation(conv: Conversation): void {
        this.selectedConversation.set(conv);
        this.loadMessages(conv.id);
        this.startPolling(conv.id);
    }

    private loadMessages(conversationId: string): void {
        this.chatService.getConversationMessages(conversationId).subscribe({
            next: (response) => {
                this.messages.set(response.content || []);
                this.shouldScrollToBottom = true;
                this.markAsRead(conversationId);
            }
        });
    }

    private startPolling(conversationId: string): void {
        this.pollingSubscription?.unsubscribe();
        this.pollingSubscription = interval(10000).subscribe(() => {
            // Silently fetch new messages without causing UI blink
            this.chatService.getConversationMessages(conversationId).subscribe({
                next: (response) => {
                    const newMessages = response.content || [];
                    const currentMessages = this.messages();

                    // Only update if there are actually new messages
                    if (newMessages.length > currentMessages.length) {
                        // Filter out duplicates by comparing IDs
                        const currentIds = new Set(currentMessages.map(m => m.id));
                        const uniqueNewMessages = newMessages.filter(m => !currentIds.has(m.id));

                        if (uniqueNewMessages.length > 0) {
                            this.messages.set(newMessages);
                            this.shouldScrollToBottom = true;
                        }
                    }
                }
            });
        });
    }

    private markAsRead(conversationId: string): void {
        this.chatService.markAllAsRead(conversationId).subscribe();
    }

    sendMessage(): void {
        if (!this.newMessage.trim() || !this.selectedConversation()) return;

        this.sending.set(true);
        const convId = this.selectedConversation()!.id;

        this.chatService.sendMessage(convId, {
            content: this.newMessage
        }).subscribe({
            next: (msg: ChatMessage) => {
                this.messages.update(msgs => [...msgs, msg]);
                this.newMessage = '';
                this.sending.set(false);
                this.shouldScrollToBottom = true;
            },
            error: () => {
                this.sending.set(false);
            }
        });
    }

    private scrollToBottom(): void {
        try {
            if (this.messagesContainer) {
                this.messagesContainer.nativeElement.scrollTop =
                    this.messagesContainer.nativeElement.scrollHeight;
            }
        } catch (err) { }
    }

    isOwnMessage(msg: ChatMessage): boolean {
        return msg.senderType === 'SUPPORT' || msg.senderId === (this.currentUser as any)?.id;
    }

    getConvInitials(conv: Conversation | null): string {
        if (!conv) return '?';
        // Use title or generate from ID
        const title = conv.title || '';
        if (title) return title.slice(0, 2).toUpperCase();
        return conv.userId?.slice(0, 2).toUpperCase() || 'U';
    }

    formatTime(date: Date | string): string {
        if (!date) return '';
        return new Date(date).toLocaleTimeString('fr-FR', {
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    formatDate(date: Date | string | undefined): string {
        if (!date) return '';
        const d = new Date(date);
        const now = new Date();
        const diff = now.getTime() - d.getTime();
        const hours = Math.floor(diff / (1000 * 60 * 60));

        if (hours < 1) return 'À l\'instant';
        if (hours < 24) return `Il y a ${hours}h`;
        return d.toLocaleDateString('fr-FR');
    }

    refresh(): void {
        this.loadConversations();
        if (this.selectedConversation()) {
            this.loadMessages(this.selectedConversation()!.id);
        }
    }
}
