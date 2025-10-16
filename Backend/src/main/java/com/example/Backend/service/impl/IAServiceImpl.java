package com.example.Backend.service.impl;

import com.example.Backend.dto.RecommendationDTO;
import com.example.Backend.entity.Product;
import com.example.Backend.entity.Recommendation;
import com.example.Backend.entity.User;
import com.example.Backend.entity.Vehicle;
import com.example.Backend.repository.ProductRepository;
import com.example.Backend.repository.RecommendationRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.repository.VehicleRepository;
import com.example.Backend.service.IAService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IAServiceImpl implements IAService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Recommendation> getUserRecommendations(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        return recommendationRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public List<Recommendation> generateRecommendations(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Get user's vehicles
        List<Vehicle> userVehicles = vehicleRepository.findByUserId(userId);

        // TODO: Implement ML-based recommendation algorithm
        // For now, create a simple recommendation based on vehicle compatibility

        for (Vehicle vehicle : userVehicles) {
            String compatibility = String.format("{\"brand\":\"%s\",\"model\":\"%s\",\"year\":%d}",
                    vehicle.getBrand(), vehicle.getModel(), vehicle.getYear());

            // Find compatible products - get top products for now
            List<Product> compatibleProducts = productRepository.findTopSellingProducts(PageRequest.of(0, 5))
                    .getContent();

            if (!compatibleProducts.isEmpty()) {
                Recommendation recommendation = new Recommendation();
                recommendation.setUser(user);
                recommendation.setSymptoms("Automatic recommendation based on vehicle: " +
                        vehicle.getBrand() + " " + vehicle.getModel());
                recommendation.setAiResponse("We recommend products compatible with your " +
                        vehicle.getBrand() + " " + vehicle.getModel());

                // Build suggested products JSON
                StringBuilder suggestedProductsJson = new StringBuilder("[");
                for (int i = 0; i < Math.min(5, compatibleProducts.size()); i++) {
                    if (i > 0)
                        suggestedProductsJson.append(",");
                    suggestedProductsJson.append("\"").append(compatibleProducts.get(i).getId()).append("\"");
                }
                suggestedProductsJson.append("]");

                recommendation.setSuggestedProducts(suggestedProductsJson.toString());
                recommendation.setConfidenceScore(0.75);
                recommendation.setCreatedAt(LocalDateTime.now());

                recommendationRepository.save(recommendation);
            }
        }

        return recommendationRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Recommendation createRecommendation(UUID userId, RecommendationDTO recommendationDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Recommendation recommendation = new Recommendation();
        recommendation.setUser(user);
        recommendation.setSymptoms(recommendationDTO.getReason());

        // If productId is provided, create recommendation for that product
        if (recommendationDTO.getProductId() != null) {
            recommendation.setSuggestedProducts("[\"" + recommendationDTO.getProductId() + "\"]");
        }

        recommendation.setConfidenceScore(recommendationDTO.getScore() != null ? recommendationDTO.getScore() : 0.5);
        recommendation.setCreatedAt(LocalDateTime.now());

        return recommendationRepository.save(recommendation);
    }

    @Override
    @Transactional(readOnly = true)
    public Recommendation getRecommendationById(UUID recommendationId) {
        return recommendationRepository.findById(recommendationId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Recommendation not found with id: " + recommendationId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recommendation> getActiveRecommendations(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        // Return high confidence recommendations (>= 0.7)
        return recommendationRepository.findUserHighConfidenceRecommendations(userId, 0.7);
    }

    @Override
    @Transactional
    public Recommendation markAsViewed(UUID recommendationId) {
        Recommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Recommendation not found with id: " + recommendationId));

        // Since entity doesn't have viewed field, just return the recommendation
        // In production, you might want to add a viewed field to the entity
        return recommendation;
    }

    @Override
    @Transactional
    public String analyzePartImage(String imageData, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // TODO: Integrate with Flask API for image recognition
        // For now, create a mock response

        Recommendation recommendation = new Recommendation();
        recommendation.setUser(user);
        recommendation.setImageUrl("data:image/base64," + imageData.substring(0, Math.min(50, imageData.length())));
        recommendation.setSymptoms("Image analysis request");
        recommendation
                .setAiResponse("Image analysis in progress. Please check back later for AI-powered part recognition.");
        recommendation.setConfidenceScore(0.5);
        recommendation.setCreatedAt(LocalDateTime.now());

        recommendationRepository.save(recommendation);

        return "Image uploaded successfully. AI analysis will be available shortly. " +
                "Recommendation ID: " + recommendation.getId();
    }

    @Override
    @Transactional
    public String virtualMechanicChat(UUID userId, String question) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // TODO: Integrate with Flask API for AI chat
        // For now, create a simple response

        Recommendation recommendation = new Recommendation();
        recommendation.setUser(user);
        recommendation.setSymptoms(question);

        // Simple keyword-based response (replace with AI integration)
        String aiResponse = generateSimpleResponse(question);
        recommendation.setAiResponse(aiResponse);
        recommendation.setConfidenceScore(0.6);
        recommendation.setCreatedAt(LocalDateTime.now());

        recommendationRepository.save(recommendation);

        return aiResponse;
    }

    /**
     * Generate a simple response based on keywords
     * TODO: Replace with actual AI integration (Flask API)
     */
    private String generateSimpleResponse(String question) {
        String lowerQuestion = question.toLowerCase();

        if (lowerQuestion.contains("brake") || lowerQuestion.contains("frein")) {
            return "Brake issues can be serious. I recommend checking your brake pads and brake fluid level. " +
                    "If you hear squeaking or grinding noises, your brake pads may need replacement.";
        } else if (lowerQuestion.contains("engine") || lowerQuestion.contains("moteur")) {
            return "Engine problems can have various causes. Check your oil level, coolant, and listen for unusual sounds. "
                    +
                    "Regular maintenance is key to preventing engine issues.";
        } else if (lowerQuestion.contains("oil") || lowerQuestion.contains("huile")) {
            return "Oil changes should be performed every 5,000-7,500 km depending on your vehicle. " +
                    "Always use the oil grade recommended in your owner's manual.";
        } else if (lowerQuestion.contains("tire") || lowerQuestion.contains("pneu")) {
            return "Check your tire pressure monthly and inspect for wear. Replace tires when tread depth is below 2mm. "
                    +
                    "Rotate tires every 10,000 km for even wear.";
        } else {
            return "Thank you for your question. Our AI mechanic is learning. " +
                    "For immediate assistance, please contact our support team or visit our parts catalog.";
        }
    }
}
