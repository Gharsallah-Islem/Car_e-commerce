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

        // Enhanced categories inspired by Titan Motorsports
        categories.add(createCategory("1", "Performance Exhaust Systems", "Axle-back, cat-back, turbo-back, headers, mufflers"));
        categories.add(createCategory("2", "Turbocharger Kits & Accessories", "Turbo upgrades, blow-off valves, wastegates, intercoolers"));
        categories.add(createCategory("3", "Clutch/Drivetrain", "Clutch kits, flywheels, axles, differentials, driveshafts"));
        categories.add(createCategory("4", "Brake Parts", "Brake kits, pads, rotors, calipers, brake lines"));
        categories.add(createCategory("5", "Suspension", "Coilovers, shock absorbers, springs, sway bars, control arms"));
        categories.add(createCategory("6", "Cooling System", "Radiators, intercoolers, oil coolers, fans, hoses"));
        categories.add(createCategory("7", "Air Induction", "Cold air intakes, intake manifolds, throttle bodies, air filters"));
        categories.add(createCategory("8", "Engine Parts", "Crankshafts, pistons, rods, head studs, valvetrain"));
        categories.add(createCategory("9", "Electronics & Tuning", "ECU tunes, displays, sensors, wiring harnesses"));
        categories.add(createCategory("10", "Fuel Systems", "Fuel pumps, injectors, regulators, fuel rails, lines"));
        categories.add(createCategory("11", "Exterior", "Aero parts, splitters, spoilers, body kits, carbon fiber"));
        categories.add(createCategory("12", "Wheels/Tires", "Performance wheels, racing tires, wheel accessories"));
        categories.add(createCategory("13", "Racing & Safety", "Roll cages, harness bars, racing seats, safety equipment"));
        categories.add(createCategory("14", "Filters", "Oil filters, air filters, fuel filters, high-performance filters"));

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
