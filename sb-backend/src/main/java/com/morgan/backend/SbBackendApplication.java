package com.morgan.backend;

import com.morgan.backend.config.CorsConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@EnableConfigurationProperties(CorsConfigProperties.class)
@SpringBootApplication
public class SbBackendApplication {
    static void main(String[] args) {
        SpringApplication.run(SbBackendApplication.class, args);
    }
}
