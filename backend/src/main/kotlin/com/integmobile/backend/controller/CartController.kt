package com.integmobile.backend.controller

import com.integmobile.backend.model.request.AddToCartRequest
import com.integmobile.backend.model.request.UpdateCartItemRequest
import com.integmobile.backend.model.response.ApiResponse
import com.integmobile.backend.model.response.CartItemResponse
import com.integmobile.backend.model.response.CartSummaryResponse
import com.integmobile.backend.service.CartService
import com.integmobile.backend.util.ResponseUtil
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/cart")
class CartController(
    private val cartService: CartService
) {
    
    @GetMapping
    fun getCart(authentication: Authentication): ResponseEntity<ApiResponse<CartSummaryResponse>> {
        return try {
            val userId = authentication.principal as String
            val cart = cartService.getCart(userId)
            ResponseEntity.ok(ResponseUtil.success(cart))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Failed to fetch cart"))
        }
    }
    
    @PostMapping
    fun addToCart(
        authentication: Authentication,
        @RequestBody request: AddToCartRequest
    ): ResponseEntity<ApiResponse<CartItemResponse>> {
        return try {
            val userId = authentication.principal as String
            val cartItem = cartService.addToCart(userId, request)
            ResponseEntity.ok(ResponseUtil.success(cartItem, "Item added to cart"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Failed to add to cart"))
        }
    }
    
    @PutMapping("/{id}")
    fun updateCartItem(
        authentication: Authentication,
        @PathVariable id: String,
        @RequestBody request: UpdateCartItemRequest
    ): ResponseEntity<ApiResponse<CartItemResponse>> {
        return try {
            val userId = authentication.principal as String
            val cartItem = cartService.updateCartItem(userId, id, request)
            ResponseEntity.ok(ResponseUtil.success(cartItem, "Cart item updated"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Failed to update cart item"))
        }
    }
    
    @DeleteMapping("/{id}")
    fun removeFromCart(
        authentication: Authentication,
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<Unit>> {
        return try {
            val userId = authentication.principal as String
            cartService.removeFromCart(userId, id)
            ResponseEntity.ok(ResponseUtil.success(Unit, "Item removed from cart"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Failed to remove item"))
        }
    }
    
    @DeleteMapping
    fun clearCart(authentication: Authentication): ResponseEntity<ApiResponse<Unit>> {
        return try {
            val userId = authentication.principal as String
            cartService.clearCart(userId)
            ResponseEntity.ok(ResponseUtil.success(Unit, "Cart cleared"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Failed to clear cart"))
        }
    }
}
