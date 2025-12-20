package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.InquiryRequest;
import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.InquiryResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.enums.InquiryStatus;
import com.wheelshiftpro.service.InquiryService;
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
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
@Tag(name = "Inquiry Management", description = "APIs for managing customer inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    @Operation(summary = "Create a new inquiry", description = "Creates a new customer inquiry")
    public ResponseEntity<ApiResponse<InquiryResponse>> createInquiry(
            @Valid @RequestBody InquiryRequest request) {
        InquiryResponse response = inquiryService.createInquiry(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inquiry created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an inquiry", description = "Updates an existing inquiry by ID")
    public ResponseEntity<ApiResponse<InquiryResponse>> updateInquiry(
            @Parameter(description = "Inquiry ID") @PathVariable Long id,
            @Valid @RequestBody InquiryRequest request) {
        InquiryResponse response = inquiryService.updateInquiry(id, request);
        return ResponseEntity.ok(ApiResponse.success("Inquiry updated successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inquiry by ID", description = "Retrieves a specific inquiry by its ID")
    public ResponseEntity<ApiResponse<InquiryResponse>> getInquiryById(
            @Parameter(description = "Inquiry ID") @PathVariable Long id) {
        InquiryResponse response = inquiryService.getInquiryById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all inquiries", description = "Retrieves all inquiries with pagination")
    public ResponseEntity<ApiResponse<PageResponse<InquiryResponse>>> getAllInquiries(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<InquiryResponse> response = inquiryService.getAllInquiries(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an inquiry", description = "Deletes an inquiry by ID")
    public ResponseEntity<ApiResponse<Void>> deleteInquiry(
            @Parameter(description = "Inquiry ID") @PathVariable Long id) {
        inquiryService.deleteInquiry(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Inquiry deleted successfully", null));
    }

    @GetMapping("/search")
    @Operation(summary = "Search inquiries", description = "Search inquiries with multiple filter criteria")
    public ResponseEntity<ApiResponse<PageResponse<InquiryResponse>>> searchInquiries(
            @Parameter(description = "Client ID filter") @RequestParam(required = false) Long clientId,
            @Parameter(description = "Inquiry status filter") @RequestParam(required = false) InquiryStatus status,
            @Parameter(description = "Start date filter") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date filter") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<InquiryResponse> response = inquiryService.searchInquiries(clientId, status, startDate, endDate, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get inquiries by status", description = "Retrieves all inquiries with a specific status")
    public ResponseEntity<ApiResponse<PageResponse<InquiryResponse>>> getInquiriesByStatus(
            @Parameter(description = "Inquiry status") @PathVariable InquiryStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<InquiryResponse> response = inquiryService.getInquiriesByStatus(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get inquiries by client", description = "Retrieves all inquiries for a specific client")
    public ResponseEntity<ApiResponse<PageResponse<InquiryResponse>>> getInquiriesByClient(
            @Parameter(description = "Client ID") @PathVariable Long clientId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<InquiryResponse> response = inquiryService.getInquiriesByClient(clientId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update inquiry status", description = "Updates the status of an inquiry")
    public ResponseEntity<ApiResponse<Void>> updateInquiryStatus(
            @Parameter(description = "Inquiry ID") @PathVariable Long id,
            @Parameter(description = "New inquiry status") @RequestParam InquiryStatus status) {
        inquiryService.updateInquiryStatus(id, status);
        return ResponseEntity.ok(ApiResponse.<Void>success("Inquiry status updated successfully", null));
    }

    @PostMapping("/{id}/convert-to-reservation")
    @Operation(summary = "Convert to reservation", description = "Converts an inquiry to a reservation")
    public ResponseEntity<ApiResponse<Object>> convertToReservation(
            @Parameter(description = "Inquiry ID") @PathVariable Long id,
            @Parameter(description = "Car ID") @RequestParam Long carId,
            @Parameter(description = "Deposit Amount") @RequestParam Double depositAmount) {
        Object response = inquiryService.convertToReservation(id, carId, depositAmount);
        return ResponseEntity.ok(ApiResponse.success("Inquiry converted to reservation successfully", response));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get inquiry statistics", description = "Retrieves inquiry statistics by status")
    public ResponseEntity<ApiResponse<Object>> getInquiryStatistics() {
        Object statistics = inquiryService.getInquiryStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
}
