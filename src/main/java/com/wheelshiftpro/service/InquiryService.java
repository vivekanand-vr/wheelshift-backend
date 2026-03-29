package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.InquiryRequest;
import com.wheelshiftpro.dto.response.InquiryResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.enums.InquiryStatus;

import java.time.LocalDate;

/**
 * Service interface for customer inquiry management operations.
 */
public interface InquiryService {

    /**
     * Creates a new customer inquiry.
     *
     * @param request the inquiry creation request
     * @return the created inquiry response
     */
    InquiryResponse createInquiry(InquiryRequest request);

    /**
     * Updates an existing inquiry.
     *
     * @param id the inquiry ID
     * @param request the update request
     * @return the updated inquiry response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if inquiry not found
     */
    InquiryResponse updateInquiry(Long id, InquiryRequest request);

    /**
     * Retrieves an inquiry by ID.
     *
     * @param id the inquiry ID
     * @return the inquiry response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if inquiry not found
     */
    InquiryResponse getInquiryById(Long id);

    /**
     * Retrieves all inquiries with pagination.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @return paginated inquiry responses
     */
    PageResponse<InquiryResponse> getAllInquiries(int page, int size);

    /**
     * Deletes an inquiry by ID.
     *
     * @param id the inquiry ID
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if inquiry not found
     */
    void deleteInquiry(Long id);

    /**
     * Searches inquiries with multiple filters.
     *
     * @param clientId optional client ID filter
     * @param status optional inquiry status filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param page the page number
     * @param size the page size
     * @return paginated search results
     */
    PageResponse<InquiryResponse> searchInquiries(Long clientId, InquiryStatus status, 
                                                   LocalDate startDate, LocalDate endDate, 
                                                   int page, int size);

    /**
     * Retrieves inquiries by status.
     *
     * @param status the inquiry status
     * @param page the page number
     * @param size the page size
     * @return paginated inquiries with the specified status
     */
    PageResponse<InquiryResponse> getInquiriesByStatus(InquiryStatus status, int page, int size);

    /**
     * Retrieves inquiries for a specific client.
     *
     * @param clientId the client ID
     * @param page the page number
     * @param size the page size
     * @return paginated client inquiries
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if client not found
     */
    PageResponse<InquiryResponse> getInquiriesByClient(Long clientId, int page, int size);

    /**
     * Updates inquiry status.
     *
     * @param id the inquiry ID
     * @param status the new status
     * @return the updated inquiry response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if inquiry not found
     */
    InquiryResponse updateInquiryStatus(Long id, InquiryStatus status);

    /**
     * Converts an inquiry into a reservation.
     * Updates inquiry status to CONVERTED and creates a new reservation.
     *
     * @param inquiryId the inquiry ID
     * @param carId the car ID for the reservation
     * @param depositAmount the deposit amount
     * @return the created reservation response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if inquiry or car not found
     * @throws com.wheelshiftpro.exception.BusinessException if inquiry already converted or car unavailable
     */
    Object convertToReservation(Long inquiryId, Long carId, Double depositAmount);

    /**
     * Retrieves inquiry statistics including total counts by status.
     *
     * @return inquiry statistics
     */
    Object getInquiryStatistics();

    /**
     * Assigns an inquiry to an employee.
     * Validates employee existence and dispatches notifications.
     *
     * @param inquiryId the inquiry ID
     * @param employeeId the employee ID to assign to
     * @return the updated inquiry response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if inquiry or employee not found
     */
    InquiryResponse assignInquiry(Long inquiryId, Long employeeId);
}
