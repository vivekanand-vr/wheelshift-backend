package com.wheelshiftpro.client;

import com.wheelshiftpro.dto.ai.SimilarVehiclesResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Optional;

/**
 * HTTP client for the WheelShift AI service.
 *
 * <p>All methods return {@link Optional#empty()} on any error so callers
 * can degrade gracefully — the AI service is non-critical and must never
 * cause a 500 response to end clients.</p>
 */
@Component
@Slf4j
public class AIServiceClient {

    private final WebClient webClient;
    private final boolean enabled;
    private final Duration timeout;

    public AIServiceClient(
            @Qualifier("aiServiceWebClient") WebClient webClient,
            @Value("${ai.service.enabled:true}") boolean enabled,
            @Value("${ai.service.timeout-seconds:3}") int timeoutSeconds) {
        this.webClient = webClient;
        this.enabled = enabled;
        this.timeout = Duration.ofSeconds(timeoutSeconds);
    }

    /**
     * Fetch hybrid similar vehicles (collaborative + content-based) from the AI service.
     *
     * @param vehicleId source vehicle ID
     * @param type      "car" or "motorcycle"
     * @param limit     max suggestions to return (1-50)
     * @return AI response, or empty on any error / when disabled
     */
    public Optional<SimilarVehiclesResponseDto> getSimilarVehicles(
            Long vehicleId, String type, int limit) {

        if (!enabled) {
            log.debug("AI service is disabled — skipping similarity lookup for {}/{}", type, vehicleId);
            return Optional.empty();
        }

        try {
            SimilarVehiclesResponseDto response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/ai/vehicles/similar")
                            .queryParam("vehicleId", vehicleId)
                            .queryParam("type", type)
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                    .bodyToMono(SimilarVehiclesResponseDto.class)
                    .timeout(timeout)
                    .block();

            return Optional.ofNullable(response);

        } catch (WebClientResponseException e) {
            log.warn("AI service returned {} for hybrid similarity {}/{}: {}",
                    e.getStatusCode(), type, vehicleId, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("AI service unavailable for hybrid similarity {}/{}: {}",
                    type, vehicleId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Fetch content-based similar vehicles only (bypasses collaborative filtering).
     *
     * @param vehicleId source vehicle ID
     * @param type      "car" or "motorcycle"
     * @param limit     max suggestions to return (1-50)
     * @return AI response, or empty on any error / when disabled
     */
    public Optional<SimilarVehiclesResponseDto> getContentBasedSimilarVehicles(
            Long vehicleId, String type, int limit) {

        if (!enabled) {
            return Optional.empty();
        }

        try {
            SimilarVehiclesResponseDto response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/ai/vehicles/similar/content")
                            .queryParam("vehicleId", vehicleId)
                            .queryParam("type", type)
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                    .bodyToMono(SimilarVehiclesResponseDto.class)
                    .timeout(timeout)
                    .block();

            return Optional.ofNullable(response);

        } catch (Exception e) {
            log.warn("AI service (content) unavailable for {}/{}: {}", type, vehicleId, e.getMessage());
            return Optional.empty();
        }
    }
}
