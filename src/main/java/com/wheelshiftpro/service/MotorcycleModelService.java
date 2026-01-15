package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.MotorcycleModelRequest;
import com.wheelshiftpro.dto.response.MotorcycleModelResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.enums.FuelType;
import com.wheelshiftpro.enums.MotorcycleVehicleType;

import java.util.List;

/**
 * Service interface for MotorcycleModel operations.
 * Handles business logic for motorcycle model management.
 */
public interface MotorcycleModelService {

    /**
     * Creates a new motorcycle model.
     * Validates uniqueness of make, model, and variant combination.
     *
     * @param request the motorcycle model data
     * @return created motorcycle model response
     * @throws com.wheelshiftpro.exception.DuplicateResourceException if combination already exists
     */
    MotorcycleModelResponse createMotorcycleModel(MotorcycleModelRequest request);

    /**
     * Updates an existing motorcycle model.
     *
     * @param id the motorcycle model ID
     * @param request updated motorcycle model data
     * @return updated motorcycle model response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if motorcycle model not found
     */
    MotorcycleModelResponse updateMotorcycleModel(Long id, MotorcycleModelRequest request);

    /**
     * Retrieves a motorcycle model by ID.
     *
     * @param id the motorcycle model ID
     * @return motorcycle model response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if motorcycle model not found
     */
    MotorcycleModelResponse getMotorcycleModelById(Long id);

    /**
     * Retrieves all motorcycle models with pagination.
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @return paginated motorcycle model responses
     */
    PageResponse<MotorcycleModelResponse> getAllMotorcycleModels(int page, int size);

    /**
     * Deletes a motorcycle model by ID.
     * Validates that no motorcycles are associated with this model.
     *
     * @param id the motorcycle model ID
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if motorcycle model not found
     * @throws com.wheelshiftpro.exception.BusinessException if motorcycles are associated
     */
    void deleteMotorcycleModel(Long id);

    /**
     * Searches motorcycle models by multiple criteria.
     *
     * @param make optional make filter
     * @param model optional model filter
     * @param fuelType optional fuel type filter
     * @param vehicleType optional vehicle type filter
     * @param page page number
     * @param size page size
     * @return paginated search results
     */
    PageResponse<MotorcycleModelResponse> searchMotorcycleModels(String make, String model, FuelType fuelType, 
                                                                   MotorcycleVehicleType vehicleType, int page, int size);

    /**
     * Retrieves all unique makes.
     *
     * @return list of unique makes
     */
    List<String> getAllMakes();

    /**
     * Retrieves all models for a specific make.
     *
     * @param make the make name
     * @return list of models
     */
    List<String> getModelsByMake(String make);

    /**
     * Retrieves all variants for a specific make and model.
     *
     * @param make the make name
     * @param model the model name
     * @return list of variants
     */
    List<String> getVariantsByMakeAndModel(String make, String model);

    /**
     * Retrieves all available fuel types.
     *
     * @return list of fuel types
     */
    List<FuelType> getAllFuelTypes();

    /**
     * Retrieves all available vehicle types.
     *
     * @return list of vehicle types
     */
    List<MotorcycleVehicleType> getAllVehicleTypes();

    /**
     * Retrieves all active motorcycle models.
     *
     * @return list of active models
     */
    List<MotorcycleModelResponse> getActiveModels();

    /**
     * Checks if a motorcycle model exists.
     *
     * @param id the motorcycle model ID
     * @return true if exists, false otherwise
     */
    boolean existsById(Long id);
}
