import { Component, OnInit, signal } from '@angular/core';
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
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../core/services/auth.service';
import { CartService } from '../../core/services/cart.service';
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
        MatDividerModule
    ],
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
    // State signals
    searchQuery = signal<string>('');
    cartItemCount = signal<number>(0);

    // User state
    isLoggedIn = signal<boolean>(false);
    userName = signal<string>('');

    // Featured products (mock data for now - will connect to API later)
    featuredProducts = signal<Product[]>([
        {
            id: 1,
            name: 'Plaquettes de frein avant',
            description: 'Plaquettes de frein haute performance pour une conduite sûre',
            price: 45.99,
            stock: 25,
            imageUrl: 'https://placehold.co/300x200/e3f2fd/1976d2?text=Brake+Pads',
            category: { id: 1, name: 'Freinage', description: 'Système de freinage' },
            brand: { id: 1, name: 'Bosch' },
            sku: 'BP-001',
            createdAt: new Date(),
            updatedAt: new Date()
        },
        {
            id: 2,
            name: 'Filtre à huile',
            description: 'Filtre à huile de qualité supérieure pour moteur',
            price: 12.99,
            stock: 50,
            imageUrl: 'https://placehold.co/300x200/fff3e0/f57c00?text=Oil+Filter',
            category: { id: 2, name: 'Filtres', description: 'Filtration' },
            brand: { id: 2, name: 'Mann-Filter' },
            sku: 'OF-002',
            createdAt: new Date(),
            updatedAt: new Date()
        },
        {
            id: 3,
            name: 'Batterie 12V',
            description: 'Batterie automobile longue durée 60Ah',
            price: 89.99,
            stock: 15,
            imageUrl: 'https://placehold.co/300x200/e8f5e9/388e3c?text=Car+Battery',
            category: { id: 3, name: 'Électrique', description: 'Système électrique' },
            brand: { id: 3, name: 'Varta' },
            sku: 'BAT-003',
            createdAt: new Date(),
            updatedAt: new Date()
        },
        {
            id: 4,
            name: 'Essuie-glaces',
            description: 'Paire d\'essuie-glaces universels 55cm',
            price: 19.99,
            stock: 40,
            imageUrl: 'https://placehold.co/300x200/fce4ec/c2185b?text=Wiper+Blades',
            category: { id: 4, name: 'Accessoires', description: 'Accessoires auto' },
            brand: { id: 1, name: 'Bosch' },
            sku: 'WB-004',
            createdAt: new Date(),
            updatedAt: new Date()
        }
    ]);

    // Categories
    categories = signal<Category[]>([
        { id: 1, name: 'Freinage', description: 'Plaquettes, disques, étriers' },
        { id: 2, name: 'Filtres', description: 'Huile, air, carburant' },
        { id: 3, name: 'Électrique', description: 'Batteries, alternateurs' },
        { id: 4, name: 'Accessoires', description: 'Essuie-glaces, ampoules' },
        { id: 5, name: 'Moteur', description: 'Pièces moteur et transmission' },
        { id: 6, name: 'Suspension', description: 'Amortisseurs, ressorts' }
    ]);

    constructor(
        public authService: AuthService,
        public cartService: CartService
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
    }

    updateCartCount(): void {
        this.cartService.cart$.subscribe(cart => {
            const count = cart?.items.reduce((total: number, item: CartItem) => total + item.quantity, 0) || 0;
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
}
