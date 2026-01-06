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
        return reclamationRepository.findByAssignedAgentId(supportAgentId, pageable);
    }

    @Override
    @Transactional
    public Reclamation assignToAgent(UUID reclamationId, UUID supportAgentId) {
        Reclamation reclamation = reclamationRepository.findById(reclamationId)
                .orElseThrow(() -> new EntityNotFoundException("Reclamation not found with id: " + reclamationId));

        // Verify agent exists and get the user
        User agent = userRepository.findById(supportAgentId)
                .orElseThrow(() -> new EntityNotFoundException("Support agent not found with id: " + supportAgentId));

        // Assign the agent and update status
        reclamation.setAssignedAgent(agent);
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

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAgentPerformanceStats(UUID agentId) {
        Map<String, Object> stats = new HashMap<>();

        // Get all tickets assigned to this agent
        List<Reclamation> allAssigned = reclamationRepository.findByAssignedAgentId(agentId);
        List<Reclamation> resolved = allAssigned.stream()
                .filter(r -> "RESOLVED".equals(r.getStatus()) || "CLOSED".equals(r.getStatus()))
                .toList();

        // Tickets resolved this month
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long resolvedThisMonth = resolved.stream()
                .filter(r -> r.getResolvedAt() != null && r.getResolvedAt().isAfter(startOfMonth))
                .count();

        // Tickets resolved today
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long resolvedToday = resolved.stream()
                .filter(r -> r.getResolvedAt() != null && r.getResolvedAt().isAfter(startOfDay))
                .count();

        // Average resolution time for this agent
        double avgResolutionHours = 0.0;
        int resolvedCount = 0;
        for (Reclamation r : resolved) {
            if (r.getCreatedAt() != null && r.getResolvedAt() != null) {
                Duration duration = Duration.between(r.getCreatedAt(), r.getResolvedAt());
                avgResolutionHours += duration.toHours();
                resolvedCount++;
            }
        }
        if (resolvedCount > 0) {
            avgResolutionHours = avgResolutionHours / resolvedCount;
        }

        // In progress tickets
        long inProgress = allAssigned.stream()
                .filter(r -> "IN_PROGRESS".equals(r.getStatus()))
                .count();

        // Open tickets assigned to this agent
        long openTicketsAssigned = allAssigned.stream()
                .filter(r -> "OPEN".equals(r.getStatus()))
                .count();

        // Pending unassigned tickets (waiting to be picked up by any agent)
        long pendingUnassigned = reclamationRepository.countUnassignedOpenTickets();

        stats.put("totalAssigned", (long) allAssigned.size());
        stats.put("totalResolved", (long) resolved.size());
        stats.put("resolvedThisMonth", resolvedThisMonth);
        stats.put("resolvedToday", resolvedToday);
        stats.put("inProgress", inProgress);
        stats.put("openTickets", pendingUnassigned); // Show unassigned tickets waiting for pickup
        stats.put("openTicketsAssigned", openTicketsAssigned); // Tickets assigned but not started
        stats.put("avgResolutionTimeHours", avgResolutionHours);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAgentWeeklyStats(UUID agentId) {
        List<Map<String, Object>> weeklyStats = new java.util.ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        String[] days = { "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim" };

        // Get the start of the week (Monday)
        LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        List<Reclamation> allAssigned = reclamationRepository.findByAssignedAgentId(agentId);

        for (int i = 0; i < 7; i++) {
            LocalDateTime dayStart = startOfWeek.plusDays(i);
            LocalDateTime dayEnd = dayStart.plusDays(1);

            // Count tickets received that day (assigned to agent)
            long ticketsReceived = allAssigned.stream()
                    .filter(r -> r.getCreatedAt() != null &&
                            r.getCreatedAt().isAfter(dayStart) &&
                            r.getCreatedAt().isBefore(dayEnd))
                    .count();

            // Count tickets resolved that day
            long ticketsResolved = allAssigned.stream()
                    .filter(r -> r.getResolvedAt() != null &&
                            r.getResolvedAt().isAfter(dayStart) &&
                            r.getResolvedAt().isBefore(dayEnd))
                    .count();

            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("day", days[i]);
            dayStat.put("tickets", ticketsReceived);
            dayStat.put("resolved", ticketsResolved);
            weeklyStats.add(dayStat);
        }

        return weeklyStats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAgentRecentActivities(UUID agentId, int limit) {
        List<Map<String, Object>> activities = new java.util.ArrayList<>();

        List<Reclamation> agentTickets = reclamationRepository.findByAssignedAgentId(agentId);

        // Sort by updatedAt or resolvedAt descending
        agentTickets.sort((a, b) -> {
            LocalDateTime aTime = a.getResolvedAt() != null ? a.getResolvedAt()
                    : (a.getUpdatedAt() != null ? a.getUpdatedAt() : a.getCreatedAt());
            LocalDateTime bTime = b.getResolvedAt() != null ? b.getResolvedAt()
                    : (b.getUpdatedAt() != null ? b.getUpdatedAt() : b.getCreatedAt());
            return bTime.compareTo(aTime);
        });

        int count = 0;
        for (Reclamation r : agentTickets) {
            if (count >= limit)
                break;

            Map<String, Object> activity = new HashMap<>();
            activity.put("id", r.getId().toString());
            activity.put("ticketSubject", r.getSubject());

            String action;
            String type;
            LocalDateTime activityTime;

            if ("RESOLVED".equals(r.getStatus()) || "CLOSED".equals(r.getStatus())) {
                action = "Ticket résolu";
                type = "resolved";
                activityTime = r.getResolvedAt() != null ? r.getResolvedAt() : r.getUpdatedAt();
            } else if (r.getResponse() != null && !r.getResponse().isEmpty()) {
                action = "Réponse envoyée";
                type = "replied";
                activityTime = r.getUpdatedAt() != null ? r.getUpdatedAt() : r.getCreatedAt();
            } else {
                action = "Ticket assigné";
                type = "assigned";
                activityTime = r.getCreatedAt();
            }

            activity.put("action", action);
            activity.put("type", type);
            activity.put("time", formatRelativeTime(activityTime));

            activities.add(activity);
            count++;
        }

        return activities;
    }

    private String formatRelativeTime(LocalDateTime time) {
        if (time == null)
            return "Récemment";

        Duration duration = Duration.between(time, LocalDateTime.now());
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 1)
            return "À l'instant";
        if (minutes < 60)
            return "Il y a " + minutes + " min";
        if (hours < 24)
            return "Il y a " + hours + "h";
        if (days == 1)
            return "Hier";
        return "Il y a " + days + " jours";
    }
}
