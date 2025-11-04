package com.example.Backend.repository;

import com.example.Backend.entity.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SuperAdminRepository extends JpaRepository<SuperAdmin, UUID> {

    /**
     * Find super admin by username
     */
    Optional<SuperAdmin> findByUsername(String username);

    /**
     * Find super admin by email
     */
    Optional<SuperAdmin> findByEmail(String email);

    /**
     * Find super admin by username or email
     */
    Optional<SuperAdmin> findByUsernameOrEmail(String username, String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find all active super admins
     */
    List<SuperAdmin> findByIsActive(Boolean isActive);

    /**
     * Find active super admins only
     */
    default List<SuperAdmin> findAllActive() {
        return findByIsActive(true);
    }
}
