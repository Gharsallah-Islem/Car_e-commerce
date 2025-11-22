package com.example.Backend.controller;

import com.example.Backend.dto.PurchaseOrderDTO;
import com.example.Backend.dto.ReorderSettingDTO;
import com.example.Backend.dto.StockMovementDTO;
import com.example.Backend.dto.SupplierDTO;
import com.example.Backend.entity.PurchaseOrder;
import com.example.Backend.entity.ReorderSetting;
import com.example.Backend.entity.StockMovement;
import com.example.Backend.entity.Supplier;
import com.example.Backend.service.PurchaseOrderService;
import com.example.Backend.service.ReorderSettingService;
import com.example.Backend.service.StockMovementService;
import com.example.Backend.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class InventoryController {

    private final SupplierService supplierService;
    private final PurchaseOrderService purchaseOrderService;
    private final StockMovementService stockMovementService;
    private final ReorderSettingService reorderSettingService;

    // ==================== SUPPLIER ENDPOINTS ====================
    
    @PostMapping("/suppliers")
    public ResponseEntity<Supplier> createSupplier(@Valid @RequestBody SupplierDTO supplierDTO) {
        Supplier supplier = supplierService.createSupplier(supplierDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(supplier);
    }

    @GetMapping("/suppliers")
    public ResponseEntity<Page<Supplier>> getAllSuppliers(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<Supplier> suppliers = supplierService.getAllSuppliers(pageable);
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/suppliers/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable UUID id) {
        Supplier supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(supplier);
    }

    @GetMapping("/suppliers/search")
    public ResponseEntity<Page<Supplier>> searchSuppliers(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Supplier> suppliers = supplierService.searchSuppliers(keyword, pageable);
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/suppliers/active")
    public ResponseEntity<List<Supplier>> getActiveSuppliers() {
        List<Supplier> suppliers = supplierService.getActiveSuppliers();
        return ResponseEntity.ok(suppliers);
    }

    @PutMapping("/suppliers/{id}")
    public ResponseEntity<Supplier> updateSupplier(
            @PathVariable UUID id,
            @Valid @RequestBody SupplierDTO supplierDTO) {
        Supplier supplier = supplierService.updateSupplier(id, supplierDTO);
        return ResponseEntity.ok(supplier);
    }

    @DeleteMapping("/suppliers/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable UUID id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/suppliers/statistics")
    public ResponseEntity<Map<String, Object>> getSupplierStatistics() {
        Map<String, Object> stats = supplierService.getSupplierStatistics();
        return ResponseEntity.ok(stats);
    }

    // ==================== PURCHASE ORDER ENDPOINTS ====================
    
    @PostMapping("/purchase-orders")
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(
            @Valid @RequestBody PurchaseOrderDTO purchaseOrderDTO) {
        PurchaseOrder po = purchaseOrderService.createPurchaseOrder(purchaseOrderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(po);
    }

    @GetMapping("/purchase-orders")
    public ResponseEntity<Page<PurchaseOrder>> getAllPurchaseOrders(
            @PageableDefault(size = 20, sort = "orderDate") Pageable pageable) {
        Page<PurchaseOrder> pos = purchaseOrderService.getAllPurchaseOrders(pageable);
        return ResponseEntity.ok(pos);
    }

    @GetMapping("/purchase-orders/{id}")
    public ResponseEntity<PurchaseOrder> getPurchaseOrderById(@PathVariable UUID id) {
        PurchaseOrder po = purchaseOrderService.getById(id);
        return ResponseEntity.ok(po);
    }

    @GetMapping("/purchase-orders/status/{status}")
    public ResponseEntity<Page<PurchaseOrder>> getPurchaseOrdersByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PurchaseOrder> pos = purchaseOrderService.getPurchaseOrdersByStatus(status, pageable);
        return ResponseEntity.ok(pos);
    }

    @GetMapping("/purchase-orders/supplier/{supplierId}")
    public ResponseEntity<Page<PurchaseOrder>> getPurchaseOrdersBySupplier(
            @PathVariable UUID supplierId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PurchaseOrder> pos = purchaseOrderService.getPurchaseOrdersBySupplier(supplierId, pageable);
        return ResponseEntity.ok(pos);
    }

    @PutMapping("/purchase-orders/{id}")
    public ResponseEntity<PurchaseOrder> updatePurchaseOrder(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseOrderDTO purchaseOrderDTO) {
        PurchaseOrder po = purchaseOrderService.updatePurchaseOrder(id, purchaseOrderDTO);
        return ResponseEntity.ok(po);
    }

    @PatchMapping("/purchase-orders/{id}/status")
    public ResponseEntity<PurchaseOrder> updatePurchaseOrderStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> statusUpdate) {
        String status = statusUpdate.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        PurchaseOrder po = purchaseOrderService.updateStatus(id, status);
        return ResponseEntity.ok(po);
    }

    @DeleteMapping("/purchase-orders/{id}")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable UUID id) {
        purchaseOrderService.deletePurchaseOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/purchase-orders/statistics")
    public ResponseEntity<Map<String, Object>> getPurchaseOrderStatistics() {
        Map<String, Object> stats = purchaseOrderService.getPurchaseOrderStatistics();
        return ResponseEntity.ok(stats);
    }

    // ==================== STOCK MOVEMENT ENDPOINTS ====================
    
    @PostMapping("/stock-movements")
    public ResponseEntity<StockMovement> recordStockMovement(
            @Valid @RequestBody StockMovementDTO movementDTO) {
        StockMovement movement = stockMovementService.recordMovement(movementDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(movement);
    }

    @GetMapping("/stock-movements")
    public ResponseEntity<Page<StockMovement>> getAllStockMovements(
            @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        Page<StockMovement> movements = stockMovementService.getAllMovements(pageable);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/stock-movements/{id}")
    public ResponseEntity<StockMovement> getStockMovementById(@PathVariable UUID id) {
        StockMovement movement = stockMovementService.getById(id);
        return ResponseEntity.ok(movement);
    }

    @GetMapping("/stock-movements/product/{productId}")
    public ResponseEntity<Page<StockMovement>> getStockMovementsByProduct(
            @PathVariable UUID productId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<StockMovement> movements = stockMovementService.getMovementsByProduct(productId, pageable);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/stock-movements/type/{type}")
    public ResponseEntity<Page<StockMovement>> getStockMovementsByType(
            @PathVariable String type,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<StockMovement> movements = stockMovementService.getMovementsByType(type, pageable);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/stock-movements/recent")
    public ResponseEntity<List<StockMovement>> getRecentStockMovements(
            @RequestParam(defaultValue = "10") int limit) {
        List<StockMovement> movements = stockMovementService.getRecentMovements(limit);
        return ResponseEntity.ok(movements);
    }

    // ==================== REORDER SETTING ENDPOINTS ====================
    
    @PostMapping("/reorder-settings")
    public ResponseEntity<ReorderSetting> createReorderSetting(
            @Valid @RequestBody ReorderSettingDTO reorderSettingDTO) {
        ReorderSetting setting = reorderSettingService.createReorderSetting(reorderSettingDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(setting);
    }

    @GetMapping("/reorder-settings")
    public ResponseEntity<Page<ReorderSetting>> getAllReorderSettings(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ReorderSetting> settings = reorderSettingService.getAllReorderSettings(pageable);
        return ResponseEntity.ok(settings);
    }

    @GetMapping("/reorder-settings/{id}")
    public ResponseEntity<ReorderSetting> getReorderSettingById(@PathVariable UUID id) {
        ReorderSetting setting = reorderSettingService.getById(id);
        return ResponseEntity.ok(setting);
    }

    @GetMapping("/reorder-settings/product/{productId}")
    public ResponseEntity<ReorderSetting> getReorderSettingByProduct(@PathVariable UUID productId) {
        Optional<ReorderSetting> setting = reorderSettingService.getByProductId(productId);
        return setting.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reorder-settings/below-reorder-point")
    public ResponseEntity<List<ReorderSetting>> getProductsBelowReorderPoint() {
        List<ReorderSetting> settings = reorderSettingService.getProductsBelowReorderPoint();
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/reorder-settings/{id}")
    public ResponseEntity<ReorderSetting> updateReorderSetting(
            @PathVariable UUID id,
            @Valid @RequestBody ReorderSettingDTO reorderSettingDTO) {
        ReorderSetting setting = reorderSettingService.updateReorderSetting(id, reorderSettingDTO);
        return ResponseEntity.ok(setting);
    }

    @DeleteMapping("/reorder-settings/{id}")
    public ResponseEntity<Void> deleteReorderSetting(@PathVariable UUID id) {
        reorderSettingService.deleteReorderSetting(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reorder-settings/check-auto-reorders")
    public ResponseEntity<Void> checkAutoReorders() {
        reorderSettingService.checkAndTriggerAutoReorders();
        return ResponseEntity.ok().build();
    }

    // ==================== INVENTORY STATISTICS ====================
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getInventoryStatistics() {
        Map<String, Object> stats = Map.of(
            "suppliers", supplierService.getSupplierStatistics(),
            "purchaseOrders", purchaseOrderService.getPurchaseOrderStatistics()
        );
        return ResponseEntity.ok(stats);
    }
}
