package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.SaleRequest;
import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.dto.response.SaleResponse;
import com.wheelshiftpro.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@Tag(name = "Sales Management", description = "APIs for managing vehicle sales")
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    @Operation(summary = "Create a new sale", description = "Creates a new vehicle sale")
    public ResponseEntity<ApiResponse<SaleResponse>> createSale(
            @Valid @RequestBody SaleRequest request) {
        SaleResponse response = saleService.createSale(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sale created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a sale", description = "Updates an existing sale by ID")
    public ResponseEntity<ApiResponse<SaleResponse>> updateSale(
            @Parameter(description = "Sale ID") @PathVariable Long id,
            @Valid @RequestBody SaleRequest request) {
        SaleResponse response = saleService.updateSale(id, request);
        return ResponseEntity.ok(ApiResponse.success("Sale updated successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sale by ID", description = "Retrieves a specific sale by its ID")
    public ResponseEntity<ApiResponse<SaleResponse>> getSaleById(
            @Parameter(description = "Sale ID") @PathVariable Long id) {
        SaleResponse response = saleService.getSaleById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all sales", description = "Retrieves all sales with pagination")
    public ResponseEntity<ApiResponse<PageResponse<SaleResponse>>> getAllSales(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<SaleResponse> response = saleService.getAllSales(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a sale", description = "Deletes a sale by ID")
    public ResponseEntity<ApiResponse<Void>> deleteSale(
            @Parameter(description = "Sale ID") @PathVariable Long id) {
        saleService.deleteSale(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Sale deleted successfully", null));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get sales by client", description = "Retrieves all sales for a specific client")
    public ResponseEntity<ApiResponse<PageResponse<SaleResponse>>> getSalesByClient(
            @Parameter(description = "Client ID") @PathVariable Long clientId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<SaleResponse> response = saleService.getSalesByClient(clientId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get sales by employee", description = "Retrieves all sales by a specific employee")
    public ResponseEntity<ApiResponse<PageResponse<SaleResponse>>> getSalesByEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<SaleResponse> response = saleService.getSalesByEmployee(employeeId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get sales by date range", description = "Retrieves all sales within a date range")
    public ResponseEntity<ApiResponse<PageResponse<SaleResponse>>> getSalesByDateRange(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<SaleResponse> response = saleService.getSalesByDateRange(
                startDate.atStartOfDay(), endDate.atTime(23, 59, 59), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/revenue/total")
    @Operation(summary = "Calculate total revenue", description = "Calculates total revenue from sales within date range")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateTotalRevenue(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusYears(1);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();
        BigDecimal revenue = saleService.calculateTotalRevenue(start, end);
        return ResponseEntity.ok(ApiResponse.success(revenue));
    }

    @GetMapping("/commission/total")
    @Operation(summary = "Calculate total commission", description = "Calculates total commission from sales within date range")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateTotalCommission(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusYears(1);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();
        BigDecimal commission = saleService.calculateTotalCommission(start, end);
        return ResponseEntity.ok(ApiResponse.success(commission));
    }

    @GetMapping("/employee/{employeeId}/performance")
    @Operation(summary = "Get employee performance", description = "Retrieves sales performance for a specific employee")
    public ResponseEntity<ApiResponse<Object>> getEmployeePerformance(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId,
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusYears(1);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();
        Object performance = saleService.getEmployeePerformance(start, end);
        return ResponseEntity.ok(ApiResponse.success(performance));
    }
}
