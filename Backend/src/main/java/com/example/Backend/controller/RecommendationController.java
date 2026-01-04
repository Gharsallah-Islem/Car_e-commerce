package com.example.Backend.controller;

import com.example.Backend.entity.Product;
import com.example.Backend.entity.Recommendation;
import com.example.Backend.security.UserPrincipal;
import com.example.Backend.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for AI-powered product recommendations.
 * Provides personalized, similar, collaborative, and trending recommendations.
 */
@Slf4j
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

        private final RecommendationService recommendationService;

        /**
         * Convert Product entity to a simple Map to avoid circular serialization
         */
        private Map<String, Object> toProductMap(Product p) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", p.getId());
                map.put("name", p.getName());
                map.put("description", p.getDescription());
                map.put("price", p.getPrice());
                map.put("stock", p.getStock());
                map.put("imageUrl", p.getImageUrl());
                map.put("model", p.getModel());
                map.put("year", p.getYear());
                map.put("compatibility", p.getCompatibility());
                if (p.getCategory() != null) {
                        map.put("category", p.getCategory().getName());
                        map.put("categoryId", p.getCategory().getId());
                }
                if (p.getBrand() != null) {
                        map.put("brand", p.getBrand().getName());
                        map.put("brandId", p.getBrand().getId());
                }
                return map;
        }

        /**
         * Get personalized recommendations for the current user
         * GET /api/recommendations/personalized?limit=10
         */
        @GetMapping("/personalized")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Map<String, Object>> getPersonalizedRecommendations(
                        @RequestParam(defaultValue = "10") int limit,
                        @AuthenticationPrincipal UserPrincipal currentUser) {

                log.info("Getting personalized recommendations for user: {}", currentUser.getId());

                List<Product> recommendations = recommendationService.getPersonalizedRecommendations(
                                currentUser.getId(), limit);

                List<Map<String, Object>> products = recommendations.stream()
                                .map(this::toProductMap)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "products", products,
                                "count", products.size(),
                                "type", "PERSONALIZED",
                                "message", "Recommended for you"));
        }

        /**
         * Get products similar to a given product
         * GET /api/recommendations/similar/{productId}?limit=6
         */
        @GetMapping("/similar/{productId}")
        public ResponseEntity<Map<String, Object>> getSimilarProducts(
                        @PathVariable UUID productId,
                        @RequestParam(defaultValue = "6") int limit) {

                log.info("Getting similar products for: {}", productId);

                List<Product> similar = recommendationService.getSimilarProducts(productId, limit);

                List<Map<String, Object>> products = similar.stream()
                                .map(this::toProductMap)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "products", products,
                                "count", products.size(),
                                "type", "SIMILAR",
                                "sourceProductId", productId,
                                "message", "Similar products"));
        }

        /**
         * Get products frequently bought together
         * GET /api/recommendations/also-bought/{productId}?limit=6
         */
        @GetMapping("/also-bought/{productId}")
        public ResponseEntity<Map<String, Object>> getAlsoBoughtProducts(
                        @PathVariable UUID productId,
                        @RequestParam(defaultValue = "6") int limit) {

                log.info("Getting also-bought products for: {}", productId);

                List<Product> alsoBought = recommendationService.getAlsoBoughtProducts(productId, limit);

                List<Map<String, Object>> products = alsoBought.stream()
                                .map(this::toProductMap)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "products", products,
                                "count", products.size(),
                                "type", "ALSO_BOUGHT",
                                "sourceProductId", productId,
                                "message", "Customers also bought"));
        }

        /**
         * Get currently trending products
         * GET /api/recommendations/trending?days=7&limit=10
         */
        @GetMapping("/trending")
        public ResponseEntity<Map<String, Object>> getTrendingProducts(
                        @RequestParam(defaultValue = "7") int days,
                        @RequestParam(defaultValue = "10") int limit) {

                log.info("Getting trending products for last {} days", days);

                List<Product> trending = recommendationService.getTrendingProducts(days, limit);

                List<Map<String, Object>> products = trending.stream()
                                .map(this::toProductMap)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "products", products,
                                "count", products.size(),
                                "type", "TRENDING",
                                "days", days,
                                "message", "Trending now"));
        }

        /**
         * Get user's recommendation history
         * GET /api/recommendations/history?limit=20
         */
        @GetMapping("/history")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Map<String, Object>> getRecommendationHistory(
                        @RequestParam(defaultValue = "20") int limit,
                        @AuthenticationPrincipal UserPrincipal currentUser) {

                List<Recommendation> history = recommendationService.getUserRecommendations(
                                currentUser.getId(), limit);

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "recommendations", history,
                                "count", history.size()));
        }

        /**
         * Mark a recommendation as viewed
         * PATCH /api/recommendations/{id}/viewed
         */
        @PatchMapping("/{id}/viewed")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Map<String, Object>> markAsViewed(@PathVariable UUID id) {
                recommendationService.markAsViewed(id);

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Recommendation marked as viewed"));
        }

        /**
         * Get combined recommendations for home page (personalized + trending)
         * GET /api/recommendations/for-you
         */
        @GetMapping("/for-you")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Map<String, Object>> getForYouSection(
                        @AuthenticationPrincipal UserPrincipal currentUser) {

                log.info("Getting 'For You' section for user: {}", currentUser.getId());

                List<Product> personalizedList = recommendationService.getPersonalizedRecommendations(
                                currentUser.getId(), 8);
                List<Product> trendingList = recommendationService.getTrendingProducts(7, 8);

                List<Map<String, Object>> personalized = personalizedList.stream()
                                .map(this::toProductMap)
                                .collect(Collectors.toList());
                List<Map<String, Object>> trending = trendingList.stream()
                                .map(this::toProductMap)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "personalized", personalized,
                                "trending", trending,
                                "message", "For you"));
        }
}
