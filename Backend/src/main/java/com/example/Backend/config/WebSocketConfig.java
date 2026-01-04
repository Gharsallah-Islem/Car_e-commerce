package com.example.Backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time delivery tracking
 * Uses STOMP over SockJS for browser compatibility
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for topics that clients subscribe to
        config.enableSimpleBroker(
                "/topic", // For broadcast messages (e.g., /topic/delivery/{id})
                "/queue" // For private messages (e.g., /queue/driver/{id})
        );

        // Set prefix for messages FROM clients TO server
        config.setApplicationDestinationPrefixes("/app");

        // Set prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Primary WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Pure WebSocket endpoint (for native clients)
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }
}
