package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.MotorcycleInspectionRequest;
import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.MotorcycleInspectionResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.service.MotorcycleInspectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/motorcycle-inspections")
@RequiredArgsConstructor
@Tag(name = "Motorcycle Inspection Management", description = "APIs for managing motorcycle inspections")
public class MotorcycleInspectionController {

    private final MotorcycleInspectionService motorcycleInspectionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSPECTOR','STORE_MANAGER')")
    @Operation(summary = "Create a new motorcycle inspection", description = "Creates a new motorcycle inspection record")
    public ResponseEntity<ApiResponse<MotorcycleInspectionResponse>> createMotorcycleInspection(
            @Valid @RequestBody MotorcycleInspectionRequest request) {
        MotorcycleInspectionResponse response = motorcycleInspectionService.createInspection(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Motorcycle inspection created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSPECTOR')")
    @Operation(summary = "Update a motorcycle inspection", description = "Updates an existing motorcycle inspection by ID")
    public ResponseEntity<ApiResponse<MotorcycleInspectionResponse>> updateMotorcycleInspection(
            @Parameter(description = "Motorcycle Inspection ID") @PathVariable Long id,
            @Valid @RequestBody MotorcycleInspectionRequest request) {
        MotorcycleInspectionResponse response = motorcycleInspectionService.updateInspection(id, request);
        return ResponseEntity.ok(ApiResponse.success("Motorcycle inspection updated successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get motorcycle inspection by ID", description = "Retrieves a specific motorcycle inspection by its ID")
    public ResponseEntity<ApiResponse<MotorcycleInspectionResponse>> getMotorcycleInspectionById(
            @Parameter(description = "Motorcycle Inspection ID") @PathVariable Long id) {
        MotorcycleInspectionResponse response = motorcycleInspectionService.getInspectionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all motorcycle inspections", description = "Retrieves all motorcycle inspections with pagination")
    public ResponseEntity<ApiResponse<PageResponse<MotorcycleInspectionResponse>>> getAllMotorcycleInspections(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<MotorcycleInspectionResponse> response = motorcycleInspectionService.getAllInspections(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Delete a motorcycle inspection", description = "Deletes a motorcycle inspection by ID")
    public ResponseEntity<ApiResponse<Void>> deleteMotorcycleInspection(
            @Parameter(description = "Motorcycle Inspection ID") @PathVariable Long id) {
        motorcycleInspectionService.deleteInspection(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Motorcycle inspection deleted successfully", null));
    }

    @GetMapping("/motorcycle/{motorcycleId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get inspections by motorcycle ID", description = "Retrieves all inspections for a specific motorcycle")
    public ResponseEntity<ApiResponse<PageResponse<MotorcycleInspectionResponse>>> getInspectionsByMotorcycleId(
            @Parameter(description = "Motorcycle ID") @PathVariable Long motorcycleId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<MotorcycleInspectionResponse> response = motorcycleInspectionService.getInspectionsByMotorcycleId(motorcycleId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/motorcycle/{motorcycleId}/latest")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get latest inspection by motorcycle ID", description = "Retrieves the most recent inspection for a motorcycle")
    public ResponseEntity<ApiResponse<MotorcycleInspectionResponse>> getLatestInspectionByMotorcycleId(
            @Parameter(description = "Motorcycle ID") @PathVariable Long motorcycleId) {
        MotorcycleInspectionResponse response = motorcycleInspectionService.getLatestInspectionByMotorcycleId(motorcycleId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/inspector/{inspectorId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get inspections by inspector ID", description = "Retrieves all inspections performed by an inspector")
    public ResponseEntity<ApiResponse<PageResponse<MotorcycleInspectionResponse>>> getInspectionsByInspectorId(
            @Parameter(description = "Inspector (Employee) ID") @PathVariable Long inspectorId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<MotorcycleInspectionResponse> response = motorcycleInspectionService.getInspectionsByInspectorId(inspectorId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/date-range")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get inspections by date range", description = "Retrieves all inspections within a date range")
    public ResponseEntity<ApiResponse<PageResponse<MotorcycleInspectionResponse>>> getInspectionsByDateRange(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<MotorcycleInspectionResponse> response = motorcycleInspectionService.getInspectionsByDateRange(startDate, endDate, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/failed")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get failed inspections", description = "Retrieves all inspections that failed")
    public ResponseEntity<ApiResponse<PageResponse<MotorcycleInspectionResponse>>> getFailedInspections(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<MotorcycleInspectionResponse> response = motorcycleInspectionService.getFailedInspections(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/requiring-repair")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get inspections requiring repair", description = "Retrieves all inspections that require repair")
    public ResponseEntity<ApiResponse<PageResponse<MotorcycleInspectionResponse>>> getInspectionsRequiringRepair(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<MotorcycleInspectionResponse> response = motorcycleInspectionService.getInspectionsRequiringRepair(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/accident-history")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get inspections with accident history", description = "Retrieves all inspections where motorcycle has accident history")
    public ResponseEntity<ApiResponse<PageResponse<MotorcycleInspectionResponse>>> getInspectionsWithAccidentHistory(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<MotorcycleInspectionResponse> response = motorcycleInspectionService.getInspectionsWithAccidentHistory(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/condition/{condition}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get inspections by condition", description = "Retrieves all inspections with a specific overall condition")
    public ResponseEntity<ApiResponse<PageResponse<MotorcycleInspectionResponse>>> getInspectionsByCondition(
            @Parameter(description = "Overall condition (e.g., EXCELLENT, GOOD, FAIR, POOR)") @PathVariable String condition,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<MotorcycleInspectionResponse> response = motorcycleInspectionService.getInspectionsByCondition(condition, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
