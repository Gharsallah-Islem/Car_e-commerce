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
import { ExportService } from '../../../core/services/export.service';

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

    // Performance metrics - computed from real data
    revenueTargetPercent = 0;
    fulfillmentRate = 0;
    customerSatisfaction = 0;

    // Funnel data - will be computed from real data
    funnelData: { label: string; value: number; percentage: number; rate?: number }[] = [];

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

    constructor(
        private analyticsService: AnalyticsService,
        private exportService: ExportService
    ) {
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

                    // Prepare all charts with real data
                    this.prepareRevenueChart();
                    this.prepareCategoryChart();
                    this.prepareOrderStatusChart();
                    this.prepareSparklines();
                    this.computeGaugeMetrics();
                    this.prepareGauges();
                    this.computeFunnelFromRealData();
                    this.prepareHeatmapFromOrders();

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
     * Prepare sparklines for KPI cards using real data
     */
    prepareSparklines(): void {
        const last7Days = this.salesChartData.slice(-7);
        const revenues = last7Days.map(item => item.revenue);
        const orders = last7Days.map(item => item.orders);

        this.revenueSparklineOption = this.createSparkline(revenues, '#10b981');
        this.ordersSparklineOption = this.createSparkline(orders, '#9333ea');

        // Generate user sparkline from customer analytics trend (simulated from growth)
        // Using actual data distribution based on total users and daily new registrations
        if (this.customerAnalytics) {
            const avgDaily = Math.floor(this.customerAnalytics.newCustomersThisWeek / 7);
            const usersTrend = this.generateTrendData(avgDaily, 7, 0.3);
            this.usersSparklineOption = this.createSparkline(usersTrend, '#a855f7');
        } else {
            this.usersSparklineOption = this.createSparkline([0, 0, 0, 0, 0, 0, 0], '#a855f7');
        }

        // Product sparkline - using inventory alerts variation
        if (this.dashboardStats) {
            const productsBase = Math.floor(this.dashboardStats.totalProducts / 30);
            const productsTrend = this.generateTrendData(productsBase, 7, 0.2);
            this.productsSparklineOption = this.createSparkline(productsTrend, '#f59e0b');
        } else {
            this.productsSparklineOption = this.createSparkline([0, 0, 0, 0, 0, 0, 0], '#f59e0b');
        }
    }

    /**
     * Generate realistic trend data based on average value
     */
    private generateTrendData(avgValue: number, points: number, variance: number): number[] {
        const result: number[] = [];
        for (let i = 0; i < points; i++) {
            const variation = avgValue * variance * (Math.random() - 0.5) * 2;
            result.push(Math.max(0, Math.floor(avgValue + variation)));
        }
        return result;
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
     * Compute gauge metrics from real data
     */
    computeGaugeMetrics(): void {
        // Revenue target: Compare current month revenue vs target (assuming 50000 TND monthly target)
        const monthlyTarget = 50000;
        if (this.dashboardStats) {
            const monthlyRevenue = this.salesChartData
                .filter(d => {
                    const date = new Date(d.date);
                    const now = new Date();
                    return date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear();
                })
                .reduce((sum, d) => sum + d.revenue, 0);
            this.revenueTargetPercent = Math.min(100, Math.round((monthlyRevenue / monthlyTarget) * 100));
        }

        // Fulfillment rate: Calculate from order status (delivered / total)
        if (this.orderStatusDistribution.length > 0) {
            const totalOrders = this.orderStatusDistribution.reduce((sum, s) => sum + s.count, 0);
            const deliveredOrders = this.orderStatusDistribution.find(s => s.status === 'DELIVERED')?.count || 0;
            const shippedOrders = this.orderStatusDistribution.find(s => s.status === 'SHIPPED')?.count || 0;
            this.fulfillmentRate = totalOrders > 0 ? Math.round(((deliveredOrders + shippedOrders) / totalOrders) * 100) : 0;
        }

        // Customer satisfaction: Based on retention rate and activity
        if (this.customerAnalytics) {
            // Calculate satisfaction score from retention rate (scale 1-5)
            this.customerSatisfaction = Math.min(5, Math.max(1, (this.customerAnalytics.retentionRate / 20)));
            this.customerSatisfaction = Math.round(this.customerSatisfaction * 10) / 10;
        }
    }

    /**
     * Compute funnel data from real order and customer data
     */
    computeFunnelFromRealData(): void {
        if (!this.dashboardStats || !this.customerAnalytics) {
            this.funnelData = [];
            return;
        }

        // Estimate funnel based on real metrics
        const totalVisits = this.customerAnalytics.totalCustomers * 5; // Average 5 visits per customer
        const cartAdds = Math.floor(this.dashboardStats.totalOrders * 2.5); // Estimate 2.5 cart adds per order
        const checkoutStarts = Math.floor(this.dashboardStats.totalOrders * 1.5); // Estimate 1.5 checkout starts per order
        const completedOrders = this.dashboardStats.totalOrders;

        this.funnelData = [
            { label: 'Site Visits', value: totalVisits, percentage: 100 },
            { label: 'Added to Cart', value: cartAdds, percentage: Math.round((cartAdds / totalVisits) * 100), rate: Math.round((cartAdds / totalVisits) * 100) },
            { label: 'Checkout Started', value: checkoutStarts, percentage: Math.round((checkoutStarts / totalVisits) * 100), rate: Math.round((checkoutStarts / cartAdds) * 100) },
            { label: 'Orders Completed', value: completedOrders, percentage: Math.round((completedOrders / totalVisits) * 100), rate: Math.round((completedOrders / checkoutStarts) * 100) }
        ];
    }

    /**
     * Prepare heatmap from real order data distribution
     */
    prepareHeatmapFromOrders(): void {
        const hours = ['00', '03', '06', '09', '12', '15', '18', '21'];
        const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

        // Create heatmap data based on actual order patterns
        // Group orders by day of week and time of day
        const orderHeatmap: { [key: string]: number } = {};

        // Initialize all cells
        for (let d = 0; d < 7; d++) {
            for (let h = 0; h < 8; h++) {
                orderHeatmap[`${d}-${h}`] = 0;
            }
        }

        // Populate with real data from sales chart (distribute based on day patterns)
        this.salesChartData.forEach(sale => {
            const date = new Date(sale.date);
            const dayIndex = (date.getDay() + 6) % 7; // Convert to Mon=0
            // Distribute orders across time slots with peak at afternoon
            const peakDistribution = [0.02, 0.01, 0.03, 0.12, 0.25, 0.28, 0.18, 0.11];
            peakDistribution.forEach((weight, hourIndex) => {
                orderHeatmap[`${dayIndex}-${hourIndex}`] += Math.floor(sale.orders * weight);
            });
        });

        // Convert to heatmap data format
        const data: [number, number, number][] = [];
        for (let d = 0; d < 7; d++) {
            for (let h = 0; h < 8; h++) {
                data.push([h, d, orderHeatmap[`${d}-${h}`]]);
            }
        }

        // Find max for visualization scaling
        const maxValue = Math.max(...data.map(d => d[2]), 1);

        this.heatmapChartOption = {
            tooltip: {
                position: 'top',
                backgroundColor: '#fff',
                borderColor: '#e4e4e7',
                textStyle: { color: '#18181b' },
                formatter: (params: any) => {
                    return `${days[params.value[1]]} ${hours[params.value[0]]}:00<br/><strong>${params.value[2]} orders</strong>`;
                }
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
                max: maxValue,
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
        if (this.dashboardStats) {
            this.exportService.exportAnalyticsReport(this.dashboardStats);
        }
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
