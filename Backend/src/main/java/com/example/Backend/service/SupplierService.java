package com.example.Backend.service;

import com.example.Backend.dto.SupplierDTO;
import com.example.Backend.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SupplierService {
    Supplier createSupplier(SupplierDTO supplierDTO);
    Supplier updateSupplier(UUID id, SupplierDTO supplierDTO);
    Supplier getSupplierById(UUID id);
    Page<Supplier> getAllSuppliers(Pageable pageable);
    Page<Supplier> searchSuppliers(String keyword, Pageable pageable);
    List<Supplier> getActiveSuppliers();
    void deleteSupplier(UUID id);
    Map<String, Object> getSupplierStatistics();
}
