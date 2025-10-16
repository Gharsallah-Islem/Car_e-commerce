package com.example.Backend.repository;

import com.example.Backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    /**
     * Find all items in a cart
     */
    List<CartItem> findByCartId(UUID cartId);

    /**
     * Find cart item by cart and product
     */
    Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);

    /**
     * Check if product exists in cart
     */
    boolean existsByCartIdAndProductId(UUID cartId, UUID productId);

    /**
     * Delete all items from a cart
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") UUID cartId);

    /**
     * Delete specific product from cart
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
    void deleteByCartIdAndProductId(@Param("cartId") UUID cartId, @Param("productId") UUID productId);

    /**
     * Count items in cart
     */
    Long countByCartId(UUID cartId);
}
