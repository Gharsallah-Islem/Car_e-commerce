package com.example.Backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST Controller for Brand management
 * Handles vehicle spare parts brands
 */
@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    /**
     * Get all brands
     * GET /api/brands
     * Security: Public endpoint
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllBrands() {
        List<Map<String, Object>> brands = new ArrayList<>();

        // Temporary static data - will be replaced with database entities
        brands.add(createBrand("1", "Bosch", "Global automotive parts manufacturer", "Germany"));
        brands.add(createBrand("2", "Brembo", "High-performance brake systems", "Italy"));
        brands.add(createBrand("3", "Denso", "Japanese automotive components", "Japan"));
        brands.add(createBrand("4", "NGK", "Spark plugs and ignition systems", "Japan"));
        brands.add(createBrand("5", "Michelin", "Tires and automotive accessories", "France"));
        brands.add(createBrand("6", "Castrol", "Engine oils and lubricants", "UK"));
        brands.add(createBrand("7", "Mann-Filter", "Filtration systems", "Germany"));
        brands.add(createBrand("8", "Valeo", "Automotive supplier", "France"));
        brands.add(createBrand("9", "Continental", "Tires and automotive systems", "Germany"));
        brands.add(createBrand("10", "ZF", "Transmission and chassis technology", "Germany"));
        brands.add(createBrand("11", "Magneti Marelli", "Automotive lighting and electronics", "Italy"));
        brands.add(createBrand("12", "Monroe", "Suspension and shock absorbers", "USA"));
        brands.add(createBrand("13", "Champion", "Spark plugs and wipers", "USA"));
        brands.add(createBrand("14", "Hella", "Lighting and electronics", "Germany"));
        brands.add(createBrand("15", "Mahle", "Engine components", "Germany"));

        return ResponseEntity.ok(brands);
    }

    /**
     * Get brand by ID
     * GET /api/brands/{id}
     * Security: Public endpoint
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBrandById(@PathVariable String id) {
        // Temporary implementation - will be replaced with database lookup
        Map<String, Object> brand = createBrand(id, "Brand " + id, "Description for brand " + id, "Unknown");
        return ResponseEntity.ok(brand);
    }

    /**
     * Get products by brand
     * GET /api/brands/{id}/products
     * Security: Public endpoint
     */
    @GetMapping("/{id}/products")
    public ResponseEntity<Map<String, Object>> getProductsByBrand(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Map<String, Object> response = new HashMap<>();
        response.put("brandId", id);
        response.put("brandName", "Brand " + id);
        response.put("products", new ArrayList<>());
        response.put("totalElements", 0);
        response.put("totalPages", 0);
        response.put("currentPage", page);
        response.put("pageSize", size);

        return ResponseEntity.ok(response);
    }

    /**
     * Get popular brands (most ordered)
     * GET /api/brands/popular
     * Security: Public endpoint
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Map<String, Object>>> getPopularBrands(
            @RequestParam(defaultValue = "10") int limit) {

        List<Map<String, Object>> popularBrands = new ArrayList<>();

        // Top brands based on popularity
        popularBrands.add(createBrandWithStats("1", "Bosch", 1250));
        popularBrands.add(createBrandWithStats("2", "Brembo", 980));
        popularBrands.add(createBrandWithStats("3", "Denso", 875));
        popularBrands.add(createBrandWithStats("5", "Michelin", 756));
        popularBrands.add(createBrandWithStats("6", "Castrol", 689));

        return ResponseEntity.ok(popularBrands.subList(0, Math.min(limit, popularBrands.size())));
    }

    /**
     * Search brands
     * GET /api/brands/search?q=bosch
     * Security: Public endpoint
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchBrands(
            @RequestParam String q) {

        List<Map<String, Object>> allBrands = getAllBrands().getBody();
        List<Map<String, Object>> results = new ArrayList<>();

        String searchTerm = q.toLowerCase();
        for (Map<String, Object> brand : allBrands) {
            String name = ((String) brand.get("name")).toLowerCase();
            String description = ((String) brand.get("description")).toLowerCase();

            if (name.contains(searchTerm) || description.contains(searchTerm)) {
                results.add(brand);
            }
        }

        return ResponseEntity.ok(results);
    }

    // Helper methods
    private Map<String, Object> createBrand(String id, String name, String description, String country) {
        Map<String, Object> brand = new HashMap<>();
        brand.put("id", id);
        brand.put("name", name);
        brand.put("description", description);
        brand.put("country", country);
        brand.put("productCount", 0);
        brand.put("logo", "/images/brands/" + name.toLowerCase().replaceAll(" ", "-") + ".png");
        return brand;
    }

    private Map<String, Object> createBrandWithStats(String id, String name, int orderCount) {
        Map<String, Object> brand = new HashMap<>();
        brand.put("id", id);
        brand.put("name", name);
        brand.put("orderCount", orderCount);
        brand.put("logo", "/images/brands/" + name.toLowerCase().replaceAll(" ", "-") + ".png");
        return brand;
    }
}
