import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatStepperModule } from '@angular/material/stepper';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatRadioModule } from '@angular/material/radio';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { CartService } from '../../core/services/cart.service';
import { NotificationService } from '../../core/services/notification.service';
import { AuthService } from '../../core/services/auth.service';
import { CartItem, User } from '../../core/models';

interface Address {
    fullName: string;
    phone: string;
    addressLine1: string;
    addressLine2?: string;
    city: string;
    postalCode: string;
    country: string;
}

@Component({
    selector: 'app-checkout',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatStepperModule,
        MatButtonModule,
        MatIconModule,
        MatCardModule,
        MatInputModule,
        MatFormFieldModule,
        MatRadioModule,
        MatCheckboxModule,
        MatDividerModule,
        MatProgressSpinnerModule
    ],
    templateUrl: './checkout.component.html',
    styleUrls: ['./checkout.component.scss']
})
export class CheckoutComponent implements OnInit {
    // Forms
    shippingForm!: FormGroup;
    paymentForm!: FormGroup;

    // State
    cartItems = signal<CartItem[]>([]);
    loading = signal<boolean>(false);
    processingPayment = signal<boolean>(false);
    orderPlaced = signal<boolean>(false);
    orderId = signal<string>('');
    currentUser = signal<User | null>(null);

    // Payment method
    selectedPaymentMethod = signal<'card' | 'cash'>('card');

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
        return this.subtotal() + this.tax() + this.shipping();
    });

    isEmpty = computed(() => {
        return this.cartItems().length === 0;
    });

    constructor(
        private fb: FormBuilder,
        private router: Router,
        private cartService: CartService,
        private authService: AuthService,
        private notificationService: NotificationService
    ) {
        this.initForms();
    }

    ngOnInit(): void {
        // Check if user is authenticated
        this.authService.currentUser$.subscribe(user => {
            this.currentUser.set(user);
            if (user) {
                this.prefillShippingForm(user);
            }
        });

        // Load cart
        this.cartService.cart$.subscribe(cart => {
            if (cart) {
                this.cartItems.set(cart.items);
            } else {
                this.cartItems.set([]);
            }

            // Redirect if cart is empty
            if (cart?.items.length === 0) {
                this.notificationService.warning('Votre panier est vide');
                this.router.navigate(['/cart']);
            }
        });
    }

    initForms(): void {
        // Shipping form
        this.shippingForm = this.fb.group({
            fullName: ['', [Validators.required, Validators.minLength(3)]],
            email: ['', [Validators.required, Validators.email]],
            phone: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
            addressLine1: ['', [Validators.required, Validators.minLength(5)]],
            addressLine2: [''],
            city: ['', [Validators.required]],
            postalCode: ['', [Validators.required, Validators.pattern(/^[0-9]{5}$/)]],
            country: ['Maroc', [Validators.required]],
            saveAddress: [false]
        });

        // Payment form
        this.paymentForm = this.fb.group({
            paymentMethod: ['card', [Validators.required]],
            // Card details (for Stripe integration)
            cardholderName: [''],
            saveCard: [false],
            // Cash on delivery
            cashNotes: ['']
        });
    }

    prefillShippingForm(user: User): void {
        if (user.email) {
            this.shippingForm.patchValue({
                fullName: `${user.firstName} ${user.lastName}`.trim() || '',
                email: user.email,
                phone: user.phoneNumber || ''
            });
        }
    }

    onPaymentMethodChange(method: 'card' | 'cash'): void {
        this.selectedPaymentMethod.set(method);
        this.paymentForm.patchValue({ paymentMethod: method });

        // Update validators based on payment method
        if (method === 'card') {
            this.paymentForm.get('cardholderName')?.setValidators([Validators.required]);
        } else {
            this.paymentForm.get('cardholderName')?.clearValidators();
        }
        this.paymentForm.get('cardholderName')?.updateValueAndValidity();
    }

    async processPayment(): Promise<void> {
        if (this.shippingForm.invalid || this.paymentForm.invalid) {
            this.notificationService.error('Veuillez remplir tous les champs obligatoires');
            return;
        }

        this.processingPayment.set(true);

        try {
            const paymentMethod = this.selectedPaymentMethod();

            if (paymentMethod === 'card') {
                // Process Stripe payment
                await this.processStripePayment();
            } else {
                // Process cash on delivery
                await this.processCashPayment();
            }
        } catch (error) {
            this.notificationService.error('Erreur lors du paiement. Veuillez réessayer.');
            this.processingPayment.set(false);
        }
    }

    async processStripePayment(): Promise<void> {
        // Simulate Stripe payment processing
        // In production, integrate with Stripe API
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                // Simulate successful payment
                this.completeOrder('ORD-' + Date.now());
                resolve();
            }, 2000);
        });
    }

    async processCashPayment(): Promise<void> {
        // Process cash on delivery order
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                this.completeOrder('ORD-' + Date.now());
                resolve();
            }, 1000);
        });
    }

    completeOrder(orderId: string): void {
        this.orderId.set(orderId);
        this.orderPlaced.set(true);
        this.processingPayment.set(false);

        // Clear cart
        this.cartService.clearCart().subscribe();

        // Show success message
        this.notificationService.success('Commande passée avec succès!');
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

    backToCart(): void {
        this.router.navigate(['/cart']);
    }

    continueShopping(): void {
        this.router.navigate(['/products']);
    }

    viewOrder(): void {
        this.router.navigate(['/profile/orders', this.orderId()]);
    }
}
