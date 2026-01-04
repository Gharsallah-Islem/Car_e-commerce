import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
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
        RouterModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        MatTableModule,
        MatChipsModule,
        MatTooltipModule,
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

    // UI State
    selectedPeriod = '30D';
    currentDate = '';
    lastSyncTime = 'just now';

    // Performance metrics
    revenueTargetPercent = 78;
    fulfillmentRate = 92;
    customerSatisfaction = 4.5;

    // Funnel data
    funnelData = [
        { label: 'Site Visits', value: 12500, percentage: 100 },
        { label: 'Added to Cart', value: 4200, percentage: 34, rate: 34 },
        { label: 'Checkout Started', value: 2100, percentage: 17, rate: 50 },
        { label: 'Orders Completed', value: 1247, percentage: 10, rate: 59 }
    ];

    // Expose Math to template
    Math = Math;

    // Date range form
    dateRangeForm = new FormGroup({
        startDate: new FormControl<Date>(new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)),
        endDate: new FormControl<Date>(new Date())
    });

    // ECharts options
    revenueChartOption: EChartsOption = {};
    categoryChartOption: EChartsOption = {};
    orderStatusChartOption: EChartsOption = {};
    heatmapChartOption: EChartsOption = {};

    // Sparkline options
    revenueSparklineOption: EChartsOption = {};
    ordersSparklineOption: EChartsOption = {};
    usersSparklineOption: EChartsOption = {};
    productsSparklineOption: EChartsOption = {};

    // Gauge options
    revenueGaugeOption: EChartsOption = {};
    fulfillmentGaugeOption: EChartsOption = {};
    satisfactionGaugeOption: EChartsOption = {};

    constructor(private analyticsService: AnalyticsService) {
        this.updateCurrentDate();
    }

    ngOnInit(): void {
        this.loadAllAnalytics();
        // Update date every minute
        setInterval(() => this.updateCurrentDate(), 60000);
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    /**
     * Get time-based greeting
     */
    getGreeting(): string {
        const hour = new Date().getHours();
        if (hour < 12) return 'Good Morning';
        if (hour < 18) return 'Good Afternoon';
        return 'Good Evening';
    }

    /**
     * Update current date display
     */
    updateCurrentDate(): void {
        const now = new Date();
        const options: Intl.DateTimeFormatOptions = {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        };
        this.currentDate = now.toLocaleDateString('en-US', options);
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

                    // Prepare all charts
                    this.prepareRevenueChart();
                    this.prepareCategoryChart();
                    this.prepareOrderStatusChart();
                    this.prepareSparklines();
                    this.prepareGauges();
                    this.prepareHeatmap();

                    this.loading = false;
                    this.lastSyncTime = 'just now';
                },
                error: (error) => {
                    console.error('Error loading analytics:', error);
                    this.loading = false;
                }
            });
    }

    /**
     * Prepare revenue area chart with gradient
     */
    prepareRevenueChart(): void {
        const dates = this.salesChartData.map(item =>
            new Date(item.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
        );
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
                axisLine: { lineStyle: { color: '#e4e4e7' } },
                axisLabel: { color: '#71717a', fontSize: 11 }
            },
            yAxis: {
                type: 'value',
                axisLine: { show: false },
                axisTick: { show: false },
                splitLine: { lineStyle: { color: '#f4f4f5' } },
                axisLabel: { color: '#71717a', fontSize: 11, formatter: '{value} TND' }
            },
            series: [{
                data: revenues,
                type: 'line',
                smooth: true,
                symbol: 'circle',
                symbolSize: 8,
                lineStyle: { color: '#9333ea', width: 3 },
                itemStyle: { color: '#9333ea', borderWidth: 3, borderColor: '#fff' },
                areaStyle: {
                    color: {
                        type: 'linear',
                        x: 0, y: 0, x2: 0, y2: 1,
                        colorStops: [
                            { offset: 0, color: 'rgba(147, 51, 234, 0.25)' },
                            { offset: 1, color: 'rgba(147, 51, 234, 0.02)' }
                        ]
                    }
                }
            }],
            tooltip: {
                trigger: 'axis',
                backgroundColor: '#fff',
                borderColor: '#e4e4e7',
                borderWidth: 1,
                textStyle: { color: '#18181b' },
                formatter: (params: any) => {
                    const param = params[0];
                    return `<div style="font-weight:600;color:#71717a">${param.name}</div>
                            <div style="color:#9333ea;font-size:18px;font-weight:700;margin-top:6px">
                                ${param.value.toLocaleString('en-US')} TND
                            </div>`;
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

        const colors = ['#9333ea', '#a855f7', '#c084fc', '#d8b4fe', '#e9d5ff'];

        this.categoryChartOption = {
            grid: {
                left: '25%',
                right: '10%',
                bottom: '5%',
                top: '5%',
                containLabel: true
            },
            xAxis: {
                type: 'value',
                axisLine: { show: false },
                axisTick: { show: false },
                splitLine: { lineStyle: { color: '#f4f4f5' } },
                axisLabel: { color: '#71717a', fontSize: 11 }
            },
            yAxis: {
                type: 'category',
                data: categories,
                axisLine: { lineStyle: { color: '#e4e4e7' } },
                axisLabel: { color: '#18181b', fontSize: 12, fontWeight: 500 }
            },
            series: [{
                data: revenues.map((val, idx) => ({
                    value: val,
                    itemStyle: { color: colors[idx % colors.length] }
                })),
                type: 'bar',
                barWidth: '55%',
                itemStyle: { borderRadius: [0, 8, 8, 0] },
                label: {
                    show: true,
                    position: 'right',
                    formatter: '{c} TND',
                    color: '#71717a',
                    fontSize: 11,
                    fontWeight: 600
                }
            }],
            tooltip: {
                trigger: 'axis',
                axisPointer: { type: 'shadow' },
                backgroundColor: '#fff',
                borderColor: '#e4e4e7',
                textStyle: { color: '#18181b' }
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
                borderColor: '#e4e4e7',
                textStyle: { color: '#18181b' },
                formatter: '{b}: <strong>{c}</strong> ({d}%)'
            },
            legend: {
                bottom: '0%',
                left: 'center',
                textStyle: { color: '#71717a', fontSize: 11 }
            },
            series: [{
                type: 'pie',
                radius: ['48%', '78%'],
                center: ['50%', '45%'],
                avoidLabelOverlap: false,
                itemStyle: {
                    borderRadius: 10,
                    borderColor: '#fff',
                    borderWidth: 4
                },
                label: { show: false },
                emphasis: {
                    label: {
                        show: true,
                        fontSize: 15,
                        fontWeight: 'bold',
                        color: '#18181b'
                    },
                    itemStyle: {
                        shadowBlur: 20,
                        shadowColor: 'rgba(147, 51, 234, 0.4)'
                    }
                },
                data: data,
                color: ['#10b981', '#9333ea', '#f59e0b', '#06b6d4', '#ef4444']
            }]
        };
    }

    /**
     * Prepare sparklines for KPI cards
     */
    prepareSparklines(): void {
        const last7Days = this.salesChartData.slice(-7);
        const revenues = last7Days.map(item => item.revenue);
        const orders = last7Days.map(item => item.orders);

        this.revenueSparklineOption = this.createSparkline(revenues, '#10b981');
        this.ordersSparklineOption = this.createSparkline(orders, '#9333ea');
        this.usersSparklineOption = this.createSparkline([120, 132, 101, 134, 90, 230, 210], '#a855f7');
        this.productsSparklineOption = this.createSparkline([220, 182, 191, 234, 290, 330, 310], '#f59e0b');
    }

    /**
     * Create sparkline chart
     */
    private createSparkline(data: number[], color: string): EChartsOption {
        return {
            grid: { left: 0, right: 0, top: 5, bottom: 5 },
            xAxis: { type: 'category', show: false, boundaryGap: false },
            yAxis: { type: 'value', show: false },
            series: [{
                data: data,
                type: 'line',
                smooth: true,
                symbol: 'none',
                lineStyle: { color: color, width: 2 },
                areaStyle: {
                    color: {
                        type: 'linear',
                        x: 0, y: 0, x2: 0, y2: 1,
                        colorStops: [
                            { offset: 0, color: color + '60' },
                            { offset: 1, color: color + '00' }
                        ]
                    }
                }
            }]
        };
    }

    /**
     * Prepare gauge charts
     */
    prepareGauges(): void {
        this.revenueGaugeOption = this.createGauge(this.revenueTargetPercent, '#10b981');
        this.fulfillmentGaugeOption = this.createGauge(this.fulfillmentRate, '#9333ea');
        this.satisfactionGaugeOption = this.createGauge((this.customerSatisfaction / 5) * 100, '#a855f7');
    }

    /**
     * Create gauge chart
     */
    private createGauge(value: number, color: string): EChartsOption {
        return {
            series: [{
                type: 'gauge',
                startAngle: 180,
                endAngle: 0,
                min: 0,
                max: 100,
                radius: '100%',
                center: ['50%', '85%'],
                splitNumber: 5,
                axisLine: {
                    lineStyle: {
                        width: 14,
                        color: [
                            [value / 100, color],
                            [1, '#e4e4e7']
                        ]
                    }
                },
                pointer: { show: false },
                axisTick: { show: false },
                splitLine: { show: false },
                axisLabel: { show: false },
                detail: { show: false }
            }]
        };
    }

    /**
     * Prepare heatmap chart
     */
    prepareHeatmap(): void {
        const hours = ['00', '03', '06', '09', '12', '15', '18', '21'];
        const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

        // Generate sample data
        const data: [number, number, number][] = [];
        for (let i = 0; i < 7; i++) {
            for (let j = 0; j < 8; j++) {
                data.push([j, i, Math.floor(Math.random() * 100)]);
            }
        }

        this.heatmapChartOption = {
            tooltip: {
                position: 'top',
                backgroundColor: '#fff',
                borderColor: '#e4e4e7',
                textStyle: { color: '#18181b' }
            },
            grid: {
                left: '15%',
                right: '5%',
                top: '5%',
                bottom: '15%'
            },
            xAxis: {
                type: 'category',
                data: hours,
                splitArea: { show: true },
                axisLine: { show: false },
                axisLabel: { color: '#71717a', fontSize: 10 }
            },
            yAxis: {
                type: 'category',
                data: days,
                splitArea: { show: true },
                axisLine: { show: false },
                axisLabel: { color: '#71717a', fontSize: 11 }
            },
            visualMap: {
                min: 0,
                max: 100,
                show: false,
                inRange: {
                    color: ['#faf5ff', '#e9d5ff', '#c084fc', '#a855f7', '#9333ea']
                }
            },
            series: [{
                type: 'heatmap',
                data: data,
                label: { show: false },
                emphasis: {
                    itemStyle: {
                        shadowBlur: 10,
                        shadowColor: 'rgba(147, 51, 234, 0.5)'
                    }
                }
            }]
        };
    }

    /**
     * Refresh data
     */
    refreshData(): void {
        this.refreshing = true;
        this.loadAllAnalytics();
        setTimeout(() => {
            this.refreshing = false;
        }, 1000);
    }

    /**
     * Change period
     */
    changePeriod(period: string): void {
        this.selectedPeriod = period;
        // Recalculate date range based on period
        const now = new Date();
        let startDate = new Date();

        switch (period) {
            case '7D':
                startDate.setDate(now.getDate() - 7);
                break;
            case '30D':
                startDate.setDate(now.getDate() - 30);
                break;
            case '90D':
                startDate.setDate(now.getDate() - 90);
                break;
            case '1Y':
                startDate.setFullYear(now.getFullYear() - 1);
                break;
        }

        this.dateRangeForm.patchValue({
            startDate: startDate,
            endDate: now
        });

        this.loadAllAnalytics();
    }

    /**
     * Export report
     */
    exportReport(): void {
        // TODO: Implement export functionality
        console.log('Export report clicked');
    }

    /**
     * Get product progress percentage for leaderboard
     */
    getProductProgress(product: TopProduct): number {
        const maxRevenue = Math.max(...this.topProducts.map(p => p.revenue));
        return (product.revenue / maxRevenue) * 100;
    }

    /**
     * Get relative time string
     */
    getRelativeTime(timestamp: string | Date): string {
        const now = new Date();
        const time = new Date(timestamp);
        const diffMs = now.getTime() - time.getTime();
        const diffMins = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMs / 3600000);
        const diffDays = Math.floor(diffMs / 86400000);

        if (diffMins < 1) return 'just now';
        if (diffMins < 60) return `${diffMins} min ago`;
        if (diffHours < 24) return `${diffHours}h ago`;
        return `${diffDays}d ago`;
    }

    /**
     * Get activity CSS class
     */
    getActivityClass(type: string): string {
        switch (type) {
            case 'ORDER': return 'order';
            case 'USER': return 'user';
            case 'PRODUCT': return 'product';
            case 'RECLAMATION': return 'alert';
            default: return 'order';
        }
    }

    /**
     * Get activity icon
     */
    getActivityIcon(type: string): string {
        switch (type) {
            case 'ORDER': return 'shopping_cart';
            case 'USER': return 'person_add';
            case 'PRODUCT': return 'inventory_2';
            case 'RECLAMATION': return 'report_problem';
            default: return 'info';
        }
    }

    /**
     * Get status label
     */
    getStatusLabel(status: string): string {
        const labels: { [key: string]: string } = {
            'PENDING': 'Pending',
            'PROCESSING': 'Processing',
            'SHIPPED': 'Shipped',
            'DELIVERED': 'Delivered',
            'CANCELLED': 'Cancelled'
        };
        return labels[status] || status;
    }

    /**
     * Get growth icon
     */
    getGrowthIcon(growth?: number): string {
        if (!growth) return '';
        return growth > 0 ? 'trending_up' : 'trending_down';
    }

    /**
     * Format percentage
     */
    formatPercentage(value: number): string {
        return `${value.toFixed(1)}%`;
    }

    /**
     * Format date
     */
    private formatDate(date: Date): string {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }
}
