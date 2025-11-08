package com.example.Backend.controller;

import com.example.Backend.dto.AnalyticsDTO;
import com.example.Backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Analytics and Dashboard Statistics
 * Only accessible by ADMIN and SUPER_ADMIN roles
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@CrossOrigin(originPatterns = "*", maxAge = 3600)
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Get dashboard statistics
     * GET /api/analytics/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsDTO.DashboardStats> getDashboardStats() {
        AnalyticsDTO.DashboardStats stats = analyticsService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get dashboard statistics with growth metrics
     * GET /api/analytics/dashboard/growth
     */
    @GetMapping("/dashboard/growth")
    public ResponseEntity<AnalyticsDTO.DashboardStats> getDashboardStatsWithGrowth() {
        AnalyticsDTO.DashboardStats stats = analyticsService.getDashboardStatsWithGrowth();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get comprehensive analytics for a date range
     * GET /api/analytics/comprehensive
     * 
     * @param startDate Start date (optional, defaults to 30 days ago)
     * @param endDate   End date (optional, defaults to today)
     */
    @GetMapping("/comprehensive")
    public ResponseEntity<AnalyticsDTO.ComprehensiveAnalytics> getComprehensiveAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        AnalyticsDTO.ComprehensiveAnalytics analytics = analyticsService.getComprehensiveAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get sales chart data
     * GET /api/analytics/sales-chart
     */
    @GetMapping("/sales-chart")
    public ResponseEntity<List<AnalyticsDTO.SalesChartData>> getSalesChartData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "DAILY") String period) {

        List<AnalyticsDTO.SalesChartData> chartData = analyticsService.getSalesChartData(startDate, endDate, period);
        return ResponseEntity.ok(chartData);
    }

    /**
     * Get category performance
     * GET /api/analytics/category-performance
     */
    @GetMapping("/category-performance")
    public ResponseEntity<List<AnalyticsDTO.CategoryPerformance>> getCategoryPerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<AnalyticsDTO.CategoryPerformance> performance = analyticsService.getCategoryPerformance(startDate,
                endDate);
        return ResponseEntity.ok(performance);
    }

    /**
     * Get top selling products
     * GET /api/analytics/top-products
     */
    @GetMapping("/top-products")
    public ResponseEntity<List<AnalyticsDTO.TopProduct>> getTopProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<AnalyticsDTO.TopProduct> topProducts = analyticsService.getTopProducts(limit, startDate, endDate);
        return ResponseEntity.ok(topProducts);
    }

    /**
     * Get revenue by period
     * GET /api/analytics/revenue-by-period
     */
    @GetMapping("/revenue-by-period")
    public ResponseEntity<List<AnalyticsDTO.RevenueByPeriod>> getRevenueByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "DAILY") String period) {

        List<AnalyticsDTO.RevenueByPeriod> revenue = analyticsService.getRevenueByPeriod(startDate, endDate, period);
        return ResponseEntity.ok(revenue);
    }

    /**
     * Get customer analytics
     * GET /api/analytics/customers
     */
    @GetMapping("/customers")
    public ResponseEntity<AnalyticsDTO.CustomerAnalytics> getCustomerAnalytics() {
        AnalyticsDTO.CustomerAnalytics analytics = analyticsService.getCustomerAnalytics();
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get order status distribution
     * GET /api/analytics/order-status-distribution
     */
    @GetMapping("/order-status-distribution")
    public ResponseEntity<List<AnalyticsDTO.OrderStatusDistribution>> getOrderStatusDistribution() {
        List<AnalyticsDTO.OrderStatusDistribution> distribution = analyticsService.getOrderStatusDistribution();
        return ResponseEntity.ok(distribution);
    }

    /**
     * Get recent activities
     * GET /api/analytics/recent-activities
     */
    @GetMapping("/recent-activities")
    public ResponseEntity<List<AnalyticsDTO.RecentActivity>> getRecentActivities(
            @RequestParam(defaultValue = "20") int limit) {

        List<AnalyticsDTO.RecentActivity> activities = analyticsService.getRecentActivities(limit);
        return ResponseEntity.ok(activities);
    }

    /**
     * Get inventory alerts
     * GET /api/analytics/inventory-alerts
     */
    @GetMapping("/inventory-alerts")
    public ResponseEntity<List<AnalyticsDTO.ProductInventoryAlert>> getInventoryAlerts() {
        List<AnalyticsDTO.ProductInventoryAlert> alerts = analyticsService.getInventoryAlerts();
        return ResponseEntity.ok(alerts);
    }
}
