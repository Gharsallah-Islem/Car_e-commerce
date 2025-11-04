import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConversationListComponent } from '../conversation-list/conversation-list.component';
import { ChatWindowComponent } from '../chat-window/chat-window.component';

@Component({
  selector: 'app-chat-page',
  standalone: true,
  imports: [CommonModule, ConversationListComponent, ChatWindowComponent],
  templateUrl: './chat-page.component.html',
  styleUrl: './chat-page.component.scss'
})
export class ChatPageComponent {
  selectedConversationId: string | null = null;
  showSidebar = true;

  onConversationSelected(conversationId: string): void {
    this.selectedConversationId = conversationId;

    // On mobile, hide sidebar when conversation is selected
    if (window.innerWidth <= 768) {
      this.showSidebar = false;
    }
  }

  toggleSidebar(): void {
    this.showSidebar = !this.showSidebar;
  }

  backToList(): void {
    this.showSidebar = true;
  }
}
