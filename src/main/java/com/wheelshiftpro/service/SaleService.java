package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.SaleRequest;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.dto.response.SaleResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service interface for Sale operations.
 * Manages completed vehicle sales and transactions.
 */
public interface SaleService {

    /**
     * Creates a new sale record.
     * Validates car availability and updates related entities.
     *
     * @param request the sale data
     * @return created sale response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if entities not found
     * @throws com.wheelshiftpro.exception.BusinessException if car already sold
     */
    SaleResponse createSale(SaleRequest request);

    /**
     * Updates an existing sale record.
     *
     * @param id the sale ID
     * @param request updated sale data
     * @return updated sale response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if sale not found
     */
    SaleResponse updateSale(Long id, SaleRequest request);

    /**
     * Retrieves a sale by ID.
     *
     * @param id the sale ID
     * @return sale response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if sale not found
     */
    SaleResponse getSaleById(Long id);

    /**
     * Retrieves sale by car ID.
     *
     * @param carId the car ID
     * @return sale response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if sale not found
     */
    SaleResponse getSaleByCarId(Long carId);

    /**
     * Retrieves all sales with pagination.
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @return paginated sale responses
     */
    PageResponse<SaleResponse> getAllSales(int page, int size);

    /**
     * Retrieves sales by client.
     *
     * @param clientId the client ID
     * @param page page number
     * @param size page size
     * @return paginated sale responses
     */
    PageResponse<SaleResponse> getSalesByClient(Long clientId, int page, int size);

    /**
     * Retrieves sales by employee.
     *
     * @param employeeId the employee ID
     * @param page page number
     * @param size page size
     * @return paginated sale responses
     */
    PageResponse<SaleResponse> getSalesByEmployee(Long employeeId, int page, int size);

    /**
     * Retrieves sales within a date range.
     *
     * @param startDate start date
     * @param endDate end date
     * @param page page number
     * @param size page size
     * @return paginated sale responses
     */
    PageResponse<SaleResponse> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate, 
                                                    int page, int size);

    /**
     * Calculates total revenue within a date range.
     *
     * @param startDate start date
     * @param endDate end date
     * @return total revenue
     */
    BigDecimal calculateTotalRevenue(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Calculates total commission within a date range.
     *
     * @param startDate start date
     * @param endDate end date
     * @return total commission
     */
    BigDecimal calculateTotalCommission(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Retrieves employee sales performance.
     *
     * @param startDate start date
     * @param endDate end date
     * @return performance statistics
     */
    Object getEmployeePerformance(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Deletes a sale record.
     * Use with caution - affects multiple entities.
     *
     * @param id the sale ID
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if sale not found
     */
    void deleteSale(Long id);
}
