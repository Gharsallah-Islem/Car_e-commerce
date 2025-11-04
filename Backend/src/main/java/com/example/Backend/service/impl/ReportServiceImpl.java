package com.example.Backend.service.impl;

import com.example.Backend.entity.Report;
import com.example.Backend.repository.DeliveryRepository;
import com.example.Backend.repository.OrderRepository;
import com.example.Backend.repository.ProductRepository;
import com.example.Backend.repository.ReclamationRepository;
import com.example.Backend.repository.ReportRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.ReportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReclamationRepository reclamationRepository;
    private final DeliveryRepository deliveryRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Report generateSalesReport(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> reportData = new HashMap<>();

        // Calculate total revenue
        BigDecimal totalRevenue = orderRepository.calculateRevenueBetween(startDate, endDate);
        reportData.put("totalRevenue", totalRevenue);

        // Count orders in period
        List<com.example.Backend.entity.Order> ordersInPeriod = orderRepository.findByCreatedAtBetween(startDate,
                endDate);
        reportData.put("orderCount", ordersInPeriod.size());

        // Get top selling products
        List<com.example.Backend.entity.Product> topProducts = productRepository.findTopSellingProducts(
                PageRequest.of(0, 10)).getContent();
        reportData.put("topSellingProducts", topProducts.stream()
                .map(p -> Map.of("name", p.getName(), "price", p.getPrice(), "stock", p.getStock()))
                .toList());

        // Order statistics by status
        reportData.put("ordersByStatus", Map.of(
                "PENDING", orderRepository.countByStatus("PENDING"),
                "CONFIRMED", orderRepository.countByStatus("CONFIRMED"),
                "SHIPPED", orderRepository.countByStatus("SHIPPED"),
                "DELIVERED", orderRepository.countByStatus("DELIVERED"),
                "CANCELLED", orderRepository.countByStatus("CANCELLED")));

        Report report = new Report();
        report.setReportType(Report.TYPE_SALES);
        report.setTitle("Sales Report: " + startDate.toLocalDate() + " to " + endDate.toLocalDate());
        report.setDescription("Comprehensive sales analysis including revenue, orders, and top products");
        report.setData(convertToJson(reportData));
        report.setCreatedAt(LocalDateTime.now());

        return reportRepository.save(report);
    }

    @Override
    @Transactional
    public Report generateProductReport(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> reportData = new HashMap<>();

        // Total products
        reportData.put("totalProducts", productRepository.count());

        // In stock count
        Long inStockCount = productRepository.countInStock();
        reportData.put("inStockProducts", inStockCount);

        // Out of stock
        List<com.example.Backend.entity.Product> outOfStock = productRepository.findOutOfStock();
        reportData.put("outOfStockProducts", outOfStock.size());

        // Low stock products
        List<com.example.Backend.entity.Product> lowStock = productRepository.findLowStock(10);
        List<String> lowStockProducts = lowStock.stream()
                .map(p -> p.getName() + " (Stock: " + p.getStock() + ")")
                .toList();
        reportData.put("lowStockProducts", lowStockProducts);

        // Products by category
        reportData.put("categories", productRepository.findDistinctCategories());

        Report report = new Report();
        report.setReportType(Report.TYPE_INVENTORY);
        report.setTitle("Product Inventory Report");
        report.setDescription("Product inventory status and stock levels");
        report.setData(convertToJson(reportData));
        report.setCreatedAt(LocalDateTime.now());

        return reportRepository.save(report);
    }

    @Override
    @Transactional
    public Report generateUserReport(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> reportData = new HashMap<>();

        // Total users
        reportData.put("totalUsers", userRepository.count());

        // New users in period
        List<com.example.Backend.entity.User> newUsers = userRepository.findByCreatedAtAfter(startDate);
        reportData.put("newUsersInPeriod", newUsers.size());

        // Active users (users with recent orders)
        int activeUsers = userRepository.findActiveUsers(endDate.minusDays(30)).size();
        reportData.put("activeUsers", activeUsers);

        // Users by role
        reportData.put("usersByRole", Map.of(
                "CLIENT", userRepository.countByRoleName("CLIENT"),
                "SUPPORT", userRepository.countByRoleName("SUPPORT"),
                "ADMIN", userRepository.countByRoleName("ADMIN"),
                "SUPER_ADMIN", userRepository.countByRoleName("SUPER_ADMIN")));

        Report report = new Report();
        report.setReportType(Report.TYPE_USERS);
        report.setTitle("User Activity Report");
        report.setDescription("User registration and activity statistics");
        report.setData(convertToJson(reportData));
        report.setCreatedAt(LocalDateTime.now());

        return reportRepository.save(report);
    }

    @Override
    @Transactional
    public Report generateCustomReport(String reportType, LocalDateTime startDate,
            LocalDateTime endDate, Map<String, Object> parameters) {
        Map<String, Object> reportData = new HashMap<>(parameters);
        reportData.put("startDate", startDate.toString());
        reportData.put("endDate", endDate.toString());

        Report report = new Report();
        report.setReportType(reportType);
        report.setTitle("Custom Report: " + reportType);
        report.setDescription("Custom generated report");
        report.setData(convertToJson(reportData));
        report.setCreatedAt(LocalDateTime.now());

        return reportRepository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public Report getReportById(UUID reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found with id: " + reportId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Report> getAllReports(Pageable pageable) {
        return reportRepository.findAllReportsSortedByDate(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Report> getReportsByType(String reportType, Pageable pageable) {
        return reportRepository.findByReportTypeOrderByCreatedAtDesc(reportType, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Report> getReportsByCreator(UUID adminId, Pageable pageable) {
        List<Report> reports = reportRepository.findByGeneratedBy(adminId);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), reports.size());
        return new org.springframework.data.domain.PageImpl<>(
                reports.subList(start, end), pageable, reports.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Report> getRecentReports(int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<Report> reports = reportRepository.findRecentReports(since);
        return reports.stream().limit(limit).toList();
    }

    @Override
    @Transactional
    public void deleteReport(UUID reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found with id: " + reportId));
        reportRepository.delete(report);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Order statistics
        stats.put("totalOrders", orderRepository.count());
        stats.put("pendingOrders", orderRepository.countByStatus("PENDING"));
        stats.put("deliveredOrders", orderRepository.countByStatus("DELIVERED"));
        stats.put("totalRevenue", orderRepository.calculateTotalRevenue());
        
        // Product statistics
        stats.put("totalProducts", productRepository.count());
        stats.put("productsInStock", productRepository.countInStock());
        stats.put("outOfStockProducts", productRepository.findOutOfStock().size());
        stats.put("lowStockProducts", productRepository.findLowStock(10).size());
        
        // User statistics
        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.findActiveUsers(LocalDateTime.now().minusDays(30)).size());
        
        // Reclamation statistics
        stats.put("totalReclamations", reclamationRepository.count());
        stats.put("pendingReclamations", reclamationRepository.countByStatus("OPEN"));
        
        // Delivery statistics
        stats.put("totalDeliveries", deliveryRepository.count());
        stats.put("activeDeliveries", deliveryRepository.findActiveDeliveries().size());
        stats.put("deliveredCount", deliveryRepository.countByStatus("DELIVERED"));
        
        // Recent activity (last 7 days)
        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        stats.put("ordersLastWeek", orderRepository.findRecentOrders(lastWeek).size());
        stats.put("usersLastWeek", userRepository.findByCreatedAtAfter(lastWeek).size());
        
        return stats;
    }    /**
     * Convert map to JSON string
     */
    private String convertToJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
