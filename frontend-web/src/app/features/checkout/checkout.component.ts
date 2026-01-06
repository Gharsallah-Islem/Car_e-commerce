import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
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
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Renderer2, Inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';

declare var Stripe: any;

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
export class CheckoutComponent implements OnInit, OnDestroy {
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

    // Stripe
    private stripe: any;
    private cardElement: any;

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
        private notificationService: NotificationService,
        private http: HttpClient,
        private renderer: Renderer2,
        @Inject(DOCUMENT) private document: Document
    ) {
        this.initForms();
    }

    ngOnInit(): void {
        // Initialize Stripe (but don't mount element yet)
        this.initializeStripe();

        // Check if user is authenticated
        this.authService.currentUser$.subscribe(user => {
            this.currentUser.set(user);
            if (user) {
                this.prefillShippingForm(user);
            }
        });

        // Load cart and validate stock
        this.cartService.cart$.subscribe(cart => {
            if (cart) {
                this.cartItems.set(cart.items);

                // Validate stock for all items BEFORE allowing checkout
                this.validateCartStock(cart.items);
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

    /**
     * Validate that all cart items have sufficient stock
     * Prevents proceeding to payment if stock is insufficient
     */
    validateCartStock(items: CartItem[]): void {
        const insufficientItems: string[] = [];

        for (const item of items) {
            if (item.product.stock < item.quantity) {
                insufficientItems.push(
                    `${item.product.name}: demandé ${item.quantity}, disponible ${item.product.stock}`
                );
            }
        }

        if (insufficientItems.length > 0) {
            const message = 'Stock insuffisant pour:\n' + insufficientItems.join('\n');
            this.notificationService.error(message);
            // Redirect back to cart to adjust quantities
            this.router.navigate(['/cart']);
        }
    }

    initializeStripe(): void {
        // Check if Stripe is loaded
        if (typeof Stripe === 'undefined') {
            console.error('Stripe.js is not loaded. Please check your index.html');
            return;
        }

        // Initialize Stripe with real publishable key
        if (!environment.stripePublicKey) {
            console.error('Stripe public key is not configured');
            return;
        }

        console.log('Initializing Stripe with key:', environment.stripePublicKey.substring(0, 20) + '...');
        this.stripe = Stripe(environment.stripePublicKey);

        // Create card element with more visible styling
        const elements = this.stripe.elements();
        this.cardElement = elements.create('card', {
            style: {
                base: {
                    fontSize: '16px',
                    color: '#32325d',
                    fontFamily: '"Helvetica Neue", Helvetica, sans-serif',
                    fontSmoothing: 'antialiased',
                    '::placeholder': {
                        color: '#aab7c4'
                    }
                },
                invalid: {
                    color: '#fa755a',
                    iconColor: '#fa755a'
                }
            }
        });

        // Set up event handlers (but don't mount yet)
        this.cardElement.on('change', (event: any) => {
            const displayError = document.getElementById('card-errors');
            if (displayError) {
                if (event.error) {
                    displayError.textContent = event.error.message;
                } else {
                    displayError.textContent = '';
                }
            }
        });
    }

    mountStripeElement(): void {
        if (!this.cardElement) {
            console.error('Stripe card element not initialized');
            return;
        }

        // Unmount if already mounted
        try {
            this.cardElement.unmount();
        } catch (e) {
            // Element might not be mounted yet, that's okay
        }

        // Wait for DOM to be ready and element to be visible
        setTimeout(() => {
            const cardElementContainer = document.getElementById('card-element');
            if (cardElementContainer) {
                try {
                    this.cardElement.mount('#card-element');
                    console.log('✓ Stripe element mounted successfully');
                } catch (error) {
                    console.error('✗ Stripe mount error:', error);
                }
            } else {
                console.error('✗ Card element container #card-element not found in DOM');
            }
        }, 300);
    }

    initForms(): void {
        // Shipping form
        this.shippingForm = this.fb.group({
            fullName: ['', [Validators.required, Validators.minLength(3)]],
            email: ['', [Validators.required, Validators.email]],
            phone: ['', [Validators.required, Validators.pattern(/^[0-9]{8}$/)]],
            addressLine1: ['', [Validators.required, Validators.minLength(5)]],
            addressLine2: [''],
            city: ['', [Validators.required]],
            postalCode: ['', [Validators.required, Validators.pattern(/^[0-9]{4}$/)]],
            country: ['Tunisie', [Validators.required]],
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

    onStepperSelectionChange(event: any): void {
        // When payment step (step index 1) is selected and card payment is chosen, mount Stripe element
        if (event.selectedIndex === 1 && this.selectedPaymentMethod() === 'card') {
            setTimeout(() => {
                this.mountStripeElement();
            }, 200);
        }
    }

    onPaymentMethodChange(method: 'card' | 'cash'): void {
        this.selectedPaymentMethod.set(method);
        this.paymentForm.patchValue({ paymentMethod: method });

        // Update validators based on payment method
        if (method === 'card') {
            this.paymentForm.get('cardholderName')?.setValidators([Validators.required]);
            // Mount Stripe element when card payment is selected
            setTimeout(() => {
                this.mountStripeElement();
            }, 200);
        } else {
            this.paymentForm.get('cardholderName')?.clearValidators();
            // Unmount Stripe element when switching away from card
            if (this.cardElement) {
                try {
                    this.cardElement.unmount();
                } catch (e) {
                    // Ignore unmount errors
                }
            }
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
        try {
            // Create payment method from card element
            const { error, paymentMethod } = await this.stripe.createPaymentMethod({
                type: 'card',
                card: this.cardElement,
                billing_details: {
                    name: this.paymentForm.get('cardholderName')?.value,
                    email: this.shippingForm.get('email')?.value,
                    address: {
                        line1: this.shippingForm.get('addressLine1')?.value,
                        city: this.shippingForm.get('city')?.value,
                        postal_code: this.shippingForm.get('postalCode')?.value,
                        country: 'MA'
                    }
                }
            });

            if (error) {
                this.notificationService.error(error.message || 'Erreur de paiement');
                this.processingPayment.set(false);
                return;
            }

            // Create order in backend
            await this.createOrderInBackend('STRIPE', paymentMethod.id);

        } catch (error: any) {
            console.error('Payment error:', error);
            this.notificationService.error('Erreur lors du traitement du paiement');
            this.processingPayment.set(false);
        }
    }

    async processCashPayment(): Promise<void> {
        try {
            // Create order in backend with cash payment
            await this.createOrderInBackend('CASH_ON_DELIVERY', null);
        } catch (error: any) {
            console.error('Order creation error:', error);
            this.notificationService.error('Erreur lors de la création de la commande');
            this.processingPayment.set(false);
        }
    }

    async createOrderInBackend(paymentMethod: string, paymentIntentId: string | null): Promise<void> {
        const orderData = {
            shippingAddress: `${this.shippingForm.get('addressLine1')?.value}, ${this.shippingForm.get('city')?.value}, ${this.shippingForm.get('postalCode')?.value}`,
            paymentMethod: paymentMethod,
            notes: this.paymentForm.get('cashNotes')?.value || ''
        };

        this.http.post<any>(`${environment.apiUrl}/orders`, orderData).subscribe({
            next: (order) => {
                this.completeOrder(order.id);
                this.notificationService.success('Commande créée avec succès!');
            },
            error: (error) => {
                console.error('Backend order creation failed:', error);

                // Check for stock-related errors
                const errorMessage = error.error?.message || error.message || '';
                if (errorMessage.includes('stock') || errorMessage.includes('Insufficient')) {
                    this.notificationService.error('Stock insuffisant pour un ou plusieurs produits. Veuillez modifier votre panier.');
                    this.router.navigate(['/cart']);
                } else if (errorMessage.includes('out of stock')) {
                    this.notificationService.error('Un produit est en rupture de stock. Veuillez modifier votre panier.');
                    this.router.navigate(['/cart']);
                } else {
                    this.notificationService.error('Erreur lors de la création de la commande');
                }
                this.processingPayment.set(false);
            }
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

    ngOnDestroy(): void {
        // Clean up Stripe element when component is destroyed
        if (this.cardElement) {
            try {
                this.cardElement.unmount();
                this.cardElement.destroy();
            } catch (e) {
                // Ignore cleanup errors
            }
        }
    }
}
