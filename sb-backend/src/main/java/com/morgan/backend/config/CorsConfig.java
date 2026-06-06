package com.morgan.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Enables CORS for scenarios where the frontend calls the backend directly
 * (e.g., Angular dev server without the proxy on port 4200, hitting localhost:8090).
 */
@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(CorsConfig.CorsConfigProperties.class)
public class CorsConfig implements WebMvcConfigurer {

    private final CorsConfigProperties corsConfigProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (corsConfigProperties.allowedOrigins().isEmpty()) {
            return; // nothing configured → no CORS mappings
        }

        registry.addMapping("/api/**")
            .allowedOrigins(corsConfigProperties.allowedOrigins().toArray(String[]::new)) // Access-Control-Allow-Origin: http://localhost:4200
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("Content-Type", "X-XSRF-TOKEN")
            .allowCredentials(true) // Access-Control-Allow-Credentials: true
            .maxAge(3600); // how long in seconds the response from a pre-flight request can be cached by clients
    }

    /**
     * CORS configuration properties.
     * Bound from `app.cors.*` in application.yml.
     * Reads the list from app.cors.allowed-origins in application.yml
     */
    @ConfigurationProperties(prefix = "app.cors")
    record CorsConfigProperties(List<String> allowedOrigins) {
        public CorsConfigProperties {
            if (allowedOrigins == null) {
                allowedOrigins = List.of(); // we don't want for allowedOrigins to ever be null
            }
        }
    }
}
