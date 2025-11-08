import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Subject, takeUntil, forkJoin } from 'rxjs';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { AnalyticsService } from '../../../core/services/analytics.service';
import {
    DashboardStats,
    TopProduct,
    CategoryPerformance,
    CustomerAnalytics,
    OrderStatusDistribution,
    RecentActivity,
    ProductInventoryAlert,
    SalesChartData
} from '../../../core/models/analytics.interface';

@Component({
    selector: 'app-analytics-dashboard',
    standalone: true,
    imports: [
        CommonModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        MatTableModule,
        MatChipsModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatFormFieldModule,
        MatInputModule,
        ReactiveFormsModule,
        NgxChartsModule
    ],
    templateUrl: './analytics-dashboard.component.html',
    styleUrls: ['./analytics-dashboard.component.scss']
})
export class AnalyticsDashboardComponent implements OnInit, OnDestroy {
    private destroy$ = new Subject<void>();

    // Data properties
    dashboardStats: DashboardStats | null = null;
    topProducts: TopProduct[] = [];
    categoryPerformance: CategoryPerformance[] = [];
    customerAnalytics: CustomerAnalytics | null = null;
    orderStatusDistribution: OrderStatusDistribution[] = [];
    recentActivities: RecentActivity[] = [];
    inventoryAlerts: ProductInventoryAlert[] = [];
    salesChartData: SalesChartData[] = [];

    // Loading states
    loading = true;
    refreshing = false;

