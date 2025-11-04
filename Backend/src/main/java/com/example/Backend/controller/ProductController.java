package com.example.Backend.controller;

import com.example.Backend.dto.ProductDTO;
import com.example.Backend.entity.Product;
import com.example.Backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Product management
 * Handles product catalog, search, filtering, and inventory
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Get all products with pagination
     * GET /api/products?page=0&size=20&sort=name,asc
     * Security: Public endpoint
     */
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Advanced product search with multiple filters
     * GET
     * /api/products/search?term=brake&category=parts&brand=Bosch&minPrice=10&maxPrice=500
     * Security: Public endpoint
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam(required = false) String term,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.searchProducts(
                term, category, brand, model, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get new arrival products (recently added)
     * GET /api/products/new-arrivals?size=12
     * Security: Public endpoint
     */
    @GetMapping("/new-arrivals")
    public ResponseEntity<Page<Product>> getNewArrivals(
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by("createdAt").descending());
        Page<Product> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get featured products
     * GET /api/products/featured?size=12
     * Security: Public endpoint
     */
    @GetMapping("/featured")
    public ResponseEntity<Page<Product>> getFeaturedProducts(
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(0, size);
        Page<Product> products = productService.getFeaturedProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get product by ID
     * GET /api/products/{id}
     * Security: Public endpoint
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable UUID id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Get products by category
     * GET /api/products/category/{category}
     * Security: Public endpoint
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        List<Product> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    /**
     * Get products by brand
     * GET /api/products/brand/{brand}
     * Security: Public endpoint
     */
    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<Product>> getProductsByBrand(@PathVariable String brand) {
        List<Product> products = productService.getProductsByBrand(brand);
        return ResponseEntity.ok(products);
    }

    /**
     * Get products compatible with vehicle
     * GET /api/products/compatible?brand=Toyota&model=Corolla&year=2020
     * Security: Public endpoint
     */
    @GetMapping("/compatible")
    public ResponseEntity<List<Product>> getCompatibleProducts(
            @RequestParam String brand,
            @RequestParam String model,
            @RequestParam Integer year) {
        List<Product> products = productService.getCompatibleProducts(brand, model, year);
        return ResponseEntity.ok(products);
    }

    /**
     * Get top selling products
     * GET /api/products/top-selling?size=10
     * Security: Public endpoint
     */
    @GetMapping("/top-selling")
    public ResponseEntity<Page<Product>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(0, size);
        Page<Product> products = productService.getTopSellingProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get products in stock
     * GET /api/products/in-stock
     * Security: Public endpoint
     */
    @GetMapping("/in-stock")
    public ResponseEntity<List<Product>> getProductsInStock() {
        List<Product> products = productService.getProductsInStock();
        return ResponseEntity.ok(products);
    }

    /**
     * Get all categories
     * GET /api/products/filters/categories
     * Security: Public endpoint
     */
    @GetMapping("/filters/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get all brands
     * GET /api/products/filters/brands
     * Security: Public endpoint
     */
    @GetMapping("/filters/brands")
    public ResponseEntity<List<String>> getAllBrands() {
        List<String> brands = productService.getAllBrands();
        return ResponseEntity.ok(brands);
    }

    /**
     * Get models by brand
     * GET /api/products/filters/models?brand=Toyota
     * Security: Public endpoint
     */
    @GetMapping("/filters/models")
    public ResponseEntity<List<String>> getModelsByBrand(@RequestParam String brand) {
        List<String> models = productService.getModelsByBrand(brand);
        return ResponseEntity.ok(models);
    }

    // ========== ADMIN ENDPOINTS ==========

    /**
     * Create new product
     * POST /api/products
     * Security: Admin or Super Admin only
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        Product product = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * Update product
     * PUT /api/products/{id}
     * Security: Admin or Super Admin only
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Product> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductDTO productDTO) {
        Product product = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(product);
    }

    /**
     * Delete product
     * DELETE /api/products/{id}
     * Security: Admin or Super Admin only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
    }

    /**
     * Update product stock
     * PATCH /api/products/{id}/stock?quantity=10
     * Security: Admin or Super Admin only
     */
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Product> updateStock(
            @PathVariable UUID id,
            @RequestParam Integer quantity) {
        Product product = productService.updateStock(id, quantity);
        return ResponseEntity.ok(product);
    }

    /**
     * Get low stock products
     * GET /api/products/inventory/low-stock?threshold=10
     * Security: Admin or Super Admin only
     */
    @GetMapping("/inventory/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<Product>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold) {
        List<Product> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }

    /**
     * Get out of stock products
     * GET /api/products/inventory/out-of-stock
     * Security: Admin or Super Admin only
     */
    @GetMapping("/inventory/out-of-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<Product>> getOutOfStockProducts() {
        List<Product> products = productService.getOutOfStockProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Get inventory statistics
     * GET /api/products/inventory/statistics
     * Security: Admin or Super Admin only
     */
    @GetMapping("/inventory/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getInventoryStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", productService.getAllProducts().size());
        stats.put("inStock", productService.countProductsInStock());
        stats.put("outOfStock", productService.getOutOfStockProducts().size());
        stats.put("lowStock", productService.getLowStockProducts(10).size());
        stats.put("categories", productService.getAllCategories().size());
        stats.put("brands", productService.getAllBrands().size());
        return ResponseEntity.ok(stats);
    }
}
