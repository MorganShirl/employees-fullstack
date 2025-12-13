package com.morgan.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Enables CORS for scenarios where the frontend calls the backend directly
 * (e.g., Angular dev server without the proxy on port 4200, hitting localhost:8090).
 */
@RequiredArgsConstructor
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final CorsConfigProperties corsConfigProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (isEmpty(corsConfigProperties.getAllowedOrigins())) {
            return; // nothing configured → no CORS mappings
        }

        registry.addMapping("/api/**")
            .allowedOrigins(corsConfigProperties.getAllowedOrigins().toArray(String[]::new)) // Access-Control-Allow-Origin: http://localhost:4200
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true) // Access-Control-Allow-Credentials: true
            .maxAge(3600); // how long in seconds the response from a pre-flight request can be cached by clients
    }
}
