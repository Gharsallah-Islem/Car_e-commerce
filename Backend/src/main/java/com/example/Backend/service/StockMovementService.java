package com.example.Backend.service;

import com.example.Backend.dto.StockMovementDTO;
import com.example.Backend.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface StockMovementService {
    StockMovement recordMovement(StockMovementDTO movementDTO);
    StockMovement getById(UUID id);
    Page<StockMovement> getAllMovements(Pageable pageable);
    Page<StockMovement> getMovementsByProduct(UUID productId, Pageable pageable);
    Page<StockMovement> getMovementsByType(String type, Pageable pageable);
    List<StockMovement> getRecentMovements(int limit);
}
