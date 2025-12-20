package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.CarModelRequest;
import com.wheelshiftpro.dto.response.CarModelResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.enums.FuelType;

import java.util.List;

/**
 * Service interface for CarModel operations.
 * Handles business logic for vehicle model management.
 */
public interface CarModelService {

    /**
     * Creates a new car model.
     * Validates uniqueness of make, model, and variant combination.
     *
     * @param request the car model data
     * @return created car model response
     * @throws com.wheelshiftpro.exception.DuplicateResourceException if combination already exists
     */
    CarModelResponse createCarModel(CarModelRequest request);

    /**
     * Updates an existing car model.
     *
     * @param id the car model ID
     * @param request updated car model data
     * @return updated car model response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if car model not found
     */
    CarModelResponse updateCarModel(Long id, CarModelRequest request);

    /**
     * Retrieves a car model by ID.
     *
     * @param id the car model ID
     * @return car model response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if car model not found
     */
    CarModelResponse getCarModelById(Long id);

    /**
     * Retrieves all car models with pagination.
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @return paginated car model responses
     */
    PageResponse<CarModelResponse> getAllCarModels(int page, int size);

    /**
     * Deletes a car model by ID.
     * Validates that no cars are associated with this model.
     *
     * @param id the car model ID
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if car model not found
     * @throws com.wheelshiftpro.exception.BusinessException if cars are associated
     */
    void deleteCarModel(Long id);

    /**
     * Searches car models by multiple criteria.
     *
     * @param make optional make filter
     * @param model optional model filter
     * @param fuelType optional fuel type filter
     * @param bodyType optional body type filter
     * @param page page number
     * @param size page size
     * @return paginated search results
     */
    PageResponse<CarModelResponse> searchCarModels(String make, String model, FuelType fuelType, 
                                                    String bodyType, int page, int size);

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
     * Retrieves all unique fuel types.
     *
     * @return list of fuel types
     */
    List<FuelType> getAllFuelTypes();

    /**
     * Retrieves all unique body types.
     *
     * @return list of body types
     */
    List<String> getAllBodyTypes();
}
