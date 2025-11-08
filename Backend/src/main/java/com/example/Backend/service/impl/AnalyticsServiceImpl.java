package com.example.Backend.service.impl;

import com.example.Backend.dto.AnalyticsDTO;
import com.example.Backend.entity.Order;
import com.example.Backend.entity.Product;
import com.example.Backend.entity.User;
import com.example.Backend.repository.OrderRepository;
import com.example.Backend.repository.ProductRepository;
import com.example.Backend.repository.ReclamationRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReclamationRepository reclamationRepository;

    private static final int LOW_STOCK_THRESHOLD = 10;

    @Override
    public AnalyticsDTO.DashboardStats getDashboardStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        // Total counts
        long totalOrders = orderRepository.count();
        long totalProducts = productRepository.count();
        long totalUsers = userRepository.count();

        // Total revenue
        BigDecimal totalRevenue = orderRepository.findAll().stream()
                .filter(order -> !"CANCELLED".equals(order.getStatus()))
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Today's metrics
        List<Order> todayOrders = orderRepository.findByCreatedAtBetween(startOfDay, endOfDay);
        long todayOrdersCount = todayOrders.size();
        BigDecimal todayRevenue = todayOrders.stream()
                .filter(order -> !"CANCELLED".equals(order.getStatus()))
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Pending orders
        long pendingOrders = orderRepository.countByStatus("PENDING");

        // Low stock products
        long lowStockProducts = productRepository.findAll().stream()
                .filter(p -> p.getStock() != null && p.getStock() < LOW_STOCK_THRESHOLD)
                .count();

        // Active users (users who logged in last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long activeUsers = userRepository.findAll().stream()
                .filter(u -> u.getUpdatedAt() != null && u.getUpdatedAt().isAfter(thirtyDaysAgo))
                .count();

        // Pending reclamations
        long pendingReclamations = reclamationRepository.countByStatus("PENDING");

        // Average order value
        Double averageOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        // Conversion rate (simplified - orders vs users)
        Double conversionRate = totalUsers > 0
                ? (totalOrders * 100.0) / totalUsers
                : 0.0;

        return AnalyticsDTO.DashboardStats.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .totalProducts(totalProducts)
                .totalUsers(totalUsers)
                .pendingOrders(pendingOrders)
                .lowStockProducts(lowStockProducts)
                .todayOrders(todayOrdersCount)
                .todayRevenue(todayRevenue)
                .activeUsers(activeUsers)
                .pendingReclamations(pendingReclamations)
                .averageOrderValue(averageOrderValue)
                .conversionRate(conversionRate)
                .build();
    }

    @Override
    public AnalyticsDTO.DashboardStats getDashboardStatsWithGrowth() {
        AnalyticsDTO.DashboardStats currentStats = getDashboardStats();

        // Calculate previous period stats (30 days ago)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime sixtyDaysAgo = LocalDateTime.now().minusDays(60);

        List<Order> previousPeriodOrders = orderRepository.findByCreatedAtBetween(sixtyDaysAgo, thirtyDaysAgo);
        long previousOrders = previousPeriodOrders.size();
        BigDecimal previousRevenue = previousPeriodOrders.stream()
                .filter(order -> !"CANCELLED".equals(order.getStatus()))
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long previousUsers = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null
                        && u.getCreatedAt().isBefore(thirtyDaysAgo))
                .count();

        // Calculate growth percentages
        Double ordersGrowth = calculateGrowth(currentStats.getTotalOrders(), previousOrders);
        Double revenueGrowth = calculateGrowth(
                currentStats.getTotalRevenue().doubleValue(),
                previousRevenue.doubleValue());
        Double usersGrowth = calculateGrowth(currentStats.getTotalUsers(), previousUsers);
        Double productsGrowth = 0.0; // Can be calculated if we track historical product counts

        currentStats.setOrdersGrowth(ordersGrowth);
        currentStats.setRevenueGrowth(revenueGrowth);
        currentStats.setUsersGrowth(usersGrowth);
        currentStats.setProductsGrowth(productsGrowth);

        return currentStats;
    }

    @Override
    public AnalyticsDTO.ComprehensiveAnalytics getComprehensiveAnalytics(LocalDate startDate, LocalDate endDate) {
        return AnalyticsDTO.ComprehensiveAnalytics.builder()
                .dashboardStats(getDashboardStatsWithGrowth())
                .salesChart(getSalesChartData(startDate, endDate, "DAILY"))
                .categoryPerformance(getCategoryPerformance(startDate, endDate))
                .topProducts(getTopProducts(10, startDate, endDate))
                .orderStatusDistribution(getOrderStatusDistribution())
                .customerAnalytics(getCustomerAnalytics())
                .recentActivities(getRecentActivities(20))
                .inventoryAlerts(getInventoryAlerts())
                .build();
    }

    @Override
    public List<AnalyticsDTO.SalesChartData> getSalesChartData(LocalDate startDate, LocalDate endDate, String period) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        // Use fetch join to avoid lazy loading issues
        List<Order> orders = orderRepository.findByCreatedAtBetweenWithItems(start, end);

        Map<LocalDate, List<Order>> ordersByDate = orders.stream()
                .filter(order -> !"CANCELLED".equals(order.getStatus()))
                .collect(Collectors.groupingBy(order -> order.getCreatedAt().toLocalDate()));

        List<AnalyticsDTO.SalesChartData> chartData = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<Order> dayOrders = ordersByDate.getOrDefault(date, Collections.emptyList());

            BigDecimal dayRevenue = dayOrders.stream()
                    .map(Order::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            chartData.add(AnalyticsDTO.SalesChartData.builder()
                    .date(date)
                    .revenue(dayRevenue)
                    .orders((long) dayOrders.size())
                    .build());
        }

        return chartData;
    }

    @Override
    public List<AnalyticsDTO.CategoryPerformance> getCategoryPerformance(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        // Use fetch join to avoid lazy loading issues
        List<Order> orders = orderRepository.findByCreatedAtBetweenWithItems(start, end);

        // Group by category
        Map<String, Set<UUID>> orderIdsByCategory = new HashMap<>();
        Map<String, Long> productCountByCategory = new HashMap<>();
        Map<String, BigDecimal> revenueByCategory = new HashMap<>();

        List<Product> allProducts = productRepository.findAll();
        allProducts.forEach(product -> {
            String category = product.getCategory() != null ? product.getCategory() : "Uncategorized";
            productCountByCategory.put(category,
                    productCountByCategory.getOrDefault(category, 0L) + 1);
        });

        orders.stream()
                .filter(order -> !"CANCELLED".equals(order.getStatus()))
                .forEach(order -> {
                    if (order.getOrderItems() != null) {
                        order.getOrderItems().forEach(item -> {
                            if (item.getProduct() != null) {
                                String category = item.getProduct().getCategory() != null
                                        ? item.getProduct().getCategory()
                                        : "Uncategorized";

                                // Use Set of order IDs to ensure distinct count
                                orderIdsByCategory.computeIfAbsent(category, k -> new HashSet<>()).add(order.getId());

                                BigDecimal itemRevenue = item.getPrice()
                                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                                revenueByCategory.put(category,
                                        revenueByCategory.getOrDefault(category, BigDecimal.ZERO).add(itemRevenue));
                            }
                        });
                    }
                });

        BigDecimal totalRevenue = revenueByCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return revenueByCategory.entrySet().stream()
                .map(entry -> {
                    String category = entry.getKey();
                    BigDecimal revenue = entry.getValue();
                    Long orderCount = (long) orderIdsByCategory.getOrDefault(category, Collections.emptySet()).size();
                    Double percentage = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                            ? revenue.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100)).doubleValue()
                            : 0.0;

                    return AnalyticsDTO.CategoryPerformance.builder()
                            .categoryName(category)
                            .productCount(productCountByCategory.getOrDefault(category, 0L))
                            .orderCount(orderCount)
                            .revenue(revenue)
                            .percentage(percentage)
                            .build();
                })
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<AnalyticsDTO.TopProduct> getTopProducts(int limit, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        // Use fetch join to avoid lazy loading issues
        List<Order> orders = orderRepository.findByCreatedAtBetweenWithItems(start, end);

        Map<String, Long> unitsSoldByProduct = new HashMap<>();
        Map<String, BigDecimal> revenueByProduct = new HashMap<>();

        orders.stream()
                .filter(order -> !"CANCELLED".equals(order.getStatus()))
                .forEach(order -> {
                    if (order.getOrderItems() != null) {
                        order.getOrderItems().forEach(item -> {
                            if (item.getProduct() != null) {
                                String productId = item.getProduct().getId().toString();
                                unitsSoldByProduct.put(productId,
                                        unitsSoldByProduct.getOrDefault(productId, 0L) + item.getQuantity());

                                BigDecimal itemRevenue = item.getPrice()
                                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                                revenueByProduct.put(productId,
                                        revenueByProduct.getOrDefault(productId, BigDecimal.ZERO).add(itemRevenue));
                            }
                        });
                    }
                });

        return unitsSoldByProduct.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(entry -> {
                    String productId = entry.getKey();
                    Product product = productRepository.findById(UUID.fromString(productId)).orElse(null);

                    if (product == null)
                        return null;

                    return AnalyticsDTO.TopProduct.builder()
                            .productId(productId)
                            .productName(product.getName())
                            .category(product.getCategory())
                            .brand(product.getBrand())
                            .unitsSold(entry.getValue())
                            .revenue(revenueByProduct.getOrDefault(productId, BigDecimal.ZERO))
                            .imageUrl(product.getImageUrl())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnalyticsDTO.RevenueByPeriod> getRevenueByPeriod(LocalDate startDate, LocalDate endDate,
            String period) {
        // Implementation for weekly/monthly grouping
        return getSalesChartData(startDate, endDate, period).stream()
                .map(data -> AnalyticsDTO.RevenueByPeriod.builder()
                        .period(period)
                        .startDate(data.getDate())
                        .endDate(data.getDate())
                        .totalRevenue(data.getRevenue())
                        .totalOrders(data.getOrders())
                        .averageOrderValue(data.getOrders() > 0
                                ? data.getRevenue().divide(BigDecimal.valueOf(data.getOrders()), 2,
                                        RoundingMode.HALF_UP)
                                : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public AnalyticsDTO.CustomerAnalytics getCustomerAnalytics() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfWeek = today.minusWeeks(1).atStartOfDay();
        LocalDateTime startOfMonth = today.minusMonths(1).atStartOfDay();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<User> allUsers = userRepository.findAll();
        long totalCustomers = allUsers.size();

        long newCustomersToday = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(startOfToday))
                .count();

        long newCustomersThisWeek = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(startOfWeek))
                .count();

        long newCustomersThisMonth = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(startOfMonth))
                .count();

        // Active customers (ordered in last 30 days)
        Set<UUID> activeCustomerIds = orderRepository.findByCreatedAtAfter(thirtyDaysAgo).stream()
                .map(order -> order.getUser().getId())
                .collect(Collectors.toSet());
        long activeCustomers = activeCustomerIds.size();

        // Returning customers (customers with more than 1 order)
        Map<UUID, Long> orderCountByCustomer = orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(order -> order.getUser().getId(), Collectors.counting()));
        long returningCustomers = orderCountByCustomer.values().stream()
                .filter(count -> count > 1)
                .count();

        // Retention rate
        Double retentionRate = totalCustomers > 0
                ? (returningCustomers * 100.0) / totalCustomers
                : 0.0;

        // Customer lifetime value (simplified)
        BigDecimal totalRevenue = orderRepository.findAll().stream()
                .filter(order -> !"CANCELLED".equals(order.getStatus()))
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal customerLifetimeValue = totalCustomers > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalCustomers), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return AnalyticsDTO.CustomerAnalytics.builder()
                .totalCustomers(totalCustomers)
                .newCustomersToday(newCustomersToday)
                .newCustomersThisWeek(newCustomersThisWeek)
                .newCustomersThisMonth(newCustomersThisMonth)
                .activeCustomers(activeCustomers)
                .returningCustomers(returningCustomers)
                .retentionRate(retentionRate)
                .customerLifetimeValue(customerLifetimeValue)
                .build();
    }

    @Override
    public List<AnalyticsDTO.OrderStatusDistribution> getOrderStatusDistribution() {
        List<Order> allOrders = orderRepository.findAll();
        long totalOrders = allOrders.size();

        Map<String, Long> statusCounts = allOrders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

        return statusCounts.entrySet().stream()
                .map(entry -> {
                    Double percentage = totalOrders > 0
                            ? (entry.getValue() * 100.0) / totalOrders
                            : 0.0;

                    return AnalyticsDTO.OrderStatusDistribution.builder()
                            .status(entry.getKey())
                            .count(entry.getValue())
                            .percentage(percentage)
                            .build();
                })
                .sorted((a, b) -> b.getCount().compareTo(a.getCount()))
                .collect(Collectors.toList());
    }

    @Override
    public List<AnalyticsDTO.RecentActivity> getRecentActivities(int limit) {
        List<AnalyticsDTO.RecentActivity> activities = new ArrayList<>();

        // Recent orders
        orderRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(limit / 2)
                .forEach(order -> activities.add(AnalyticsDTO.RecentActivity.builder()
                        .type("ORDER")
                        .description(String.format("New order #%s - %s",
                                order.getId().toString().substring(0, 8),
                                order.getStatus()))
                        .timestamp(order.getCreatedAt())
                        .userId(order.getUser().getId().toString())
                        .userName(order.getUser().getUsername())
                        .metadata(Map.of("orderId", order.getId().toString(),
                                "total", order.getTotalPrice().toString()))
                        .build()));

        // Recent users
        userRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(limit / 4)
                .forEach(user -> activities.add(AnalyticsDTO.RecentActivity.builder()
                        .type("USER")
                        .description(String.format("New user registered: %s", user.getUsername()))
                        .timestamp(user.getCreatedAt())
                        .userId(user.getId().toString())
                        .userName(user.getUsername())
                        .metadata(Map.of("email", user.getEmail()))
                        .build()));

        return activities.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnalyticsDTO.ProductInventoryAlert> getInventoryAlerts() {
        return productRepository.findAll().stream()
                .filter(p -> p.getStock() != null && p.getStock() < LOW_STOCK_THRESHOLD)
                .map(product -> {
                    String severity;
                    if (product.getStock() == 0) {
                        severity = "CRITICAL";
                    } else if (product.getStock() < 5) {
                        severity = "WARNING";
                    } else {
                        severity = "INFO";
                    }

                    return AnalyticsDTO.ProductInventoryAlert.builder()
                            .productId(product.getId().toString())
                            .productName(product.getName())
                            .category(product.getCategory())
                            .currentStock(product.getStock())
                            .minimumStock(LOW_STOCK_THRESHOLD)
                            .severity(severity)
                            .lastUpdated(product.getUpdatedAt())
                            .build();
                })
                .sorted((a, b) -> {
                    // Sort by severity: CRITICAL > WARNING > INFO
                    int severityCompare = getSeverityValue(b.getSeverity()) - getSeverityValue(a.getSeverity());
                    if (severityCompare != 0)
                        return severityCompare;
                    return a.getCurrentStock().compareTo(b.getCurrentStock());
                })
                .collect(Collectors.toList());
    }

    private Double calculateGrowth(Long current, Long previous) {
        if (previous == null || previous == 0)
            return 0.0;
        return ((current - previous) * 100.0) / previous;
    }

    private Double calculateGrowth(Double current, Double previous) {
        if (previous == null || previous == 0.0)
            return 0.0;
        return ((current - previous) * 100.0) / previous;
    }

    private int getSeverityValue(String severity) {
        switch (severity) {
            case "CRITICAL":
                return 3;
            case "WARNING":
                return 2;
            case "INFO":
                return 1;
            default:
                return 0;
        }
    }
}
