package com.integmobile.data.repository

import com.integmobile.data.api.ProductApiService
import com.integmobile.data.db.dao.ProductDao
import com.integmobile.data.db.entity.Product
import com.integmobile.data.model.request.FilterProductsRequest
import com.integmobile.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Repository for product operations
 * Implements cache-first strategy: API -> Room cache -> fallback to cache
 * Single source of truth for product data
 */
class ProductRepository(
    private val apiService: ProductApiService,
    private val productDao: ProductDao
) {
    
    /**
     * Get all products (cache-first strategy)
     */
    suspend fun getProducts(): Result<List<Product>> {
        return try {
            // Try to fetch from API
            val response = apiService.getAllProducts()
            
            if (response.isSuccessful && response.body()?.success == true) {
                val products = response.body()?.data ?: emptyList()
                // Cache in Room
                if (products.isNotEmpty()) {
                    productDao.insertAll(products)
                }
                Result.Success(products)
            } else {
                // Fallback to cached data
                val cachedProducts = productDao.getAllProducts().first()
                if (cachedProducts.isNotEmpty()) {
                    Result.Success(cachedProducts)
                } else {
                    Result.Error(Exception("No products available"))
                }
            }
        } catch (e: Exception) {
            // Network error - try cache
            try {
                val cachedProducts = productDao.getAllProducts().first()
                if (cachedProducts.isNotEmpty()) {
                    Result.Success(cachedProducts)
                } else {
                    Result.Error(e, "Network error and no cached data")
                }
            } catch (cacheError: Exception) {
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }
    
    /**
     * Get product by ID
     */
    suspend fun getProductById(productId: String): Result<Product> {
        return try {
            // Try API first
            val response = apiService.getProductById(productId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val product = response.body()?.data
                if (product != null) {
                    productDao.insert(product)
                    Result.Success(product)
                } else {
                    // Try cache
                    val cachedProduct = productDao.getProductById(productId)
                    if (cachedProduct != null) {
                        Result.Success(cachedProduct)
                    } else {
                        Result.Error(Exception("Product not found"))
                    }
                }
            } else {
                // Fallback to cache
                val cachedProduct = productDao.getProductById(productId)
                if (cachedProduct != null) {
                    Result.Success(cachedProduct)
                } else {
                    Result.Error(Exception("Product not found"))
                }
            }
        } catch (e: Exception) {
            // Try cache on network error
            val cachedProduct = productDao.getProductById(productId)
            if (cachedProduct != null) {
                Result.Success(cachedProduct)
            } else {
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }
    
    /**
     * Search products
     */
    suspend fun searchProducts(query: String): Result<List<Product>> {
        return try {
            val response = apiService.searchProducts(query)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val products = response.body()?.data ?: emptyList()
                Result.Success(products)
            } else {
                // Fallback to local search
                val cachedResults = productDao.searchProducts(query).first()
                Result.Success(cachedResults)
            }
        } catch (e: Exception) {
            // Network error - search in cache
            try {
                val cachedResults = productDao.searchProducts(query).first()
                Result.Success(cachedResults)
            } catch (cacheError: Exception) {
                Result.Error(e, "Search failed: ${e.message}")
            }
        }
    }
    
    /**
     * Filter products
     */
    suspend fun filterProducts(filterRequest: FilterProductsRequest): Result<List<Product>> {
        return try {
            val response = apiService.filterProducts(filterRequest)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val products = response.body()?.data ?: emptyList()
                Result.Success(products)
            } else {
                Result.Error(Exception("Filter failed"))
            }
        } catch (e: Exception) {
            Result.Error(e, "Network error: ${e.message}")
        }
    }
    
    /**
     * Get all products as Flow for reactive updates
     */
    fun getProductsFlow(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }
    
    /**
     * Get all brands
     */
    suspend fun getAllBrands(): List<String> {
        return productDao.getAllBrands()
    }
    
    /**
     * Get all categories
     */
    suspend fun getAllCategories(): List<String> {
        return productDao.getAllCategories()
    }
}
