package com.example.carpartsecom.util

import com.google.gson.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Gson deserializer that handles API responses that may return either:
 * - A single object: {"id": 1, "name": "Product"}
 * - An array of objects: [{"id": 1, "name": "Product"}, {"id": 2, "name": "Product2"}]
 *
 * This normalizes both to always return a List<T>
 */
class SingleOrListDeserializer<T> : JsonDeserializer<List<T>> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<T> {
        val result = mutableListOf<T>()

        // Get the actual type parameter (e.g., ProductResponse from List<ProductResponse>)
        val itemType = (typeOfT as ParameterizedType).actualTypeArguments[0]

        when {
            json.isJsonArray -> {
                // It's an array - deserialize each element
                json.asJsonArray.forEach { element ->
                    val item: T = context.deserialize(element, itemType)
                    result.add(item)
                }
            }
            json.isJsonObject -> {
                // It's a single object - wrap it in a list
                val item: T = context.deserialize(json, itemType)
                result.add(item)
            }
            json.isJsonNull -> {
                // Return empty list for null
            }
        }

        return result
    }
}

