import { Component, OnInit, signal, ViewChild, ElementRef } from '@angular/core';
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

    // State
    selectedTabIndex = signal<number>(0);

    // Image Identification
    uploadedImage = signal<string | null>(null);
    identificationLoading = signal<boolean>(false);
    identificationResult = signal<IdentificationResult | null>(null);

    constructor(
        private notificationService: NotificationService,
        private aiService: AiService,
        private router: Router
    ) { }

    ngOnInit(): void {
        // Component initialization - image recognition only
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

    onDrop(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();

        const files = event.dataTransfer?.files;
        if (files && files.length > 0) {
            const file = files[0];

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
                        confidence: response.confidence * 100,
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
            // Navigate to the first matching product
            const productId = result.products[0].id;
            console.log('Navigating to product:', productId);
            this.router.navigate(['/products', productId]);
        } else if (result?.partName) {
            // If no exact product match, search in catalog by part name
            console.log('No products found, searching catalog for:', result.partName);
            this.router.navigate(['/catalogue'], {
                queryParams: { search: result.partName }
            });
        } else {
            this.notificationService.warning('Aucun produit correspondant trouvé');
        }
    }

    viewProduct(productId: string | number): void {
        this.router.navigate(['/products', productId]);
    }

    addToCart(): void {
        const result = this.identificationResult();
        if (result?.products && result.products.length > 0) {
            const product = result.products[0];
            // Import CartService and add the product
            // For now, navigate to the product page where user can add to cart
            this.router.navigate(['/products', product.id]);
            this.notificationService.info('Cliquez sur "Ajouter au panier" sur la page du produit');
        } else {
            this.notificationService.warning('Aucun produit trouvé à ajouter au panier');
        }
    }
}
