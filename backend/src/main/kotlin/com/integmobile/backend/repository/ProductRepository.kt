package com.integmobile.backend.repository

import com.integmobile.backend.model.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, String> {
    
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun searchProducts(@Param("query") query: String): List<Product>
    
    fun findByBrandIn(brands: List<String>): List<Product>
    
    fun findByCategory(category: String): List<Product>
    
    fun findByInStock(inStock: Boolean): List<Product>
    
    @Query("SELECT p FROM Product p WHERE " +
           "(:brands IS NULL OR p.brand IN :brands) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:inStockOnly = false OR p.inStock = true)")
    fun filterProducts(
        @Param("brands") brands: List<String>?,
        @Param("minPrice") minPrice: Double?,
        @Param("maxPrice") maxPrice: Double?,
        @Param("inStockOnly") inStockOnly: Boolean
    ): List<Product>
}
