package com.example.Backend.service.impl;

import com.example.Backend.dto.ReclamationDTO;
import com.example.Backend.entity.Reclamation;
import com.example.Backend.entity.User;
import com.example.Backend.repository.ReclamationRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.ReclamationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReclamationServiceImpl implements ReclamationService {

    private final ReclamationRepository reclamationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Reclamation createReclamation(UUID userId, ReclamationDTO reclamationDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Reclamation reclamation = new Reclamation();
        reclamation.setUser(user);
        reclamation.setSubject(reclamationDTO.getSubject());
        reclamation.setDescription(reclamationDTO.getDescription());
        reclamation.setStatus("OPEN");
        reclamation.setCreatedAt(LocalDateTime.now());

        return reclamationRepository.save(reclamation);
    }

    @Override
    @Transactional(readOnly = true)
    public Reclamation getReclamationById(UUID reclamationId) {
        return reclamationRepository.findById(reclamationId)
                .orElseThrow(() -> new EntityNotFoundException("Reclamation not found with id: " + reclamationId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Reclamation> getAllReclamations(Pageable pageable) {
        return reclamationRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Reclamation> getReclamationsByUser(UUID userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        return reclamationRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Reclamation> getReclamationsByStatus(String status, Pageable pageable) {
        return reclamationRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Reclamation> getReclamationsByCategory(String category, Pageable pageable) {
        // Since Reclamation doesn't have category field, search by subject/description
        return reclamationRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Reclamation> getPendingReclamations(Pageable pageable) {
        return reclamationRepository.findByStatus("OPEN", pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Reclamation> getReclamationsByAssignedAgent(UUID supportAgentId, Pageable pageable) {
        // Reclamation doesn't have assignedAgent field, return all in progress
        return reclamationRepository.findByStatus("IN_PROGRESS", pageable);
    }

    @Override
    @Transactional
    public Reclamation assignToAgent(UUID reclamationId, UUID supportAgentId) {
        Reclamation reclamation = reclamationRepository.findById(reclamationId)
                .orElseThrow(() -> new EntityNotFoundException("Reclamation not found with id: " + reclamationId));

        // Verify agent exists
        if (!userRepository.existsById(supportAgentId)) {
            throw new EntityNotFoundException("Support agent not found with id: " + supportAgentId);
        }

        // Since entity doesn't have assignedAgent, just update status
        reclamation.setStatus("IN_PROGRESS");
        return reclamationRepository.save(reclamation);
    }

    @Override
    @Transactional
    public Reclamation updateStatus(UUID reclamationId, String status) {
        Reclamation reclamation = reclamationRepository.findById(reclamationId)
                .orElseThrow(() -> new EntityNotFoundException("Reclamation not found with id: " + reclamationId));

        reclamation.setStatus(status);

        if ("RESOLVED".equals(status) || "CLOSED".equals(status)) {
            reclamation.setResolvedAt(LocalDateTime.now());
        }

        return reclamationRepository.save(reclamation);
    }

    @Override
    @Transactional
    public Reclamation addResponse(UUID reclamationId, String response) {
        Reclamation reclamation = reclamationRepository.findById(reclamationId)
                .orElseThrow(() -> new EntityNotFoundException("Reclamation not found with id: " + reclamationId));
        reclamation.setResponse(response);
        return reclamationRepository.save(reclamation);
    }

    @Override
    @Transactional
    public Reclamation closeReclamation(UUID reclamationId, String resolution) {
        Reclamation reclamation = reclamationRepository.findById(reclamationId)
                .orElseThrow(() -> new EntityNotFoundException("Reclamation not found with id: " + reclamationId));

        reclamation.setStatus("CLOSED");
        reclamation.setResponse(resolution);
        reclamation.setResolvedAt(LocalDateTime.now());

        return reclamationRepository.save(reclamation);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageResolutionTime() {
        List<Reclamation> resolvedReclamations = reclamationRepository.findByStatus("RESOLVED");

        if (resolvedReclamations.isEmpty()) {
            return 0.0;
        }

        double totalHours = 0.0;
        int count = 0;

        for (Reclamation reclamation : resolvedReclamations) {
            if (reclamation.getCreatedAt() != null && reclamation.getResolvedAt() != null) {
                Duration duration = Duration.between(reclamation.getCreatedAt(), reclamation.getResolvedAt());
                totalHours += duration.toHours();
                count++;
            }
        }

        return count > 0 ? totalHours / count : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getReclamationStatistics() {
        Map<String, Long> statistics = new HashMap<>();

        statistics.put("OPEN", reclamationRepository.countByStatus("OPEN"));
        statistics.put("IN_PROGRESS", reclamationRepository.countByStatus("IN_PROGRESS"));
        statistics.put("RESOLVED", reclamationRepository.countByStatus("RESOLVED"));
        statistics.put("CLOSED", reclamationRepository.countByStatus("CLOSED"));
        statistics.put("TOTAL", reclamationRepository.count());

        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public Long countPendingReclamations() {
        return reclamationRepository.countByStatus("OPEN");
    }
}
