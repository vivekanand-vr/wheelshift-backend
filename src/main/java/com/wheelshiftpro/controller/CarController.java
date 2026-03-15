package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.CarRequest;
import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.CarResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.enums.CarStatus;
import com.wheelshiftpro.service.CarService;
import com.wheelshiftpro.validation.OnCreate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
@Tag(name = "Car Management", description = "APIs for managing vehicles in inventory")
public class CarController {

    private final CarService carService;

    @PostMapping
    @Operation(summary = "Create a new car", description = "Adds a new car to the inventory")
    public ResponseEntity<ApiResponse<CarResponse>> createCar(
            @Validated(OnCreate.class) @RequestBody CarRequest request) {
        CarResponse response = carService.createCar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Car created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a car", description = "Updates an existing car by ID")
    public ResponseEntity<ApiResponse<CarResponse>> updateCar(
            @Parameter(description = "Car ID") @PathVariable Long id,
            @Valid @RequestBody CarRequest request) {
        CarResponse response = carService.updateCar(id, request);
        return ResponseEntity.ok(ApiResponse.success("Car updated successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get car by ID", description = "Retrieves a specific car by its ID")
    public ResponseEntity<ApiResponse<CarResponse>> getCarById(
            @Parameter(description = "Car ID") @PathVariable Long id) {
        CarResponse response = carService.getCarById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all cars", description = "Retrieves all cars with pagination")
    public ResponseEntity<ApiResponse<PageResponse<CarResponse>>> getAllCars(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<CarResponse> response = carService.getAllCars(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a car", description = "Deletes a car by ID")
    public ResponseEntity<ApiResponse<Void>> deleteCar(
            @Parameter(description = "Car ID") @PathVariable Long id) {
        carService.deleteCar(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Car deleted successfully", null));
    }

    @GetMapping("/search")
    @Operation(summary = "Search cars", description = "Search cars with multiple filter criteria")
    public ResponseEntity<ApiResponse<PageResponse<CarResponse>>> searchCars(
            @Parameter(description = "VIN filter") @RequestParam(required = false) String vin,
            @Parameter(description = "Car status filter") @RequestParam(required = false) CarStatus status,
            @Parameter(description = "Storage location ID filter") @RequestParam(required = false) Long storageLocationId,
            @Parameter(description = "Brand filter") @RequestParam(required = false) String brand,
            @Parameter(description = "Model filter") @RequestParam(required = false) String model,
            @Parameter(description = "Min year filter") @RequestParam(required = false) Integer minYear,
            @Parameter(description = "Max year filter") @RequestParam(required = false) Integer maxYear,
            @Parameter(description = "Min price filter") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Max price filter") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<CarResponse> response = carService.searchCars(
                status, storageLocationId, brand, model, minYear, maxYear, minPrice, maxPrice, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get cars by status", description = "Retrieves all cars with a specific status")
    public ResponseEntity<ApiResponse<PageResponse<CarResponse>>> getCarsByStatus(
            @Parameter(description = "Car status") @PathVariable CarStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<CarResponse> response = carService.searchCars(status, null, null, null, null, null, null, null, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/location/{locationId}")
    @Operation(summary = "Get cars by location", description = "Retrieves all cars at a specific storage location")
    public ResponseEntity<ApiResponse<PageResponse<CarResponse>>> getCarsByLocation(
            @Parameter(description = "Storage Location ID") @PathVariable Long locationId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<CarResponse> response = carService.searchCars(null, locationId, null, null, null, null, null, null, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/move")
    @Operation(summary = "Move car to location", description = "Moves a car to a different storage location")
    public ResponseEntity<ApiResponse<Void>> moveCarToLocation(
            @Parameter(description = "Car ID") @PathVariable Long id,
            @Parameter(description = "Target Storage Location ID") @RequestParam Long locationId) {
        carService.moveCarToLocation(id, locationId);
        return ResponseEntity.ok(ApiResponse.<Void>success("Car moved successfully", null));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update car status", description = "Updates the status of a car")
    public ResponseEntity<ApiResponse<Void>> updateCarStatus(
            @Parameter(description = "Car ID") @PathVariable Long id,
            @Parameter(description = "New car status") @RequestParam CarStatus status) {
        carService.updateCarStatus(id, status);
        return ResponseEntity.ok(ApiResponse.<Void>success("Car status updated successfully", null));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get inventory statistics", description = "Retrieves inventory statistics by status")
    public ResponseEntity<ApiResponse<Object>> getInventoryStatistics() {
        Object statistics = carService.getInventoryStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/inventory-value")
    @Operation(summary = "Calculate inventory value", description = "Calculates total value of inventory")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateInventoryValue() {
        BigDecimal value = carService.calculateInventoryValue();
        return ResponseEntity.ok(ApiResponse.success(value));
    }
}
