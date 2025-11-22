package com.example.Backend.service;

import com.example.Backend.dto.PurchaseOrderDTO;
import com.example.Backend.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface PurchaseOrderService {
    PurchaseOrder createPurchaseOrder(PurchaseOrderDTO purchaseOrderDTO);
    PurchaseOrder updatePurchaseOrder(UUID id, PurchaseOrderDTO purchaseOrderDTO);
    PurchaseOrder getById(UUID id);
    Page<PurchaseOrder> getAllPurchaseOrders(Pageable pageable);
    Page<PurchaseOrder> getPurchaseOrdersByStatus(String status, Pageable pageable);
    Page<PurchaseOrder> getPurchaseOrdersBySupplier(UUID supplierId, Pageable pageable);
    PurchaseOrder updateStatus(UUID id, String status);
    void deletePurchaseOrder(UUID id);
    Map<String, Object> getPurchaseOrderStatistics();
}
