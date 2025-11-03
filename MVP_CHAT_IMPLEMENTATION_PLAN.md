# MVP Chat Implementation Plan - Polling-Based
**Start Date:** November 3, 2025  
**Target Completion:** November 14-17, 2025 (1-2 weeks)  
**Approach:** Polling-based real-time updates (3-second interval)

---

## ✅ Phase 1: Backend Fixes (COMPLETED - Nov 3, 2025)

### Tasks Completed:
- [x] Fixed `GET /api/chat/support/conversations` - Support dashboard endpoint
- [x] Fixed `POST /api/chat/support` - Start support chat endpoint
- [x] Added ConversationRepository injection to ChatController

**Result:** All 14 backend endpoints now fully functional! ✅

---

## 🔄 Phase 2: Frontend Foundation (Nov 4-5, 2025 - 2 days)

### Task 2.1: Fix Type Definitions (2 hours)
**File:** `frontend-web/src/app/core/models/chat.model.ts`

**Changes Required:**
```typescript
// BEFORE (current - mismatched types):
export interface ChatMessage {
    id?: number;  // ❌ Backend uses UUID
    conversationId: number;  // ❌
    senderId: number;  // ❌
    senderRole: 'CLIENT' | 'ADMIN';  // ❌ Backend uses USER/SUPPORT/ADMIN
    // ... extra fields not in backend
}

// AFTER (fixed to match backend):
export interface ChatMessage {
    id?: string;  // ✅ UUID
    conversationId: string;  // ✅ UUID
    senderId: string;  // ✅ UUID
    senderType: 'USER' | 'SUPPORT' | 'ADMIN';  // ✅ Match backend
    content: string;
    attachmentUrl?: string;  // ✅ Match backend field name
    isRead: boolean;
    createdAt: Date;
}

export interface Conversation {
    id: string;  // ✅ UUID
    userId: string;  // ✅ Changed from clientId
    title?: string;
    isActive: boolean;
    messages?: ChatMessage[];  // ✅ Add for convenience
    lastMessage?: ChatMessage;
    createdAt: Date;
    updatedAt: Date;
}

// NEW DTOs for API calls:
export interface SendMessageRequest {
    content: string;
    attachmentUrl?: string;
}

export interface MessageDTO {
    content: string;
    attachmentUrl?: string;
}
```

**Action Items:**
- [ ] Update ChatMessage interface (remove id type mismatch)
- [ ] Update Conversation interface (userId instead of clientId)
- [ ] Add SendMessageRequest DTO
- [ ] Remove unused fields (senderName, adminId, adminName)

**Files to modify:**
- `frontend-web/src/app/core/models/chat.model.ts`
- `frontend-web/src/app/core/models/index.ts` (verify export)

---

### Task 2.2: Create ChatService (1 day)
**File:** `frontend-web/src/app/core/services/chat.service.ts`

**Methods to Implement:**

