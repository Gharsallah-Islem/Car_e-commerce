package com.example.carpartsecom.data.repository

import androidx.lifecycle.LiveData
import com.example.carpartsecom.data.local.dao.ProductDao
import com.example.carpartsecom.data.local.entities.ProductEntity
import com.example.carpartsecom.data.remote.api.ProductService
import com.example.carpartsecom.data.remote.dto.toEntity
import com.example.carpartsecom.util.NetworkErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductRepository(
    private val productService: ProductService,
    private val productDao: ProductDao
) {
    fun getAllProducts(): LiveData<List<ProductEntity>> {
        refreshProducts()
        return productDao.getAllProducts()
    }
    
    fun getProductById(id: Long): LiveData<ProductEntity?> = productDao.getProductById(id)
    
    fun sortProducts(sortBy: String): LiveData<List<ProductEntity>> {
        refreshProductsBySort(sortBy)
        return when(sortBy) {
            "price_asc" -> productDao.getProductsSortedByPriceAsc()
            "price_desc" -> productDao.getProductsSortedByPriceDesc()
            "rating" -> productDao.getProductsSortedByRating()
            "name" -> productDao.getProductsSortedByName()
            else -> productDao.getAllProducts()
        }
    }
    
    fun searchProducts(query: String): LiveData<List<ProductEntity>> {
        refreshProductsBySearch(query)
        return productDao.searchProducts("%$query%")
    }
    
    fun getProductsByCategory(category: String): LiveData<List<ProductEntity>> {
        refreshProductsByCategory(category)
        return productDao.getProductsByCategory(category)
    }
    
    private fun refreshProducts() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = productService.getAllProducts()
                if (response.isSuccessful && response.body() != null) {
                    val products = response.body()!!.map { it.toEntity() }
                    productDao.insertAll(products)
                }
            } catch (e: Exception) {
                // Silent fail - UI will show cached data
            }
        }
    }
    
    private fun refreshProductsBySort(sortBy: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = productService.sortProducts(sortBy)
                if (response.isSuccessful && response.body() != null) {
                    val products = response.body()!!.map { it.toEntity() }
                    productDao.insertAll(products)
                }
            } catch (e: Exception) {
                // Silent fail - UI will show cached data
            }
        }
    }
    
    private fun refreshProductsBySearch(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = productService.searchProducts(query)
                if (response.isSuccessful && response.body() != null) {
                    val products = response.body()!!.map { it.toEntity() }
                    productDao.insertAll(products)
                }
            } catch (e: Exception) {
                // Silent fail - UI will show cached data
            }
        }
    }
    
    private fun refreshProductsByCategory(category: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = productService.getProductsByCategory(category)
                if (response.isSuccessful && response.body() != null) {
                    val products = response.body()!!.map { it.toEntity() }
                    productDao.insertAll(products)
                }
            } catch (e: Exception) {
                // Silent fail - UI will show cached data
            }
        }
    }
}
