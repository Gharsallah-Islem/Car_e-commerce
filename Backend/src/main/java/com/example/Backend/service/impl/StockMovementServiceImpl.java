package com.example.Backend.service.impl;

import com.example.Backend.dto.StockMovementDTO;
import com.example.Backend.entity.Product;
import com.example.Backend.entity.StockMovement;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.repository.ProductRepository;
import com.example.Backend.repository.StockMovementRepository;
import com.example.Backend.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    @Override
    public StockMovement recordMovement(StockMovementDTO dto) {
        log.info("Recording stock movement for product: {}", dto.getProductId());

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Update product stock
        int currentStock = product.getStock();
        int newStock = currentStock;
        StockMovement.MovementType movementType;

        switch (dto.getType().toUpperCase()) {
            case "IN":
                product.increaseStock(dto.getQuantity());
                newStock = product.getStock();
                movementType = StockMovement.MovementType.PURCHASE;
                break;
            case "OUT":
                product.decreaseStock(dto.getQuantity());
                newStock = product.getStock();
                movementType = StockMovement.MovementType.SALE;
                break;
            case "ADJUSTMENT":
                newStock = dto.getQuantity();
                product.setStock(newStock);
                movementType = StockMovement.MovementType.ADJUSTMENT;
                break;
            default:
                throw new IllegalArgumentException("Invalid movement type: " + dto.getType());
        }

        productRepository.save(product);

        // Record movement
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setMovementType(movementType);
        movement.setQuantity(dto.getQuantity());
        movement.setPreviousStock(currentStock);
        movement.setNewStock(newStock);
        movement.setReferenceType(dto.getReference());
        movement.setNotes(dto.getReason());
        movement.setPerformedBy("ADMIN"); // TODO: Get from security context

        return stockMovementRepository.save(movement);
    }

    @Override
    @Transactional(readOnly = true)
    public StockMovement getById(UUID id) {
        return stockMovementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock movement not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockMovement> getAllMovements(Pageable pageable) {
        return stockMovementRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockMovement> getMovementsByProduct(UUID productId, Pageable pageable) {
        return stockMovementRepository.findByProductId(productId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockMovement> getMovementsByType(String type, Pageable pageable) {
        StockMovement.MovementType movementType;
        switch (type.toUpperCase()) {
            case "IN":
            case "PURCHASE":
                movementType = StockMovement.MovementType.PURCHASE;
                break;
            case "OUT":
            case "SALE":
                movementType = StockMovement.MovementType.SALE;
                break;
            case "ADJUSTMENT":
                movementType = StockMovement.MovementType.ADJUSTMENT;
                break;
            default:
                movementType = StockMovement.MovementType.valueOf(type.toUpperCase());
        }
        List<StockMovement> movements = stockMovementRepository.findByMovementType(movementType);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), movements.size());
        return new org.springframework.data.domain.PageImpl<>(
                movements.subList(start, end),
                pageable,
                movements.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovement> getRecentMovements(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "date"));
        return stockMovementRepository.findAll(pageable).getContent();
    }
}
