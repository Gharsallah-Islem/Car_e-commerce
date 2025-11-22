package com.example.Backend.service.impl;

import com.example.Backend.dto.PurchaseOrderDTO;
import com.example.Backend.entity.Product;
import com.example.Backend.entity.PurchaseOrder;
import com.example.Backend.entity.PurchaseOrderItem;
import com.example.Backend.entity.Supplier;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.repository.ProductRepository;
import com.example.Backend.repository.PurchaseOrderRepository;
import com.example.Backend.repository.SupplierRepository;
import com.example.Backend.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    @Override
    public PurchaseOrder createPurchaseOrder(PurchaseOrderDTO dto) {
        log.info("Creating new purchase order for supplier: {}", dto.getSupplierId());

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(generateOrderNumber());
        po.setSupplier(supplier);
        po.setOrderDate(dto.getOrderDate() != null ? dto.getOrderDate() : LocalDate.now());
        po.setExpectedDeliveryDate(dto.getExpectedDelivery());
        po.setStatus(dto.getStatus() != null ? PurchaseOrder.POStatus.valueOf(dto.getStatus())
                : PurchaseOrder.POStatus.DRAFT);
        po.setNotes(dto.getNotes());

        // Add items if provided
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (PurchaseOrderDTO.PurchaseOrderItemDTO itemDTO : dto.getItems()) {
                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setPurchaseOrder(po);
                item.setProduct(product);
                item.setQuantity(itemDTO.getQuantity());
                item.setUnitPrice(BigDecimal.valueOf(itemDTO.getUnitPrice()));

                po.getItems().add(item);
            }

            po.calculateTotals();
        }

        return purchaseOrderRepository.save(po);
    }

    @Override
    public PurchaseOrder updatePurchaseOrder(UUID id, PurchaseOrderDTO dto) {
        log.info("Updating purchase order: {}", id);

        PurchaseOrder po = getById(id);

        if (dto.getSupplierId() != null && !dto.getSupplierId().equals(po.getSupplier().getId())) {
            Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
            po.setSupplier(supplier);
        }

        if (dto.getOrderDate() != null)
            po.setOrderDate(dto.getOrderDate());
        if (dto.getExpectedDelivery() != null)
            po.setExpectedDeliveryDate(dto.getExpectedDelivery());
        if (dto.getNotes() != null)
            po.setNotes(dto.getNotes());
        if (dto.getStatus() != null)
            po.setStatus(PurchaseOrder.POStatus.valueOf(dto.getStatus()));

        return purchaseOrderRepository.save(po);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrder getById(UUID id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseOrder> getAllPurchaseOrders(Pageable pageable) {
        return purchaseOrderRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseOrder> getPurchaseOrdersByStatus(String status, Pageable pageable) {
        return purchaseOrderRepository.findByStatus(PurchaseOrder.POStatus.valueOf(status), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseOrder> getPurchaseOrdersBySupplier(UUID supplierId, Pageable pageable) {
        List<PurchaseOrder> pos = purchaseOrderRepository.findBySupplierId(supplierId);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), pos.size());
        return new org.springframework.data.domain.PageImpl<>(
                pos.subList(start, end),
                pageable,
                pos.size());
    }

    @Override
    public PurchaseOrder updateStatus(UUID id, String status) {
        log.info("Updating purchase order {} status to: {}", id, status);

        PurchaseOrder po = getById(id);
        po.setStatus(PurchaseOrder.POStatus.valueOf(status));

        return purchaseOrderRepository.save(po);
    }

    @Override
    public void deletePurchaseOrder(UUID id) {
        log.info("Deleting purchase order: {}", id);

        PurchaseOrder po = getById(id);
        purchaseOrderRepository.delete(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPurchaseOrderStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalPOs = purchaseOrderRepository.count();
        long pendingPOs = purchaseOrderRepository.countByStatus(PurchaseOrder.POStatus.PENDING);
        long approvedPOs = purchaseOrderRepository.countByStatus(PurchaseOrder.POStatus.APPROVED);
        long receivedPOs = purchaseOrderRepository.countByStatus(PurchaseOrder.POStatus.RECEIVED);

        stats.put("totalPurchaseOrders", totalPOs);
        stats.put("pendingPurchaseOrders", pendingPOs);
        stats.put("approvedPurchaseOrders", approvedPOs);
        stats.put("receivedPurchaseOrders", receivedPOs);

        return stats;
    }

    private String generateOrderNumber() {
        return "PO-" + LocalDate.now().getYear() + "-" +
                String.format("%06d", purchaseOrderRepository.count() + 1);
    }
}
