package com.example.Backend.controller;

import com.example.Backend.dto.RecommendationDTO;
import com.example.Backend.entity.Recommendation;
import com.example.Backend.security.UserPrincipal;
import com.example.Backend.service.IAService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ia")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class IAController {

    private final IAService iaService;

    /**
     * Get AI recommendations for current user
     * GET /api/ia/recommendations
     */
    @GetMapping("/recommendations")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<Recommendation>> getMyRecommendations(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<Recommendation> recommendations = iaService.getUserRecommendations(currentUser.getId());
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Get active recommendations for current user
     * GET /api/ia/recommendations/active
     */
    @GetMapping("/recommendations/active")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<Recommendation>> getActiveRecommendations(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<Recommendation> recommendations = iaService.getActiveRecommendations(currentUser.getId());
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Generate new recommendations based on user's history
     * POST /api/ia/recommendations/generate
     */
    @PostMapping("/recommendations/generate")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<Recommendation>> generateRecommendations(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<Recommendation> recommendations = iaService.generateRecommendations(currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(recommendations);
    }

    /**
     * Get recommendation by ID
     * GET /api/ia/recommendations/{id}
     */
    @GetMapping("/recommendations/{id}")
    public ResponseEntity<Recommendation> getRecommendation(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Recommendation recommendation = iaService.getRecommendationById(id);

        // Verify user owns this recommendation or is admin
        boolean isOwner = recommendation.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().matches("ROLE_(ADMIN|SUPER_ADMIN)"));

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(recommendation);
    }

    /**
     * Mark recommendation as viewed
     * PATCH /api/ia/recommendations/{id}/viewed
     */
    @PatchMapping("/recommendations/{id}/viewed")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Recommendation> markAsViewed(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Recommendation recommendation = iaService.getRecommendationById(id);

        // Verify ownership
        if (!recommendation.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Recommendation updated = iaService.markAsViewed(id);
        return ResponseEntity.ok(updated);
    }

    /**
     * Create manual recommendation (ADMIN only)
     * POST /api/ia/recommendations/manual
     */
    @PostMapping("/recommendations/manual")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Recommendation> createManualRecommendation(
            @RequestParam UUID userId,
            @Valid @RequestBody RecommendationDTO recommendationDTO) {

        Recommendation recommendation = iaService.createRecommendation(userId, recommendationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(recommendation);
    }

    /**
     * Analyze image for part recognition (virtual mechanic)
     * POST /api/ia/analyze-image
     */
    @PostMapping("/analyze-image")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Map<String, String>> analyzePartImage(
            @RequestBody Map<String, String> imageData,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        String imageBase64 = imageData.get("imageData");
        if (imageBase64 == null || imageBase64.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String analysis = iaService.analyzePartImage(imageBase64, currentUser.getId());
        return ResponseEntity.ok(Map.of("analysis", analysis));
    }

    /**
     * Virtual mechanic chat
     * POST /api/ia/virtual-mechanic
     */
    @PostMapping("/virtual-mechanic")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Map<String, String>> virtualMechanicChat(
            @RequestBody Map<String, String> requestData,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        String question = requestData.get("question");
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String response = iaService.virtualMechanicChat(currentUser.getId(), question);
        return ResponseEntity.ok(Map.of("response", response));
    }

    /**
     * Get user recommendations by user ID (ADMIN only)
     * GET /api/ia/recommendations/user/{userId}
     */
    @GetMapping("/recommendations/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<Recommendation>> getUserRecommendations(@PathVariable UUID userId) {
        List<Recommendation> recommendations = iaService.getUserRecommendations(userId);
        return ResponseEntity.ok(recommendations);
    }
}
