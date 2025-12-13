package com.morgan.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * CORS configuration properties.
 * Bound from `app.cors.*` in application.yml.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.cors")
public class CorsConfigProperties {
    private List<String> allowedOrigins = List.of(); // Reads the list from app.cors.allowed-origins in application.yml
}
