import { Component, OnInit, signal, HostListener, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRippleModule } from '@angular/material/core';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../core/services/auth.service';
import { CartService } from '../../core/services/cart.service';
import { ProductService } from '../../core/services/product.service';
import { Product, Category, CartItem } from '../../core/models';

@Component({
    selector: 'app-home',
    standalone: true,
    imports: [
        CommonModule,
        RouterLink,
        FormsModule,
        MatToolbarModule,
        MatButtonModule,
        MatIconModule,
        MatCardModule,
        MatMenuModule,
        MatBadgeModule,
        MatInputModule,
        MatFormFieldModule,
        MatDividerModule,
        MatProgressSpinnerModule,
        MatRippleModule
    ],
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit, AfterViewInit {
    // State signals
    searchQuery = signal<string>('');
    cartItemCount = signal<number>(0);

    // User state
    isLoggedIn = signal<boolean>(false);
    userName = signal<string>('');

    // Featured products - loaded from API
    featuredProducts = signal<Product[]>([]);
    isLoadingProducts = signal<boolean>(true);

    // Categories - loaded from API
    categories = signal<Category[]>([]);
    isLoadingCategories = signal<boolean>(true);

    // Animation states
    scrollY = 0;
    isScrolled = signal<boolean>(false);

    // Stats
    stats = [
        { value: '10,000+', label: 'Pièces Disponibles', icon: 'inventory_2' },
        { value: '5,000+', label: 'Clients Satisfaits', icon: 'people' },
        { value: '99%', label: 'Taux de Satisfaction', icon: 'star' },
        { value: '24/7', label: 'Support Client', icon: 'support_agent' }
    ];

    constructor(
        public authService: AuthService,
        public cartService: CartService,
        private productService: ProductService
    ) { }

    ngOnInit(): void {
        // Check authentication status
        this.isLoggedIn.set(this.authService.isAuthenticated());

        if (this.isLoggedIn()) {
            this.authService.currentUser$.subscribe(user => {
                if (user) {
                    this.userName.set(user.firstName || user.email);
                }
            });
        }

        // Get cart item count
        this.updateCartCount();

        // Load featured products from API
        this.loadFeaturedProducts();

        // Load categories from API
        this.loadCategories();
    }

    ngAfterViewInit(): void {
        // Initialize scroll animations
        this.observeElements();
    }

    @HostListener('window:scroll', ['$event'])
    onScroll(): void {
        this.scrollY = window.scrollY;
        this.isScrolled.set(this.scrollY > 50);
        this.handleParallax();
    }

    loadFeaturedProducts(): void {
        this.isLoadingProducts.set(true);
        // Get 8 newest products for featured section
        this.productService.getProducts({
            page: 0,
            size: 8,
            sort: 'newest'
        }).subscribe({
            next: (response) => {
                this.featuredProducts.set(response.content);
                this.isLoadingProducts.set(false);
            },
            error: (error) => {
                console.error('Error loading featured products:', error);
                this.isLoadingProducts.set(false);
            }
        });
    }

    loadCategories(): void {
        this.isLoadingCategories.set(true);
        this.productService.getCategories().subscribe({
            next: (categories) => {
                this.categories.set(categories);
                this.isLoadingCategories.set(false);
            },
            error: (error) => {
                console.error('Error loading categories:', error);
                this.isLoadingCategories.set(false);
            }
        });
    }

    updateCartCount(): void {
        this.cartService.cart$.subscribe(cart => {
            const count = cart?.items?.reduce((total: number, item: CartItem) => total + item.quantity, 0) || 0;
            this.cartItemCount.set(count);
        });
    }

    onSearch(): void {
        const query = this.searchQuery();
        if (query.trim()) {
            console.log('Searching for:', query);
            // TODO: Navigate to products page with search query
        }
    }

    onLogout(): void {
        this.authService.logout();
        this.isLoggedIn.set(false);
        this.userName.set('');
        this.updateCartCount();
    }

    scrollToSection(sectionId: string): void {
        const element = document.getElementById(sectionId);
        if (element) {
            element.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }

    private handleParallax(): void {
        const parallaxElements = document.querySelectorAll('.parallax');
        parallaxElements.forEach((element) => {
            const speed = 0.5;
            const yPos = -(this.scrollY * speed);
            (element as HTMLElement).style.transform = `translateY(${yPos}px)`;
        });
    }

    private observeElements(): void {
        const observerOptions = {
            root: null,
            threshold: 0.1,
            rootMargin: '0px 0px -100px 0px'
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('animate-in');
                }
            });
        }, observerOptions);

        // Observe all animatable elements
        const elements = document.querySelectorAll('.fade-in, .slide-up, .scale-in, .feature-card, .product-card, .category-card');
        elements.forEach((element) => observer.observe(element));
    }

    addToCart(product: Product): void {
        if (product.stock > 0) {
            this.cartService.addToCart({
                productId: product.id,
                quantity: 1
            }).subscribe({
                next: () => {
                    console.log('Product added to cart');
                },
                error: (error) => {
                    console.error('Error adding to cart:', error);
                }
            });
        }
    }
}
