import { Component, Input, Output, EventEmitter, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-message-input',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './message-input.component.html',
  styleUrl: './message-input.component.scss'
})
export class MessageInputComponent {
  @Input() disabled = false;
  @Input() placeholder = 'Type a message...';
  @Output() sendMessage = new EventEmitter<string>();
  @ViewChild('messageInput') messageInputRef!: ElementRef<HTMLTextAreaElement>;

  messageText = '';

  send(): void {
    const trimmedMessage = this.messageText.trim();
    if (trimmedMessage) {
      this.sendMessage.emit(trimmedMessage);
      this.messageText = '';
      this.adjustTextareaHeight();
    }
  }

  onKeyDown(event: KeyboardEvent): void {
    // Enter without Shift = send message
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  onInput(): void {
    this.adjustTextareaHeight();
  }

  private adjustTextareaHeight(): void {
    if (this.messageInputRef) {
      const textarea = this.messageInputRef.nativeElement;
      textarea.style.height = 'auto';
      textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px';
    }
  }

  attachFile(): void {
    // TODO: Implement file upload in Phase 3
    alert('File attachment coming soon!');
  }
}
