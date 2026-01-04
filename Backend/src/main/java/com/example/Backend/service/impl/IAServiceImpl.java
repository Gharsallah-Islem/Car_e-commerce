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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IAServiceImpl implements IAService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.module.url:http://localhost:5000}")
    private String aiModuleUrl;

    @Value("${ai.module.timeout:30000}")
    private int aiModuleTimeout;

    public IAServiceImpl(
            RecommendationRepository recommendationRepository,
            UserRepository userRepository,
            VehicleRepository vehicleRepository,
            ProductRepository productRepository) {
        this.recommendationRepository = recommendationRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.productRepository = productRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

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

        log.info("Analyzing part image for user: {}", userId);

        String partName = "Unknown Part";
        double confidence = 0.0;
        List<Product> matchedProducts = List.of();
        String aiResponse;

        try {
            // Call AI module API
            String aiUrl = aiModuleUrl + "/api/v1/visual-search/predict";
            log.info("Calling AI module at: {}", aiUrl);

            // Create request body
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("image", imageData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            // Make the API call
            ResponseEntity<String> response = restTemplate.postForEntity(aiUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Parse the response
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());

                if (jsonResponse.has("success") && jsonResponse.get("success").asBoolean()) {
                    partName = jsonResponse.get("top_prediction").asText("Unknown");
                    confidence = jsonResponse.get("confidence").asDouble(0.0);

                    log.info("AI prediction: {} with confidence {:.2f}", partName, confidence);

                    // Search for matching products in the catalog
                    matchedProducts = productRepository.findByNameContaining(partName);

                    // If no exact match, try searching by keywords
                    if (matchedProducts.isEmpty()) {
                        // Try partial match - e.g., "BRAKE PAD" -> search for "brake" or "pad"
                        String[] keywords = partName.toLowerCase().replace("_", " ").split(" ");
                        for (String keyword : keywords) {
                            if (keyword.length() > 3) { // Skip short words
                                matchedProducts = productRepository.findByNameContaining(keyword);
                                if (!matchedProducts.isEmpty())
                                    break;
                            }
                        }
                    }

                    // Limit to top 5 products
                    if (matchedProducts.size() > 5) {
                        matchedProducts = matchedProducts.subList(0, 5);
                    }

                    aiResponse = buildAiResponseMessage(partName, confidence, matchedProducts);
                } else {
                    String errorMsg = jsonResponse.has("detail") ? jsonResponse.get("detail").asText()
                            : "Unknown error";
                    log.warn("AI module returned error: {}", errorMsg);
                    aiResponse = "Could not identify the part. Please try with a clearer image.";
                }
            } else {
                log.warn("AI module returned non-OK status: {}", response.getStatusCode());
                aiResponse = "AI service temporarily unavailable. Please try again later.";
            }

        } catch (Exception e) {
            log.error("Error calling AI module: {}", e.getMessage(), e);
            aiResponse = "AI analysis temporarily unavailable. Error: " + e.getMessage();
        }

        // Save recommendation to database
        Recommendation recommendation = new Recommendation();
        recommendation.setUser(user);
        recommendation.setImageUrl("uploaded_image");
        recommendation.setSymptoms("Image analysis request - Part: " + partName);
        recommendation.setAiResponse(aiResponse);
        recommendation.setConfidenceScore(confidence);
        recommendation.setCreatedAt(LocalDateTime.now());

        // Save matched product IDs
        if (!matchedProducts.isEmpty()) {
            String productIds = matchedProducts.stream()
                    .map(p -> "\"" + p.getId() + "\"")
                    .collect(Collectors.joining(",", "[", "]"));
            recommendation.setSuggestedProducts(productIds);
        }

        recommendationRepository.save(recommendation);

        // Build JSON response for frontend
        return buildJsonResponse(partName, confidence, matchedProducts, recommendation.getId());
    }

    /**
     * Build a user-friendly response message
     */
    private String buildAiResponseMessage(String partName, double confidence, List<Product> products) {
        StringBuilder sb = new StringBuilder();

        String formattedName = partName.replace("_", " ");
        sb.append("I identified this as a **").append(formattedName).append("** ");
        sb.append("with ").append(String.format("%.1f%%", confidence * 100)).append(" confidence.\n\n");

        if (!products.isEmpty()) {
            sb.append("We have ").append(products.size()).append(" matching product(s) in stock:\n");
            for (Product p : products) {
                sb.append("• ").append(p.getName());
                if (p.getPrice() != null) {
                    sb.append(" - ").append(String.format("%.2f €", p.getPrice()));
                }
                sb.append("\n");
            }
        } else {
            sb.append("Unfortunately, we don't have this exact part in stock. ");
            sb.append("Would you like me to help you find an alternative?");
        }

        return sb.toString();
    }

    /**
     * Build JSON response for the frontend
     */
    private String buildJsonResponse(String partName, double confidence, List<Product> products,
            UUID recommendationId) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("partName", partName.replace("_", " "));
            response.put("confidence", confidence);
            response.put("confidencePercent", String.format("%.1f%%", confidence * 100));
            response.put("recommendationId", recommendationId.toString());

            List<Map<String, Object>> productList = products.stream().map(p -> {
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("id", p.getId());
                productMap.put("name", p.getName());
                productMap.put("price", p.getPrice());
                productMap.put("stock", p.getStock());
                return productMap;
            }).collect(Collectors.toList());

            response.put("products", productList);
            response.put("productsFound", !products.isEmpty());

            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Error building JSON response: {}", e.getMessage());
            return "{\"success\": true, \"partName\": \"" + partName + "\", \"confidence\": " + confidence
                    + ", \"products\": []}";
        }
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
