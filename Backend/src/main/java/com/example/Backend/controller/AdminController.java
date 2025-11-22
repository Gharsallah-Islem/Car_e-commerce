package com.example.Backend.controller;

import com.example.Backend.dto.AdminDTO;
import com.example.Backend.entity.User;
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
    public ResponseEntity<User> createAdmin(@Valid @RequestBody AdminDTO adminDTO) {
        User admin = adminService.createAdmin(adminDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(admin);
    }

    /**
     * Get admin by ID
     * GET /api/admin/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getAdmin(@PathVariable UUID id) {
        User admin = adminService.getAdminById(id);
        return ResponseEntity.ok(admin);
    }

    /**
     * Get admin by username
     * GET /api/admin/username/{username}
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getAdminByUsername(@PathVariable String username) {
        User admin = adminService.getAdminByUsername(username);
        return ResponseEntity.ok(admin);
    }

    /**
     * Get all admins
     * GET /api/admin
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllAdmins() {
        List<User> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }

    /**
     * Get all active admins
     * GET /api/admin/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<User>> getActiveAdmins() {
        List<User> admins = adminService.getActiveAdmins();
        return ResponseEntity.ok(admins);
    }

    /**
     * Update admin
     * PUT /api/admin/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> updateAdmin(
            @PathVariable UUID id,
            @Valid @RequestBody AdminDTO adminDTO) {

        User admin = adminService.updateAdmin(id, adminDTO);
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
     * Activate user account (Generic)
     * PATCH /api/admin/users/{id}/activate
     */
    @PatchMapping("/users/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<User> activateUser(@PathVariable UUID id) {
        User user = adminService.activateUser(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Deactivate user account (Generic)
     * PATCH /api/admin/users/{id}/deactivate
     */
    @PatchMapping("/users/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<User> deactivateUser(@PathVariable UUID id) {
        User user = adminService.deactivateUser(id);
        return ResponseEntity.ok(user);
    }
}
