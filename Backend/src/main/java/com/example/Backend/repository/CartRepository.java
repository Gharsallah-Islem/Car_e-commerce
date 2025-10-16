package com.example.Backend.repository;

import com.example.Backend.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    /**
     * Find cart by user ID
     */
    Optional<Cart> findByUserId(UUID userId);

    /**
     * Check if user has a cart
     */
    boolean existsByUserId(UUID userId);

    /**
     * Get cart with items for a user (fetch join to avoid N+1)
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") UUID userId);

    /**
     * Count total items in user's cart
     */
    @Query("SELECT SUM(ci.quantity) FROM Cart c JOIN c.cartItems ci WHERE c.user.id = :userId")
    Integer countItemsByUserId(@Param("userId") UUID userId);
}
