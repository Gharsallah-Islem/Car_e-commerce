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
            next: (response: any) => {
                // Spring Boot Page format: { content: [], totalElements: number, ... }
                const productsArray = response.content || response || [];
                const total = response.totalElements || response.totalElements || productsArray.length;
                
                // Map products to ensure brand is properly formatted and images are valid
                const mappedProducts = Array.isArray(productsArray) ? productsArray.map((p: any) => {
                    // Handle BigDecimal price from Spring Boot
                    let price = p.price;
                    if (typeof price === 'object' && price !== null) {
                        price = price.value || price.amount || parseFloat(price.toString()) || 0;
                    }
                    price = parseFloat(price) || 0;
                    
                    return {
                        ...p,
                        id: p.id || p.uuid || Math.random(),
                        brand: typeof p.brand === 'string' ? { name: p.brand } : p.brand,
                        imageUrl: p.imageUrl || p.image_url || this.getDefaultImageForCategory(p.category || ''),
                        price: price,
                        stock: p.stock || 0,
                        category: typeof p.category === 'string' ? { id: 0, name: p.category } : p.category
                    };
                }) : [];
                
                this.products.set(mappedProducts);
                this.totalElements.set(total);
                this.loading.set(false);
                window.scrollTo({ top: 0, behavior: 'smooth' });
            },
            error: (error) => {
                console.error('Error loading products:', error);
                this.notificationService.error('Erreur lors du chargement des produits');
                this.loading.set(false);
                // Set empty array on error
                this.products.set([]);
                this.totalElements.set(0);
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

    getBrandDisplay(product: any): string {
        if (!product.brand) return '—';
        return typeof product.brand === 'string' ? product.brand : (product.brand.name || '—');
    }

    getDefaultImageForCategory(category: string): string {
        // Real direct image links matching each category - from user provided links
        const categoryImages: { [key: string]: string } = {
            'Performance Exhaust Systems': 'data:image/jpeg;base64,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',
            'Turbocharger Kits & Accessories': 'https://dieselcomponentsinc.com/wp-content/uploads/2018/08/turbo-kit.jpg',
            'Brake Parts': 'https://www.eurotyre.fr/wp-content/uploads/sites/2/2022/08/ATE_Ceramic_Package_Still_21cm_300dpi_CMYK_retu.jpg',
            'Suspension': 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80',
            'Cooling System': 'https://images.unsplash.com/photo-1605559424843-9e4c228bf1c2?auto=format&fit=crop&w=800&q=80',
            'Air Induction': 'https://images.unsplash.com/photo-1605559424843-9e4c228bf1c2?auto=format&fit=crop&w=800&q=80',
            'Engine Parts': 'https://i0.wp.com/www.mendmotor.com/wp-content/uploads/2024/01/Car-Engine-Parts-Diagram.webp?resize=840%2C473',
            'Clutch/Drivetrain': 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS0PtdArCV1Pcmp4N4sBodwzXoIN9fEF7BY4Q&s',
            'Electronics & Tuning': 'https://images.unsplash.com/photo-1605559424843-9e4c228bf1c2?auto=format&fit=crop&w=800&q=80',
            'Fuel Systems': 'https://images.unsplash.com/photo-1605559424843-9e4c228bf1c2?auto=format&fit=crop&w=800&q=80',
            'Exterior': 'https://images.unsplash.com/photo-1605559424843-9e4c228bf1c2?auto=format&fit=crop&w=800&q=80',
            'Wheels/Tires': 'https://images.unsplash.com/photo-1605559424843-9e4c228bf1c2?auto=format&fit=crop&w=800&q=80',
            'Racing & Safety': 'https://images.unsplash.com/photo-1605559424843-9e4c228bf1c2?auto=format&fit=crop&w=800&q=80',
            'Filters': 'data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBxMSEhUSEhMWFhUVGBgXGBcYFxUXGBoYFxUYGBYWGBcYHSggGBonGxgXITEhJSkrLi4uFx8zODMtNygtLisBCgoKDg0OGhAQGi4mHyYtLS0tLS4tLS0vKy0tLS0tLS0tLS0tLS0tLTUtLS0tLSstLS0tLi0tLTUtLS0tNy0tK//AABEIALcBEwMBIgACEQEDEQH/xAAcAAEAAQUBAQAAAAAAAAAAAAAABwECAwUGBAj/xAA/EAABAwEFBQYDBgUEAgMAAAABAAIDEQQSITFBBQZRYfAHEyJxgZEyQqEUUmKxwdEjQ3Lh8RUzgqIkU2Oywv/EABkBAQADAQEAAAAAAAAAAAAAAAABAgMEBf/EACwRAAICAQIDBwQDAQAAAAAAAAABAhEDITISE0EEMlFhscHhImKh0XGBolL/2gAMAwEAAhEDEQA/AJxREQBERAEREAREQBEVksoaKk0QF6Lmds74QwCpLRwLjQHyA8TvNocFy8nakyuBbT+mSnuWV+imiLJORcXsjf2KXEgEDMsJNOZaQHAc7tOa62x2xkrQ+Nwc06go1QTszoiKCQtdtfblnsrb08rWcAT4j5NGJ9AuU7RN+RYx3EBBtDhicxGDqRq46D1OgMK2q1PlcXyOL3Ozc4kk+ZK0jjvUynlUdETVN2rWIGgbM7mGNH/2eCsth7UbDI6ju9i5vYCP+hdRQXVFpyomXOkfUdit0UzQ+KRr2nVpDh9F6F8ybF21NZZBLA8tcM/uuHBwyIU87mb1x2+K83wyNoJGVyJyI4tOh9FlKHCbQyKR0SIioaBERAEREAREQBERAEREAREQBERAEREAREQBERAEREAWOW0NaKuIA5mgWntm8kMZpeBO4Y+9MvVQmQdMii9VqrVf/9k='
        };
        return categoryImages[category] || 'https://images.unsplash.com/photo-1515923162041-1f4a3f07d2b6?auto=format&fit=crop&w=800&q=70';
    }
}
