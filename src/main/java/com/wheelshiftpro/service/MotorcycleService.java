package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.MotorcycleRequest;
import com.wheelshiftpro.dto.response.MotorcycleResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.enums.MotorcycleStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for motorcycle management operations.
 */
public interface MotorcycleService {

    /**
     * Create a new motorcycle
     */
    MotorcycleResponse createMotorcycle(MotorcycleRequest request);

    /**
     * Get motorcycle by ID
     */
    MotorcycleResponse getMotorcycleById(Long id);

    /**
     * Get motorcycle by VIN number
     */
    MotorcycleResponse getMotorcycleByVin(String vinNumber);

    /**
     * Get motorcycle by registration number
     */
    MotorcycleResponse getMotorcycleByRegistration(String registrationNumber);

    /**
     * Get all motorcycles with pagination
     */
    PageResponse<MotorcycleResponse> getAllMotorcycles(int page, int size, String sortBy, String sortDir);

    /**
     * Get motorcycles by status
     */
    List<MotorcycleResponse> getMotorcyclesByStatus(MotorcycleStatus status);

    /**
     * Get motorcycles by storage location
     */
    List<MotorcycleResponse> getMotorcyclesByStorageLocation(Long storageLocationId);

    /**
     * Get motorcycles by model
     */
    List<MotorcycleResponse> getMotorcyclesByModel(Long motorcycleModelId);

    /**
     * Search motorcycles
     */
    PageResponse<MotorcycleResponse> searchMotorcycles(String searchTerm, int page, int size);

    /**
     * Get motorcycles by price range
     */
    PageResponse<MotorcycleResponse> getMotorcyclesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page, int size);

    /**
     * Get available motorcycles
     */
    List<MotorcycleResponse> getAvailableMotorcycles();

    /**
     * Get motorcycles needing attention
     */
    List<MotorcycleResponse> getMotorcyclesNeedingAttention();

    /**
     * Get recently added motorcycles
     */
    List<MotorcycleResponse> getRecentlyAddedMotorcycles(int limit);

    /**
     * Update motorcycle
     */
    MotorcycleResponse updateMotorcycle(Long id, MotorcycleRequest request);

    /**
     * Update motorcycle status
     */
    MotorcycleResponse updateMotorcycleStatus(Long id, MotorcycleStatus status);

    /**
     * Move motorcycle to a different storage location
     */
    MotorcycleResponse moveMotorcycleToLocation(Long motorcycleId, Long locationId);

    /**
     * Delete motorcycle
     */
    void deleteMotorcycle(Long id);

    /**
     * Count motorcycles by status
     */
    Long countMotorcyclesByStatus(MotorcycleStatus status);

    /**
     * Get total inventory value
     */
    BigDecimal getTotalInventoryValue();

    /**
     * Get average selling price
     */
    BigDecimal getAverageSellingPrice();

    /**
     * Check if VIN exists
     */
    boolean existsByVin(String vinNumber);

    /**
     * Check if registration exists
     */
    boolean existsByRegistration(String registrationNumber);
}
