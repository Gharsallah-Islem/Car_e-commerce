package com.integmobile.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig {
    
    @Value("\${cors.allowed-origins}")
    private lateinit var allowedOrigins: String
    
    @Value("\${cors.allowed-methods}")
    private lateinit var allowedMethods: String
    
    @Value("\${cors.allowed-headers}")
    private lateinit var allowedHeaders: String
    
    @Value("\${cors.allow-credentials}")
    private var allowCredentials: Boolean = true
    
    @Bean
    fun corsFilter(): CorsFilter {
        val config = CorsConfiguration()
        config.allowedOrigins = allowedOrigins.split(",")
        config.allowedMethods = allowedMethods.split(",")
        config.allowedHeaders = listOf(allowedHeaders)
        config.allowCredentials = allowCredentials
        config.maxAge = 3600
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        
        return CorsFilter(source)
    }
}
