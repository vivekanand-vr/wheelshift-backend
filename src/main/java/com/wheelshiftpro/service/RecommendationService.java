package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.response.SimilarCarsResponse;
import com.wheelshiftpro.dto.response.SimilarMotorcyclesResponse;

/**
 * Service interface for AI-powered vehicle recommendations.
 */
public interface RecommendationService {

    /**
     * Returns cars similar to the given car, ranked by the AI service.
     * If the AI service is unavailable, returns an empty result — never throws.
     *
     * @param carId source car ID
     * @param limit max results (1-20)
     * @return enriched similar-cars response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if the source car does not exist
     */
    SimilarCarsResponse getSimilarCars(Long carId, int limit);

    /**
     * Returns motorcycles similar to the given motorcycle, ranked by the AI service.
     * If the AI service is unavailable, returns an empty result — never throws.
     *
     * @param motorcycleId source motorcycle ID
     * @param limit        max results (1-20)
     * @return enriched similar-motorcycles response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if the source motorcycle does not exist
     */
    SimilarMotorcyclesResponse getSimilarMotorcycles(Long motorcycleId, int limit);
}
