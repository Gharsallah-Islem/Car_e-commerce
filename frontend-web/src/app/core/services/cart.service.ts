import { Injectable, signal, computed } from '@angular/core';
import { Observable, BehaviorSubject, tap, catchError, throwError } from 'rxjs';
import { ApiService } from './api.service';
import { StorageService } from './storage.service';
import { NotificationService } from './notification.service';
import {
    Cart,
    CartItem,
    AddToCartRequest,
    UpdateCartItemRequest,
    CartSummary
} from '../models';

/**
 * Shopping Cart Service
 * Manages cart state both for authenticated and guest users
 */
@Injectable({
    providedIn: 'root'
})
export class CartService {
    // Cart state with signals
    private cartSubject = new BehaviorSubject<Cart | null>(null);
    public cart$ = this.cartSubject.asObservable();

    public cart = signal<Cart | null>(null);
    public cartItemCount = computed(() => {
        const cart = this.cart();
        return cart?.totalItems || 0;
    });
    public cartTotal = computed(() => {
        const cart = this.cart();
        return cart?.totalAmount || 0;
    });
    public isEmpty = computed(() => this.cartItemCount() === 0);

    constructor(
        private apiService: ApiService,
        private storageService: StorageService,
        private notificationService: NotificationService
    ) {
        this.loadCart();
    }

    /**
     * Load cart from backend or localStorage
     */
    loadCart(): void {
        const token = this.storageService.getToken();

        if (token) {
            // Authenticated user - fetch from backend
            this.getCartFromBackend().subscribe({
                error: () => {
                    // If backend fails, try localStorage
                    this.loadCartFromStorage();
                }
            });
        } else {
            // Guest user - load from localStorage
            this.loadCartFromStorage();
        }
    }

    /**
     * Get cart from backend (authenticated users)
     */
    getCartFromBackend(): Observable<Cart> {
        return this.apiService.get<Cart>('cart').pipe(
            tap(cart => {
                this.updateCartState(cart);
            }),
            catchError(error => {
                console.error('Failed to load cart', error);
                return throwError(() => error);
            })
        );
    }

    /**
     * Add item to cart
     */
    addToCart(request: AddToCartRequest): Observable<Cart> {
        const token = this.storageService.getToken();

        if (token) {
            // Authenticated user - add via API
            return this.apiService.post<Cart>('cart/add', request).pipe(
                tap(cart => {
                    this.updateCartState(cart);
                    this.notificationService.success('Product added to cart');
                }),
                catchError(error => {
                    this.notificationService.error(error.error?.message || 'Failed to add to cart');
                    return throwError(() => error);
                })
            );
        } else {
            // Guest user - add to localStorage
            return this.addToLocalCart(request);
        }
    }

    /**
     * Update cart item quantity
     */
    updateCartItem(request: UpdateCartItemRequest): Observable<Cart> {
        const token = this.storageService.getToken();

        if (token) {
            return this.apiService.put<Cart>('cart/update', request).pipe(
                tap(cart => {
                    this.updateCartState(cart);
                    this.notificationService.success('Cart updated');
                }),
                catchError(error => {
                    this.notificationService.error('Failed to update cart');
                    return throwError(() => error);
                })
            );
        } else {
            return this.updateLocalCartItem(request);
        }
    }

    /**
     * Remove item from cart
     */
    removeFromCart(cartItemId: number): Observable<Cart> {
        const token = this.storageService.getToken();

        if (token) {
            return this.apiService.delete<Cart>(`cart/remove/${cartItemId}`).pipe(
                tap(cart => {
                    this.updateCartState(cart);
                    this.notificationService.success('Item removed from cart');
                }),
                catchError(error => {
                    this.notificationService.error('Failed to remove item');
                    return throwError(() => error);
                })
            );
        } else {
            return this.removeFromLocalCart(cartItemId);
        }
    }

    /**
     * Clear cart
     */
    clearCart(): Observable<void> {
        const token = this.storageService.getToken();

        if (token) {
            return this.apiService.delete<void>('cart/clear').pipe(
                tap(() => {
                    this.updateCartState(null);
                    this.notificationService.success('Cart cleared');
                }),
                catchError(error => {
                    this.notificationService.error('Failed to clear cart');
                    return throwError(() => error);
                })
            );
        } else {
            this.storageService.removeCart();
            this.updateCartState(null);
            this.notificationService.success('Cart cleared');
            return new Observable(observer => {
                observer.next();
                observer.complete();
            });
        }
    }

