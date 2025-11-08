import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';

import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { AnalyticsService } from '../../core/services/analytics.service';
import { Product, User, UserRole, ProductSpecification, VehicleCompatibility } from '../../core/models';
import { AnalyticsDashboardComponent } from './analytics-dashboard/analytics-dashboard.component';
import { AdminNavbarComponent } from './admin-navbar/admin-navbar.component';

interface AdminOrder {
    id: string;
    orderNumber: string;
    customerName: string;
    customerEmail: string;
    date: Date;
    status: 'pending' | 'processing' | 'shipped' | 'delivered' | 'cancelled';
    total: number;
    itemCount: number;
    paymentMethod: string;
}

interface DashboardStats {
    totalOrders: number;
    totalRevenue: number;
    totalProducts: number;
    totalUsers: number;
    pendingOrders: number;
    lowStockProducts: number;
}

@Component({
    selector: 'app-admin',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatTabsModule,
        MatButtonModule,
        MatIconModule,
        MatCardModule,
        MatInputModule,
        MatFormFieldModule,
        MatTableModule,
        MatChipsModule,
        MatDividerModule,
        MatProgressSpinnerModule,
        MatSelectModule,
        MatDialogModule,
        MatTooltipModule,
        MatSlideToggleModule,
        MatPaginatorModule,
        MatSortModule,
        AnalyticsDashboardComponent,
        AdminNavbarComponent
    ],
    templateUrl: './admin.component.html',
    styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit {
    // Forms
    productForm!: FormGroup;

    // State
    currentUser = signal<User | null>(null);
    loading = signal<boolean>(false);
    selectedTabIndex = signal<number>(0);

    // Dashboard stats
    stats = signal<DashboardStats>({
        totalOrders: 0,
        totalRevenue: 0,
        totalProducts: 0,
        totalUsers: 0,
        pendingOrders: 0,
        lowStockProducts: 0
    });

    // Products
    products = signal<Product[]>([]);
    editingProduct = signal<Product | null>(null);
    productColumns: string[] = ['image', 'name', 'category', 'brand', 'price', 'stock', 'status', 'actions'];

    // Orders
    orders = signal<AdminOrder[]>([]);
    orderColumns: string[] = ['orderNumber', 'customer', 'date', 'status', 'items', 'total', 'actions'];

    // Users
    users = signal<User[]>([]);
    userColumns: string[] = ['id', 'name', 'email', 'role', 'provider', 'enabled', 'actions'];

    // Pagination & Sorting
    pageSize = signal<number>(10);
    pageIndex = signal<number>(0);
    sortField = signal<string>('');
    sortDirection = signal<'asc' | 'desc'>('asc');

    // Categories and brands
    categories = ['Freinage', 'Suspension', 'Moteur', 'Électrique', 'Carrosserie', 'Échappement'];
    brands = ['Bosch', 'Brembo', 'Monroe', 'NGK', 'Valeo', 'Denso', 'Continental'];

    constructor(
        private fb: FormBuilder,
        private router: Router,
        private dialog: MatDialog,
        private authService: AuthService,
        private notificationService: NotificationService,
        private analyticsService: AnalyticsService
    ) {
        this.initForms();
    }

    ngOnInit(): void {
        this.checkAdminAccess();
        this.loadDashboardStats();
        this.loadProducts();
        this.loadOrders();
        this.loadUsers();
    }

    checkAdminAccess(): void {
        this.authService.currentUser$.subscribe({
            next: (user) => {
                if (user) {
                    this.currentUser.set(user);
                    if (user.role !== UserRole.ADMIN && user.role !== UserRole.SUPER_ADMIN) {
                        this.notificationService.error('Accès refusé - Droits administrateur requis');
                        this.router.navigate(['/']);
                    }
                } else {
                    this.router.navigate(['/auth/login']);
                }
            }
        });
    }

    initForms(): void {
        this.productForm = this.fb.group({
            name: ['', [Validators.required, Validators.minLength(3)]],
            description: ['', [Validators.required, Validators.minLength(10)]],
            category: ['', [Validators.required]],
            brand: ['', [Validators.required]],
            price: [0, [Validators.required, Validators.min(0)]],
            discountPrice: [0, [Validators.min(0)]],
            stock: [0, [Validators.required, Validators.min(0)]],
            imageUrl: ['', [Validators.required]],
            specifications: [''],
            compatibility: ['']
        });
    }

    loadDashboardStats(): void {
        this.loading.set(true);
        this.analyticsService.getDashboardStatsWithGrowth().subscribe({
            next: (data) => {
                this.stats.set({
                    totalOrders: data.totalOrders,
                    totalRevenue: data.totalRevenue,
                    totalProducts: data.totalProducts,
                    totalUsers: data.totalUsers,
                    pendingOrders: data.pendingOrders,
                    lowStockProducts: data.lowStockProducts
                });
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error loading dashboard stats:', error);
                this.notificationService.error('Erreur lors du chargement des statistiques');
                this.loading.set(false);
            }
        });
    }

    loadProducts(): void {
        // Simulate loading products
        setTimeout(() => {
            const mockProducts: Product[] = [
                {
                    id: 1,
                    name: 'Plaquettes de frein avant',
                    description: 'Plaquettes de frein haute performance',
                    category: { id: 1, name: 'Freinage' },
                    brand: { id: 1, name: 'Brembo' },
                    price: 89.99,
                    discount: 11,
                    stock: 45,
                    imageUrl: 'https://placehold.co/300x200/e3f2fd/1976d2?text=Brake+Pads',
                    specifications: [
                        { key: 'material', value: 'Céramique' },
                        { key: 'garantie', value: '2 ans' }
                    ],
                    compatibility: [
                        { id: 1, brand: 'Peugeot', model: '208', year: 2020 },
                        { id: 2, brand: 'Renault', model: 'Clio', year: 2019 }
                    ],
                    createdAt: new Date()
                },
                {
                    id: 2,
                    name: 'Amortisseurs arrière',
                    description: 'Amortisseurs hydrauliques pour suspension',
                    category: { id: 2, name: 'Suspension' },
                    brand: { id: 2, name: 'Monroe' },
                    price: 159.99,
                    stock: 5,
                    imageUrl: 'https://placehold.co/300x200/fce4ec/c2185b?text=Shocks',
                    specifications: [
                        { key: 'type', value: 'Hydraulique' },
                        { key: 'longueur', value: '350mm' }
                    ],
                    compatibility: [
                        { id: 3, brand: 'Volkswagen', model: 'Golf', year: 2021 },
                        { id: 4, brand: 'Seat', model: 'Leon', year: 2020 }
                    ],
                    createdAt: new Date()
                },
                {
                    id: 3,
                    name: 'Filtre à huile',
                    description: 'Filtre à huile moteur haute filtration',
                    category: { id: 3, name: 'Moteur' },
                    brand: { id: 3, name: 'Bosch' },
                    price: 12.99,
                    stock: 0,
                    imageUrl: 'https://placehold.co/300x200/fff3e0/f57c00?text=Oil+Filter',
                    specifications: [
                        { key: 'diametre', value: '76mm' }
                    ],
                    compatibility: [
                        { id: 5, brand: 'Renault', model: 'Megane', year: 2018 },
                        { id: 6, brand: 'Dacia', model: 'Sandero', year: 2019 }
                    ],
                    createdAt: new Date()
                }
            ];
            this.products.set(mockProducts);
        }, 500);
    }

    loadOrders(): void {
        setTimeout(() => {
            const mockOrders: AdminOrder[] = [
                {
                    id: '1',
                    orderNumber: 'ORD-2024-001',
                    customerName: 'Ahmed Benali',
                    customerEmail: 'ahmed@example.com',
                    date: new Date('2024-11-01'),
                    status: 'pending',
                    total: 1250.50,
                    itemCount: 3,
                    paymentMethod: 'Carte bancaire'
                },
                {
                    id: '2',
                    orderNumber: 'ORD-2024-002',
                    customerName: 'Sara El Amrani',
                    customerEmail: 'sara@example.com',
                    date: new Date('2024-11-02'),
                    status: 'processing',
                    total: 450.00,
                    itemCount: 2,
                    paymentMethod: 'Cash'
                }
            ];
            this.orders.set(mockOrders);
        }, 500);
    }

    loadUsers(): void {
        setTimeout(() => {
            const mockUsers: User[] = [
                {
                    id: 1,
                    email: 'admin@autoshop.ma',
                    firstName: 'Admin',
                    lastName: 'User',
                    role: UserRole.ADMIN,
                    enabled: true,
                    provider: 'LOCAL'
                },
                {
                    id: 2,
                    email: 'user@example.com',
                    firstName: 'Mohammed',
                    lastName: 'Alaoui',
                    phoneNumber: '0612345678',
                    role: UserRole.CLIENT,
                    enabled: true,
                    provider: 'GOOGLE'
                }
            ];
            this.users.set(mockUsers);
        }, 500);
    }

    // Product Management
    openProductForm(product?: Product): void {
        if (product) {
            this.editingProduct.set(product);

            // Convert specifications array to JSON string
            const specsObj: { [key: string]: string } = {};
            product.specifications?.forEach(spec => {
                specsObj[spec.key] = spec.value;
            });

            // Convert compatibility array to comma-separated string
            const compatStr = product.compatibility?.map(c => `${c.brand} ${c.model}`).join(', ') || '';

            // Calculate discount price from discount percentage
            const discountPrice = product.discount ? product.price * (1 - product.discount / 100) : 0;

            this.productForm.patchValue({
                name: product.name,
                description: product.description,
                category: product.category.name,
                brand: product.brand.name,
                price: product.price,
                discountPrice: discountPrice,
                stock: product.stock,
                imageUrl: product.imageUrl,
                specifications: JSON.stringify(specsObj),
                compatibility: compatStr
            });
        } else {
            this.editingProduct.set(null);
            this.productForm.reset();
        }
    }

    saveProduct(): void {
        if (this.productForm.invalid) {
            this.notificationService.warning('Veuillez remplir tous les champs obligatoires');
            return;
        }

        this.loading.set(true);
        const formValue = this.productForm.value;

        // Parse specifications from JSON string to array
        let specifications: ProductSpecification[] = [];
        if (formValue.specifications) {
            try {
                const specObj = JSON.parse(formValue.specifications);
                specifications = Object.entries(specObj).map(([key, value]) => ({
                    key,
                    value: String(value)
                }));
            } catch (e) {
                this.notificationService.error('Format JSON des spécifications invalide');
                this.loading.set(false);
                return;
            }
        }

        // Parse compatibility from comma-separated string to array
        const compatibility: VehicleCompatibility[] = [];
        if (formValue.compatibility) {
            const compatList = formValue.compatibility.split(',').map((c: string) => c.trim());
            compatList.forEach((comp: string, index: number) => {
                const parts = comp.split(' ');
                compatibility.push({
                    id: index + 1,
                    brand: parts[0] || '',
                    model: parts.slice(1).join(' ') || '',
                    year: 2024
                });
            });
        }

        const productData: Partial<Product> = {
            name: formValue.name,
            description: formValue.description,
            category: { id: this.categories.indexOf(formValue.category) + 1, name: formValue.category },
            brand: { id: this.brands.indexOf(formValue.brand) + 1, name: formValue.brand },
            price: formValue.price,
            discount: formValue.discountPrice ? Math.round(((formValue.price - formValue.discountPrice) / formValue.price) * 100) : 0,
            stock: formValue.stock,
            imageUrl: formValue.imageUrl,
            specifications,
            compatibility
        };

        // Simulate API call
        setTimeout(() => {
            if (this.editingProduct()) {
                // Update existing product
                this.products.update(products =>
                    products.map(p => p.id === this.editingProduct()!.id ? { ...p, ...productData } : p)
                );
                this.notificationService.success('Produit mis à jour avec succès');
            } else {
                // Add new product
                const newId = Math.max(...this.products().map(p => p.id), 0) + 1;
                const newProduct: Product = {
                    ...productData as Product,
                    id: newId,
                    createdAt: new Date()
                };
                this.products.update(products => [...products, newProduct]);
                this.notificationService.success('Produit ajouté avec succès');
            }
            this.productForm.reset();
            this.editingProduct.set(null);
            this.loading.set(false);
        }, 1000);
    }

    deleteProduct(productId: number): void {
        if (confirm('Êtes-vous sûr de vouloir supprimer ce produit ?')) {
            this.products.update(products => products.filter(p => p.id !== productId));
            this.notificationService.success('Produit supprimé avec succès');
        }
    }

    cancelProductEdit(): void {
        this.editingProduct.set(null);
        this.productForm.reset();
    }

    // Order Management
    updateOrderStatus(orderId: string, newStatus: AdminOrder['status']): void {
        this.orders.update(orders =>
            orders.map(o => o.id === orderId ? { ...o, status: newStatus } : o)
        );
        this.notificationService.success('Statut de commande mis à jour');
    }

    viewOrderDetails(orderId: string): void {
        this.router.navigate(['/admin/orders', orderId]);
    }

    // User Management
    toggleUserStatus(userId: number): void {
        this.users.update(users =>
            users.map(u => u.id === userId ? { ...u, enabled: !u.enabled } : u)
        );
        this.notificationService.success('Statut utilisateur mis à jour');
    }

    changeUserRole(userId: number, newRole: UserRole): void {
        this.users.update(users =>
            users.map(u => u.id === userId ? { ...u, role: newRole } : u)
        );
        this.notificationService.success('Rôle utilisateur mis à jour');
    }

    // Utility methods
    getStockStatus(stock: number): string {
        if (stock === 0) return 'Rupture';
        if (stock < 10) return 'Stock faible';
        return 'En stock';
    }

    getStockColor(stock: number): string {
        if (stock === 0) return 'warn';
        if (stock < 10) return 'accent';
        return 'primary';
    }

    getOrderStatusColor(status: string): string {
        const colors: { [key: string]: string } = {
            pending: 'accent',
            processing: 'primary',
            shipped: 'primary',
            delivered: 'primary',
            cancelled: 'warn'
        };
        return colors[status] || 'primary';
    }

    getOrderStatusLabel(status: string): string {
        const labels: { [key: string]: string } = {
            pending: 'En attente',
            processing: 'En préparation',
            shipped: 'Expédiée',
            delivered: 'Livrée',
            cancelled: 'Annulée'
        };
        return labels[status] || status;
    }

    handlePageEvent(event: PageEvent): void {
        this.pageSize.set(event.pageSize);
        this.pageIndex.set(event.pageIndex);
    }

    handleSort(sort: Sort): void {
        this.sortField.set(sort.active);
        this.sortDirection.set(sort.direction as 'asc' | 'desc');
    }
}
