package com.example.Backend.repository;

import com.example.Backend.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {

    /**
     * Find admin by username
     */
    Optional<Admin> findByUsername(String username);

    /**
     * Find admin by email
     */
    Optional<Admin> findByEmail(String email);

    /**
     * Find admin by username or email
     */
    Optional<Admin> findByUsernameOrEmail(String username, String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find all active admins
     */
    List<Admin> findByIsActive(Boolean isActive);

    /**
     * Find active admins only
     */
    default List<Admin> findAllActive() {
        return findByIsActive(true);
    }

    /**
     * Count active admins
     */
    Long countByIsActive(Boolean isActive);
}
