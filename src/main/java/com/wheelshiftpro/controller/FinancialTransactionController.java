package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.FinancialTransactionRequest;
import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.FinancialTransactionResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.enums.PaymentMethod;
import com.wheelshiftpro.enums.TransactionType;
import com.wheelshiftpro.service.FinancialTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/financial-transactions")
@RequiredArgsConstructor
@Tag(name = "Financial Transaction Management", description = "APIs for managing financial transactions")
public class FinancialTransactionController {

    private final FinancialTransactionService financialTransactionService;

    @PostMapping
    @Operation(summary = "Create a new financial transaction", description = "Creates a new financial transaction record")
    public ResponseEntity<ApiResponse<FinancialTransactionResponse>> createFinancialTransaction(
            @Valid @RequestBody FinancialTransactionRequest request) {
        FinancialTransactionResponse response = financialTransactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Financial transaction created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a financial transaction", description = "Updates an existing financial transaction by ID")
    public ResponseEntity<ApiResponse<FinancialTransactionResponse>> updateFinancialTransaction(
            @Parameter(description = "Financial Transaction ID") @PathVariable Long id,
            @Valid @RequestBody FinancialTransactionRequest request) {
        FinancialTransactionResponse response = financialTransactionService.updateTransaction(id, request);
        return ResponseEntity.ok(ApiResponse.success("Financial transaction updated successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get financial transaction by ID", description = "Retrieves a specific financial transaction by its ID")
    public ResponseEntity<ApiResponse<FinancialTransactionResponse>> getFinancialTransactionById(
            @Parameter(description = "Financial Transaction ID") @PathVariable Long id) {
        FinancialTransactionResponse response = financialTransactionService.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all financial transactions", description = "Retrieves all financial transactions with pagination")
    public ResponseEntity<ApiResponse<PageResponse<FinancialTransactionResponse>>> getAllFinancialTransactions(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<FinancialTransactionResponse> response = financialTransactionService.getAllTransactions(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a financial transaction", description = "Deletes a financial transaction by ID")
    public ResponseEntity<ApiResponse<Void>> deleteFinancialTransaction(
            @Parameter(description = "Financial Transaction ID") @PathVariable Long id) {
        financialTransactionService.deleteTransaction(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Financial transaction deleted successfully", null));
    }

    @GetMapping("/search")
    @Operation(summary = "Search financial transactions", description = "Search financial transactions with multiple filter criteria")
    public ResponseEntity<ApiResponse<PageResponse<FinancialTransactionResponse>>> searchTransactions(
            @Parameter(description = "Sale ID filter") @RequestParam(required = false) Long saleId,
            @Parameter(description = "Transaction type filter") @RequestParam(required = false) TransactionType transactionType,
            @Parameter(description = "Payment method filter") @RequestParam(required = false) PaymentMethod paymentMethod,
            @Parameter(description = "Start date filter") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date filter") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<FinancialTransactionResponse> response = financialTransactionService.searchTransactions(
                saleId, transactionType, paymentMethod, startDate, endDate, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/sale/{saleId}")
    @Operation(summary = "Get transactions by sale ID", description = "Retrieves all transactions for a specific sale")
    public ResponseEntity<ApiResponse<PageResponse<FinancialTransactionResponse>>> getTransactionsBySaleId(
            @Parameter(description = "Sale ID") @PathVariable Long saleId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<FinancialTransactionResponse> response = financialTransactionService.getTransactionsBySaleId(saleId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/type/{transactionType}")
    @Operation(summary = "Get transactions by type", description = "Retrieves all transactions of a specific type")
    public ResponseEntity<ApiResponse<PageResponse<FinancialTransactionResponse>>> getTransactionsByType(
            @Parameter(description = "Transaction type") @PathVariable TransactionType transactionType,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<FinancialTransactionResponse> response = financialTransactionService.getTransactionsByType(transactionType, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/payment-method/{paymentMethod}")
    @Operation(summary = "Get transactions by payment method", description = "Retrieves all transactions using a specific payment method")
    public ResponseEntity<ApiResponse<PageResponse<FinancialTransactionResponse>>> getTransactionsByPaymentMethod(
            @Parameter(description = "Payment method") @PathVariable PaymentMethod paymentMethod,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<FinancialTransactionResponse> response = financialTransactionService.getTransactionsByPaymentMethod(paymentMethod, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get transactions by date range", description = "Retrieves all transactions within a date range")
    public ResponseEntity<ApiResponse<PageResponse<FinancialTransactionResponse>>> getTransactionsByDateRange(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<FinancialTransactionResponse> response = financialTransactionService.getTransactionsByDateRange(startDate, endDate, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/total-by-type/{transactionType}")
    @Operation(summary = "Calculate total by type", description = "Calculates total amount for a specific transaction type")
    public ResponseEntity<ApiResponse<Double>> calculateTotalByType(
            @Parameter(description = "Transaction type") @PathVariable TransactionType transactionType,
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusYears(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        Double total = financialTransactionService.calculateTotalByType(transactionType, start, end);
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get financial summary", description = "Retrieves comprehensive financial summary")
    public ResponseEntity<ApiResponse<Object>> getFinancialSummary(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusYears(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        Object summary = financialTransactionService.getFinancialSummary(start, end);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
