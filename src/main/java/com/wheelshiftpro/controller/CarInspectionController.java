package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.CarInspectionRequest;
import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.CarInspectionResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.service.CarInspectionService;
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
@RequestMapping("/api/v1/car-inspections")
@RequiredArgsConstructor
@Tag(name = "Car Inspection Management", description = "APIs for managing vehicle inspections")
public class CarInspectionController {

    private final CarInspectionService carInspectionService;

    @PostMapping
    @Operation(summary = "Create a new car inspection", description = "Creates a new vehicle inspection record")
    public ResponseEntity<ApiResponse<CarInspectionResponse>> createCarInspection(
            @Valid @RequestBody CarInspectionRequest request) {
        CarInspectionResponse response = carInspectionService.createInspection(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Car inspection created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a car inspection", description = "Updates an existing car inspection by ID")
    public ResponseEntity<ApiResponse<CarInspectionResponse>> updateCarInspection(
            @Parameter(description = "Car Inspection ID") @PathVariable Long id,
            @Valid @RequestBody CarInspectionRequest request) {
        CarInspectionResponse response = carInspectionService.updateInspection(id, request);
        return ResponseEntity.ok(ApiResponse.success("Car inspection updated successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get car inspection by ID", description = "Retrieves a specific car inspection by its ID")
    public ResponseEntity<ApiResponse<CarInspectionResponse>> getCarInspectionById(
            @Parameter(description = "Car Inspection ID") @PathVariable Long id) {
        CarInspectionResponse response = carInspectionService.getInspectionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all car inspections", description = "Retrieves all car inspections with pagination")
    public ResponseEntity<ApiResponse<PageResponse<CarInspectionResponse>>> getAllCarInspections(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<CarInspectionResponse> response = carInspectionService.getAllInspections(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a car inspection", description = "Deletes a car inspection by ID")
    public ResponseEntity<ApiResponse<Void>> deleteCarInspection(
            @Parameter(description = "Car Inspection ID") @PathVariable Long id) {
        carInspectionService.deleteInspection(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Car inspection deleted successfully", null));
    }

    @GetMapping("/car/{carId}")
    @Operation(summary = "Get inspections by car ID", description = "Retrieves all inspections for a specific car")
    public ResponseEntity<ApiResponse<PageResponse<CarInspectionResponse>>> getInspectionsByCarId(
            @Parameter(description = "Car ID") @PathVariable Long carId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<CarInspectionResponse> response = carInspectionService.getInspectionsByCarId(carId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/car/{carId}/latest")
    @Operation(summary = "Get latest inspection by car ID", description = "Retrieves the most recent inspection for a car")
    public ResponseEntity<ApiResponse<CarInspectionResponse>> getLatestInspectionByCarId(
            @Parameter(description = "Car ID") @PathVariable Long carId) {
        CarInspectionResponse response = carInspectionService.getLatestInspectionByCarId(carId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get inspections by employee ID", description = "Retrieves all inspections performed by an employee")
    public ResponseEntity<ApiResponse<PageResponse<CarInspectionResponse>>> getInspectionsByEmployeeId(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<CarInspectionResponse> response = carInspectionService.getInspectionsByEmployeeId(employeeId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get inspections by date range", description = "Retrieves all inspections within a date range")
    public ResponseEntity<ApiResponse<PageResponse<CarInspectionResponse>>> getInspectionsByDateRange(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<CarInspectionResponse> response = carInspectionService.getInspectionsByDateRange(startDate, endDate, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/requiring-inspection")
    @Operation(summary = "Get cars requiring inspection", description = "Retrieves cars that require inspection")
    public ResponseEntity<ApiResponse<PageResponse<?>>> getCarsRequiringInspection(
            @Parameter(description = "Days since last inspection") @RequestParam(defaultValue = "365") int daysSinceLastInspection,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<?> response = carInspectionService.getCarsRequiringInspection(daysSinceLastInspection, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
