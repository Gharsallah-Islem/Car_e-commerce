package com.example.Backend.repository;

import com.example.Backend.entity.Reclamation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReclamationRepository extends JpaRepository<Reclamation, UUID> {

    /**
     * Find all reclamations for a user
     */
    List<Reclamation> findByUserId(UUID userId);

    /**
     * Find reclamations by user with pagination
     */
    Page<Reclamation> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find reclamations assigned to a specific agent
     */
    Page<Reclamation> findByAssignedAgentId(UUID agentId, Pageable pageable);

    /**
     * Find all reclamations assigned to a specific agent (without pagination)
     */
    List<Reclamation> findByAssignedAgentId(UUID agentId);

    /**
     * Find reclamations by status
     */
    List<Reclamation> findByStatus(String status);

    /**
     * Find reclamations by status with pagination
     */
    Page<Reclamation> findByStatus(String status, Pageable pageable);

    /**
     * Find reclamations by user and status
     */
    List<Reclamation> findByUserIdAndStatus(UUID userId, String status);

    /**
     * Find open reclamations (for support dashboard)
     */
    @Query("SELECT r FROM Reclamation r WHERE r.status IN ('OPEN', 'IN_PROGRESS') ORDER BY r.createdAt ASC")
    List<Reclamation> findOpenReclamations();

    /**
     * Find recent reclamations (last N days)
     */
    @Query("SELECT r FROM Reclamation r WHERE r.createdAt >= :since ORDER BY r.createdAt DESC")
    List<Reclamation> findRecentReclamations(@Param("since") LocalDateTime since);

    /**
     * Search reclamations by subject or description
     */
    @Query("SELECT r FROM Reclamation r WHERE " +
            "LOWER(r.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Reclamation> searchReclamations(@Param("searchTerm") String searchTerm);

    /**
     * Count reclamations by status
     */
    Long countByStatus(String status);

    /**
     * Count reclamations by user
     */
    Long countByUserId(UUID userId);

    /**
     * Find unresolved reclamations (OPEN or IN_PROGRESS)
     */
    @Query("SELECT r FROM Reclamation r WHERE r.status IN ('OPEN', 'IN_PROGRESS')")
    List<Reclamation> findUnresolvedReclamations();

    /**
     * Find reclamations requiring attention (open for more than X days)
     */
    @Query("SELECT r FROM Reclamation r WHERE r.status = 'OPEN' AND r.createdAt <= :threshold")
    List<Reclamation> findStaleReclamations(@Param("threshold") LocalDateTime threshold);

    /**
     * Count unassigned open tickets (pending to be picked up by an agent)
     */
    @Query("SELECT COUNT(r) FROM Reclamation r WHERE r.status = 'OPEN' AND r.assignedAgent IS NULL")
    Long countUnassignedOpenTickets();

    /**
     * Find unassigned open tickets
     */
    @Query("SELECT r FROM Reclamation r WHERE r.status = 'OPEN' AND r.assignedAgent IS NULL ORDER BY r.createdAt ASC")
    List<Reclamation> findUnassignedOpenTickets();
}
