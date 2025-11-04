package com.example.Backend.service;

import com.example.Backend.entity.Cart;

import java.math.BigDecimal;
import java.util.UUID;

public interface CartService {

    /**
     * Get or create cart for user
     * 
     * @param userId User ID
     * @return User's cart
     */
    Cart getOrCreateCart(UUID userId);

    /**
     * Get cart by ID
     * 
     * @param cartId Cart ID
     * @return Cart entity
     */
    Cart getCartById(UUID cartId);

    /**
     * Add item to cart
     * 
     * @param userId    User ID
     * @param productId Product ID
     * @param quantity  Quantity to add
     * @return Updated cart
     */
    Cart addItemToCart(UUID userId, UUID productId, Integer quantity);

    /**
     * Update cart item quantity
     * 
     * @param userId    User ID
     * @param productId Product ID
     * @param quantity  New quantity
     * @return Updated cart
     */
    Cart updateCartItemQuantity(UUID userId, UUID productId, Integer quantity);

    /**
     * Remove item from cart
     * 
     * @param userId    User ID
     * @param productId Product ID
     * @return Updated cart
     */
    Cart removeItemFromCart(UUID userId, UUID productId);

    /**
     * Clear all items from cart
     * 
     * @param userId User ID
     * @return Empty cart
     */
    Cart clearCart(UUID userId);

    /**
     * Get cart total price
     * 
     * @param userId User ID
     * @return Total price
     */
    BigDecimal getCartTotalPrice(UUID userId);

    /**
     * Get cart total items count
     * 
     * @param userId User ID
     * @return Total items
     */
    Integer getCartTotalItems(UUID userId);

    /**
     * Validate cart (check stock availability)
     * 
     * @param userId User ID
     * @return true if all items are available
     */
    boolean validateCart(UUID userId);

    /**
     * Merge guest cart with user cart
     * 
     * @param userId      User ID
     * @param guestCartId Guest cart ID
     * @return Merged cart
     */
    Cart mergeGuestCart(UUID userId, UUID guestCartId);
}
