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
import { NgxEchartsModule, provideEchartsCore } from 'ngx-echarts';
import { EChartsOption } from 'echarts';
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
        NgxEchartsModule
    ],
    providers: [provideEchartsCore({ echarts: () => import('echarts') })],
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

    // Expose Math to template
    Math = Math;

    // Date range form
    dateRangeForm = new FormGroup({
        startDate: new FormControl<Date>(new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)),
        endDate: new FormControl<Date>(new Date())
    });

    // Table columns
    productsDisplayedColumns: string[] = ['image', 'name', 'category', 'brand', 'unitsSold', 'revenue'];
    activitiesDisplayedColumns: string[] = ['type', 'description', 'userName', 'timestamp'];

    // ECharts options
    revenueChartOption: EChartsOption = {};
    categoryChartOption: EChartsOption = {};
    orderStatusChartOption: EChartsOption = {};

    // Sparkline options for KPI cards
    revenueSparklineOption: EChartsOption = {};
    ordersSparklineOption: EChartsOption = {};
    usersSparklineOption: EChartsOption = {};
    productsSparklineOption: EChartsOption = {};

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

                    // Prepare ECharts
                    this.prepareRevenueChart();
                    this.prepareCategoryChart();
                    this.prepareOrderStatusChart();
                    this.prepareSparklines();

                    this.loading = false;
                },
                error: (error) => {
                    console.error('Error loading analytics:', error);
                    this.loading = false;
                }
            });
    }

    /**
     * Prepare revenue area chart
     */
    prepareRevenueChart(): void {
        const dates = this.salesChartData.map(item => new Date(item.date).toLocaleDateString('fr-FR', { month: 'short', day: 'numeric' }));
        const revenues = this.salesChartData.map(item => item.revenue);

        this.revenueChartOption = {
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                top: '10%',
                containLabel: true
            },
            xAxis: {
                type: 'category',
                data: dates,
                boundaryGap: false,
                axisLine: { lineStyle: { color: '#e5e5e5' } },
                axisLabel: { color: '#666', fontSize: 11 }
            },
            yAxis: {
                type: 'value',
                axisLine: { show: false },
                axisTick: { show: false },
                splitLine: { lineStyle: { color: '#f5f5f5' } },
                axisLabel: { color: '#666', fontSize: 11, formatter: '{value} TND' }
            },
            series: [{
                data: revenues,
                type: 'line',
                smooth: true,
                symbol: 'circle',
                symbolSize: 6,
                lineStyle: { color: '#0070f3', width: 2 },
                itemStyle: { color: '#0070f3' },
                areaStyle: {
                    color: {
                        type: 'linear',
                        x: 0, y: 0, x2: 0, y2: 1,
                        colorStops: [
                            { offset: 0, color: 'rgba(0, 112, 243, 0.2)' },
                            { offset: 1, color: 'rgba(0, 112, 243, 0.0)' }
                        ]
                    }
                }
            }],
            tooltip: {
                trigger: 'axis',
                backgroundColor: '#fff',
                borderColor: '#e5e5e5',
                borderWidth: 1,
                textStyle: { color: '#000' },
                formatter: (params: any) => {
                    const param = params[0];
                    return `${param.name}<br/><strong>${param.value.toLocaleString('fr-TN')} TND</strong>`;
                }
            }
        };
    }

    /**
     * Prepare category horizontal bar chart
     */
    prepareCategoryChart(): void {
        const categories = this.categoryPerformance.map(cat => cat.categoryName);
        const revenues = this.categoryPerformance.map(cat => cat.revenue);

        this.categoryChartOption = {
            grid: {
                left: '20%',
                right: '10%',
                bottom: '3%',
                top: '3%',
                containLabel: true
            },
            xAxis: {
                type: 'value',
                axisLine: { show: false },
                axisTick: { show: false },
                splitLine: { lineStyle: { color: '#f5f5f5' } },
                axisLabel: { color: '#666', fontSize: 11 }
            },
            yAxis: {
                type: 'category',
                data: categories,
                axisLine: { lineStyle: { color: '#e5e5e5' } },
                axisLabel: { color: '#666', fontSize: 12 }
            },
            series: [{
                data: revenues,
                type: 'bar',
                barWidth: '60%',
                itemStyle: {
                    color: '#0070f3',
                    borderRadius: [0, 4, 4, 0]
                },
                label: {
                    show: true,
                    position: 'right',
                    formatter: '{c} TND',
                    color: '#666',
                    fontSize: 11
                }
            }],
            tooltip: {
                trigger: 'axis',
                axisPointer: { type: 'shadow' },
                backgroundColor: '#fff',
                borderColor: '#e5e5e5',
                borderWidth: 1,
                textStyle: { color: '#000' }
            }
        };
    }

    /**
     * Prepare order status donut chart
     */
    prepareOrderStatusChart(): void {
        const data = this.orderStatusDistribution.map(status => ({
            name: this.getStatusLabel(status.status),
            value: status.count
        }));

        this.orderStatusChartOption = {
            tooltip: {
                trigger: 'item',
                backgroundColor: '#fff',
                borderColor: '#e5e5e5',
                borderWidth: 1,
                textStyle: { color: '#000' },
                formatter: '{b}: <strong>{c}</strong> ({d}%)'
            },
            legend: {
                bottom: '0%',
                left: 'center',
                textStyle: { color: '#666', fontSize: 12 }
            },
            series: [{
                type: 'pie',
                radius: ['40%', '70%'],
                center: ['50%', '45%'],
                avoidLabelOverlap: false,
                itemStyle: {
                    borderRadius: 8,
                    borderColor: '#fff',
                    borderWidth: 2
                },
                label: { show: false },
                emphasis: {
                    label: {
                        show: true,
                        fontSize: 14,
                        fontWeight: 'bold'
                    }
                },
                data: data,
                color: ['#0070f3', '#7928ca', '#ff0080', '#00dfd8', '#f5a623']
            }]
        };
    }

    /**
     * Prepare sparkline charts for KPI cards
     */
    prepareSparklines(): void {
        const last7Days = this.salesChartData.slice(-7);
        const revenues = last7Days.map(item => item.revenue);
        const orders = last7Days.map(item => item.orders);

        // Revenue sparkline
        this.revenueSparklineOption = this.createSparkline(revenues, '#10b981');

        // Orders sparkline
        this.ordersSparklineOption = this.createSparkline(orders, '#0070f3');

        // Users sparkline (mock data for now)
        this.usersSparklineOption = this.createSparkline([120, 132, 101, 134, 90, 230, 210], '#8b5cf6');

        // Products sparkline (mock data for now)
        this.productsSparklineOption = this.createSparkline([220, 182, 191, 234, 290, 330, 310], '#f59e0b');
    }

    /**
     * Create a sparkline chart
     */
    private createSparkline(data: number[], color: string): EChartsOption {
        return {
            grid: {
                left: 0,
                right: 0,
                top: 0,
                bottom: 0
            },
            xAxis: {
                type: 'category',
                show: false,
                boundaryGap: false
            },
            yAxis: {
                type: 'value',
                show: false
            },
            series: [{
                data: data,
                type: 'line',
                smooth: true,
                symbol: 'none',
                lineStyle: { color: color, width: 1.5 },
                areaStyle: {
                    color: {
                        type: 'linear',
                        x: 0, y: 0, x2: 0, y2: 1,
                        colorStops: [
                            { offset: 0, color: color + '40' },
                            { offset: 1, color: color + '00' }
                        ]
                    }
                }
            }]
        };
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
        return new Intl.NumberFormat('fr-TN', {
            style: 'currency',
            currency: 'TND',
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
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
        return growth > 0 ? 'positive' : 'negative';
    }
}
