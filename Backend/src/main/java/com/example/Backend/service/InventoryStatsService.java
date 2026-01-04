package com.example.Backend.service;

import com.example.Backend.repository.ProductRepository;
import com.example.Backend.repository.SupplierRepository;
import com.example.Backend.repository.PurchaseOrderRepository;
import com.example.Backend.repository.StockMovementRepository;
import com.example.Backend.entity.PurchaseOrder;
import com.example.Backend.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for calculating comprehensive inventory statistics
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InventoryStatsService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockMovementRepository stockMovementRepository;

    /**
     * Get comprehensive inventory statistics for dashboard
     */
    public Map<String, Object> getFullStats() {
        Map<String, Object> stats = new HashMap<>();

        // Product counts
        long totalProducts = productRepository.count();
        stats.put("totalProducts", totalProducts);

        // Stock value calculation (with null safety)
        List<Product> allProducts = productRepository.findAll();
        BigDecimal totalValue = allProducts.stream()
                .filter(p -> p.getPrice() != null && p.getStock() != null)
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalValue", totalValue);

        // Stock status counts
        long outOfStockItems = allProducts.stream()
                .filter(p -> p.getStock() == null || p.getStock() <= 0)
                .count();
        stats.put("outOfStockItems", outOfStockItems);

        long lowStockItems = allProducts.stream()
                .filter(p -> p.getStock() != null && p.getStock() > 0 && p.getStock() <= 10)
                .count();
        stats.put("lowStockItems", lowStockItems);

        long healthyStockItems = allProducts.stream()
                .filter(p -> p.getStock() != null && p.getStock() > 10)
                .count();
        stats.put("healthyStockItems", healthyStockItems);

        // Supplier counts
        long totalSuppliers = supplierRepository.count();
        long activeSuppliers = supplierRepository.countByIsActiveTrue();
        stats.put("totalSuppliers", totalSuppliers);
        stats.put("activeSuppliers", activeSuppliers);

        // Purchase Order counts
        long pendingPOs = purchaseOrderRepository.countByStatus(PurchaseOrder.POStatus.PENDING);
        long draftPOs = purchaseOrderRepository.countByStatus(PurchaseOrder.POStatus.DRAFT);
        long approvedPOs = purchaseOrderRepository.countByStatus(PurchaseOrder.POStatus.APPROVED);
        long receivedPOs = purchaseOrderRepository.countByStatus(PurchaseOrder.POStatus.RECEIVED);
        stats.put("pendingPOs", pendingPOs);
        stats.put("draftPOs", draftPOs);
        stats.put("approvedPOs", approvedPOs);
        stats.put("receivedPOs", receivedPOs);

        // Stock movement counts (last 30 days could be added later)
        long totalMovements = stockMovementRepository.count();
        stats.put("totalMovements", totalMovements);

        log.info("Inventory stats calculated: {} products, {} value, {} out of stock",
                totalProducts, totalValue, outOfStockItems);

        return stats;
    }

    /**
     * Get product stock overview with status indicators
     */
    public List<Map<String, Object>> getProductStockOverview() {
        return productRepository.findAll().stream()
                .map(p -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", p.getId());
                    item.put("name", p.getName());
                    item.put("sku", p.getId().toString().substring(0, 8).toUpperCase());

                    int stock = p.getStock() != null ? p.getStock() : 0;
                    BigDecimal price = p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO;

                    item.put("currentStock", stock);
                    item.put("price", price);
                    item.put("value", price.multiply(BigDecimal.valueOf(stock)));
                    item.put("brand", p.getBrand() != null ? p.getBrand().getName() : null);
                    item.put("category", p.getCategory() != null ? p.getCategory().getName() : null);

                    // Status calculation (using local stock variable)
                    String status;
                    if (stock <= 0) {
                        status = "OUT_OF_STOCK";
                    } else if (stock <= 5) {
                        status = "CRITICAL";
                    } else if (stock <= 10) {
                        status = "LOW";
                    } else {
                        status = "OK";
                    }
                    item.put("status", status);

                    return item;
                })
                .toList();
    }
}
