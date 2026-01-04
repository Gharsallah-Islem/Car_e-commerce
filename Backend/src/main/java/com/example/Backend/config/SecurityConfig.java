package com.example.Backend.config;

import com.example.Backend.security.CustomOAuth2UserService;
import com.example.Backend.security.JwtAuthenticationFilter;
import com.example.Backend.security.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/dev/**").permitAll() // TEMPORARY - for password hash generation
                        .requestMatchers("/api/products/**").permitAll()
                        .requestMatchers("/api/categories/**").permitAll()
                        .requestMatchers("/api/brands/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Recommendation endpoints (trending & similar are public)
                        .requestMatchers("/api/recommendations/trending").permitAll()
                        .requestMatchers("/api/recommendations/similar/**").permitAll()
                        .requestMatchers("/api/recommendations/also-bought/**").permitAll()

                        // Activity tracking (requires auth - handled by controller)

                        // OAuth2 endpoints
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/login/oauth2/**").permitAll()

                        // Payment webhook endpoint (Stripe webhooks)
                        .requestMatchers("/api/payments/webhook").permitAll()

                        // Delivery tracking (public for customers)
                        .requestMatchers("/api/delivery/track/**").permitAll()
                        .requestMatchers("/api/delivery/migrate-shipped-orders").permitAll()
                        .requestMatchers("/api/delivery/debug-orders").permitAll()
                        .requestMatchers("/api/delivery/sync-tracking-numbers").permitAll()

                        // Driver public endpoints
                        .requestMatchers("/api/drivers/statistics").permitAll()

                        // WebSocket endpoints (authentication handled internally)
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/ws-native/**").permitAll()

                        // Actuator endpoints (health check)
                        .requestMatchers("/actuator/**").permitAll()

                        // Swagger/OpenAPI documentation
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        // Admin endpoints
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/reports/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // User endpoints
                        .requestMatchers("/api/cart/**").hasRole("CLIENT")
                        .requestMatchers("/api/orders/**").authenticated()
                        .requestMatchers("/api/vehicles/**").hasRole("CLIENT")

                        // Support endpoints
                        .requestMatchers("/api/reclamations/**").authenticated()
                        .requestMatchers("/api/chat/**").authenticated()

                        // All other endpoints require authentication
                        .anyRequest().authenticated())

                // OAuth2 Login Configuration
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler))

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
