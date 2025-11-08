package com.example.Backend.service;

import com.example.Backend.dto.AnalyticsDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Analytics and Dashboard Statistics
 */
public interface AnalyticsService {

    /**
     * Get comprehensive dashboard statistics
     */
    AnalyticsDTO.DashboardStats getDashboardStats();

    /**
     * Get comprehensive analytics for admin dashboard
     */
    AnalyticsDTO.ComprehensiveAnalytics getComprehensiveAnalytics(LocalDate startDate, LocalDate endDate);

    /**
     * Get sales chart data for a date range
     */
    List<AnalyticsDTO.SalesChartData> getSalesChartData(LocalDate startDate, LocalDate endDate, String period);

    /**
     * Get category performance metrics
     */
    List<AnalyticsDTO.CategoryPerformance> getCategoryPerformance(LocalDate startDate, LocalDate endDate);

    /**
     * Get top selling products
     */
    List<AnalyticsDTO.TopProduct> getTopProducts(int limit, LocalDate startDate, LocalDate endDate);

    /**
     * Get revenue by period (daily, weekly, monthly)
     */
    List<AnalyticsDTO.RevenueByPeriod> getRevenueByPeriod(LocalDate startDate, LocalDate endDate, String period);

    /**
     * Get customer analytics
     */
    AnalyticsDTO.CustomerAnalytics getCustomerAnalytics();

    /**
     * Get order status distribution
     */
    List<AnalyticsDTO.OrderStatusDistribution> getOrderStatusDistribution();

    /**
     * Get recent activities (orders, users, products)
     */
    List<AnalyticsDTO.RecentActivity> getRecentActivities(int limit);

    /**
     * Get low stock product alerts
     */
    List<AnalyticsDTO.ProductInventoryAlert> getInventoryAlerts();

    /**
     * Calculate growth percentage compared to previous period
     */
    AnalyticsDTO.DashboardStats getDashboardStatsWithGrowth();
}
