package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.FinancialTransactionRequest;
import com.wheelshiftpro.dto.response.FinancialTransactionResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.enums.PaymentMethod;
import com.wheelshiftpro.enums.TransactionType;

import java.time.LocalDate;

/**
 * Service interface for financial transaction management operations.
 */
public interface FinancialTransactionService {

    /**
     * Creates a new financial transaction.
     *
     * @param request the transaction creation request
     * @return the created transaction response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if sale not found
     */
    FinancialTransactionResponse createTransaction(FinancialTransactionRequest request);

    /**
     * Updates an existing transaction.
     *
     * @param id the transaction ID
     * @param request the update request
     * @return the updated transaction response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if transaction not found
     */
    FinancialTransactionResponse updateTransaction(Long id, FinancialTransactionRequest request);

    /**
     * Retrieves a transaction by ID.
     *
     * @param id the transaction ID
     * @return the transaction response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if transaction not found
     */
    FinancialTransactionResponse getTransactionById(Long id);

    /**
     * Retrieves all transactions with pagination.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @return paginated transaction responses
     */
    PageResponse<FinancialTransactionResponse> getAllTransactions(int page, int size);

    /**
     * Deletes a transaction.
     *
     * @param id the transaction ID
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if transaction not found
     */
    void deleteTransaction(Long id);

    /**
     * Searches transactions with multiple filters.
     *
     * @param saleId optional sale ID filter
     * @param transactionType optional transaction type filter
     * @param paymentMethod optional payment method filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param page the page number
     * @param size the page size
     * @return paginated search results
     */
    PageResponse<FinancialTransactionResponse> searchTransactions(Long saleId, TransactionType transactionType,
                                                                   PaymentMethod paymentMethod, LocalDate startDate,
                                                                   LocalDate endDate, int page, int size);

    /**
     * Retrieves transactions for a specific sale.
     *
     * @param saleId the sale ID
     * @param page the page number
     * @param size the page size
     * @return paginated sale transactions
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if sale not found
     */
    PageResponse<FinancialTransactionResponse> getTransactionsBySaleId(Long saleId, int page, int size);

    /**
     * Retrieves transactions by type.
     *
     * @param transactionType the transaction type
     * @param page the page number
     * @param size the page size
     * @return paginated transactions of the specified type
     */
    PageResponse<FinancialTransactionResponse> getTransactionsByType(TransactionType transactionType, int page, int size);

    /**
     * Retrieves transactions by payment method.
     *
     * @param paymentMethod the payment method
     * @param page the page number
     * @param size the page size
     * @return paginated transactions using the specified payment method
     */
    PageResponse<FinancialTransactionResponse> getTransactionsByPaymentMethod(PaymentMethod paymentMethod, int page, int size);

    /**
     * Retrieves transactions within a date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @param page the page number
     * @param size the page size
     * @return paginated transactions within the date range
     */
    PageResponse<FinancialTransactionResponse> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate, 
                                                                           int page, int size);

    /**
     * Calculates total transaction amount by type within a date range.
     *
     * @param transactionType the transaction type
     * @param startDate the start date
     * @param endDate the end date
     * @return the total amount
     */
    Double calculateTotalByType(TransactionType transactionType, LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves financial summary including totals by type and payment method.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return financial summary statistics
     */
    Object getFinancialSummary(LocalDate startDate, LocalDate endDate);
}
