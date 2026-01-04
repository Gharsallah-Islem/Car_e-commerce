import { Component, OnInit, OnDestroy, signal, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTabsModule } from '@angular/material/tabs';

import { NotificationService } from '../../core/services/notification.service';
import { ChatService } from '../../core/services/chat.service';
import { AiService, AnalyzedProduct } from '../../core/services/ai.service';
import { Router } from '@angular/router';

interface IdentificationResult {
    partName: string;
    partNumber: string;
    confidence: number;
    confidencePercent: string;
    category: string;
    brand: string;
    price: number;
    stock: number;
    compatibility: string[];
    imageUrl: string;
    products: AnalyzedProduct[];
    productsFound: boolean;
}

interface ChatMessage {
    id: string;
    text: string;
    sender: 'user' | 'ai';
    timestamp: Date;
    relatedProducts?: {
        id: number;
        name: string;
        price: number;
        imageUrl: string;
    }[];
}

@Component({
    selector: 'app-ai-mechanic',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatButtonModule,
        MatIconModule,
        MatCardModule,
        MatInputModule,
        MatFormFieldModule,
        MatProgressSpinnerModule,
        MatChipsModule,
        MatDividerModule,
        MatTooltipModule,
        MatTabsModule
    ],
    templateUrl: './ai-mechanic.component.html',
    styleUrls: ['./ai-mechanic.component.scss']
})
export class AiMechanicComponent implements OnInit, OnDestroy {
    @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
    @ViewChild('chatContainer') chatContainer!: ElementRef<HTMLDivElement>;

    // State
    selectedTabIndex = signal<number>(0);

    // Image Identification
    uploadedImage = signal<string | null>(null);
    identificationLoading = signal<boolean>(false);
    identificationResult = signal<IdentificationResult | null>(null);

    // Chatbot
    chatMessages = signal<ChatMessage[]>([]);
    userMessage = signal<string>('');
    chatLoading = signal<boolean>(false);

    // Real backend integration
    private conversationId: string | null = null;
    private pollingInterval: any;

    // Suggested questions
    suggestedQuestions = [
        'I need brake pads for my car',
        'Do you have oil filters in stock?',
        'What brake discs do you recommend?',
        'I need help choosing car parts',
        'Show me your best deals'
    ];

    constructor(
        private notificationService: NotificationService,
        private chatService: ChatService,
        private aiService: AiService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.initializeChat();
    }

    ngOnDestroy(): void {
        if (this.pollingInterval) {
            clearInterval(this.pollingInterval);
        }
    }

    initializeChat(): void {
        // Start real conversation with backend
        this.chatService.startSupportChat().subscribe({
            next: (conversation) => {
                this.conversationId = conversation.id;
                console.log('✅ AI Conversation started:', this.conversationId);

                // Welcome message
                this.chatMessages.set([
                    {
                        id: '1',
                        text: 'Bonjour ! Je suis votre assistant mécanique virtuel alimenté par IA Gemini. Comment puis-je vous aider aujourd\'hui ? Vous pouvez me poser des questions sur l\'entretien automobile, le diagnostic de problèmes, ou les pièces détachées disponibles en stock.',
                        sender: 'ai',
                        timestamp: new Date()
                    }
                ]);

                // Start polling for new messages
                // this.startPolling(); // DISABLED to prevent refreshing
            },
            error: (error) => {
                console.error('❌ Error starting conversation:', error);
                this.notificationService.error('Erreur de connexion au service de chat');

                // Fallback welcome message
                this.chatMessages.set([
                    {
                        id: '1',
                        text: 'Désolé, je ne peux pas me connecter au serveur pour le moment. Veuillez réessayer plus tard.',
                        sender: 'ai',
                        timestamp: new Date()
                    }
                ]);
            }
        });
    }

    startPolling(): void {
        // DISABLED - Polling causes annoying page refresh
        // We'll manually poll after sending messages instead
    }

