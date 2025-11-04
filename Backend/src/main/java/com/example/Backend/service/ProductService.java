package com.example.Backend.service;

import com.example.Backend.dto.ProductDTO;
import com.example.Backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductService {

    /**
     * Create a new product
     * 
     * @param productDTO Product data
     * @return Created product
     */
    Product createProduct(ProductDTO productDTO);

    /**
     * Get product by ID
     * 
     * @param id Product ID
     * @return Product entity
     */
    Product getProductById(UUID id);

    /**
     * Update product
     * 
     * @param id         Product ID
     * @param productDTO Updated product data
     * @return Updated product
     */
    Product updateProduct(UUID id, ProductDTO productDTO);

    /**
     * Delete product
     * 
     * @param id Product ID
     */
    void deleteProduct(UUID id);

    /**
     * Get all products
     * 
     * @return List of all products
     */
    List<Product> getAllProducts();

    /**
     * Get products with pagination
     * 
     * @param pageable Pagination parameters
     * @return Page of products
     */
    Page<Product> getAllProducts(Pageable pageable);

    /**
     * Search products with multiple filters
     * 
     * @param searchTerm Search term (name/description)
     * @param category   Category filter
     * @param brand      Brand filter
     * @param model      Model filter
     * @param minPrice   Minimum price
     * @param maxPrice   Maximum price
     * @param pageable   Pagination parameters
     * @return Page of matching products
     */
    Page<Product> searchProducts(String searchTerm, String category, String brand,
            String model, BigDecimal minPrice, BigDecimal maxPrice,
            Pageable pageable);

    /**
     * Filter products by category, brand, price range, stock, and search term
     * 
     * @param categoryId Category ID
     * @param brandId    Brand ID
     * @param minPrice   Minimum price
     * @param maxPrice   Maximum price
     * @param search     Search term
     * @param inStock    Filter by stock availability
     * @param pageable   Pagination parameters
     * @return Page of filtered products
     */
    Page<Product> filterProducts(Long categoryId, Long brandId, BigDecimal minPrice,
            BigDecimal maxPrice, String search, Boolean inStock, Pageable pageable);

    /**
     * Get products by category
     * 
     * @param category Category name
     * @return List of products
     */
    List<Product> getProductsByCategory(String category);

    /**
     * Get products by brand
     * 
     * @param brand Brand name
     * @return List of products
     */
    List<Product> getProductsByBrand(String brand);

    /**
     * Get products compatible with vehicle
     * 
     * @param brand Vehicle brand
     * @param model Vehicle model
     * @param year  Vehicle year
     * @return List of compatible products
     */
    List<Product> getCompatibleProducts(String brand, String model, Integer year);

    /**
     * Get products in stock
     * 
     * @return List of products with stock > 0
     */
    List<Product> getProductsInStock();

    /**
     * Get low stock products
     * 
     * @param threshold Stock threshold
     * @return List of low stock products
     */
    List<Product> getLowStockProducts(Integer threshold);

    /**
     * Get out of stock products
     * 
     * @return List of out of stock products
     */
    List<Product> getOutOfStockProducts();

    /**
     * Get top selling products
     * 
     * @param pageable Pagination parameters
     * @return Page of top selling products
     */
    Page<Product> getTopSellingProducts(Pageable pageable);

    /**
     * Get featured products
     * 
     * @param pageable Pagination parameters
     * @return Page of featured products
     */
    Page<Product> getFeaturedProducts(Pageable pageable);

    /**
     * Get all categories
     * 
     * @return List of distinct categories
     */
    List<String> getAllCategories();

    /**
     * Get all brands
     * 
     * @return List of distinct brands
     */
    List<String> getAllBrands();

    /**
     * Get models for brand
     * 
     * @param brand Brand name
     * @return List of models
     */
    List<String> getModelsByBrand(String brand);

    /**
     * Update product stock
     * 
     * @param id       Product ID
     * @param quantity Quantity to add (positive) or subtract (negative)
     * @return Updated product
     */
    Product updateStock(UUID id, Integer quantity);

    /**
     * Count products in stock
     * 
     * @return Number of products in stock
     */
    Long countProductsInStock();
}
