package com.integmobile.data.db.dao

import androidx.room.*
import com.integmobile.data.db.entity.Product
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Product entity operations
 * Handles product caching with search and filter capabilities
 */
@Dao
interface ProductDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)
    
    @Update
    suspend fun update(product: Product)
    
    @Delete
    suspend fun delete(product: Product)
    
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: String): Product?
    
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE brand = :brand")
    fun getProductsByBrand(brand: String): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE category = :category")
    fun getProductsByCategory(category: String): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE price BETWEEN :minPrice AND :maxPrice")
    fun getProductsByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE inStock = 1")
    fun getInStockProducts(): Flow<List<Product>>
    
    @Query("SELECT DISTINCT brand FROM products ORDER BY brand ASC")
    suspend fun getAllBrands(): List<String>
    
    @Query("SELECT DISTINCT category FROM products ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>
    
    @Query("DELETE FROM products")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
}
