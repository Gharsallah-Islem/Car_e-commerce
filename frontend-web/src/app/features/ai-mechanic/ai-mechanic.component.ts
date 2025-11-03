import { Component, OnInit, signal, computed, ViewChild, ElementRef } from '@angular/core';
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
import { Router } from '@angular/router';

interface IdentificationResult {
    partName: string;
    partNumber: string;
    confidence: number;
    category: string;
    brand: string;
    price: number;
    stock: number;
    compatibility: string[];
    imageUrl: string;
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
export class AiMechanicComponent implements OnInit {
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

    // Suggested questions
    suggestedQuestions = [
        'Comment changer mes plaquettes de frein ?',
        'Quel type d\'huile moteur dois-je utiliser ?',
        'Comment diagnostiquer un problème de démarrage ?',
        'Quels sont les signes d\'usure des amortisseurs ?',
        'Comment entretenir ma batterie ?'
    ];

    constructor(
        private notificationService: NotificationService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.initializeChat();
    }

    initializeChat(): void {
        // Welcome message
        this.chatMessages.set([
            {
                id: '1',
                text: 'Bonjour ! Je suis votre assistant mécanique virtuel. Comment puis-je vous aider aujourd\'hui ? Vous pouvez me poser des questions sur l\'entretien automobile, le diagnostic de problèmes, ou me montrer une photo d\'une pièce pour l\'identifier.',
                sender: 'ai',
                timestamp: new Date()
            }
        ]);
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
        this.identificationLoading.set(true);

        // Simulate AI identification process (2-3 seconds)
        setTimeout(() => {
            // Mock result - in production, this would call an AI API
            const mockResult: IdentificationResult = {
                partName: 'Plaquettes de frein avant',
                partNumber: 'BRK-FR-001',
                confidence: 94.5,
                category: 'Freinage',
                brand: 'Brembo',
                price: 89.99,
                stock: 45,
                compatibility: [
                    'Peugeot 208 (2012-2019)',
                    'Renault Clio IV (2012-2019)',
                    'Citroën C3 (2010-2016)'
                ],
                imageUrl: 'https://placehold.co/300x200/e3f2fd/1976d2?text=Brake+Pads'
            };

            this.identificationResult.set(mockResult);
            this.identificationLoading.set(false);
            this.notificationService.success('Pièce identifiée avec succès !');
        }, 2500);
    }

    viewProductDetails(): void {
        // Navigate to product detail page
        this.router.navigate(['/products', 1]); // Mock product ID
    }

    addToCart(): void {
        this.notificationService.success('Produit ajouté au panier');
    }

    // Chatbot
    sendMessage(): void {
        const message = this.userMessage().trim();
        if (!message) return;

        // Add user message
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

        // Simulate AI response (1-2 seconds)
        setTimeout(() => {
            const aiResponse = this.generateAIResponse(message);
            this.chatMessages.update(messages => [...messages, aiResponse]);
            this.chatLoading.set(false);
            setTimeout(() => this.scrollToBottom(), 100);
        }, 1500);
    }

