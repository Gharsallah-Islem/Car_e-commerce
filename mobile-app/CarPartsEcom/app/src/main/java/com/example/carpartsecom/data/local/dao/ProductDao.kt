package com.example.carpartsecom.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.carpartsecom.data.local.entities.ProductEntity

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)
    
    @Query("SELECT * FROM products")
    fun getAllProducts(): LiveData<List<ProductEntity>>
    
    @Query("SELECT * FROM products ORDER BY price ASC")
    fun getProductsSortedByPriceAsc(): LiveData<List<ProductEntity>>
    
    @Query("SELECT * FROM products ORDER BY price DESC")
    fun getProductsSortedByPriceDesc(): LiveData<List<ProductEntity>>
    
    @Query("SELECT * FROM products ORDER BY rating DESC")
    fun getProductsSortedByRating(): LiveData<List<ProductEntity>>
    
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getProductsSortedByName(): LiveData<List<ProductEntity>>
    
    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Long): LiveData<ProductEntity?>
    
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductByIdSync(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE name LIKE :query OR description LIKE :query OR category LIKE :query")
    fun searchProducts(query: String): LiveData<List<ProductEntity>>
    
    @Query("SELECT * FROM products WHERE name LIKE :query OR description LIKE :query OR category LIKE :query")
    suspend fun searchProductsSync(query: String): List<ProductEntity>

    @Query("SELECT * FROM products WHERE category = :category")
    fun getProductsByCategory(category: String): LiveData<List<ProductEntity>>
}
