package com.integmobile.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.integmobile.data.db.converters.StringListConverter
import com.integmobile.data.db.converters.StringMapConverter

/**
 * Room entity for product caching
 * Stores product catalog data for offline access
 */
@Entity(tableName = "products")
@TypeConverters(StringListConverter::class, StringMapConverter::class)
data class Product(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val originalPrice: Double? = null,
    val discount: Int? = null,
    val brand: String,
    val category: String,
    val quantityAvailable: Int,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val imageUrl: List<String>,
    val specifications: Map<String, String>,
    val compatibility: List<String>,
    val inStock: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
