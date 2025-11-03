import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSliderModule } from '@angular/material/slider';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatTooltipModule } from '@angular/material/tooltip';

import { ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Product, Category, Brand, ProductFilter } from '../../../core/models';

@Component({
    selector: 'app-product-list',
    standalone: true,
    imports: [
        CommonModule,
        RouterLink,
        FormsModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatSliderModule,
        MatChipsModule,
        MatPaginatorModule,
        MatProgressSpinnerModule,
        MatButtonToggleModule,
        MatCheckboxModule,
        MatTooltipModule
    ],
    templateUrl: './product-list.component.html',
    styleUrls: ['./product-list.component.scss']
})
export class ProductListComponent implements OnInit {
    // State signals
    products = signal<Product[]>([]);
    categories = signal<Category[]>([]);
    brands = signal<Brand[]>([]);
    loading = signal<boolean>(false);

    // Pagination
    totalElements = signal<number>(0);
    pageSize = signal<number>(12);
    pageIndex = signal<number>(0);

    // View mode
    viewMode = signal<'grid' | 'list'>('grid');

    // Filter state
    searchQuery = signal<string>('');
    selectedCategory = signal<number | null>(null);
    selectedBrand = signal<number | null>(null);
    priceRange = signal<{ min: number; max: number }>({ min: 0, max: 1000 });
    inStockOnly = signal<boolean>(false);
    sortBy = signal<'price_asc' | 'price_desc' | 'name' | 'newest'>('newest');

    // Computed
    hasFilters = computed(() =>
        this.searchQuery() !== '' ||
        this.selectedCategory() !== null ||
        this.selectedBrand() !== null ||
        this.inStockOnly()
    );

    constructor(
        private productService: ProductService,
        private cartService: CartService,
        private notificationService: NotificationService,
        private route: ActivatedRoute,
        private router: Router
    ) { }

    ngOnInit(): void {
        // Load categories and brands
        this.loadCategories();
        this.loadBrands();

        // Check for query params
        this.route.queryParams.subscribe(params => {
            if (params['category']) {
                this.selectedCategory.set(+params['category']);
            }
            if (params['brand']) {
                this.selectedBrand.set(+params['brand']);
            }
            if (params['search']) {
                this.searchQuery.set(params['search']);
            }

            this.loadProducts();
        });
    }

    loadProducts(): void {
        this.loading.set(true);

        const filter: ProductFilter = {
            page: this.pageIndex(),
            size: this.pageSize(),
            search: this.searchQuery() || undefined,
            categoryId: this.selectedCategory() || undefined,
            brandId: this.selectedBrand() || undefined,
            minPrice: this.priceRange().min,
            maxPrice: this.priceRange().max,
            inStock: this.inStockOnly() || undefined,
            sort: this.sortBy()
        };

        this.productService.getProducts(filter).subscribe({
            next: (response) => {
                this.products.set(response.content);
                this.totalElements.set(response.totalElements);
                this.loading.set(false);
                window.scrollTo({ top: 0, behavior: 'smooth' });
            },
            error: (error) => {
                console.error('Error loading products:', error);
                this.notificationService.error('Erreur lors du chargement des produits');
                this.loading.set(false);
            }
        });
    }

    loadCategories(): void {
        this.productService.getCategories().subscribe({
            next: (categories) => this.categories.set(categories),
            error: (error) => console.error('Error loading categories:', error)
        });
    }

    loadBrands(): void {
        this.productService.getBrands().subscribe({
            next: (brands) => this.brands.set(brands),
            error: (error) => console.error('Error loading brands:', error)
        });
    }

    onSearch(): void {
        this.pageIndex.set(0);
        this.loadProducts();
    }

    onCategoryChange(categoryId: number | null): void {
        this.selectedCategory.set(categoryId);
        this.pageIndex.set(0);
        this.loadProducts();
    }

    onBrandChange(brandId: number | null): void {
        this.selectedBrand.set(brandId);
        this.pageIndex.set(0);
        this.loadProducts();
    }

    onSortChange(sort: 'price_asc' | 'price_desc' | 'name' | 'newest'): void {
        this.sortBy.set(sort);
        this.pageIndex.set(0);
        this.loadProducts();
    }

    onPriceRangeChange(): void {
        this.pageIndex.set(0);
        this.loadProducts();
    }

    onInStockChange(): void {
        this.inStockOnly.update(value => !value);
        this.pageIndex.set(0);
        this.loadProducts();
    }

    onPageChange(event: PageEvent): void {
        this.pageIndex.set(event.pageIndex);
        this.pageSize.set(event.pageSize);
        this.loadProducts();
    }

    clearFilters(): void {
        this.searchQuery.set('');
        this.selectedCategory.set(null);
        this.selectedBrand.set(null);
        this.priceRange.set({ min: 0, max: 1000 });
        this.inStockOnly.set(false);
        this.sortBy.set('newest');
        this.pageIndex.set(0);
        this.router.navigate(['/products']);
        this.loadProducts();
    }

    removeFilter(filterType: string): void {
        switch (filterType) {
            case 'search':
                this.searchQuery.set('');
                break;
            case 'category':
                this.selectedCategory.set(null);
                break;
            case 'brand':
                this.selectedBrand.set(null);
                break;
            case 'inStock':
                this.inStockOnly.set(false);
                break;
        }
        this.pageIndex.set(0);
        this.loadProducts();
    }

    addToCart(product: Product): void {
        if (product.stock <= 0) {
            this.notificationService.warning('Ce produit est en rupture de stock');
            return;
        }

        this.cartService.addToCart({
            productId: product.id,
            quantity: 1
        }).subscribe({
            next: () => {
                this.notificationService.success(`${product.name} ajouté au panier`);
            },
            error: (error: any) => {
                console.error('Error adding to cart:', error);
                this.notificationService.error('Erreur lors de l\'ajout au panier');
            }
        });
    }

    getCategoryName(categoryId: number | null): string {
        if (!categoryId) return '';
        const category = this.categories().find(c => c.id === categoryId);
        return category?.name || '';
    }

    getBrandName(brandId: number | null): string {
        if (!brandId) return '';
        const brand = this.brands().find(b => b.id === brandId);
        return brand?.name || '';
    }

    toggleViewMode(): void {
        this.viewMode.update(mode => mode === 'grid' ? 'list' : 'grid');
    }
}
