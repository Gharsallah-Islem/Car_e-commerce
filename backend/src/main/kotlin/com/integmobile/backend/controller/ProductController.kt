package com.integmobile.backend.controller

import com.integmobile.backend.model.entity.Product
import com.integmobile.backend.model.request.ProductFilterRequest
import com.integmobile.backend.model.response.ApiResponse
import com.integmobile.backend.service.ProductService
import com.integmobile.backend.util.ResponseUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService
) {
    
    @GetMapping
    fun getAllProducts(): ResponseEntity<ApiResponse<List<Product>>> {
        return try {
            val products = productService.getAllProducts()
            ResponseEntity.ok(ResponseUtil.success(products))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Failed to fetch products"))
        }
    }
    
    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: String): ResponseEntity<ApiResponse<Product>> {
        return try {
            val product = productService.getProductById(id)
            ResponseEntity.ok(ResponseUtil.success(product))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Product not found"))
        }
    }
    
    @GetMapping("/search")
    fun searchProducts(@RequestParam query: String): ResponseEntity<ApiResponse<List<Product>>> {
        return try {
            val products = productService.searchProducts(query)
            ResponseEntity.ok(ResponseUtil.success(products))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Search failed"))
        }
    }
    
    @PostMapping("/filter")
    fun filterProducts(@RequestBody request: ProductFilterRequest): ResponseEntity<ApiResponse<List<Product>>> {
        return try {
            val products = productService.filterProducts(request)
            ResponseEntity.ok(ResponseUtil.success(products))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Filter failed"))
        }
    }
}
