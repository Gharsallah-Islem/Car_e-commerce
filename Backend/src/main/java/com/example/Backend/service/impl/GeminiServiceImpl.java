package com.example.Backend.service.impl;

import com.example.Backend.entity.Product;
import com.example.Backend.service.GeminiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GeminiServiceImpl implements GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generateResponse(String userMessage, String conversationHistory) {
        String systemPrompt = buildSystemPrompt();
        String fullPrompt = buildPrompt(systemPrompt, userMessage, conversationHistory, null);
        return callGeminiAPI(fullPrompt);
    }

    @Override
    public String generateResponseWithProducts(String userMessage, List<Product> products, String conversationHistory) {
        String systemPrompt = buildSystemPrompt();
        String productContext = buildProductContext(products);
        String fullPrompt = buildPrompt(systemPrompt, userMessage, conversationHistory, productContext);
        return callGeminiAPI(fullPrompt);
    }

    @Override
    public String generateStockResponse(String userMessage, String productName) {
        String systemPrompt = buildSystemPrompt();
        String stockPrompt = String.format(
                "%s\\n\\nUser is asking about: %s\\nProvide information about stock availability and help them find the product.",
                systemPrompt, productName);
        return callGeminiAPI(stockPrompt + "\\n\\nUser: " + userMessage);
    }

    /**
     * Build the system prompt that defines the AI's role and behavior
     */
    private String buildSystemPrompt() {
        return """
                You are an intelligent AI assistant for an automotive spare parts e-commerce platform in Tunisia.
                Your role combines being a virtual mechanic and stock manager.

                Your responsibilities:
                1. VIRTUAL MECHANIC:
                   - Help customers identify the right spare parts for their vehicles
                   - Provide technical advice about car parts and compatibility
                   - Explain installation procedures and maintenance tips
                   - Answer questions about part specifications and quality

                2. STOCK MANAGER:
                   - Inform customers about product availability
                   - Provide accurate stock information
                   - Suggest alternatives when items are out of stock
                   - Help customers find the best deals

                3. CUSTOMER SERVICE:
                   - Be friendly, professional, and helpful
                   - Use simple, clear language (French preferred for Tunisia)
                   - Provide accurate information based on the product catalog
                   - If you don't know something, admit it honestly

                Important guidelines:
                - All prices are in TND (Tunisian Dinar)
                - Focus on Tunisian market and customer needs
                - Recommend products based on quality, compatibility, and price
                - Always prioritize customer safety and satisfaction
                - Keep responses concise but informative (2-4 sentences usually)

                When discussing products:
                - Mention brand, price, and stock status
                - Highlight key features and benefits
                - Suggest compatible or alternative products when relevant
                """;
    }

    /**
     * Build product context from list of products
     */
    private String buildProductContext(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return "No specific products available in current context.";
        }

        StringBuilder context = new StringBuilder("\\n\\nAVAILABLE PRODUCTS:\\n");
        for (Product product : products.stream().limit(10).collect(Collectors.toList())) {
            context.append(String.format(
                    "- %s (ID: %s)\\n  Brand: %s | Category: %s | Price: %.2f TND | Stock: %d units\\n  Description: %s\\n\\n",
                    product.getName(),
                    product.getId(),
                    product.getBrand() != null ? product.getBrand().getName() : "N/A",
                    product.getCategory() != null ? product.getCategory().getName() : "N/A",
                    product.getPrice(),
                    product.getStock(),
                    product.getDescription() != null
                            ? product.getDescription().substring(0, Math.min(100, product.getDescription().length()))
                            : ""));
        }
        return context.toString();
    }

    /**
     * Build the complete prompt with all context
     */
    private String buildPrompt(String systemPrompt, String userMessage, String conversationHistory,
            String productContext) {
        StringBuilder prompt = new StringBuilder(systemPrompt);

        if (productContext != null && !productContext.isEmpty()) {
            prompt.append("\\n").append(productContext);
        }

        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            prompt.append("\\n\\nRECENT CONVERSATION:\\n").append(conversationHistory);
        }

        prompt.append("\\n\\nUser: ").append(userMessage);
        prompt.append("\\n\\nAssistant:");

        return prompt.toString();
    }

    /**
     * Call Gemini API and get response
     */
    private String callGeminiAPI(String prompt) {
        try {
            // Build request body for Gemini API
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();

            part.put("text", prompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));

            // Add generation config for better responses
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 1024);
            requestBody.put("generationConfig", generationConfig);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request entity
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Make API call
            String url = apiUrl + "?key=" + apiKey;
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class);

            // Parse response
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseGeminiResponse(response.getBody());
            } else {
                log.error("Gemini API returned non-OK status: {}", response.getStatusCode());
                return "Désolé, je ne peux pas répondre pour le moment. Veuillez réessayer.";
            }

        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            return "Désolé, une erreur s'est produite. Un agent humain vous répondra bientôt.";
        }
    }

    /**
     * Parse Gemini API response to extract the generated text
     */
    private String parseGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    return text.trim();
                }
            }

            log.warn("Could not parse Gemini response, unexpected format");
            return "Désolé, je n'ai pas pu générer une réponse appropriée.";

        } catch (Exception e) {
            log.error("Error parsing Gemini response", e);
            return "Désolé, une erreur s'est produite lors du traitement de la réponse.";
        }
    }
}
