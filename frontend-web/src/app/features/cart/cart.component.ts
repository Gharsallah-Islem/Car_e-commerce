import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';

import { CartService } from '../../core/services/cart.service';
import { NotificationService } from '../../core/services/notification.service';
import { CartItem } from '../../core/models';

@Component({
    selector: 'app-cart',
    standalone: true,
    imports: [
        CommonModule,
        RouterLink,
        FormsModule,
        MatButtonModule,
        MatIconModule,
        MatCardModule,
        MatDividerModule,
        MatInputModule,
        MatFormFieldModule
    ],
    templateUrl: './cart.component.html',
    styleUrls: ['./cart.component.scss']
})
export class CartComponent implements OnInit {
    cartItems = signal<CartItem[]>([]);
    loading = signal<boolean>(false);
    couponCode = signal<string>('');
    discount = signal<number>(0);

    // Computed values
    subtotal = computed(() => {
        const items = this.cartItems();
        if (!items || items.length === 0) {
            return 0;
        }
        return items.reduce((sum, item) => {
            const price = item.product.discount && item.product.discount > 0
                ? item.product.price * (1 - item.product.discount / 100)
                : item.product.price;
            return sum + (price * item.quantity);
        }, 0);
    });

    tax = computed(() => {
        return this.subtotal() * 0.20; // 20% TVA
    });

    shipping = computed(() => {
        return this.subtotal() > 500 ? 0 : 30; // Free shipping over 500 MAD
    });

    total = computed(() => {
        return this.subtotal() + this.tax() + this.shipping() - this.discount();
    });

    isEmpty = computed(() => {
        const items = this.cartItems();
        return !items || items.length === 0;
    });

    // Check if all cart items have valid quantities
    hasInvalidQuantities = computed(() => {
        const items = this.cartItems();
        return items.some(item =>
            item.quantity <= 0 ||
            item.quantity > item.product.stock
        );
    });

    constructor(
        private router: Router,
        private cartService: CartService,
        private notificationService: NotificationService
    ) { }

    ngOnInit(): void {
        this.loadCart();

        // Subscribe to cart updates
        this.cartService.cart$.subscribe(cart => {
            if (cart && cart.items) {
                this.cartItems.set(cart.items);
            } else {
                this.cartItems.set([]);
            }
        });
    }

    loadCart(): void {
        this.loading.set(true);
        this.cartService.getCartFromBackend().subscribe({
            next: (cart) => {
                if (cart && cart.items) {
                    this.cartItems.set(cart.items);
                } else {
                    this.cartItems.set([]);
                }
                this.loading.set(false);
            },
            error: () => {
                // Cart loading handled by service - initialize with empty array
                this.cartItems.set([]);
                this.loading.set(false);
            }
        });
    }

    updateQuantity(item: CartItem, quantity: number): void {
        if (quantity < 1) {
            this.removeItem(item);
            return;
        }

        if (quantity > item.product.stock) {
            this.notificationService.warning(`Stock disponible: ${item.product.stock}`);
            return;
        }

        // Use product.id as the backend expects productId, not cartItemId
        this.cartService.updateCartItem({
            cartItemId: item.product.id,
            quantity: quantity
        }).subscribe();
    }

    increaseQuantity(item: CartItem): void {
        this.updateQuantity(item, item.quantity + 1);
    }

    decreaseQuantity(item: CartItem): void {
        this.updateQuantity(item, item.quantity - 1);
    }

    removeItem(item: CartItem): void {
        // Use product.id as the backend expects productId
        this.cartService.removeFromCart(item.product.id).subscribe({
            next: () => {
                this.notificationService.success('Produit retiré du panier');
            }
        });
    }

    clearCart(): void {
        if (confirm('Êtes-vous sûr de vouloir vider le panier ?')) {
            this.cartService.clearCart().subscribe({
                next: () => {
                    this.notificationService.success('Panier vidé');
                }
            });
        }
    }

    applyCoupon(): void {
        const code = this.couponCode();
        if (!code) {
            this.notificationService.warning('Veuillez entrer un code promo');
            return;
        }

        this.loading.set(true);
        // Simulate API call to validate coupon
        setTimeout(() => {
            // Mock validation - replace with actual API call
            if (code.toUpperCase() === 'WELCOME10') {
                this.discount.set(this.subtotal() * 0.10);
                this.notificationService.success('Code promo appliqué: 10% de réduction');
            } else if (code.toUpperCase() === 'SAVE20') {
                this.discount.set(this.subtotal() * 0.20);
                this.notificationService.success('Code promo appliqué: 20% de réduction');
            } else {
                this.notificationService.error('Code promo invalide');
            }
            this.loading.set(false);
        }, 500);
    }

    removeCoupon(): void {
        this.discount.set(0);
        this.couponCode.set('');
        this.notificationService.info('Code promo retiré');
    }

    continueShopping(): void {
        this.router.navigate(['/products']);
    }

    proceedToCheckout(): void {
        // Check if any item has invalid quantity
        if (this.hasInvalidQuantities()) {
            this.notificationService.error('Veuillez corriger les quantités invalides avant de continuer');
            return;
        }

        // Check if any item is out of stock
        const outOfStockItems = this.cartItems().filter(item => item.product.stock <= 0);
        if (outOfStockItems.length > 0) {
            this.notificationService.error('Certains articles ne sont plus en stock. Veuillez les retirer du panier.');
            return;
        }

        this.router.navigate(['/checkout']);
    }

    getItemPrice(item: CartItem): number {
        if (item.product.discount && item.product.discount > 0) {
            return item.product.price * (1 - item.product.discount / 100);
        }
        return item.product.price;
    }

    getItemTotal(item: CartItem): number {
        return this.getItemPrice(item) * item.quantity;
    }
}
