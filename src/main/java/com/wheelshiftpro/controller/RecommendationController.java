package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.SimilarCarsResponse;
import com.wheelshiftpro.dto.response.SimilarMotorcyclesResponse;
import com.wheelshiftpro.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Validated
@Tag(name = "AI Recommendations", description = "AI-powered vehicle similarity recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/cars/{id}/similar")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get similar cars",
            description = "Returns cars similar to the given car, ranked by the AI service using hybrid " +
                    "(collaborative + content-based) filtering. Falls back to an empty result gracefully " +
                    "if the AI service is unavailable."
    )
    public ResponseEntity<ApiResponse<SimilarCarsResponse>> getSimilarCars(
            @Parameter(description = "Source car ID") @PathVariable Long id,
            @Parameter(description = "Maximum number of similar cars to return (1-20)")
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit) {
        SimilarCarsResponse response = recommendationService.getSimilarCars(id, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/motorcycles/{id}/similar")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get similar motorcycles",
            description = "Returns motorcycles similar to the given motorcycle, ranked by the AI service " +
                    "using hybrid (collaborative + content-based) filtering. Falls back to an empty result " +
                    "gracefully if the AI service is unavailable."
    )
    public ResponseEntity<ApiResponse<SimilarMotorcyclesResponse>> getSimilarMotorcycles(
            @Parameter(description = "Source motorcycle ID") @PathVariable Long id,
            @Parameter(description = "Maximum number of similar motorcycles to return (1-20)")
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit) {
        SimilarMotorcyclesResponse response = recommendationService.getSimilarMotorcycles(id, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
