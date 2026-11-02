package com.neobank.neobank_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Autoriser les origines (React, Flutter web, etc.)
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",   // React Vite
                "http://localhost:3000",   // React CRA
                "http://localhost:8080",   // Backend
                "http://127.0.0.1:5173",
                "http://127.0.0.1:3000"
        ));

        // Autoriser toutes les méthodes HTTP
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Autoriser tous les headers
        config.setAllowedHeaders(List.of("*"));

        // Autoriser l'envoi de cookies/credentials
        config.setAllowCredentials(true);

        // Exposer les headers personnalisés
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Access-Control-Allow-Origin"
        ));

        // Durée de cache du preflight (1 heure)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}