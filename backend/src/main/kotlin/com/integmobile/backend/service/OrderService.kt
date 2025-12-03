package com.integmobile.backend.service

import com.integmobile.backend.model.entity.Order
import com.integmobile.backend.model.request.CancelOrderRequest
import com.integmobile.backend.model.request.CreateOrderRequest
import com.integmobile.backend.model.request.SubmitClaimRequest
import com.integmobile.backend.repository.CartItemRepository
import com.integmobile.backend.repository.OrderRepository
import com.integmobile.backend.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val cartItemRepository: CartItemRepository
) {
    
    fun getOrders(userId: String): List<Order> {
        val user = userRepository.findById(userId)
            .orElseThrow { Exception("User not found") }
        
        return orderRepository.findByUserOrderByCreatedAtDesc(user)
    }
    
    fun getOrderById(userId: String, orderId: String): Order {
        val user = userRepository.findById(userId)
            .orElseThrow { Exception("User not found") }
        
        val order = orderRepository.findById(orderId)
            .orElseThrow { Exception("Order not found") }
        
        if (order.user.id != user.id) {
            throw Exception("Unauthorized")
        }
        
        return order
    }
    
    fun createOrder(userId: String, request: CreateOrderRequest): Order {
        val user = userRepository.findById(userId)
            .orElseThrow { Exception("User not found") }
        
        // Calculate totals
        val subtotal = request.items.sumOf { it.price * it.quantity }
        val tax = subtotal * 0.19
        val shipping = 500.0
        val total = subtotal + tax + shipping
        
        val order = Order(
            user = user,
            items = request.items,
            totalAmount = total,
            subtotal = subtotal,
            tax = tax,
            shipping = shipping,
            status = "PENDING",
            deliveryAddress = request.deliveryAddress,
            latitude = request.latitude,
            longitude = request.longitude,
            phoneNumber = request.phoneNumber,
            paymentMethod = request.paymentMethod
        )
        
        val savedOrder = orderRepository.save(order)
        
        // Clear cart after order
        cartItemRepository.deleteByUser(user)
        
        return savedOrder
    }
    
    fun cancelOrder(userId: String, orderId: String, request: CancelOrderRequest): Order {
        val user = userRepository.findById(userId)
            .orElseThrow { Exception("User not found") }
        
        val order = orderRepository.findById(orderId)
            .orElseThrow { Exception("Order not found") }
        
        if (order.user.id != user.id) {
            throw Exception("Unauthorized")
        }
        
        if (order.status !in listOf("PENDING", "CONFIRMED")) {
            throw Exception("Cannot cancel order with status: ${order.status}")
        }
        
        order.status = "CANCELLED"
        order.updatedAt = LocalDateTime.now()
        
        return orderRepository.save(order)
    }
    
    fun submitClaim(userId: String, request: SubmitClaimRequest): Map<String, String> {
        val user = userRepository.findById(userId)
            .orElseThrow { Exception("User not found") }
        
        val order = orderRepository.findById(request.orderId)
            .orElseThrow { Exception("Order not found") }
        
        if (order.user.id != user.id) {
            throw Exception("Unauthorized")
        }
        
        if (order.status != "DELIVERED") {
            throw Exception("Can only submit claims for delivered orders")
        }
        
        // TODO: Save claim to database
        // For now, just return success
        
        return mapOf(
            "message" to "Claim submitted successfully",
            "claimId" to "CLAIM-${System.currentTimeMillis()}"
        )
    }
}