```typescript
@Injectable({ providedIn: 'root' })
export class ChatService {
  private apiUrl = `${environment.apiUrl}/api/chat`;
  
  constructor(private http: HttpClient) {}

  // 1. Conversation Management
  startSupportChat(): Observable<Conversation> {
    return this.http.post<Conversation>(`${this.apiUrl}/support`, {});
  }

  getUserConversations(): Observable<Conversation[]> {
    return this.http.get<Conversation[]>(`${this.apiUrl}/conversations`);
  }

  getConversationById(conversationId: string): Observable<Conversation> {
    return this.http.get<Conversation>(`${this.apiUrl}/conversations/${conversationId}`);
  }

  // 2. Message Operations
  getConversationMessages(
    conversationId: string, 
    page: number = 0, 
    size: number = 50
  ): Observable<Page<ChatMessage>> {
    return this.http.get<Page<ChatMessage>>(
      `${this.apiUrl}/conversations/${conversationId}/messages`,
      { params: { page: page.toString(), size: size.toString() } }
    );
  }

  // 🔑 KEY METHOD FOR POLLING:
  getRecentMessages(
    conversationId: string, 
    since: Date
  ): Observable<ChatMessage[]> {
    const timestamp = since.toISOString();
    return this.http.get<ChatMessage[]>(
      `${this.apiUrl}/conversations/${conversationId}/messages/recent`,
      { params: { since: timestamp } }
    );
  }

  sendMessage(
    conversationId: string, 
    request: SendMessageRequest
  ): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(
      `${this.apiUrl}/conversations/${conversationId}/messages`,
      request
    );
  }

  searchMessages(
    conversationId: string, 
    query: string, 
    page: number = 0
  ): Observable<Page<ChatMessage>> {
    return this.http.get<Page<ChatMessage>>(
      `${this.apiUrl}/conversations/${conversationId}/messages/search`,
      { params: { query, page: page.toString(), size: '20' } }
    );
  }

  // 3. Read Status
  markMessageAsRead(messageId: string): Observable<ChatMessage> {
    return this.http.patch<ChatMessage>(
      `${this.apiUrl}/messages/${messageId}/read`,
      {}
    );
  }

  markAllAsRead(conversationId: string): Observable<void> {
    return this.http.patch<void>(
      `${this.apiUrl}/conversations/${conversationId}/read-all`,
      {}
    );
  }

  getUnreadCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/unread-count`);
  }

  // 4. Admin Operations
  deleteMessage(messageId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/messages/${messageId}`);
  }

  archiveConversation(conversationId: string): Observable<void> {
    return this.http.patch<void>(
      `${this.apiUrl}/conversations/${conversationId}/archive`,
      {}
    );
  }

  // 5. Support Dashboard (for staff)
  getAllActiveConversations(): Observable<Conversation[]> {
    return this.http.get<Conversation[]>(`${this.apiUrl}/support/conversations`);
  }
}

// Helper interface for pagination
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
```

**Action Items:**
- [ ] Generate service: `ng generate service core/services/chat`
- [ ] Implement all 13 methods above
- [ ] Add proper error handling
- [ ] Add HTTP interceptor for authentication (if not already present)

---

## 🎨 Phase 3: UI Components (Nov 6-10, 2025 - 5 days)

### Task 3.1: Generate Components
```bash
cd frontend-web

# Main chat page
ng generate component features/chat/chat-page --skip-tests

# Sub-components
ng generate component features/chat/conversation-list --skip-tests
ng generate component features/chat/chat-window --skip-tests
ng generate component features/chat/message-bubble --skip-tests
ng generate component features/chat/message-input --skip-tests

# Support button (for header/navbar)
ng generate component shared/components/support-chat-button --skip-tests
```

---

### Task 3.2: ConversationListComponent (Day 1)
**File:** `conversation-list.component.ts`

**Features:**
- Display list of user's conversations
- Show last message preview
- Unread count badge
- Click to open conversation
- Real-time updates (new message = move to top)

**Template Structure:**
```html
<div class="conversation-list">
  <div class="conversation-header">
    <h2>Messages</h2>
    <button (click)="startNewChat()">+ New Chat</button>
  </div>
  
  <div class="conversation-items">
    <div 
      *ngFor="let conv of conversations" 
      class="conversation-item"
      [class.active]="selectedConversationId === conv.id"
      [class.unread]="hasUnread(conv)"
      (click)="selectConversation(conv.id)">
      
      <div class="avatar">
        <i class="icon-support"></i>
      </div>
      
      <div class="conversation-info">
        <div class="conversation-title">
          {{ conv.title || 'Support Chat' }}
          <span class="timestamp">{{ conv.updatedAt | timeAgo }}</span>
        </div>
        <div class="last-message">
          {{ conv.lastMessage?.content | truncate:50 }}
        </div>
      </div>
      
      <div class="unread-badge" *ngIf="getUnreadCount(conv) > 0">
        {{ getUnreadCount(conv) }}
      </div>
    </div>
  </div>
</div>
```

**Component Logic:**
```typescript
export class ConversationListComponent implements OnInit {
  @Output() conversationSelected = new EventEmitter<string>();
  
  conversations: Conversation[] = [];
  selectedConversationId?: string;
  
  constructor(private chatService: ChatService) {}
  
  ngOnInit() {
    this.loadConversations();
    // Refresh every 10 seconds to catch new conversations
    setInterval(() => this.loadConversations(), 10000);
  }
  
  loadConversations() {
    this.chatService.getUserConversations().subscribe(conversations => {
      this.conversations = conversations.sort((a, b) => 
        new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
      );
    });
  }
  
  selectConversation(conversationId: string) {
    this.selectedConversationId = conversationId;
    this.conversationSelected.emit(conversationId);
  }
  
  startNewChat() {
    this.chatService.startSupportChat().subscribe(conversation => {
      this.conversations.unshift(conversation);
      this.selectConversation(conversation.id);
    });
  }
}
```

---

### Task 3.3: ChatWindowComponent (Day 2-3) - CRITICAL
**File:** `chat-window.component.ts`

**Features:**
- Display messages in conversation
- Auto-scroll to bottom on new message
- **POLLING: Check for new messages every 3 seconds**
- Mark messages as read when viewed
- Loading states

**Template Structure:**
```html
<div class="chat-window" *ngIf="conversationId">
  <!-- Header -->
  <div class="chat-header">
    <div class="chat-title">
      <i class="icon-support"></i>
      <span>Support Chat</span>
    </div>
    <div class="chat-actions">
      <button (click)="archiveChat()" title="Archive">
        <i class="icon-archive"></i>
      </button>
    </div>
  </div>
  
  <!-- Messages Container -->
  <div class="messages-container" #messagesContainer>
    <div class="loading" *ngIf="loading">
      Loading messages...
    </div>
    
    <div class="messages-list" *ngIf="!loading">
      <app-message-bubble
        *ngFor="let message of messages"
        [message]="message"
        [isOwn]="isOwnMessage(message)"
        (delete)="deleteMessage($event)">
      </app-message-bubble>
    </div>
    
    <!-- Typing indicator (optional) -->
    <div class="typing-indicator" *ngIf="isTyping">
      Support is typing...
    </div>
  </div>
  
  <!-- Input Area -->
  <app-message-input
    [disabled]="sending"
    (sendMessage)="onSendMessage($event)">
  </app-message-input>
</div>

<div class="no-conversation" *ngIf="!conversationId">
  <i class="icon-chat"></i>
  <p>Select a conversation or start a new chat</p>
  <button (click)="startNewChat()">Start Chat</button>
</div>
```

**Component Logic with POLLING:**
```typescript
export class ChatWindowComponent implements OnInit, OnDestroy {
  @Input() conversationId?: string;
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;
  
  messages: ChatMessage[] = [];
  loading = false;
  sending = false;
  
  private pollingInterval?: any;
  private lastPollTimestamp?: Date;
  private currentUserId?: string;
  
  constructor(
    private chatService: ChatService,
    private authService: AuthService
  ) {}
  
  ngOnInit() {
    this.currentUserId = this.authService.getCurrentUserId();
    if (this.conversationId) {
      this.loadMessages();
      this.startPolling();
    }
  }
  
  ngOnDestroy() {
    this.stopPolling();
  }
  
  @Input()
  set conversation(id: string | undefined) {
    if (id !== this.conversationId) {
      this.stopPolling();
      this.conversationId = id;
      if (id) {
        this.loadMessages();
        this.startPolling();
      }
    }
  }
  
  loadMessages() {
    if (!this.conversationId) return;
    
    this.loading = true;
    this.chatService.getConversationMessages(this.conversationId, 0, 50)
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
  startPolling() {
    this.stopPolling(); // Clear any existing interval
    
    this.pollingInterval = setInterval(() => {
      if (!this.conversationId || !this.lastPollTimestamp) return;
      
      this.chatService.getRecentMessages(
        this.conversationId, 
        this.lastPollTimestamp
      ).subscribe({
        next: (newMessages) => {
          if (newMessages && newMessages.length > 0) {
            console.log(`Received ${newMessages.length} new message(s)`);
            this.messages.push(...newMessages);
            this.lastPollTimestamp = new Date();
            this.scrollToBottom();
            this.markAllAsRead();
            
            // Optional: Play notification sound
            // this.playNotificationSound();
          }
        },
        error: (err) => {
          console.error('Polling error:', err);
        }
      });
    }, 3000); // Poll every 3 seconds
  }
  
  stopPolling() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
      this.pollingInterval = null;
    }
  }
  
  onSendMessage(content: string) {
    if (!this.conversationId || !content.trim()) return;
    
    this.sending = true;
    const request: SendMessageRequest = { content: content.trim() };
    
    this.chatService.sendMessage(this.conversationId, request)
      .subscribe({
        next: (message) => {
          this.messages.push(message);
          this.lastPollTimestamp = new Date(); // Update to avoid re-fetching own message
          this.sending = false;
          this.scrollToBottom();
        },
        error: (err) => {
          console.error('Failed to send message:', err);
          this.sending = false;
        }
      });
  }
  
  scrollToBottom() {
    setTimeout(() => {
      if (this.messagesContainer) {
        const container = this.messagesContainer.nativeElement;
        container.scrollTop = container.scrollHeight;
      }
    }, 100);
  }
  
  markAllAsRead() {
    if (!this.conversationId) return;
    
    // Mark all unread messages
    const unreadMessages = this.messages.filter(m => 
      !m.isRead && !this.isOwnMessage(m)
    );
    
    if (unreadMessages.length > 0) {
      this.chatService.markAllAsRead(this.conversationId).subscribe();
    }
  }
  
  isOwnMessage(message: ChatMessage): boolean {
    return message.senderId === this.currentUserId;
  }
  
  deleteMessage(messageId: string) {
    if (confirm('Delete this message?')) {
      this.chatService.deleteMessage(messageId).subscribe(() => {
        this.messages = this.messages.filter(m => m.id !== messageId);
      });
    }
  }
  
  archiveChat() {
    if (!this.conversationId) return;
    
    if (confirm('Archive this conversation?')) {
      this.chatService.archiveConversation(this.conversationId).subscribe(() => {
        // Notify parent to remove from list
        this.conversationId = undefined;
        this.messages = [];
        this.stopPolling();
      });
    }
  }
  
  startNewChat() {
    this.chatService.startSupportChat().subscribe(conversation => {
      this.conversationId = conversation.id;
      this.loadMessages();
      this.startPolling();
    });
  }
}
```

**CRITICAL POINTS:**
- ✅ Polling runs every 3 seconds via `setInterval`
- ✅ Uses `lastPollTimestamp` to only fetch new messages
- ✅ Updates timestamp after each poll to prevent duplicates
- ✅ Cleans up interval on destroy to prevent memory leaks
- ✅ Auto-scrolls to bottom on new messages

---

### Task 3.4: MessageBubbleComponent (Day 4)
**File:** `message-bubble.component.ts`

**Template:**
```html
<div class="message-bubble" [class.own]="isOwn" [class.support]="!isOwn">
  <div class="message-content">
    <div class="sender-info" *ngIf="!isOwn">
      <strong>{{ getSenderLabel() }}</strong>
    </div>
    
    <div class="message-text">
      {{ message.content }}
    </div>
    
    <div class="message-attachment" *ngIf="message.attachmentUrl">
      <a [href]="message.attachmentUrl" target="_blank">
        <i class="icon-attachment"></i> View Attachment
      </a>
    </div>
    
    <div class="message-footer">
      <span class="timestamp">{{ message.createdAt | date:'short' }}</span>
      <span class="read-status" *ngIf="isOwn">
        <i class="icon-check" *ngIf="message.isRead">✓✓</i>
        <i class="icon-check-single" *ngIf="!message.isRead">✓</i>
      </span>
    </div>
  </div>
  
  <button 
    class="delete-btn" 
    *ngIf="isOwn" 
    (click)="onDelete()"
    title="Delete message">
    <i class="icon-trash"></i>
  </button>
</div>
```

**Component:**
```typescript
export class MessageBubbleComponent {
  @Input() message!: ChatMessage;
  @Input() isOwn = false;
  @Output() delete = new EventEmitter<string>();
  
  getSenderLabel(): string {
    switch (this.message.senderType) {
      case 'SUPPORT': return 'Support Team';
      case 'ADMIN': return 'Administrator';
      default: return 'User';
    }
  }
  
  onDelete() {
    if (this.message.id) {
      this.delete.emit(this.message.id);
    }
  }
}
```

---

### Task 3.5: MessageInputComponent (Day 4)
**File:** `message-input.component.ts`

**Template:**
```html
<div class="message-input-container">
  <div class="input-wrapper">
    <textarea
      #messageInput
      [(ngModel)]="messageText"
      (keydown.enter)="onEnter($event)"
      [disabled]="disabled"
      placeholder="Type your message..."
      rows="1">
    </textarea>
    
    <button 
      class="attach-btn" 
      (click)="attachFile()"
      [disabled]="disabled"
      title="Attach file">
      <i class="icon-paperclip"></i>
    </button>
  </div>
  
  <button 
    class="send-btn" 
    (click)="send()"
    [disabled]="disabled || !messageText.trim()">
    <i class="icon-send"></i> Send
  </button>
</div>
```

**Component:**
```typescript
export class MessageInputComponent {
  @Input() disabled = false;
  @Output() sendMessage = new EventEmitter<string>();
  
  messageText = '';
  
  send() {
    if (this.messageText.trim()) {
      this.sendMessage.emit(this.messageText.trim());
      this.messageText = '';
    }
  }
  
  onEnter(event: KeyboardEvent) {
    if (!event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }
  
  attachFile() {
    // TODO: Implement file upload in Phase 3
    alert('File attachment coming soon!');
  }
}
```

---

### Task 3.6: ChatPageComponent (Day 5)
**File:** `chat-page.component.ts`

**Template:**
```html
<div class="chat-page">
  <div class="chat-container">
    <!-- Sidebar: Conversation List -->
    <div class="chat-sidebar">
      <app-conversation-list
        [selectedConversationId]="selectedConversationId"
        (conversationSelected)="onConversationSelected($event)">
      </app-conversation-list>
    </div>
    
    <!-- Main: Chat Window -->
    <div class="chat-main">
      <app-chat-window
        [conversationId]="selectedConversationId">
      </app-chat-window>
    </div>
  </div>
</div>
```

**Component:**
```typescript
export class ChatPageComponent {
  selectedConversationId?: string;
  
  onConversationSelected(conversationId: string) {
    this.selectedConversationId = conversationId;
  }
}
```

---

## 🛣️ Phase 4: Routing & Navigation (Nov 11, 2025 - 2 hours)

### Task 4.1: Add Chat Routes
**File:** `frontend-web/src/app/app.routes.ts`

```typescript
import { ChatPageComponent } from './features/chat/chat-page/chat-page.component';

export const routes: Routes = [
  // ... existing routes
  {
    path: 'chat',
    component: ChatPageComponent,
    canActivate: [AuthGuard],
    data: { title: 'Messages' }
  },
  // ... other routes
];
```

### Task 4.2: Add Support Chat Button to Navbar
**File:** `shared/components/support-chat-button/support-chat-button.component.ts`

```typescript
export class SupportChatButtonComponent implements OnInit {
  unreadCount = 0;
  
  constructor(
    private chatService: ChatService,
    private router: Router
  ) {}
  
  ngOnInit() {
    this.loadUnreadCount();
    // Refresh every 10 seconds
    setInterval(() => this.loadUnreadCount(), 10000);
  }
  
  loadUnreadCount() {
    this.chatService.getUnreadCount().subscribe(count => {
      this.unreadCount = count;
    });
  }
  
  openChat() {
    this.router.navigate(['/chat']);
  }
}
```

**Template:**
```html
<button class="support-chat-btn" (click)="openChat()">
  <i class="icon-chat"></i>
  <span>Support</span>
  <span class="badge" *ngIf="unreadCount > 0">{{ unreadCount }}</span>
</button>
```

---

## 🎨 Phase 5: Styling (Nov 12, 2025 - 1 day)

### Chat Page Layout (chat-page.component.scss)
```scss
.chat-page {
  height: calc(100vh - 60px); // Adjust based on navbar height
  background: #f5f5f5;
}

.chat-container {
  display: grid;
  grid-template-columns: 350px 1fr;
  height: 100%;
  max-width: 1400px;
  margin: 0 auto;
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.chat-sidebar {
  border-right: 1px solid #e0e0e0;
  overflow-y: auto;
}

.chat-main {
  display: flex;
  flex-direction: column;
}

@media (max-width: 768px) {
  .chat-container {
    grid-template-columns: 1fr;
  }
  
  .chat-sidebar {
    display: none; // Or implement mobile toggle
  }
}
```

### Message Bubbles (message-bubble.component.scss)
```scss
.message-bubble {
  display: flex;
  margin: 8px 0;
  padding: 0 16px;
  
  &.own {
    justify-content: flex-end;
    
    .message-content {
      background: #007bff;
      color: white;
      border-radius: 18px 18px 4px 18px;
    }
  }
  
  &.support {
    justify-content: flex-start;
    
    .message-content {
      background: #f0f0f0;
      color: #333;
      border-radius: 18px 18px 18px 4px;
    }
  }
}

.message-content {
  max-width: 60%;
  padding: 12px 16px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.message-text {
  white-space: pre-wrap;
  word-break: break-word;
}

.message-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
  font-size: 0.75rem;
  opacity: 0.7;
}
```

---

## ✅ Phase 6: Testing & Bug Fixes (Nov 13-14, 2025 - 2 days)

### Test Cases:

**Backend API Tests:**
- [ ] POST /api/chat/support - Creates new conversation
- [ ] GET /api/chat/conversations - Returns user's conversations
- [ ] GET /api/chat/conversations/{id}/messages - Paginated messages
- [ ] GET /api/chat/conversations/{id}/messages/recent - Polling works
- [ ] POST /api/chat/conversations/{id}/messages - Sends message
- [ ] GET /api/chat/support/conversations - Support dashboard (SUPPORT role)
- [ ] PATCH /api/chat/messages/{id}/read - Marks as read
- [ ] GET /api/chat/unread-count - Returns correct count

**Frontend Component Tests:**
- [ ] ChatService - All API calls work
- [ ] ConversationList - Displays conversations
- [ ] ChatWindow - Polling receives new messages every 3 seconds
- [ ] MessageBubble - Displays own vs support messages differently
- [ ] MessageInput - Enter key sends, Shift+Enter newline

**Integration Tests:**
- [ ] User starts new chat → conversation created
- [ ] User sends message → appears in chat window
- [ ] Support replies → user receives via polling within 3 seconds
- [ ] Unread count updates correctly
- [ ] Mark all as read works
- [ ] Archive conversation works

**Performance Tests:**
- [ ] Polling doesn't cause memory leaks (check with long session)
- [ ] Pagination works for conversations with 100+ messages
- [ ] Multiple conversations can be switched without issues

---

## 🚀 Phase 7: Deployment & Documentation (Nov 15-17, 2025)

### Deployment Checklist:
- [ ] Build frontend: `npm run build --configuration production`
- [ ] Run backend tests: `mvn test`
- [ ] Check for console errors
- [ ] Test on different browsers (Chrome, Firefox, Safari)
- [ ] Test responsive design (mobile, tablet, desktop)
- [ ] Update API documentation
- [ ] Create user guide for support team

### Git Workflow:
```bash
# After each phase:
git add .
git commit -m "feat(chat): Implement [feature name] - Phase X"
git push origin develop

# Final merge:
git checkout main
git merge develop
git push origin main
git tag -a v1.0-chat-mvp -m "Chat MVP - Polling-based"
git push --tags
```

---

## 📊 Progress Tracking

| Phase | Tasks | Time Est. | Status | Actual Time |
|-------|-------|-----------|--------|-------------|
| 1. Backend Fixes | 2 endpoints | 3 hours | ✅ DONE | 1 hour |
| 2. Frontend Foundation | Models + Service | 2 days | 🔄 IN PROGRESS | - |
| 3. UI Components | 5 components | 5 days | ⏳ PENDING | - |
| 4. Routing | Routes + nav | 2 hours | ⏳ PENDING | - |
| 5. Styling | CSS/SCSS | 1 day | ⏳ PENDING | - |
| 6. Testing | Unit + E2E | 2 days | ⏳ PENDING | - |
| 7. Deployment | Deploy + docs | 2 days | ⏳ PENDING | - |
| **TOTAL** | | **12-14 days** | **8% complete** | - |

---

## 🎯 Success Criteria

MVP is complete when:
- ✅ User can start support chat from any page
- ✅ User can send messages to support
- ✅ Support can reply (via support dashboard)
- ✅ Messages appear within 3 seconds (polling)
- ✅ Unread count badge shows on navbar
- ✅ Mark as read functionality works
- ✅ Conversation history persists
- ✅ Archive conversation works
- ✅ Responsive design works on mobile
- ✅ No memory leaks from polling

---

## 🔮 Future Enhancements (Post-MVP)

After MVP is complete, consider:
1. **WebSocket upgrade** (instant messaging) - 2 weeks
2. **File uploads** (images, videos) - 3 days
3. **Typing indicators** - 1 day
4. **Push notifications** - 2 days
5. **Support team assignment** - 3 days
6. **Canned responses** - 2 days
7. **Chat analytics** - 1 week

---

**Next Immediate Steps:**
1. Fix frontend type definitions (chat.model.ts)
2. Create ChatService
3. Generate UI components
4. Start building ChatWindowComponent with polling

Let's get started! 🚀
