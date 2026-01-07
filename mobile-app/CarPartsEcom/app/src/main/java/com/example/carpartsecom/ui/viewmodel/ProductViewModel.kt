package com.example.carpartsecom.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.carpartsecom.data.local.entities.ProductEntity
import com.example.carpartsecom.data.repository.ProductRepository

class ProductViewModel(private val productRepository: ProductRepository) : ViewModel() {
    
    private val _filterParams = MutableLiveData<FilterParams>(FilterParams())
    
    val products: LiveData<List<ProductEntity>> = _filterParams.switchMap { params ->
        when {
            params.searchQuery.isNotEmpty() -> productRepository.searchProducts(params.searchQuery)
            params.category.isNotEmpty() -> productRepository.getProductsByCategory(params.category)
            params.sortType != "all" -> productRepository.sortProducts(params.sortType)
            else -> productRepository.getAllProducts()
        }
    }
    
    fun setSortType(sortType: String) {
        _filterParams.value = _filterParams.value?.copy(sortType = sortType) ?: FilterParams(sortType = sortType)
    }
    
    fun searchProducts(query: String) {
        _filterParams.value = FilterParams(searchQuery = query)
    }
    
    fun filterByCategory(category: String) {
        _filterParams.value = FilterParams(category = category)
    }
    
    fun clearFilters() {
        _filterParams.value = FilterParams()
    }
    
    fun getProductById(id: Long): LiveData<ProductEntity?> = productRepository.getProductById(id)
    
    data class FilterParams(
        val sortType: String = "all",
        val searchQuery: String = "",
        val category: String = ""
    )
}