    /**
     * Get cart summary (subtotal, tax, shipping, etc.)
     */
    getCartSummary(): CartSummary {
        const cart = this.cart();

        if (!cart || cart.items.length === 0) {
            return {
                subtotal: 0,
                tax: 0,
                shipping: 0,
                discount: 0,
                total: 0,
                itemCount: 0
            };
        }

        const subtotal = cart.totalAmount;
        const tax = subtotal * 0.2; // 20% VAT (adjust as needed)
        const shipping = subtotal > 100 ? 0 : 10; // Free shipping over €100
        const discount = 0; // Can be calculated based on promo codes
        const total = subtotal + tax + shipping - discount;

        return {
            subtotal,
            tax,
            shipping,
            discount,
            total,
            itemCount: cart.totalItems
        };
    }

    /**
     * Merge guest cart with user cart on login
     */
    mergeGuestCart(): void {
        const guestCart = this.storageService.getCart();

        if (guestCart && guestCart.items && guestCart.items.length > 0) {
            // Send guest cart items to backend
            this.apiService.post<Cart>('cart/merge', guestCart).subscribe({
                next: (cart) => {
                    this.updateCartState(cart);
                    this.storageService.removeCart();
                    this.notificationService.success('Cart merged successfully');
                },
                error: () => {
                    this.notificationService.warning('Failed to merge cart');
                }
            });
        }
    }

    /**
     * Add to localStorage cart (guest users)
     */
    private addToLocalCart(request: AddToCartRequest): Observable<Cart> {
        return new Observable(observer => {
            try {
                let cart = this.storageService.getCart() || this.createEmptyCart();

                // Check if product already exists
                const existingItem = cart.items.find((item: CartItem) => item.product.id === request.productId);

                if (existingItem) {
                    existingItem.quantity += request.quantity;
                    existingItem.subtotal = existingItem.price * existingItem.quantity;
                } else {
                    // In real scenario, fetch product details from backend
                    // For now, create a placeholder
                    const newItem: CartItem = {
                        id: Date.now(),
                        product: { id: request.productId } as any, // Placeholder
                        quantity: request.quantity,
                        price: 0, // Should fetch from product
                        subtotal: 0
                    };
                    cart.items.push(newItem);
                }

                this.recalculateCart(cart);
                this.storageService.saveCart(cart);
                this.updateCartState(cart);
                this.notificationService.success('Product added to cart');

                observer.next(cart);
                observer.complete();
            } catch (error) {
                this.notificationService.error('Failed to add to cart');
                observer.error(error);
            }
        });
    }

    /**
     * Update localStorage cart item
     */
    private updateLocalCartItem(request: UpdateCartItemRequest): Observable<Cart> {
        return new Observable(observer => {
            try {
                const cart = this.storageService.getCart();

                if (!cart) {
                    throw new Error('Cart not found');
                }

                const item = cart.items.find((i: CartItem) => i.id === request.cartItemId);

                if (item) {
                    item.quantity = request.quantity;
                    item.subtotal = item.price * item.quantity;

                    this.recalculateCart(cart);
                    this.storageService.saveCart(cart);
                    this.updateCartState(cart);
                    this.notificationService.success('Cart updated');
                }

                observer.next(cart);
                observer.complete();
            } catch (error) {
                this.notificationService.error('Failed to update cart');
                observer.error(error);
            }
        });
    }

    /**
     * Remove from localStorage cart
     */
    private removeFromLocalCart(cartItemId: number): Observable<Cart> {
        return new Observable(observer => {
            try {
                const cart = this.storageService.getCart();

                if (!cart) {
                    throw new Error('Cart not found');
                }

                cart.items = cart.items.filter((item: CartItem) => item.id !== cartItemId);

                this.recalculateCart(cart);
                this.storageService.saveCart(cart);
                this.updateCartState(cart);
                this.notificationService.success('Item removed from cart');

                observer.next(cart);
                observer.complete();
            } catch (error) {
                this.notificationService.error('Failed to remove item');
                observer.error(error);
            }
        });
    }

    /**
     * Load cart from localStorage
     */
    private loadCartFromStorage(): void {
        const cart = this.storageService.getCart();
        if (cart) {
            this.updateCartState(cart);
        }
    }

    /**
     * Update cart state
     */
    private updateCartState(cart: Cart | null): void {
        this.cart.set(cart);
        this.cartSubject.next(cart);
    }

    /**
     * Recalculate cart totals
     */
    private recalculateCart(cart: Cart): void {
        cart.totalAmount = cart.items.reduce((sum: number, item: CartItem) => sum + item.subtotal, 0);
        cart.totalItems = cart.items.reduce((sum: number, item: CartItem) => sum + item.quantity, 0);
    }

    /**
     * Create empty cart
     */
    private createEmptyCart(): Cart {
        return {
            items: [],
            totalAmount: 0,
            totalItems: 0
        };
    }
}
