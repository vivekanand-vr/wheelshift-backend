package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.CarRequest;
import com.wheelshiftpro.dto.response.CarResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.enums.CarStatus;

import java.math.BigDecimal;

/**
 * Service interface for Car operations.
 * Manages vehicle inventory and lifecycle.
 */
public interface CarService {

    /**
     * Creates a new car in inventory.
     * Validates VIN uniqueness and storage location capacity.
     *
     * @param request the car data
     * @return created car response
     * @throws com.wheelshiftpro.exception.DuplicateResourceException if VIN exists
     * @throws com.wheelshiftpro.exception.BusinessException if storage capacity exceeded
     */
    CarResponse createCar(CarRequest request);

    /**
     * Updates an existing car.
     * Validates business rules for status changes.
     *
     * @param id the car ID
     * @param request updated car data
     * @return updated car response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if car not found
     * @throws com.wheelshiftpro.exception.BusinessException if business rules violated
     */
    CarResponse updateCar(Long id, CarRequest request);

    /**
     * Retrieves a car by ID with full details.
     *
     * @param id the car ID
     * @return car response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if car not found
     */
    CarResponse getCarById(Long id);

    /**
     * Retrieves a car by VIN number.
     *
     * @param vinNumber the VIN
     * @return car response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if car not found
     */
    CarResponse getCarByVin(String vinNumber);

    /**
     * Retrieves a car by registration number.
     *
     * @param registrationNumber the registration number
     * @return car response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if car not found
     */
    CarResponse getCarByRegistration(String registrationNumber);

    /**
     * Retrieves all cars with pagination.
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @return paginated car responses
     */
    PageResponse<CarResponse> getAllCars(int page, int size);

    /**
     * Searches cars with multiple filters.
     *
     * @param status optional status filter
     * @param locationId optional location filter
     * @param make optional make filter
     * @param model optional model filter
     * @param minYear optional minimum year
     * @param maxYear optional maximum year
     * @param minPrice optional minimum price
     * @param maxPrice optional maximum price
     * @param page page number
     * @param size page size
     * @return paginated search results
     */
    PageResponse<CarResponse> searchCars(CarStatus status, Long locationId, String make, 
                                         String model, Integer minYear, Integer maxYear,
                                         BigDecimal minPrice, BigDecimal maxPrice, 
                                         int page, int size);

    /**
     * Updates car status.
     * Validates status transitions.
     *
     * @param id the car ID
     * @param status new status
     * @return updated car response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if car not found
     * @throws com.wheelshiftpro.exception.BusinessException if status transition invalid
     */
    CarResponse updateCarStatus(Long id, CarStatus status);

    /**
     * Moves a car to a different storage location.
     * Validates capacity and records movement.
     *
     * @param carId the car ID
     * @param newLocationId new storage location ID
     * @return updated car response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if car or location not found
     * @throws com.wheelshiftpro.exception.BusinessException if capacity exceeded
     */
    CarResponse moveCarToLocation(Long carId, Long newLocationId);

    /**
     * Deletes a car from inventory.
     * Only AVAILABLE cars can be deleted (soft delete).
     *
     * @param id the car ID
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if car not found
     * @throws com.wheelshiftpro.exception.BusinessException if car cannot be deleted
     */
    void deleteCar(Long id);

    /**
     * Retrieves inventory statistics grouped by status.
     *
     * @return statistics map
     */
    Object getInventoryStatistics();

    /**
     * Calculates total inventory value.
     *
     * @return total value
     */
    BigDecimal calculateInventoryValue();
}
