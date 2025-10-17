package com.example.Backend.controller;

import com.example.Backend.entity.Cart;
import com.example.Backend.security.UserPrincipal;
import com.example.Backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Shopping Cart management
 * Handles cart operations for authenticated users
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class CartController {

    private final CartService cartService;

    /**
     * Get current user's cart
     * GET /api/cart
     * Security: CLIENT role required
     */
    @GetMapping
    public ResponseEntity<Cart> getCart(@AuthenticationPrincipal UserPrincipal currentUser) {
        Cart cart = cartService.getOrCreateCart(currentUser.getId());
        return ResponseEntity.ok(cart);
    }

    /**
     * Add item to cart
     * POST /api/cart/items
     * Body: { "productId": "uuid", "quantity": 2 }
     * Security: CLIENT role required
     */
    @PostMapping("/items")
    public ResponseEntity<Cart> addItemToCart(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody AddToCartRequest request) {
        Cart cart = cartService.addItemToCart(
                currentUser.getId(),
                request.getProductId(),
                request.getQuantity());
        return ResponseEntity.ok(cart);
    }

    /**
     * Update cart item quantity
     * PUT /api/cart/items/{productId}
     * Body: { "quantity": 3 }
     * Security: CLIENT role required
     */
    @PutMapping("/items/{productId}")
    public ResponseEntity<Cart> updateCartItem(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID productId,
            @RequestBody UpdateQuantityRequest request) {
        Cart cart = cartService.updateCartItemQuantity(
                currentUser.getId(),
                productId,
                request.getQuantity());
        return ResponseEntity.ok(cart);
    }

    /**
     * Remove item from cart
     * DELETE /api/cart/items/{productId}
     * Security: CLIENT role required
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Cart> removeItemFromCart(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID productId) {
        Cart cart = cartService.removeItemFromCart(currentUser.getId(), productId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Clear entire cart
     * DELETE /api/cart
     * Security: CLIENT role required
     */
    @DeleteMapping
    public ResponseEntity<Cart> clearCart(@AuthenticationPrincipal UserPrincipal currentUser) {
        Cart cart = cartService.clearCart(currentUser.getId());
        return ResponseEntity.ok(cart);
    }

    /**
     * Get cart total price
     * GET /api/cart/total
     * Security: CLIENT role required
     */
    @GetMapping("/total")
    public ResponseEntity<Map<String, Object>> getCartTotal(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        BigDecimal totalPrice = cartService.getCartTotalPrice(currentUser.getId());
        Integer totalItems = cartService.getCartTotalItems(currentUser.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("totalPrice", totalPrice);
        response.put("totalItems", totalItems);

        return ResponseEntity.ok(response);
    }

    /**
     * Get cart items count
     * GET /api/cart/count
     * Security: CLIENT role required
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getCartItemsCount(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Integer count = cartService.getCartTotalItems(currentUser.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Validate cart (check stock availability)
     * GET /api/cart/validate
     * Security: CLIENT role required
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateCart(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isValid = cartService.validateCart(currentUser.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("isValid", isValid);

        if (!isValid) {
            response.put("message", "Some items in your cart are out of stock or unavailable");
        } else {
            response.put("message", "Your cart is valid and ready for checkout");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Merge guest cart with user cart (after login)
     * POST /api/cart/merge/{guestCartId}
     * Security: CLIENT role required
     */
    @PostMapping("/merge/{guestCartId}")
    public ResponseEntity<Cart> mergeGuestCart(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID guestCartId) {
        Cart cart = cartService.mergeGuestCart(currentUser.getId(), guestCartId);
        return ResponseEntity.ok(cart);
    }

    // ========== Request DTOs ==========

    /**
     * Request body for adding item to cart
     */
    @lombok.Data
    public static class AddToCartRequest {
        private UUID productId;
        private Integer quantity;
    }

    /**
     * Request body for updating item quantity
     */
    @lombok.Data
    public static class UpdateQuantityRequest {
        private Integer quantity;
    }
}
