package com.example.Backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST Controller for Category management
 * Handles product categories and hierarchical category tree
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    /**
     * Get all categories
     * GET /api/categories
     * Security: Public endpoint
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllCategories() {
        List<Map<String, Object>> categories = new ArrayList<>();

        // Temporary static data - will be replaced with database entities
        categories.add(createCategory("1", "Engine Parts", "Parts for engine systems"));
        categories.add(createCategory("2", "Brake Systems", "Brake pads, discs, and components"));
        categories.add(createCategory("3", "Suspension", "Suspension and steering parts"));
        categories.add(createCategory("4", "Electrical", "Electrical components and sensors"));
        categories.add(createCategory("5", "Body Parts", "Body panels and exterior parts"));
        categories.add(createCategory("6", "Filters", "Oil, air, and fuel filters"));
        categories.add(createCategory("7", "Exhaust", "Exhaust systems and components"));
        categories.add(createCategory("8", "Transmission", "Transmission and drivetrain parts"));
        categories.add(createCategory("9", "Cooling System", "Radiators and cooling components"));
        categories.add(createCategory("10", "Interior", "Interior parts and accessories"));

        return ResponseEntity.ok(categories);
    }

    /**
     * Get category tree (hierarchical structure)
     * GET /api/categories/tree
     * Security: Public endpoint
     */
    @GetMapping("/tree")
    public ResponseEntity<Map<String, Object>> getCategoryTree() {
        Map<String, Object> tree = new HashMap<>();
        List<Map<String, Object>> rootCategories = new ArrayList<>();

        // Main category: Mechanical
        Map<String, Object> mechanical = new HashMap<>();
        mechanical.put("id", "MECH");
        mechanical.put("name", "Mechanical Parts");
        mechanical.put("children", Arrays.asList(
                createCategoryNode("1", "Engine Parts"),
                createCategoryNode("3", "Suspension"),
                createCategoryNode("8", "Transmission")));

        // Main category: Electrical
        Map<String, Object> electrical = new HashMap<>();
        electrical.put("id", "ELEC");
        electrical.put("name", "Electrical & Electronics");
        electrical.put("children", Arrays.asList(
                createCategoryNode("4", "Electrical"),
                createCategoryNode("SENSORS", "Sensors")));

        // Main category: Body & Interior
        Map<String, Object> bodyInterior = new HashMap<>();
        bodyInterior.put("id", "BODY");
        bodyInterior.put("name", "Body & Interior");
        bodyInterior.put("children", Arrays.asList(
                createCategoryNode("5", "Body Parts"),
                createCategoryNode("10", "Interior")));

        // Main category: Maintenance
        Map<String, Object> maintenance = new HashMap<>();
        maintenance.put("id", "MAINT");
        maintenance.put("name", "Maintenance");
        maintenance.put("children", Arrays.asList(
                createCategoryNode("2", "Brake Systems"),
                createCategoryNode("6", "Filters"),
                createCategoryNode("7", "Exhaust"),
                createCategoryNode("9", "Cooling System")));

        rootCategories.add(mechanical);
        rootCategories.add(electrical);
        rootCategories.add(bodyInterior);
        rootCategories.add(maintenance);

        tree.put("categories", rootCategories);
        tree.put("totalCount", 14);

        return ResponseEntity.ok(tree);
    }

    /**
     * Get category by ID
     * GET /api/categories/{id}
     * Security: Public endpoint
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable String id) {
        // Temporary implementation - will be replaced with database lookup
        Map<String, Object> category = createCategory(id, "Category " + id, "Description for category " + id);
        return ResponseEntity.ok(category);
    }

    /**
     * Get products by category
     * GET /api/categories/{id}/products
     * Security: Public endpoint
     */
    @GetMapping("/{id}/products")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Map<String, Object> response = new HashMap<>();
        response.put("categoryId", id);
        response.put("products", new ArrayList<>());
        response.put("totalElements", 0);
        response.put("totalPages", 0);
        response.put("currentPage", page);
        response.put("pageSize", size);

        return ResponseEntity.ok(response);
    }

    // Helper methods
    private Map<String, Object> createCategory(String id, String name, String description) {
        Map<String, Object> category = new HashMap<>();
        category.put("id", id);
        category.put("name", name);
        category.put("description", description);
        category.put("productCount", 0);
        return category;
    }

    private Map<String, Object> createCategoryNode(String id, String name) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", id);
        node.put("name", name);
        node.put("productCount", 0);
        return node;
    }
}
