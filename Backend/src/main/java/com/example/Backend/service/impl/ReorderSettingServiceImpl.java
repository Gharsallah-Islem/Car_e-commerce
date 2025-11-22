package com.example.Backend.service.impl;

import com.example.Backend.dto.ReorderSettingDTO;
import com.example.Backend.entity.Product;
import com.example.Backend.entity.ReorderSetting;
import com.example.Backend.entity.Supplier;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.repository.ProductRepository;
import com.example.Backend.repository.ReorderSettingRepository;
import com.example.Backend.repository.SupplierRepository;
import com.example.Backend.service.ReorderSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReorderSettingServiceImpl implements ReorderSettingService {

    private final ReorderSettingRepository reorderSettingRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    @Override
    public ReorderSetting createReorderSetting(ReorderSettingDTO dto) {
        log.info("Creating reorder setting for product: {}", dto.getProductId());
        
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Check if setting already exists
        Optional<ReorderSetting> existing = reorderSettingRepository.findByProductId(dto.getProductId());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Reorder setting already exists for this product");
        }
        
        ReorderSetting setting = new ReorderSetting();
        setting.setProduct(product);
        setting.setReorderPoint(dto.getReorderPoint());
        setting.setReorderQuantity(dto.getReorderQuantity());
        setting.setAutoReorder(dto.getAutoReorder() != null ? dto.getAutoReorder() : false);
        setting.setIsEnabled(true);
        
        if (dto.getPreferredSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(dto.getPreferredSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
            setting.setPreferredSupplier(supplier);
        }
        
        return reorderSettingRepository.save(setting);
    }

    @Override
    public ReorderSetting updateReorderSetting(UUID id, ReorderSettingDTO dto) {
        log.info("Updating reorder setting: {}", id);
        
        ReorderSetting setting = getById(id);
        
        setting.setReorderPoint(dto.getReorderPoint());
        setting.setReorderQuantity(dto.getReorderQuantity());
        if (dto.getAutoReorder() != null) {
            setting.setAutoReorder(dto.getAutoReorder());
        }
        
        if (dto.getPreferredSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(dto.getPreferredSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
            setting.setPreferredSupplier(supplier);
        }
        
        setting.setUpdatedAt(LocalDateTime.now());
        
        return reorderSettingRepository.save(setting);
    }

    @Override
    @Transactional(readOnly = true)
    public ReorderSetting getById(UUID id) {
        return reorderSettingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reorder setting not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReorderSetting> getByProductId(UUID productId) {
        return reorderSettingRepository.findByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReorderSetting> getAllReorderSettings(Pageable pageable) {
        return reorderSettingRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReorderSetting> getProductsBelowReorderPoint() {
        return reorderSettingRepository.findProductsNeedingReorder();
    }

    @Override
    public void deleteReorderSetting(UUID id) {
        log.info("Deleting reorder setting: {}", id);
        
        ReorderSetting setting = getById(id);
        reorderSettingRepository.delete(setting);
    }

    @Override
    public void checkAndTriggerAutoReorders() {
        log.info("Checking for products that need auto-reorder");
        
        List<ReorderSetting> belowReorderPoint = getProductsBelowReorderPoint();
        
        for (ReorderSetting setting : belowReorderPoint) {
            if (setting.getAutoReorder() && setting.getPreferredSupplier() != null) {
                log.info("Auto-reorder triggered for product: {} from supplier: {}", 
                        setting.getProduct().getName(), 
                        setting.getPreferredSupplier().getName());
                
                // TODO: Create purchase order automatically
                // This would integrate with PurchaseOrderService
            }
        }
    }
}
