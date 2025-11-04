package com.example.Backend.service;

import com.example.Backend.dto.ReclamationDTO;
import com.example.Backend.entity.Reclamation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface ReclamationService {

    /**
     * Create a new reclamation
     * 
     * @param userId         User ID
     * @param reclamationDTO Reclamation data
     * @return Created reclamation
     */
    Reclamation createReclamation(UUID userId, ReclamationDTO reclamationDTO);

    /**
     * Get reclamation by ID
     * 
     * @param reclamationId Reclamation ID
     * @return Reclamation entity
     */
    Reclamation getReclamationById(UUID reclamationId);

    /**
     * Get all reclamations
     * 
     * @param pageable Pagination parameters
     * @return Page of reclamations
     */
    Page<Reclamation> getAllReclamations(Pageable pageable);

    /**
     * Get reclamations by user
     * 
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of user's reclamations
     */
    Page<Reclamation> getReclamationsByUser(UUID userId, Pageable pageable);

    /**
     * Get reclamations by status
     * 
     * @param status   Status
     * @param pageable Pagination parameters
     * @return Page of reclamations
     */
    Page<Reclamation> getReclamationsByStatus(String status, Pageable pageable);

    /**
     * Get reclamations by category
     * 
     * @param category Category
     * @param pageable Pagination parameters
     * @return Page of reclamations
     */
    Page<Reclamation> getReclamationsByCategory(String category, Pageable pageable);

    /**
     * Get pending reclamations
     * 
     * @param pageable Pagination parameters
     * @return Page of pending reclamations
     */
    Page<Reclamation> getPendingReclamations(Pageable pageable);

    /**
     * Get reclamations assigned to support agent
     * 
     * @param supportAgentId Support agent ID
     * @param pageable       Pagination parameters
     * @return Page of reclamations
     */
    Page<Reclamation> getReclamationsByAssignedAgent(UUID supportAgentId, Pageable pageable);

    /**
     * Assign reclamation to support agent
     * 
     * @param reclamationId  Reclamation ID
     * @param supportAgentId Support agent ID
     * @return Updated reclamation
     */
    Reclamation assignToAgent(UUID reclamationId, UUID supportAgentId);

    /**
     * Update reclamation status
     * 
     * @param reclamationId Reclamation ID
     * @param status        New status
     * @return Updated reclamation
     */
    Reclamation updateStatus(UUID reclamationId, String status);

    /**
     * Add response to reclamation
     * 
     * @param reclamationId Reclamation ID
     * @param response      Response text
     * @return Updated reclamation
     */
    Reclamation addResponse(UUID reclamationId, String response);

    /**
     * Close reclamation
     * 
     * @param reclamationId Reclamation ID
     * @param resolution    Resolution text
     * @return Updated reclamation
     */
    Reclamation closeReclamation(UUID reclamationId, String resolution);

    /**
     * Get average resolution time
     * 
     * @return Average time in hours
     */
    Double getAverageResolutionTime();

    /**
     * Get reclamation statistics
     * 
     * @return Map of counts by status
     */
    Map<String, Long> getReclamationStatistics();

    /**
     * Count pending reclamations
     * 
     * @return Count
     */
    Long countPendingReclamations();
}
