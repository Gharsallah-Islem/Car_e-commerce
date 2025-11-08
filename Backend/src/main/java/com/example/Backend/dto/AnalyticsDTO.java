package com.example.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Objects for Analytics
 */
public class AnalyticsDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardStats {
        private Long totalOrders;
        private BigDecimal totalRevenue;
        private Long totalProducts;
        private Long totalUsers;
        private Long pendingOrders;
        private Long lowStockProducts;
        private Long todayOrders;
        private BigDecimal todayRevenue;
        private Long activeUsers;
        private Long pendingReclamations;
        private Double averageOrderValue;
        private Double conversionRate;

        // Growth percentages (compared to previous period)
        private Double ordersGrowth;
        private Double revenueGrowth;
        private Double usersGrowth;
        private Double productsGrowth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesChartData {
        private LocalDate date;
        private BigDecimal revenue;
        private Long orders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryPerformance {
        private String categoryName;
        private Long productCount;
        private Long orderCount;
        private BigDecimal revenue;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private String productId;
        private String productName;
        private String category;
        private String brand;
        private Long unitsSold;
        private BigDecimal revenue;
        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueByPeriod {
        private String period; // Daily, Weekly, Monthly, Yearly
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal totalRevenue;
        private Long totalOrders;
        private BigDecimal averageOrderValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerAnalytics {
        private Long totalCustomers;
        private Long newCustomersToday;
        private Long newCustomersThisWeek;
        private Long newCustomersThisMonth;
        private Long activeCustomers; // Customers who ordered in last 30 days
        private Long returningCustomers;
        private Double retentionRate;
        private BigDecimal customerLifetimeValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusDistribution {
        private String status;
        private Long count;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivity {
        private String type; // ORDER, USER, PRODUCT, RECLAMATION
        private String description;
        private LocalDateTime timestamp;
        private String userId;
        private String userName;
        private Map<String, Object> metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInventoryAlert {
        private String productId;
        private String productName;
        private String category;
        private Integer currentStock;
        private Integer minimumStock;
        private String severity; // CRITICAL, WARNING, INFO
        private LocalDateTime lastUpdated;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComprehensiveAnalytics {
        private DashboardStats dashboardStats;
        private List<SalesChartData> salesChart;
        private List<CategoryPerformance> categoryPerformance;
        private List<TopProduct> topProducts;
        private List<OrderStatusDistribution> orderStatusDistribution;
        private CustomerAnalytics customerAnalytics;
        private List<RecentActivity> recentActivities;
        private List<ProductInventoryAlert> inventoryAlerts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateRangeRequest {
        private LocalDate startDate;
        private LocalDate endDate;
        private String period; // DAILY, WEEKLY, MONTHLY
    }
}