    generateAIResponse(userMessage: string): ChatMessage {
        const lowerMessage = userMessage.toLowerCase();

        // Simple keyword-based responses (in production, use actual AI/LLM)
        let responseText = '';
        let relatedProducts = undefined;

        if (lowerMessage.includes('frein') || lowerMessage.includes('plaquette')) {
            responseText = 'Pour changer vos plaquettes de frein :\n\n1. Soulevez le véhicule et retirez la roue\n2. Retirez les vis de l\'étrier\n3. Remplacez les plaquettes usées\n4. Remontez l\'étrier et la roue\n5. Pompez la pédale de frein plusieurs fois\n\n⚠️ Important : Vérifiez également l\'état des disques de frein. Si vous n\'êtes pas sûr, consultez un professionnel.';
            relatedProducts = [
                { id: 1, name: 'Plaquettes de frein Brembo', price: 89.99, imageUrl: 'https://placehold.co/300x200/e3f2fd/1976d2?text=Brake+Pads' },
                { id: 2, name: 'Disques de frein avant', price: 159.99, imageUrl: 'https://placehold.co/300x200/e3f2fd/1976d2?text=Brake+Discs' }
            ];
        } else if (lowerMessage.includes('huile') || lowerMessage.includes('moteur')) {
            responseText = 'Le choix de l\'huile moteur dépend de votre véhicule :\n\n• Consultez le manuel du propriétaire pour la viscosité recommandée (ex: 5W-30)\n• Huile synthétique : meilleure protection, intervalles plus longs\n• Huile minérale : moins chère, convient aux moteurs anciens\n• Huile semi-synthétique : bon compromis\n\nFréquence de vidange : tous les 10 000 à 15 000 km ou 1 fois par an.';
            relatedProducts = [
                { id: 3, name: 'Huile moteur 5W-30 Castrol', price: 45.99, imageUrl: 'https://placehold.co/300x200/fff3e0/f57c00?text=Motor+Oil' },
                { id: 4, name: 'Filtre à huile Bosch', price: 12.99, imageUrl: 'https://placehold.co/300x200/fff3e0/f57c00?text=Oil+Filter' }
            ];
        } else if (lowerMessage.includes('démarrage') || lowerMessage.includes('batterie')) {
            responseText = 'Problèmes de démarrage - Causes possibles :\n\n1. **Batterie déchargée** : Testez la tension (12.6V à pleine charge)\n2. **Alternateur défaillant** : Vérifiez la charge (13.5-14.5V moteur en marche)\n3. **Démarreur HS** : Écoutez les clics au démarrage\n4. **Bougies d\'allumage** : Vérifiez l\'état et l\'écartement\n5. **Pompe à carburant** : Écoutez le bruit à la mise du contact\n\nCommencez par vérifier la batterie et les connexions.';
            relatedProducts = [
                { id: 5, name: 'Batterie 12V 70Ah', price: 89.99, imageUrl: 'https://placehold.co/300x200/e8f5e9/388e3c?text=Car+Battery' }
            ];
        } else if (lowerMessage.includes('amortisseur') || lowerMessage.includes('suspension')) {
            responseText = 'Signes d\'usure des amortisseurs :\n\n✓ Véhicule qui rebondit après un dos d\'âne\n✓ Usure irrégulière des pneus\n✓ Distance de freinage allongée\n✓ Fuite d\'huile sur l\'amortisseur\n✓ Bruits métalliques dans les virages\n\nTest simple : Appuyez fort sur un coin du véhicule et relâchez. Si le véhicule rebondit plus de 2 fois, les amortisseurs sont usés.';
            relatedProducts = [
                { id: 6, name: 'Amortisseurs avant Monroe', price: 159.99, imageUrl: 'https://placehold.co/300x200/fce4ec/c2185b?text=Shocks' }
            ];
        } else if (lowerMessage.includes('prix') || lowerMessage.includes('coût')) {
            responseText = 'Les prix varient selon les pièces :\n\n• Plaquettes de frein : 50-150 MAD\n• Disques de frein : 120-250 MAD\n• Amortisseurs : 150-400 MAD/unité\n• Batterie : 800-1500 MAD\n• Vidange d\'huile : 300-600 MAD\n\nConsultez notre catalogue pour des prix détaillés et des promotions.';
        } else {
            responseText = 'Je comprends votre question. Pour vous aider au mieux, pourriez-vous me donner plus de détails ?\n\nVous pouvez me poser des questions sur :\n• L\'entretien et la maintenance\n• Le diagnostic de pannes\n• Le choix de pièces détachées\n• Les compatibilités véhicules\n• Les procédures de remplacement\n\nOu utilisez l\'onglet "Identification" pour identifier une pièce via photo.';
        }

        return {
            id: Date.now().toString(),
            text: responseText,
            sender: 'ai',
            timestamp: new Date(),
            relatedProducts
        };
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

    viewProduct(productId: number): void {
        this.router.navigate(['/products', productId]);
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
