package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.MotorcycleInspectionRequest;
import com.wheelshiftpro.dto.response.MotorcycleInspectionResponse;
import com.wheelshiftpro.dto.response.PageResponse;

import java.time.LocalDate;

/**
 * Service interface for motorcycle inspection management operations.
 */
public interface MotorcycleInspectionService {

    /**
     * Creates a new motorcycle inspection record.
     *
     * @param request the inspection creation request
     * @return the created inspection response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if motorcycle or employee not found
     */
    MotorcycleInspectionResponse createInspection(MotorcycleInspectionRequest request);

    /**
     * Updates an existing inspection record.
     *
     * @param id the inspection ID
     * @param request the update request
     * @return the updated inspection response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if inspection not found
     */
    MotorcycleInspectionResponse updateInspection(Long id, MotorcycleInspectionRequest request);

    /**
     * Retrieves an inspection by ID.
     *
     * @param id the inspection ID
     * @return the inspection response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if inspection not found
     */
    MotorcycleInspectionResponse getInspectionById(Long id);

    /**
     * Retrieves all inspections with pagination.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @return paginated inspection responses
     */
    PageResponse<MotorcycleInspectionResponse> getAllInspections(int page, int size);

    /**
     * Deletes an inspection record.
     *
     * @param id the inspection ID
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if inspection not found
     */
    void deleteInspection(Long id);

    /**
     * Retrieves all inspections for a specific motorcycle.
     *
     * @param motorcycleId the motorcycle ID
     * @param page the page number
     * @param size the page size
     * @return paginated motorcycle inspections
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if motorcycle not found
     */
    PageResponse<MotorcycleInspectionResponse> getInspectionsByMotorcycleId(Long motorcycleId, int page, int size);

    /**
     * Retrieves the latest inspection for a specific motorcycle.
     *
     * @param motorcycleId the motorcycle ID
     * @return the latest inspection response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if motorcycle not found or no inspections exist
     */
    MotorcycleInspectionResponse getLatestInspectionByMotorcycleId(Long motorcycleId);

    /**
     * Retrieves inspections performed by a specific inspector.
     *
     * @param inspectorId the inspector (employee) ID
     * @param page the page number
     * @param size the page size
     * @return paginated inspections performed by the inspector
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if inspector not found
     */
    PageResponse<MotorcycleInspectionResponse> getInspectionsByInspectorId(Long inspectorId, int page, int size);

    /**
     * Retrieves inspections within a date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @param page the page number
     * @param size the page size
     * @return paginated inspections within the date range
     */
    PageResponse<MotorcycleInspectionResponse> getInspectionsByDateRange(LocalDate startDate, LocalDate endDate, 
                                                                          int page, int size);

    /**
     * Retrieves failed inspections.
     *
     * @param page the page number
     * @param size the page size
     * @return paginated failed inspections
     */
    PageResponse<MotorcycleInspectionResponse> getFailedInspections(int page, int size);

    /**
     * Retrieves inspections requiring repair.
     *
     * @param page the page number
     * @param size the page size
     * @return paginated inspections requiring repair
     */
    PageResponse<MotorcycleInspectionResponse> getInspectionsRequiringRepair(int page, int size);

    /**
     * Retrieves motorcycles with accident history.
     *
     * @param page the page number
     * @param size the page size
     * @return paginated inspections with accident history
     */
    PageResponse<MotorcycleInspectionResponse> getInspectionsWithAccidentHistory(int page, int size);

    /**
     * Retrieves inspections by overall condition.
     *
     * @param condition the overall condition (e.g., EXCELLENT, GOOD, FAIR, POOR)
     * @param page the page number
     * @param size the page size
     * @return paginated inspections with specified condition
     */
    PageResponse<MotorcycleInspectionResponse> getInspectionsByCondition(String condition, int page, int size);
}
