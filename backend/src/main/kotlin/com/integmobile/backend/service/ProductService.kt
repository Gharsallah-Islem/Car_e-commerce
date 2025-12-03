package com.integmobile.backend.service

import com.integmobile.backend.model.entity.Product
import com.integmobile.backend.model.request.ProductFilterRequest
import com.integmobile.backend.repository.ProductRepository
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository
) {
    
    fun getAllProducts(): List<Product> {
        return productRepository.findAll()
    }
    
    fun getProductById(id: String): Product {
        return productRepository.findById(id)
            .orElseThrow { Exception("Product not found") }
    }
    
    fun searchProducts(query: String): List<Product> {
        return productRepository.searchProducts(query)
    }
    
    fun filterProducts(request: ProductFilterRequest): List<Product> {
        return productRepository.filterProducts(
            brands = request.brands,
            minPrice = request.minPrice,
            maxPrice = request.maxPrice,
            inStockOnly = request.inStockOnly
        )
    }
}
