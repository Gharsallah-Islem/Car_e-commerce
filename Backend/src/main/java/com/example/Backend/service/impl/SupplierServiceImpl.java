package com.example.Backend.service.impl;

import com.example.Backend.dto.SupplierDTO;
import com.example.Backend.entity.Supplier;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.repository.SupplierRepository;
import com.example.Backend.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    public Supplier createSupplier(SupplierDTO supplierDTO) {
        log.info("Creating new supplier: {}", supplierDTO.getName());

        Supplier supplier = new Supplier();
        supplier.setName(supplierDTO.getName());
        supplier.setContactPerson(supplierDTO.getContactPerson());
        supplier.setEmail(supplierDTO.getEmail());
        supplier.setPhone(supplierDTO.getPhone());
        supplier.setAddress(supplierDTO.getAddress());
        supplier.setIsActive(true);
        supplier.setRating(supplierDTO.getRating() != null ? supplierDTO.getRating() : 0.0);

        return supplierRepository.save(supplier);
    }

    @Override
    public Supplier updateSupplier(UUID id, SupplierDTO supplierDTO) {
        log.info("Updating supplier: {}", id);

        Supplier supplier = getSupplierById(id);

        supplier.setName(supplierDTO.getName());
        supplier.setContactPerson(supplierDTO.getContactPerson());
        supplier.setEmail(supplierDTO.getEmail());
        supplier.setPhone(supplierDTO.getPhone());
        supplier.setAddress(supplierDTO.getAddress());
        if (supplierDTO.getRating() != null) {
            supplier.setRating(supplierDTO.getRating());
        }

        return supplierRepository.save(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public Supplier getSupplierById(UUID id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Supplier> getAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Supplier> searchSuppliers(String keyword, Pageable pageable) {
        // Use the existing searchSuppliers method and convert to Page
        List<Supplier> suppliers = supplierRepository.searchSuppliers(keyword);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), suppliers.size());
        return new org.springframework.data.domain.PageImpl<>(
                suppliers.subList(start, end),
                pageable,
                suppliers.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> getActiveSuppliers() {
        return supplierRepository.findByIsActiveTrue();
    }

    @Override
    public void deleteSupplier(UUID id) {
        log.info("Deleting supplier: {}", id);

        Supplier supplier = getSupplierById(id);
        supplierRepository.delete(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSupplierStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalSuppliers = supplierRepository.count();
        long activeSuppliers = supplierRepository.countByIsActiveTrue();
        long inactiveSuppliers = totalSuppliers - activeSuppliers;

        stats.put("totalSuppliers", totalSuppliers);
        stats.put("activeSuppliers", activeSuppliers);
        stats.put("inactiveSuppliers", inactiveSuppliers);

        return stats;
    }
}
