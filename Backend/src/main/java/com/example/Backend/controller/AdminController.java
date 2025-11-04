package com.example.Backend.controller;

import com.example.Backend.dto.AdminDTO;
import com.example.Backend.entity.Admin;
import com.example.Backend.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /**
     * Create new admin account (SUPER_ADMIN only)
     * POST /api/admin
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Admin> createAdmin(@Valid @RequestBody AdminDTO adminDTO) {
        Admin admin = adminService.createAdmin(adminDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(admin);
    }

    /**
     * Get admin by ID
     * GET /api/admin/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdmin(@PathVariable UUID id) {
        Admin admin = adminService.getAdminById(id);
        return ResponseEntity.ok(admin);
    }

    /**
     * Get admin by username
     * GET /api/admin/username/{username}
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<Admin> getAdminByUsername(@PathVariable String username) {
        Admin admin = adminService.getAdminByUsername(username);
        return ResponseEntity.ok(admin);
    }

    /**
     * Get all admins
     * GET /api/admin
     */
    @GetMapping
    public ResponseEntity<List<Admin>> getAllAdmins() {
        List<Admin> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }

    /**
     * Get all active admins
     * GET /api/admin/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<Admin>> getActiveAdmins() {
        List<Admin> admins = adminService.getActiveAdmins();
        return ResponseEntity.ok(admins);
    }

    /**
     * Update admin
     * PUT /api/admin/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Admin> updateAdmin(
            @PathVariable UUID id,
            @Valid @RequestBody AdminDTO adminDTO) {

        Admin admin = adminService.updateAdmin(id, adminDTO);
        return ResponseEntity.ok(admin);
    }

    /**
     * Delete admin (SUPER_ADMIN only)
     * DELETE /api/admin/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteAdmin(@PathVariable UUID id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activate admin account (SUPER_ADMIN only)
     * PATCH /api/admin/{id}/activate
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Admin> activateAdmin(@PathVariable UUID id) {
        Admin admin = adminService.activateAdmin(id);
        return ResponseEntity.ok(admin);
    }

    /**
     * Deactivate admin account (SUPER_ADMIN only)
     * PATCH /api/admin/{id}/deactivate
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Admin> deactivateAdmin(@PathVariable UUID id) {
        Admin admin = adminService.deactivateAdmin(id);
        return ResponseEntity.ok(admin);
    }
}
