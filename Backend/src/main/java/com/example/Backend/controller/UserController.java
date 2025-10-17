package com.example.Backend.controller;

import com.example.Backend.dto.UserDTO;
import com.example.Backend.entity.User;
import com.example.Backend.security.UserPrincipal;
import com.example.Backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for User management
 * Handles user profile operations, account management
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get current authenticated user profile
     * GET /api/users/me
     * Security: Authenticated users only
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        User user = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(user);
    }

    /**
     * Update current user profile
     * PUT /api/users/me
     * Security: Authenticated users only
     */
    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUser(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody UserDTO userDTO) {
        User updatedUser = userService.updateUser(currentUser.getId(), userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete current user account
     * DELETE /api/users/me
     * Security: Authenticated users only
     */
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        userService.deleteUser(currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }

    /**
     * Get user by ID
     * GET /api/users/{id}
     * Security: Admin or Super Admin only
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Get all users
     * GET /api/users
     * Security: Admin or Super Admin only
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Search users by term
     * GET /api/users/search?term={searchTerm}
     * Security: Admin or Super Admin only
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String term) {
        List<User> users = userService.searchUsers(term);
        return ResponseEntity.ok(users);
    }

    /**
     * Get users by role
     * GET /api/users/role/{roleName}
     * Security: Admin or Super Admin only
     */
    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String roleName) {
        List<User> users = userService.getUsersByRole(roleName);
        return ResponseEntity.ok(users);
    }

    /**
     * Get active users (with recent orders)
     * GET /api/users/active?days={days}
     * Security: Admin or Super Admin only
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<User>> getActiveUsers(@RequestParam(defaultValue = "30") int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<User> users = userService.getActiveUsers(since);
        return ResponseEntity.ok(users);
    }

    /**
     * Update user by ID (Admin operation)
     * PUT /api/users/{id}
     * Security: Admin or Super Admin only
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<User> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserDTO userDTO) {
        User updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete user by ID (Admin operation)
     * DELETE /api/users/{id}
     * Security: Super Admin only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    /**
     * Check if username exists
     * GET /api/users/check/username?username={username}
     * Security: Public endpoint (for registration validation)
     */
    @GetMapping("/check/username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        boolean exists = userService.usernameExists(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Check if email exists
     * GET /api/users/check/email?email={email}
     * Security: Public endpoint (for registration validation)
     */
    @GetMapping("/check/email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Get user statistics (count by role)
     * GET /api/users/statistics
     * Security: Admin or Super Admin only
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.getAllUsers().size());
        stats.put("clients", userService.countUsersByRole("CLIENT"));
        stats.put("admins", userService.countUsersByRole("ADMIN"));
        stats.put("support", userService.countUsersByRole("SUPPORT"));
        stats.put("superAdmins", userService.countUsersByRole("SUPER_ADMIN"));

        // Active users in last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        stats.put("activeUsersLast30Days", userService.getActiveUsers(thirtyDaysAgo).size());

        return ResponseEntity.ok(stats);
    }
}
