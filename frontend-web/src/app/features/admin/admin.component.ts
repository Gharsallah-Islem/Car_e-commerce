import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { AnalyticsService } from '../../core/services/analytics.service';
import { AdminNavigationService } from '../../core/services/admin-navigation.service';
import { User, UserRole } from '../../core/models';

import { AnalyticsDashboardComponent } from './analytics-dashboard/analytics-dashboard.component';
import { AdminNavbarComponent } from './admin-navbar/admin-navbar.component';
import { ProductManagementComponent } from './components/product-management/product-management.component';
import { OrderManagementComponent } from './components/order-management/order-management.component';
import { UserManagementComponent } from './components/user-management/user-management.component';
import { CategoryManagementComponent } from './components/category-management/category-management.component';
import { BrandManagementComponent } from './components/brand-management/brand-management.component';
import { InventoryManagementComponent } from './inventory-management/inventory-management.component';
import { DeliveryManagementComponent } from './delivery-management/delivery-management.component';

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
        MatTabsModule,
        MatIconModule,
        MatProgressSpinnerModule,
        AnalyticsDashboardComponent,
        AdminNavbarComponent,
        ProductManagementComponent,
        CategoryManagementComponent,
        BrandManagementComponent,
        OrderManagementComponent,
        UserManagementComponent,
        InventoryManagementComponent,
        DeliveryManagementComponent
    ],
    templateUrl: './admin.component.html',
    styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit {
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

    constructor(
        private router: Router,
        private authService: AuthService,
        private notificationService: NotificationService,
        private analyticsService: AnalyticsService,
        public adminNavService: AdminNavigationService
    ) { }

    ngOnInit(): void {
        this.checkAdminAccess();
        this.loadDashboardStats();

        // Sync with navigation service
        this.adminNavService.selectedTabIndex.set(this.selectedTabIndex());
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
                // this.notificationService.error('Erreur lors du chargement des statistiques');
                this.loading.set(false);
            }
        });
    }
}
