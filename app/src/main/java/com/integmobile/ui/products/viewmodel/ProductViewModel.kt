package com.integmobile.ui.products.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.integmobile.data.db.entity.Product
import com.integmobile.data.model.request.FilterProductsRequest
import com.integmobile.data.repository.ProductRepository
import com.integmobile.utils.Constants
import com.integmobile.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for product operations
 * Manages product listing, search, filtering, and sorting
 */
class ProductViewModel(
    private val repository: ProductRepository
) : ViewModel() {
    
    // Products state
    private val _products = MutableLiveData<Result<List<Product>>>()
    val products: LiveData<Result<List<Product>>> = _products
    
    // Product detail state
    private val _productDetail = MutableLiveData<Result<Product>>()
    val productDetail: LiveData<Result<Product>> = _productDetail
    
    // Filter states
    private val _selectedBrands = MutableStateFlow<Set<String>>(emptySet())
    val selectedBrands: StateFlow<Set<String>> = _selectedBrands
    
    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories: StateFlow<Set<String>> = _selectedCategories
    
    private val _priceRange = MutableStateFlow<Pair<Double, Double>>(0.0 to Double.MAX_VALUE)
    val priceRange: StateFlow<Pair<Double, Double>> = _priceRange
    
    private val _inStockOnly = MutableStateFlow(false)
    val inStockOnly: StateFlow<Boolean> = _inStockOnly
    
    private val _sortBy = MutableStateFlow(Constants.SortBy.NEWEST)
    val sortBy: StateFlow<String> = _sortBy
    
    // Available brands and categories
    private val _availableBrands = MutableLiveData<List<String>>()
    val availableBrands: LiveData<List<String>> = _availableBrands
    
    private val _availableCategories = MutableLiveData<List<String>>()
    val availableCategories: LiveData<List<String>> = _availableCategories
    
    /**
     * Load all products
     */
    fun loadProducts() {
        viewModelScope.launch {
            _products.value = Result.Loading
            val result = repository.getProducts()
            _products.value = result
        }
    }
    
    /**
     * Load product by ID
     */
    fun loadProductById(productId: String) {
        viewModelScope.launch {
            _productDetail.value = Result.Loading
            val result = repository.getProductById(productId)
            _productDetail.value = result
        }
    }
    
    /**
     * Search products
     */
    fun searchProducts(query: String) {
        viewModelScope.launch {
            _products.value = Result.Loading
            val result = repository.searchProducts(query)
            _products.value = result
        }
    }
    
    /**
     * Apply filters and get filtered products
     */
    fun applyFilters() {
        viewModelScope.launch {
            _products.value = Result.Loading
            
            val filterRequest = FilterProductsRequest(
                brands = if (_selectedBrands.value.isNotEmpty()) _selectedBrands.value.toList() else null,
                categories = if (_selectedCategories.value.isNotEmpty()) _selectedCategories.value.toList() else null,
                minPrice = _priceRange.value.first,
                maxPrice = _priceRange.value.second,
                inStockOnly = _inStockOnly.value,
                sortBy = _sortBy.value
            )
            
            val result = repository.filterProducts(filterRequest)
            _products.value = result
        }
    }
    
    /**
     * Update selected brands
     */
    fun updateSelectedBrands(brands: Set<String>) {
        _selectedBrands.value = brands
    }
    
    /**
     * Update selected categories
     */
    fun updateSelectedCategories(categories: Set<String>) {
        _selectedCategories.value = categories
    }
    
    /**
     * Update price range
     */
    fun updatePriceRange(min: Double, max: Double) {
        _priceRange.value = min to max
    }
    
    /**
     * Toggle in stock only filter
     */
    fun toggleInStockOnly() {
        _inStockOnly.value = !_inStockOnly.value
    }
    
    /**
     * Update sort option
     */
    fun updateSortBy(sortOption: String) {
        _sortBy.value = sortOption
    }
    
    /**
     * Clear all filters
     */
    fun clearFilters() {
        _selectedBrands.value = emptySet()
        _selectedCategories.value = emptySet()
        _priceRange.value = 0.0 to Double.MAX_VALUE
        _inStockOnly.value = false
        _sortBy.value = Constants.SortBy.NEWEST
    }
    
    /**
     * Load available brands
     */
    fun loadBrands() {
        viewModelScope.launch {
            val brands = repository.getAllBrands()
            _availableBrands.value = brands
        }
    }
    
    /**
     * Load available categories
     */
    fun loadCategories() {
        viewModelScope.launch {
            val categories = repository.getAllCategories()
            _availableCategories.value = categories
        }
    }
}
