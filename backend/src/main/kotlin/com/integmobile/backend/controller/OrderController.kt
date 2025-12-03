package com.integmobile.backend.controller

import com.integmobile.backend.model.entity.Order
import com.integmobile.backend.model.request.CancelOrderRequest
import com.integmobile.backend.model.request.CreateOrderRequest
import com.integmobile.backend.model.request.SubmitClaimRequest
import com.integmobile.backend.model.response.ApiResponse
import com.integmobile.backend.service.OrderService
import com.integmobile.backend.util.ResponseUtil
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService
) {
    
    @GetMapping
    fun getOrders(authentication: Authentication): ResponseEntity<ApiResponse<List<Order>>> {
        return try {
            val userId = authentication.principal as String
            val orders = orderService.getOrders(userId)
            ResponseEntity.ok(ResponseUtil.success(orders))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Failed to fetch orders"))
        }
    }
    
    @GetMapping("/{id}")
    fun getOrderById(
        authentication: Authentication,
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<Order>> {
        return try {
            val userId = authentication.principal as String
            val order = orderService.getOrderById(userId, id)
            ResponseEntity.ok(ResponseUtil.success(order))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Order not found"))
        }
    }
    
    @PostMapping
    fun createOrder(
        authentication: Authentication,
        @RequestBody request: CreateOrderRequest
    ): ResponseEntity<ApiResponse<Order>> {
        return try {
            val userId = authentication.principal as String
            val order = orderService.createOrder(userId, request)
            ResponseEntity.ok(ResponseUtil.success(order, "Order created successfully"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Failed to create order"))
        }
    }
    
    @PutMapping("/{id}/cancel")
    fun cancelOrder(
        authentication: Authentication,
        @PathVariable id: String,
        @RequestBody request: CancelOrderRequest
    ): ResponseEntity<ApiResponse<Order>> {
        return try {
            val userId = authentication.principal as String
            val order = orderService.cancelOrder(userId, id, request)
            ResponseEntity.ok(ResponseUtil.success(order, "Order cancelled successfully"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Failed to cancel order"))
        }
    }
    
    @PostMapping("/claim")
    fun submitClaim(
        authentication: Authentication,
        @RequestBody request: SubmitClaimRequest
    ): ResponseEntity<ApiResponse<Map<String, String>>> {
        return try {
            val userId = authentication.principal as String
            val result = orderService.submitClaim(userId, request)
            ResponseEntity.ok(ResponseUtil.success(result))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Failed to submit claim"))
        }
    }
}
