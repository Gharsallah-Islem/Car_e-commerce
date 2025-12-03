package com.integmobile.backend.service

import com.integmobile.backend.model.entity.CartItem
import com.integmobile.backend.model.request.AddToCartRequest
import com.integmobile.backend.model.request.UpdateCartItemRequest
import com.integmobile.backend.model.response.CartItemResponse
import com.integmobile.backend.model.response.CartSummaryResponse
import com.integmobile.backend.repository.CartItemRepository
import com.integmobile.backend.repository.ProductRepository
import com.integmobile.backend.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CartService(
    private val cartItemRepository: CartItemRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository
) {
    
    fun getCart(userId: String): CartSummaryResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { Exception("User not found") }
        
        val cartItems = cartItemRepository.findByUser(user)
        val cartItemResponses = cartItems.map { it.toCartItemResponse() }
        
        val subtotal = cartItemResponses.sumOf { it.price * it.quantity }
        val tax = subtotal * 0.19 // 19% tax
        val shipping = if (subtotal > 0) 500.0 else 0.0 // Fixed shipping
        val total = subtotal + tax + shipping
        
        return CartSummaryResponse(
            items = cartItemResponses,
            subtotal = subtotal,
            tax = tax,
            shipping = shipping,
            total = total
        )
    }
    
    fun addToCart(userId: String, request: AddToCartRequest): CartItemResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { Exception("User not found") }
        
        val product = productRepository.findById(request.productId)
            .orElseThrow { Exception("Product not found") }
        
        // Check if item already exists in cart
        val existingItem = cartItemRepository.findByUserAndProductId(user, product.id)
        
        val cartItem = if (existingItem != null) {
            existingItem.quantity += request.quantity
            existingItem.updatedAt = LocalDateTime.now()
            cartItemRepository.save(existingItem)
        } else {
            val newItem = CartItem(
                user = user,
                product = product,
                quantity = request.quantity
            )
            cartItemRepository.save(newItem)
        }
        
        return cartItem.toCartItemResponse()
    }
    
    fun updateCartItem(userId: String, cartItemId: String, request: UpdateCartItemRequest): CartItemResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { Exception("User not found") }
        
        val cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow { Exception("Cart item not found") }
        
        if (cartItem.user.id != user.id) {
            throw Exception("Unauthorized")
        }
        
        cartItem.quantity = request.quantity
        cartItem.updatedAt = LocalDateTime.now()
        cartItemRepository.save(cartItem)
        
        return cartItem.toCartItemResponse()
    }
    
    fun removeFromCart(userId: String, cartItemId: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { Exception("User not found") }
        
        val cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow { Exception("Cart item not found") }
        
        if (cartItem.user.id != user.id) {
            throw Exception("Unauthorized")
        }
        
        cartItemRepository.delete(cartItem)
    }
    
    fun clearCart(userId: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { Exception("User not found") }
        
        cartItemRepository.deleteByUser(user)
    }
    
    private fun CartItem.toCartItemResponse() = CartItemResponse(
        id = id,
        productId = product.id,
        productName = product.name,
        price = product.price,
        quantity = quantity,
        imageUrl = product.imageUrls.firstOrNull() ?: ""
    )
}
