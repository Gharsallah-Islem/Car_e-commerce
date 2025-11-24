package com.example.Backend.service;

import com.example.Backend.entity.Product;

import java.util.List;

/**
 * Service interface for Gemini AI integration
 * Provides intelligent responses for customer support and product queries
 */
public interface GeminiService {

    /**
     * Generate AI response for user message
     * 
     * @param userMessage         The user's message/question
     * @param conversationHistory Recent conversation context (optional)
     * @return AI-generated response
     */
    String generateResponse(String userMessage, String conversationHistory);

    /**
     * Generate AI response with product context
     * Used when user asks about specific products or needs recommendations
     * 
     * @param userMessage         The user's message/question
     * @param products            List of relevant products to include in context
     * @param conversationHistory Recent conversation context (optional)
     * @return AI-generated response with product recommendations
     */
    String generateResponseWithProducts(String userMessage, List<Product> products, String conversationHistory);

    /**
     * Generate AI response for stock/inventory queries
     * 
     * @param userMessage The user's message/question
     * @param productName Product name to check
     * @return AI-generated response about stock status
     */
    String generateStockResponse(String userMessage, String productName);
}
