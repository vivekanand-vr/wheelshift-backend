package com.wheelshiftpro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for the WheelShift AI service HTTP client.
 * Attaches the shared API key to every outbound request.
 */
@Configuration
public class AIServiceConfig {

    @Value("${ai.service.base-url}")
    private String baseUrl;

    @Value("${ai.service.api-key}")
    private String apiKey;

    @Bean("aiServiceWebClient")
    public WebClient aiServiceWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-API-Key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
