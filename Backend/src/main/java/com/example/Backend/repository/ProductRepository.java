package com.example.Backend.repository;

import com.example.Backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

        /**
         * Find products by name (case-insensitive, partial match)
         */
        @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
        List<Product> findByNameContaining(@Param("name") String name);

        // Removed - incompatible with new entity structure
        // List<Product> findByCategory(String category);
        // List<Product> findByBrand(String brand);
        // List<Product> findByBrandAndModel(String brand, String model);

        /**
         * Find products compatible with specific vehicle - DEPRECATED
         * Use filterProducts instead
         */
        @Query("SELECT p FROM Product p WHERE " +
                        "p.brand.name = :brand AND p.model = :model AND p.year = :year")
        List<Product> findCompatibleProducts(@Param("brand") String brand,
                        @Param("model") String model,
                        @Param("year") Integer year);

        /**
         * Advanced search with multiple filters - DEPRECATED
         * Use filterProducts instead
         */
        @Query("SELECT p FROM Product p WHERE " +
                        "(:searchTerm IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
                        "(:category IS NULL OR p.category.name = :category) AND " +
                        "(:brand IS NULL OR p.brand.name = :brand) AND " +
                        "(:model IS NULL OR p.model = :model) AND " +
                        "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
                        "(:maxPrice IS NULL OR p.price <= :maxPrice)")
        Page<Product> searchProducts(@Param("searchTerm") String searchTerm,
                        @Param("category") String category,
                        @Param("brand") String brand,
                        @Param("model") String model,
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice,
                        Pageable pageable);

        /**
         * Find products in stock
         */
        @Query("SELECT p FROM Product p WHERE p.stock > 0")
        List<Product> findInStock();

        /**
         * Find low stock products (stock <= threshold)
         */
        @Query("SELECT p FROM Product p WHERE p.stock > 0 AND p.stock <= :threshold")
        List<Product> findLowStock(@Param("threshold") Integer threshold);

        /**
         * Find out of stock products
         */
        @Query("SELECT p FROM Product p WHERE p.stock = 0")
        List<Product> findOutOfStock();

        /**
         * Find products by price range
         */
        List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

        /**
         * Get top selling products (based on order items count)
         */
        @Query("SELECT p FROM Product p LEFT JOIN p.orderItems oi " +
                        "GROUP BY p ORDER BY COUNT(oi) DESC")
        Page<Product> findTopSellingProducts(Pageable pageable);

        /**
         * Get featured/recommended products (in stock, sorted by creation date)
         */
        @Query("SELECT p FROM Product p WHERE p.stock > 0 ORDER BY p.createdAt DESC")
        Page<Product> findFeaturedProducts(Pageable pageable);

        /**
         * Get distinct categories - returns category names
         */
        @Query("SELECT DISTINCT p.category.name FROM Product p WHERE p.category IS NOT NULL ORDER BY p.category.name")
        List<String> findDistinctCategories();

        /**
         * Get distinct brands - returns brand names
         */
        @Query("SELECT DISTINCT p.brand.name FROM Product p WHERE p.brand IS NOT NULL ORDER BY p.brand.name")
        List<String> findDistinctBrands();

        /**
         * Get distinct models for a brand
         */
        @Query("SELECT DISTINCT p.model FROM Product p WHERE p.brand.name = :brand AND p.model IS NOT NULL ORDER BY p.model")
        List<String> findDistinctModelsByBrand(@Param("brand") String brand);

        /**
         * Count products by category - DEPRECATED, use countByCategoryId
         */
        @Query("SELECT COUNT(p) FROM Product p WHERE p.category.name = :category")
        Long countByCategory(@Param("category") String category);

        /**
         * Count products in stock
         */
        @Query("SELECT COUNT(p) FROM Product p WHERE p.stock > 0")
        Long countInStock();

        /**
         * Count products by brand ID
         */
        @Query("SELECT COUNT(p) FROM Product p WHERE p.brand.id = :brandId")
        long countByBrandId(@Param("brandId") Long brandId);

        /**
         * Count products by category ID
         */
        @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
        long countByCategoryId(@Param("categoryId") Long categoryId);
}
