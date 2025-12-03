package com.integmobile.data.db.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.integmobile.data.db.entity.OrderItem

/**
 * Type converter for List<OrderItem> to store in Room database
 */
class OrderItemListConverter {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromOrderItemList(value: List<OrderItem>?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toOrderItemList(value: String?): List<OrderItem>? {
        if (value == null) return null
        val listType = object : TypeToken<List<OrderItem>>() {}.type
        return gson.fromJson(value, listType)
    }
}
