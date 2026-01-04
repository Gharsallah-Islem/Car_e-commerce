package com.example.Backend.controller;

import com.example.Backend.dto.UserActivityDTO;
import com.example.Backend.entity.ActivityType;
import com.example.Backend.entity.UserActivity;
import com.example.Backend.security.UserPrincipal;
import com.example.Backend.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for tracking user activities.
 * These activities are used by the AI recommendation engine.
 */
@Slf4j
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class UserActivityController {

    private final UserActivityService activityService;

    /**
     * Track a product view
     * POST /api/activities/view
     */
    @PostMapping("/view")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> trackProductView(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        String productIdStr = request.get("productId");
        String sessionId = request.get("sessionId");

        if (productIdStr == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "productId is required"));
        }

        try {
            UUID productId = UUID.fromString(productIdStr);
            UserActivity activity = activityService.trackProductView(
                    currentUser.getId(), productId, sessionId);

            if (activity != null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Product view tracked",
                        "activityId", activity.getId()));
            } else {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Duplicate view skipped"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid productId format"));
        }
    }

    /**
     * Track add to cart action
     * POST /api/activities/cart
     */
    @PostMapping("/cart")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> trackAddToCart(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        String productIdStr = request.get("productId");

        if (productIdStr == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "productId is required"));
        }

        try {
            UUID productId = UUID.fromString(productIdStr);
            UserActivity activity = activityService.trackAddToCart(currentUser.getId(), productId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Add to cart tracked",
                    "activityId", activity.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid productId format"));
        }
    }

    /**
     * Track a search query
     * POST /api/activities/search
     */
    @PostMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> trackSearch(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        String query = request.get("query");

        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "query is required"));
        }

        UserActivity activity = activityService.trackSearch(currentUser.getId(), query);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Search tracked",
                "activityId", activity.getId()));
    }

    /**
     * Track a generic activity
     * POST /api/activities
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> trackActivity(
            @RequestBody UserActivityDTO activityDTO,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (activityDTO.getActivityType() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "activityType is required"));
        }

        UserActivity activity = activityService.trackActivity(currentUser.getId(), activityDTO);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Activity tracked",
                "activityId", activity.getId()));
    }

    /**
     * Get user's recently viewed products
     * GET /api/activities/recent-views?limit=10
     */
    @GetMapping("/recent-views")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getRecentViews(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<UUID> productIds = activityService.getRecentlyViewedProducts(currentUser.getId(), limit);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "productIds", productIds,
                "count", productIds.size()));
    }

    /**
     * Get user's activity count by type
     * GET /api/activities/count?type=VIEW
     */
    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getActivityCount(
            @RequestParam ActivityType type,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Long count = activityService.getActivityCount(currentUser.getId(), type);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "type", type,
                "count", count));
    }
}
