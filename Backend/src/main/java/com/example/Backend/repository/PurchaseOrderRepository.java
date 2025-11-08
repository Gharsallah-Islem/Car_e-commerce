package com.example.Backend.repository;

import com.example.Backend.entity.PurchaseOrder;
import com.example.Backend.entity.PurchaseOrder.POStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Purchase Order Repository
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {

    /**
     * Find purchase order by PO number
     */
    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    /**
     * Find purchase orders by status
     */
    List<PurchaseOrder> findByStatus(POStatus status);

    /**
     * Find purchase orders by supplier
     */
    List<PurchaseOrder> findBySupplierId(UUID supplierId);

    /**
     * Find purchase orders by supplier and status
     */
    List<PurchaseOrder> findBySupplierIdAndStatus(UUID supplierId, POStatus status);

    /**
     * Find purchase orders with pagination
     */
    Page<PurchaseOrder> findByStatus(POStatus status, Pageable pageable);

    /**
     * Find purchase orders by date range
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE po.orderDate BETWEEN :startDate AND :endDate")
    List<PurchaseOrder> findByOrderDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find overdue purchase orders (expected delivery date passed)
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE " +
            "po.expectedDeliveryDate < :currentDate AND " +
            "po.status IN ('ORDERED', 'SHIPPED') AND " +
            "po.actualDeliveryDate IS NULL")
    List<PurchaseOrder> findOverduePurchaseOrders(@Param("currentDate") LocalDate currentDate);

    /**
     * Calculate total purchase amount by date range
     */
    @Query("SELECT SUM(po.grandTotal) FROM PurchaseOrder po WHERE " +
            "po.orderDate BETWEEN :startDate AND :endDate AND " +
            "po.status NOT IN ('DRAFT', 'CANCELLED')")
    BigDecimal calculateTotalPurchaseAmount(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Count purchase orders by status
     */
    Long countByStatus(POStatus status);

    /**
     * Find recent purchase orders
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE po.createdAt >= :since ORDER BY po.createdAt DESC")
    List<PurchaseOrder> findRecentPurchaseOrders(@Param("since") LocalDateTime since);

    /**
     * Find purchase orders awaiting approval
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE po.status = 'PENDING' ORDER BY po.createdAt ASC")
    List<PurchaseOrder> findPendingApproval();

    /**
     * Search purchase orders
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE " +
            "LOWER(po.poNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(po.supplier.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<PurchaseOrder> searchPurchaseOrders(@Param("searchTerm") String searchTerm);
}
