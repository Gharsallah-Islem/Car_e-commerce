package com.example.Backend.repository;

import com.example.Backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    /**
     * Find all items in an order
     */
    List<OrderItem> findByOrderId(UUID orderId);

    /**
     * Find order items by product
     */
    List<OrderItem> findByProductId(UUID productId);

    /**
     * Get total quantity sold for a product
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
    Long getTotalQuantitySoldForProduct(@Param("productId") UUID productId);

    /**
     * Get top selling products (by quantity)
     */
    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as totalSold " +
            "FROM OrderItem oi GROUP BY oi.product.id, oi.product.name " +
            "ORDER BY totalSold DESC")
    List<Object[]> getTopSellingProducts();

    /**
     * Count items in order
     */
    Long countByOrderId(UUID orderId);
}
