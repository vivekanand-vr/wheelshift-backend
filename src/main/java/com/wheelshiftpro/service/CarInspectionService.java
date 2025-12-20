package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.CarInspectionRequest;
import com.wheelshiftpro.dto.response.CarInspectionResponse;
import com.wheelshiftpro.dto.response.PageResponse;

import java.time.LocalDate;

/**
 * Service interface for car inspection management operations.
 */
public interface CarInspectionService {

    /**
     * Creates a new car inspection record.
     *
     * @param request the inspection creation request
     * @return the created inspection response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if car or employee not found
     */
    CarInspectionResponse createInspection(CarInspectionRequest request);

    /**
     * Updates an existing inspection record.
     *
     * @param id the inspection ID
     * @param request the update request
     * @return the updated inspection response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if inspection not found
     */
    CarInspectionResponse updateInspection(Long id, CarInspectionRequest request);

    /**
     * Retrieves an inspection by ID.
     *
     * @param id the inspection ID
     * @return the inspection response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if inspection not found
     */
    CarInspectionResponse getInspectionById(Long id);

    /**
     * Retrieves all inspections with pagination.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @return paginated inspection responses
     */
    PageResponse<CarInspectionResponse> getAllInspections(int page, int size);

    /**
     * Deletes an inspection record.
     *
     * @param id the inspection ID
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if inspection not found
     */
    void deleteInspection(Long id);

    /**
     * Retrieves all inspections for a specific car.
     *
     * @param carId the car ID
     * @param page the page number
     * @param size the page size
     * @return paginated car inspections
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if car not found
     */
    PageResponse<CarInspectionResponse> getInspectionsByCarId(Long carId, int page, int size);

    /**
     * Retrieves the latest inspection for a specific car.
     *
     * @param carId the car ID
     * @return the latest inspection response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if car not found or no inspections exist
     */
    CarInspectionResponse getLatestInspectionByCarId(Long carId);

    /**
     * Retrieves inspections performed by a specific employee.
     *
     * @param employeeId the employee ID
     * @param page the page number
     * @param size the page size
     * @return paginated inspections performed by the employee
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if employee not found
     */
    PageResponse<CarInspectionResponse> getInspectionsByEmployeeId(Long employeeId, int page, int size);

    /**
     * Retrieves inspections within a date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @param page the page number
     * @param size the page size
     * @return paginated inspections within the date range
     */
    PageResponse<CarInspectionResponse> getInspectionsByDateRange(LocalDate startDate, LocalDate endDate, 
                                                                    int page, int size);

    /**
     * Retrieves cars requiring inspection (last inspection older than specified days).
     *
     * @param daysSinceLastInspection the number of days since last inspection
     * @param page the page number
     * @param size the page size
     * @return paginated cars requiring inspection
     */
    PageResponse<?> getCarsRequiringInspection(int daysSinceLastInspection, int page, int size);
}
