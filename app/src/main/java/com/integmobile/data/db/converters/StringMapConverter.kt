package com.integmobile.data.db.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Type converter for Map<String, String> to store in Room database
 */
class StringMapConverter {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? {
        if (value == null) return null
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, mapType)
    }
}
