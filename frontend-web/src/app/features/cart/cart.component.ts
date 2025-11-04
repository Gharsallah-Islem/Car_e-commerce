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
        return this.cartItems().reduce((sum, item) => {
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
        return this.cartItems().length === 0;
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
            if (cart) {
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
                this.cartItems.set(cart.items);
                this.loading.set(false);
            },
            error: () => {
                // Cart loading handled by service
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

        if (!item.id) {
            this.notificationService.error('Erreur: ID de l\'article invalide');
            return;
        }

        this.cartService.updateCartItem({
            cartItemId: item.id,
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
        if (!item.id) {
            this.notificationService.error('Erreur: ID de l\'article invalide');
            return;
        }

        this.cartService.removeFromCart(item.id).subscribe({
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
