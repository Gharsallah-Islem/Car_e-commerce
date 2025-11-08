package com.example.Backend.repository;

import com.example.Backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username or email (for login)
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find users by role
     */
    @Query("SELECT u FROM User u WHERE u.role.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    /**
     * Find all clients (users with CLIENT role)
     */
    @Query("SELECT u FROM User u WHERE u.role.name = 'CLIENT'")
    List<User> findAllClients();

    /**
     * Find users created after a specific date
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Search users by username or email or full name
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    /**
     * Count users by role
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role.name = :roleName")
    Long countByRoleName(@Param("roleName") String roleName);

    /**
     * Find active users (those who placed orders recently)
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.orders o WHERE o.createdAt >= :since")
    List<User> findActiveUsers(@Param("since") LocalDateTime since);

    /**
     * Find user by email verification token
     */
    Optional<User> findByEmailVerificationToken(String token);

    /**
     * Find user by password reset token
     */
    Optional<User> findByPasswordResetToken(String token);
}
