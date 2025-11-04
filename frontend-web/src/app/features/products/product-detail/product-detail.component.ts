import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDividerModule } from '@angular/material/divider';

import { ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Product } from '../../../core/models';

@Component({
    selector: 'app-product-detail',
    standalone: true,
    imports: [
        CommonModule,
        RouterLink,
        MatButtonModule,
        MatIconModule,
        MatCardModule,
        MatProgressSpinnerModule,
        MatTabsModule,
        MatDividerModule
    ],
    templateUrl: './product-detail.component.html',
    styleUrls: ['./product-detail.component.scss']
})
export class ProductDetailComponent implements OnInit {
    product = signal<Product | null>(null);
    relatedProducts = signal<Product[]>([]);
    loading = signal<boolean>(false);
    quantity = signal<number>(1);
    selectedImage = signal<number>(0);

    // Computed properties for safe access
    hasRating = computed(() => {
        const p = this.product();
        return p?.rating !== undefined && p.rating !== null && p.rating > 0;
    });

    hasDiscount = computed(() => {
        const p = this.product();
        return p?.discount !== undefined && p.discount !== null && p.discount > 0;
    });

    hasSpecifications = computed(() => {
        const p = this.product();
        return p?.specifications !== undefined && p.specifications !== null && p.specifications.length > 0;
    });

    hasCompatibility = computed(() => {
        const p = this.product();
        return p?.compatibilityString !== undefined && p.compatibilityString !== null && p.compatibilityString.trim().length > 0;
    });

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private productService: ProductService,
        private cartService: CartService,
        private notificationService: NotificationService
    ) { }

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            const id = params['id']; // Keep as string (UUID from backend)
            if (id) {
                this.loadProduct(id);
            }
        });
    }

    loadProduct(id: string | number): void {
        this.loading.set(true);

        this.productService.getProductById(id).subscribe({
            next: (product) => {
                this.product.set(product);
                this.loading.set(false);
                // For related products, try to extract a numeric ID or use the string ID
                const numericId = typeof id === 'number' ? id : parseInt(id, 10);
                if (!isNaN(numericId)) {
                    this.loadRelatedProducts(numericId);
                }
            },
            error: (error: any) => {
                console.error('Error loading product:', error);
                this.notificationService.error('Produit non trouvé');
                this.loading.set(false);
                this.router.navigate(['/products']);
            }
        });
    }

    loadRelatedProducts(productId: number): void {
        this.productService.getRelatedProducts(productId, 4).subscribe({
            next: (products) => this.relatedProducts.set(products),
            error: (error: any) => console.error('Error loading related products:', error)
        });
    }

    increaseQuantity(): void {
        const product = this.product();
        if (product && this.quantity() < product.stock) {
            this.quantity.update(q => q + 1);
        }
    }

    decreaseQuantity(): void {
        if (this.quantity() > 1) {
            this.quantity.update(q => q - 1);
        }
    }

    addToCart(): void {
        const product = this.product();
        if (!product) return;

        if (product.stock <= 0) {
            this.notificationService.warning('Ce produit est en rupture de stock');
            return;
        }

        this.cartService.addToCart({
            productId: product.id,
            quantity: this.quantity()
        }).subscribe({
            next: () => {
                this.notificationService.success(`${product.name} ajouté au panier`);
                this.quantity.set(1);
            },
            error: (error: any) => {
                console.error('Error adding to cart:', error);
                this.notificationService.error('Erreur lors de l\'ajout au panier');
            }
        });
    }

    selectImage(index: number): void {
        this.selectedImage.set(index);
    }

    buyNow(): void {
        this.addToCart();
        this.router.navigate(['/cart']);
    }
}
