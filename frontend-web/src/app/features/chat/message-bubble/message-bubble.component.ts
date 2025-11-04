import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatMessage, SenderType } from '../../../core/models/chat.model';

@Component({
  selector: 'app-message-bubble',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './message-bubble.component.html',
  styleUrl: './message-bubble.component.scss'
})
export class MessageBubbleComponent {
  @Input() message!: ChatMessage;
  @Input() isOwn = false;
  @Input() showSenderName = false;
  @Output() delete = new EventEmitter<string>();

  get senderLabel(): string {
    switch (this.message.senderType) {
      case SenderType.SUPPORT:
        return 'Support Team';
      case SenderType.ADMIN:
        return 'Administrator';
      default:
        return 'You';
    }
  }

  get messageTime(): string {
    const date = new Date(this.message.createdAt);
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
  }

  onDelete(): void {
    if (this.message.id && confirm('Delete this message?')) {
      this.delete.emit(this.message.id);
    }
  }
}
