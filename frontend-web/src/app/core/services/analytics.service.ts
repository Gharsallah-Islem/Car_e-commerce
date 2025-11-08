import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
    DashboardStats,
    SalesChartData,
    CategoryPerformance,
    TopProduct,
    RevenueByPeriod,
    CustomerAnalytics,
    OrderStatusDistribution,
    RecentActivity,
    ProductInventoryAlert,
    ComprehensiveAnalytics
} from '../models/analytics.interface';

@Injectable({
    providedIn: 'root'
})
export class AnalyticsService {
    private apiUrl = `${environment.apiUrl}/analytics`;

    constructor(private http: HttpClient) { }

    /**
     * Get basic dashboard statistics
     */
    getDashboardStats(): Observable<DashboardStats> {
        return this.http.get<DashboardStats>(`${this.apiUrl}/dashboard`);
    }

    /**
     * Get dashboard statistics with growth metrics
     */
    getDashboardStatsWithGrowth(): Observable<DashboardStats> {
        return this.http.get<DashboardStats>(`${this.apiUrl}/dashboard/growth`);
    }

    /**
     * Get comprehensive analytics for a date range
     */
    getComprehensiveAnalytics(startDate?: string, endDate?: string): Observable<ComprehensiveAnalytics> {
        let params = new HttpParams();
        if (startDate) {
            params = params.set('startDate', startDate);
        }
        if (endDate) {
            params = params.set('endDate', endDate);
        }
        return this.http.get<ComprehensiveAnalytics>(`${this.apiUrl}/comprehensive`, { params });
    }

    /**
     * Get sales chart data
     */
    getSalesChartData(startDate: string, endDate: string, period: string = 'DAILY'): Observable<SalesChartData[]> {
        const params = new HttpParams()
            .set('startDate', startDate)
            .set('endDate', endDate)
            .set('period', period);
        return this.http.get<SalesChartData[]>(`${this.apiUrl}/sales-chart`, { params });
    }

    /**
     * Get category performance
     */
    getCategoryPerformance(startDate?: string, endDate?: string): Observable<CategoryPerformance[]> {
        let params = new HttpParams();
        if (startDate) {
            params = params.set('startDate', startDate);
        }
        if (endDate) {
            params = params.set('endDate', endDate);
        }
        return this.http.get<CategoryPerformance[]>(`${this.apiUrl}/category-performance`, { params });
    }

    /**
     * Get top selling products
     */
    getTopProducts(limit: number = 10, startDate?: string, endDate?: string): Observable<TopProduct[]> {
        let params = new HttpParams().set('limit', limit.toString());
        if (startDate) {
            params = params.set('startDate', startDate);
        }
        if (endDate) {
            params = params.set('endDate', endDate);
        }
        return this.http.get<TopProduct[]>(`${this.apiUrl}/top-products`, { params });
    }

    /**
     * Get revenue by period
     */
    getRevenueByPeriod(startDate: string, endDate: string, period: string = 'DAILY'): Observable<RevenueByPeriod[]> {
        const params = new HttpParams()
            .set('startDate', startDate)
            .set('endDate', endDate)
            .set('period', period);
        return this.http.get<RevenueByPeriod[]>(`${this.apiUrl}/revenue-by-period`, { params });
    }

    /**
     * Get customer analytics
     */
    getCustomerAnalytics(): Observable<CustomerAnalytics> {
        return this.http.get<CustomerAnalytics>(`${this.apiUrl}/customers`);
    }

    /**
     * Get order status distribution
     */
    getOrderStatusDistribution(): Observable<OrderStatusDistribution[]> {
        return this.http.get<OrderStatusDistribution[]>(`${this.apiUrl}/order-status-distribution`);
    }

    /**
     * Get recent activities
     */
    getRecentActivities(limit: number = 20): Observable<RecentActivity[]> {
        const params = new HttpParams().set('limit', limit.toString());
        return this.http.get<RecentActivity[]>(`${this.apiUrl}/recent-activities`, { params });
    }

    /**
     * Get inventory alerts
     */
    getInventoryAlerts(): Observable<ProductInventoryAlert[]> {
        return this.http.get<ProductInventoryAlert[]>(`${this.apiUrl}/inventory-alerts`);
    }
}