    // Image Upload & Identification
    onFileSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files && input.files[0]) {
            const file = input.files[0];

            // Validate file type
            if (!file.type.startsWith('image/')) {
                this.notificationService.error('Veuillez sélectionner une image valide');
                return;
            }

            // Validate file size (max 5MB)
            if (file.size > 5 * 1024 * 1024) {
                this.notificationService.error('L\'image ne doit pas dépasser 5 MB');
                return;
            }

            // Read and display image
            const reader = new FileReader();
            reader.onload = (e) => {
                this.uploadedImage.set(e.target?.result as string);
                this.identifyPart();
            };
            reader.readAsDataURL(file);
        }
    }

    triggerFileInput(): void {
        this.fileInput.nativeElement.click();
    }

    clearImage(): void {
        this.uploadedImage.set(null);
        this.identificationResult.set(null);
        if (this.fileInput) {
            this.fileInput.nativeElement.value = '';
        }
    }

    identifyPart(): void {
        const imageData = this.uploadedImage();
        if (!imageData) {
            this.notificationService.error('No image to analyze');
            return;
        }

        this.identificationLoading.set(true);
        console.log('🔍 Sending image to AI for analysis...');

        this.aiService.analyzePartImage(imageData).subscribe({
            next: (response) => {
                console.log('✅ AI Analysis response:', response);

                if (response.success) {
                    // Generate compatibility list based on part type
                    const getCompatibility = (partName: string): string[] => {
                        const partLower = partName.toLowerCase();
                        // Common part types have universal compatibility
                        if (partLower.includes('brake') || partLower.includes('pad') || partLower.includes('rotor')) {
                            return ['Renault Clio', 'Peugeot 208', 'Volkswagen Golf', 'Toyota Corolla'];
                        } else if (partLower.includes('oil') || partLower.includes('filter')) {
                            return ['Tous véhicules', 'Universel'];
                        } else if (partLower.includes('battery') || partLower.includes('alternator')) {
                            return ['12V Standard', 'Véhicules européens'];
                        } else if (partLower.includes('headlight') || partLower.includes('taillight')) {
                            return ['Selon modèle', 'Vérifier référence'];
                        }
                        return ['Compatibilité universelle'];
                    };

                    // Map the API response to our IdentificationResult format
                    const result: IdentificationResult = {
                        partName: response.partName,
                        partNumber: response.recommendationId?.substring(0, 8).toUpperCase() || 'N/A',
                        confidence: response.confidence * 100, // Convert to percentage
                        confidencePercent: response.confidencePercent,
                        category: 'Auto Parts',
                        brand: 'Various',
                        price: response.products.length > 0 ? response.products[0].price : 0,
                        stock: response.products.length > 0 ? response.products[0].stock : 0,
                        compatibility: getCompatibility(response.partName),
                        imageUrl: imageData,
                        products: response.products,
                        productsFound: response.productsFound
                    };

                    this.identificationResult.set(result);

                    if (response.productsFound) {
                        this.notificationService.success(
                            `Part identified: ${response.partName} - ${response.products.length} product(s) found!`
                        );
                    } else {
                        this.notificationService.info(
                            `Part identified: ${response.partName} - No exact matches in catalog`
                        );
                    }
                } else {
                    this.notificationService.warning('Could not identify the part. Try a clearer image.');
                }

                this.identificationLoading.set(false);
            },
            error: (error) => {
                console.error('❌ AI Analysis error:', error);
                this.identificationLoading.set(false);
                this.notificationService.error('Failed to analyze image. Please try again.');
            }
        });
    }

    viewProductDetails(): void {
        const result = this.identificationResult();
        if (result?.products && result.products.length > 0) {
            // Navigate to the first matched product
            this.router.navigate(['/products', result.products[0].id]);
        } else {
            // Fallback: search products by part name
            this.router.navigate(['/products'], {
                queryParams: { search: result?.partName || '' }
            });
        }
    }

    viewProduct(productId: string | number): void {
        this.router.navigate(['/products', productId]);
    }

    addToCart(): void {
        this.notificationService.success('Produit ajouté au panier');
    }

    clearHistory(): void {
        if (!this.conversationId) return;

        if (confirm('Êtes-vous sûr de vouloir effacer l\'historique de la conversation ?')) {
            this.chatService.archiveConversation(this.conversationId).subscribe({
                next: () => {
                    this.notificationService.success('Historique effacé');
                    this.chatMessages.set([]);
                    // Restart conversation to get a new ID
                    this.initializeChat();
                },
                error: (err) => {
                    console.error('Error clearing history:', err);
                    this.notificationService.error('Erreur lors de la suppression de l\'historique');
                }
            });
        }
    }

    // Chatbot - Real AI Integration
    sendMessage(): void {
        const message = this.userMessage().trim();
        if (!message || !this.conversationId) {
            if (!this.conversationId) {
                this.notificationService.error('Connexion au chat non établie');
            }
            return;
        }

        // Add user message to UI immediately
        const userMsg: ChatMessage = {
            id: Date.now().toString(),
            text: message,
            sender: 'user',
            timestamp: new Date()
        };

        this.chatMessages.update(messages => [...messages, userMsg]);
        this.userMessage.set('');
        this.chatLoading.set(true);

        // Scroll to bottom
        setTimeout(() => this.scrollToBottom(), 100);

        console.log('📤 Sending message to AI:', message);

        // Send message to backend - AI will respond automatically
        this.chatService.sendMessage(this.conversationId, {
            content: message
        }).subscribe({
            next: (sentMessage) => {
                console.log('✅ Message sent successfully, waiting for AI response...');

                // Fetch AI response after a short delay (give AI time to generate)
                setTimeout(() => {
                    this.fetchAiResponse();
                }, 2000);
            },
            error: (error) => {
                console.error('❌ Error sending message:', error);
                this.chatLoading.set(false);
                this.notificationService.error('Erreur lors de l\'envoi du message');

                // Add error message
                this.chatMessages.update(messages => [...messages, {
                    id: Date.now().toString(),
                    text: 'Désolé, une erreur s\'est produite. Veuillez réessayer.',
                    sender: 'ai',
                    timestamp: new Date()
                }]);
            }
        });
    }

    fetchAiResponse(): void {
        if (!this.conversationId) return;

        // Get messages since just before we sent ours
        const since = new Date(Date.now() - 10000);

        this.chatService.getRecentMessages(this.conversationId, since).subscribe({
            next: (messages) => {
                let aiResponded = false;
                messages.forEach(msg => {
                    // Only add if not already in list and is from AI
                    const exists = this.chatMessages().some(m => m.id === msg.id);
                    if (!exists && msg.senderType !== 'USER') {
                        const chatMsg: ChatMessage = {
                            id: msg.id!,
                            text: msg.content,
                            sender: 'ai',
                            timestamp: msg.createdAt
                        };
                        this.chatMessages.update(msgs => [...msgs, chatMsg]);
                        aiResponded = true;
                    }
                });

                if (aiResponded) {
                    this.chatLoading.set(false);
                    setTimeout(() => this.scrollToBottom(), 100);
                } else {
                    // If no response yet, try again in 2 seconds
                    setTimeout(() => this.fetchAiResponse(), 2000);
                }
            },
            error: (err) => {
                console.error('Error fetching AI response:', err);
                this.chatLoading.set(false);
            }
        });
    }

    sendSuggestedQuestion(question: string): void {
        this.userMessage.set(question);
        this.sendMessage();
    }

    scrollToBottom(): void {
        if (this.chatContainer) {
            const container = this.chatContainer.nativeElement;
            container.scrollTop = container.scrollHeight;
        }
    }

    formatTimestamp(date: Date): string {
        const now = new Date();
        const diff = now.getTime() - date.getTime();
        const minutes = Math.floor(diff / 60000);

        if (minutes < 1) return 'À l\'instant';
        if (minutes < 60) return `Il y a ${minutes} min`;

        const hours = Math.floor(minutes / 60);
        if (hours < 24) return `Il y a ${hours}h`;

        return date.toLocaleDateString('fr-FR');
    }
}
