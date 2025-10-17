package com.example.Backend.controller;

import com.example.Backend.dto.ReclamationDTO;
import com.example.Backend.entity.Reclamation;
import com.example.Backend.security.UserPrincipal;
import com.example.Backend.service.ReclamationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reclamations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ReclamationController {

    private final ReclamationService reclamationService;

    /**
     * Create a new reclamation (CLIENT only)
     * POST /api/reclamations
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Reclamation> createReclamation(
            @Valid @RequestBody ReclamationDTO reclamationDTO,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Reclamation reclamation = reclamationService.createReclamation(
                currentUser.getId(),
                reclamationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(reclamation);
    }

    /**
     * Get reclamation by ID
     * GET /api/reclamations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Reclamation> getReclamation(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Reclamation reclamation = reclamationService.getReclamationById(id);

        // Verify user owns this reclamation or is staff
        boolean isOwner = reclamation.getUser().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().matches("ROLE_(SUPPORT|ADMIN|SUPER_ADMIN)"));

        if (!isOwner && !isStaff) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(reclamation);
    }

    /**
     * Get all reclamations (SUPPORT/ADMIN only)
     * GET /api/reclamations
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Reclamation>> getAllReclamations(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<Reclamation> reclamations = reclamationService.getAllReclamations(pageable);
        return ResponseEntity.ok(reclamations);
    }

    /**
     * Get my reclamations (CLIENT)
     * GET /api/reclamations/my-reclamations
     */
    @GetMapping("/my-reclamations")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<Reclamation>> getMyReclamations(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Page<Reclamation> reclamations = reclamationService.getReclamationsByUser(
                currentUser.getId(),
                pageable);
        return ResponseEntity.ok(reclamations);
    }

    /**
     * Get reclamations by status (SUPPORT/ADMIN only)
     * GET /api/reclamations/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Reclamation>> getReclamationsByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<Reclamation> reclamations = reclamationService.getReclamationsByStatus(status, pageable);
        return ResponseEntity.ok(reclamations);
    }

    /**
     * Get reclamations by category (SUPPORT/ADMIN only)
     * GET /api/reclamations/category/{category}
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Reclamation>> getReclamationsByCategory(
            @PathVariable String category,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<Reclamation> reclamations = reclamationService.getReclamationsByCategory(category, pageable);
        return ResponseEntity.ok(reclamations);
    }

    /**
     * Get pending reclamations (SUPPORT/ADMIN only)
     * GET /api/reclamations/pending
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Reclamation>> getPendingReclamations(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<Reclamation> reclamations = reclamationService.getPendingReclamations(pageable);
        return ResponseEntity.ok(reclamations);
    }

    /**
     * Get reclamations assigned to me (SUPPORT/ADMIN)
     * GET /api/reclamations/assigned-to-me
     */
    @GetMapping("/assigned-to-me")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Reclamation>> getMyAssignedReclamations(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Page<Reclamation> reclamations = reclamationService.getReclamationsByAssignedAgent(
                currentUser.getId(),
                pageable);
        return ResponseEntity.ok(reclamations);
    }

    /**
     * Get reclamations assigned to specific agent (ADMIN only)
     * GET /api/reclamations/assigned/{agentId}
     */
    @GetMapping("/assigned/{agentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Reclamation>> getReclamationsByAgent(
            @PathVariable UUID agentId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<Reclamation> reclamations = reclamationService.getReclamationsByAssignedAgent(agentId, pageable);
        return ResponseEntity.ok(reclamations);
    }

    /**
     * Assign reclamation to support agent (ADMIN only)
     * PATCH /api/reclamations/{id}/assign/{agentId}
     */
    @PatchMapping("/{id}/assign/{agentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Reclamation> assignToAgent(
            @PathVariable UUID id,
            @PathVariable UUID agentId) {

        Reclamation reclamation = reclamationService.assignToAgent(id, agentId);
        return ResponseEntity.ok(reclamation);
    }

    /**
     * Assign reclamation to self (SUPPORT/ADMIN)
     * PATCH /api/reclamations/{id}/assign-to-me
     */
    @PatchMapping("/{id}/assign-to-me")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Reclamation> assignToMe(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Reclamation reclamation = reclamationService.assignToAgent(id, currentUser.getId());
        return ResponseEntity.ok(reclamation);
    }

    /**
     * Update reclamation status (SUPPORT/ADMIN only)
     * PATCH /api/reclamations/{id}/status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Reclamation> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> statusUpdate) {

        String status = statusUpdate.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Reclamation reclamation = reclamationService.updateStatus(id, status);
        return ResponseEntity.ok(reclamation);
    }

    /**
     * Add response to reclamation (SUPPORT/ADMIN only)
     * POST /api/reclamations/{id}/response
     */
    @PostMapping("/{id}/response")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Reclamation> addResponse(
            @PathVariable UUID id,
            @RequestBody Map<String, String> responseData) {

        String response = responseData.get("response");
        if (response == null || response.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Reclamation reclamation = reclamationService.addResponse(id, response);
        return ResponseEntity.ok(reclamation);
    }

    /**
     * Close reclamation (SUPPORT/ADMIN only)
     * PATCH /api/reclamations/{id}/close
     */
    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Reclamation> closeReclamation(
            @PathVariable UUID id,
            @RequestBody Map<String, String> resolutionData) {

        String resolution = resolutionData.get("resolution");
        if (resolution == null || resolution.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Reclamation reclamation = reclamationService.closeReclamation(id, resolution);
        return ResponseEntity.ok(reclamation);
    }

    /**
     * Get reclamation statistics (ADMIN only)
     * GET /api/reclamations/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Long>> getStatistics() {

        Map<String, Long> statistics = reclamationService.getReclamationStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get average resolution time (ADMIN only)
     * GET /api/reclamations/average-resolution-time
     */
    @GetMapping("/average-resolution-time")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Double> getAverageResolutionTime() {

        Double avgTime = reclamationService.getAverageResolutionTime();
        return ResponseEntity.ok(avgTime);
    }

    /**
     * Get pending reclamation count (SUPPORT/ADMIN)
     * GET /api/reclamations/pending/count
     */
    @GetMapping("/pending/count")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Long> getPendingCount() {

        Long count = reclamationService.countPendingReclamations();
        return ResponseEntity.ok(count);
    }
}