    // Date range form
    dateRangeForm = new FormGroup({
        startDate: new FormControl<Date>(new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)),
        endDate: new FormControl<Date>(new Date())
    });

    // Table columns
    productsDisplayedColumns: string[] = ['image', 'name', 'category', 'brand', 'unitsSold', 'revenue'];
    activitiesDisplayedColumns: string[] = ['type', 'description', 'userName', 'timestamp'];

    // Chart data
    salesChartMulti: any[] = [];
    categoryChartData: any[] = [];
    orderStatusChartData: any[] = [];

    // Chart options
    view: [number, number] = [700, 300];
    showXAxis = true;
    showYAxis = true;
    gradient = false;
    showLegend = true;
    showXAxisLabel = true;
    showYAxisLabel = true;
    xAxisLabel = 'Date';
    yAxisLabel = 'Revenu (MAD)';
    colorScheme: any = {
        domain: ['#5AA454', '#A10A28', '#C7B42C', '#AAAAAA']
    };
    cardColor = '#232837';

    constructor(private analyticsService: AnalyticsService) { }

    ngOnInit(): void {
        this.loadAllAnalytics();
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    /**
     * Load all analytics data
     */
    loadAllAnalytics(): void {
        this.loading = true;

        const startDate = this.formatDate(this.dateRangeForm.value.startDate || new Date(Date.now() - 30 * 24 * 60 * 60 * 1000));
        const endDate = this.formatDate(this.dateRangeForm.value.endDate || new Date());

        forkJoin({
            stats: this.analyticsService.getDashboardStatsWithGrowth(),
            topProducts: this.analyticsService.getTopProducts(5),
            categories: this.analyticsService.getCategoryPerformance(),
            customers: this.analyticsService.getCustomerAnalytics(),
            orderStatus: this.analyticsService.getOrderStatusDistribution(),
            activities: this.analyticsService.getRecentActivities(10),
            alerts: this.analyticsService.getInventoryAlerts(),
            salesChart: this.analyticsService.getSalesChartData(startDate, endDate, 'DAILY')
        })
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (data) => {
                    this.dashboardStats = data.stats;
                    this.topProducts = data.topProducts;
                    this.categoryPerformance = data.categories;
                    this.customerAnalytics = data.customers;
                    this.orderStatusDistribution = data.orderStatus;
                    this.recentActivities = data.activities;
                    this.inventoryAlerts = data.alerts;
                    this.salesChartData = data.salesChart;

                    // Prepare chart data
                    this.prepareSalesChartData();
                    this.prepareCategoryChartData();
                    this.prepareOrderStatusChartData();

                    this.loading = false;
                },
                error: (error) => {
                    console.error('Error loading analytics:', error);
                    this.loading = false;
                }
            });
    }

    /**
     * Prepare sales chart data for ngx-charts
     */
    prepareSalesChartData(): void {
        this.salesChartMulti = [
            {
                name: 'Revenu',
                series: this.salesChartData.map(item => ({
                    name: new Date(item.date),
                    value: item.revenue
                }))
            },
            {
                name: 'Commandes',
                series: this.salesChartData.map(item => ({
                    name: new Date(item.date),
                    value: item.orders * 100 // Scale for visibility
                }))
            }
        ];
    }

    /**
     * Prepare category chart data
     */
    prepareCategoryChartData(): void {
        this.categoryChartData = this.categoryPerformance.map(cat => ({
            name: cat.categoryName,
            value: cat.revenue
        }));
    }

    /**
     * Prepare order status chart data
     */
    prepareOrderStatusChartData(): void {
        this.orderStatusChartData = this.orderStatusDistribution.map(status => ({
            name: this.getStatusLabel(status.status),
            value: status.count
        }));
    }

    /**
     * Refresh analytics data
     */
    refreshData(): void {
        this.refreshing = true;
        this.loadAllAnalytics();
        setTimeout(() => {
            this.refreshing = false;
        }, 1000);
    }

    /**
     * Apply date range filter
     */
    applyDateFilter(): void {
        const startDate = this.dateRangeForm.get('startDate')?.value;
        const endDate = this.dateRangeForm.get('endDate')?.value;

        if (startDate && endDate) {
            this.loading = true;
            const start = this.formatDate(startDate);
            const end = this.formatDate(endDate);

            forkJoin({
                topProducts: this.analyticsService.getTopProducts(5, start, end),
                categories: this.analyticsService.getCategoryPerformance(start, end)
            })
                .pipe(takeUntil(this.destroy$))
                .subscribe({
                    next: (data) => {
                        this.topProducts = data.topProducts;
                        this.categoryPerformance = data.categories;
                        this.loading = false;
                    },
                    error: (error) => {
                        console.error('Error applying date filter:', error);
                        this.loading = false;
                    }
                });
        }
    }

    /**
     * Format date to YYYY-MM-DD
     */
    private formatDate(date: Date): string {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    /**
     * Get severity color for inventory alerts
     */
    getSeverityColor(severity: string): string {
        switch (severity) {
            case 'CRITICAL':
                return 'warn';
            case 'WARNING':
                return 'accent';
            case 'INFO':
                return 'primary';
            default:
                return 'primary';
        }
    }

    /**
     * Get status label in French
     */
    getStatusLabel(status: string): string {
        const labels: { [key: string]: string } = {
            'PENDING': 'En attente',
            'PROCESSING': 'En traitement',
            'SHIPPED': 'Expédié',
            'DELIVERED': 'Livré',
            'CANCELLED': 'Annulé'
        };
        return labels[status] || status;
    }

    /**
     * Get activity type icon
     */
    getActivityIcon(type: string): string {
        switch (type) {
            case 'ORDER':
                return 'shopping_cart';
            case 'USER':
                return 'person_add';
            case 'PRODUCT':
                return 'inventory_2';
            case 'RECLAMATION':
                return 'report_problem';
            default:
                return 'info';
        }
    }

    /**
     * Format currency
     */
    formatCurrency(value: number): string {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD'
        }).format(value);
    }

    /**
     * Format percentage
     */
    formatPercentage(value: number): string {
        return `${value.toFixed(2)}%`;
    }

    /**
     * Get growth icon
     */
    getGrowthIcon(growth?: number): string {
        if (!growth) return '';
        return growth > 0 ? 'trending_up' : 'trending_down';
    }

    /**
     * Get growth color
     */
    getGrowthColor(growth?: number): string {
        if (!growth) return '';
        return growth > 0 ? 'success' : 'error';
    }
}
