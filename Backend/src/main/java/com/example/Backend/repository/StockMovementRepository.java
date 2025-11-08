package com.example.Backend.repository;

import com.example.Backend.entity.StockMovement;
import com.example.Backend.entity.StockMovement.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Stock Movement Repository
 */
@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    /**
     * Find stock movements by product
     */
    List<StockMovement> findByProductId(UUID productId);

    /**
     * Find stock movements by product with pagination
     */
    Page<StockMovement> findByProductId(UUID productId, Pageable pageable);

    /**
     * Find stock movements by movement type
     */
    List<StockMovement> findByMovementType(MovementType movementType);

    /**
     * Find stock movements by date range
     */
    @Query("SELECT sm FROM StockMovement sm WHERE " +
            "sm.movementDate BETWEEN :startDate AND :endDate " +
            "ORDER BY sm.movementDate DESC")
    List<StockMovement> findByMovementDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find stock movements by product and date range
     */
    @Query("SELECT sm FROM StockMovement sm WHERE " +
            "sm.product.id = :productId AND " +
            "sm.movementDate BETWEEN :startDate AND :endDate " +
            "ORDER BY sm.movementDate DESC")
    List<StockMovement> findByProductIdAndDateRange(
            @Param("productId") UUID productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find stock movements by reference (Order, PO, etc.)
     */
    List<StockMovement> findByReferenceIdAndReferenceType(UUID referenceId, String referenceType);

    /**
     * Find recent stock movements
     */
    @Query("SELECT sm FROM StockMovement sm WHERE sm.movementDate >= :since ORDER BY sm.movementDate DESC")
    List<StockMovement> findRecentMovements(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Calculate total quantity moved by product and movement type
     */
    @Query("SELECT SUM(sm.quantity) FROM StockMovement sm WHERE " +
            "sm.product.id = :productId AND " +
            "sm.movementType = :movementType AND " +
            "sm.movementDate BETWEEN :startDate AND :endDate")
    Integer calculateTotalQuantityMoved(
            @Param("productId") UUID productId,
            @Param("movementType") MovementType movementType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get stock movement history for a product (latest first)
     */
    @Query("SELECT sm FROM StockMovement sm WHERE sm.product.id = :productId ORDER BY sm.movementDate DESC")
    List<StockMovement> getProductStockHistory(@Param("productId") UUID productId, Pageable pageable);

    /**
     * Find stock movements performed by user
     */
    List<StockMovement> findByPerformedBy(String username);
}
