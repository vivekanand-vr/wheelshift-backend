package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.MotorcycleRequest;
import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.MotorcycleResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.enums.MotorcycleStatus;
import com.wheelshiftpro.service.MotorcycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/motorcycles")
@RequiredArgsConstructor
@Tag(name = "Motorcycle Management", description = "APIs for managing motorcycles in inventory")
public class MotorcycleController {

    private final MotorcycleService motorcycleService;

    @PostMapping
    @Operation(summary = "Create a new motorcycle", description = "Adds a new motorcycle to the inventory")
    public ResponseEntity<ApiResponse<MotorcycleResponse>> createMotorcycle(
            @Valid @RequestBody MotorcycleRequest request) {
        MotorcycleResponse response = motorcycleService.createMotorcycle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Motorcycle created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a motorcycle", description = "Updates an existing motorcycle by ID")
    public ResponseEntity<ApiResponse<MotorcycleResponse>> updateMotorcycle(
            @Parameter(description = "Motorcycle ID") @PathVariable Long id,
            @Valid @RequestBody MotorcycleRequest request) {
        MotorcycleResponse response = motorcycleService.updateMotorcycle(id, request);
        return ResponseEntity.ok(ApiResponse.success("Motorcycle updated successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get motorcycle by ID", description = "Retrieves a specific motorcycle by its ID")
    public ResponseEntity<ApiResponse<MotorcycleResponse>> getMotorcycleById(
            @Parameter(description = "Motorcycle ID") @PathVariable Long id) {
        MotorcycleResponse response = motorcycleService.getMotorcycleById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all motorcycles", description = "Retrieves all motorcycles with pagination and sorting")
    public ResponseEntity<ApiResponse<PageResponse<MotorcycleResponse>>> getAllMotorcycles(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        PageResponse<MotorcycleResponse> response = motorcycleService.getAllMotorcycles(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a motorcycle", description = "Deletes a motorcycle by ID")
    public ResponseEntity<ApiResponse<Void>> deleteMotorcycle(
            @Parameter(description = "Motorcycle ID") @PathVariable Long id) {
        motorcycleService.deleteMotorcycle(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Motorcycle deleted successfully", null));
    }

    @GetMapping("/search")
    @Operation(summary = "Search motorcycles", description = "Search motorcycles by keyword (VIN, registration, make, model)")
    public ResponseEntity<ApiResponse<PageResponse<MotorcycleResponse>>> searchMotorcycles(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<MotorcycleResponse> response = motorcycleService.searchMotorcycles(searchTerm, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/vin/{vinNumber}")
    @Operation(summary = "Get motorcycle by VIN", description = "Retrieves a motorcycle by VIN number")
    public ResponseEntity<ApiResponse<MotorcycleResponse>> getMotorcycleByVin(
            @Parameter(description = "VIN number") @PathVariable String vinNumber) {
        MotorcycleResponse response = motorcycleService.getMotorcycleByVin(vinNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/registration/{registrationNumber}")
    @Operation(summary = "Get motorcycle by registration", description = "Retrieves a motorcycle by registration number")
    public ResponseEntity<ApiResponse<MotorcycleResponse>> getMotorcycleByRegistration(
            @Parameter(description = "Registration number") @PathVariable String registrationNumber) {
        MotorcycleResponse response = motorcycleService.getMotorcycleByRegistration(registrationNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get motorcycles by status", description = "Retrieves all motorcycles with a specific status")
    public ResponseEntity<ApiResponse<List<MotorcycleResponse>>> getMotorcyclesByStatus(
            @Parameter(description = "Motorcycle status") @PathVariable MotorcycleStatus status) {
        List<MotorcycleResponse> response = motorcycleService.getMotorcyclesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/location/{locationId}")
    @Operation(summary = "Get motorcycles by location", description = "Retrieves all motorcycles at a specific storage location")
    public ResponseEntity<ApiResponse<List<MotorcycleResponse>>> getMotorcyclesByLocation(
            @Parameter(description = "Storage Location ID") @PathVariable Long locationId) {
        List<MotorcycleResponse> response = motorcycleService.getMotorcyclesByStorageLocation(locationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/model/{modelId}")
    @Operation(summary = "Get motorcycles by model", description = "Retrieves all motorcycles of a specific model")
    public ResponseEntity<ApiResponse<List<MotorcycleResponse>>> getMotorcyclesByModel(
            @Parameter(description = "Motorcycle Model ID") @PathVariable Long modelId) {
        List<MotorcycleResponse> response = motorcycleService.getMotorcyclesByModel(modelId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available motorcycles", description = "Retrieves all available motorcycles")
    public ResponseEntity<ApiResponse<List<MotorcycleResponse>>> getAvailableMotorcycles() {
        List<MotorcycleResponse> response = motorcycleService.getAvailableMotorcycles();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/needing-attention")
    @Operation(summary = "Get motorcycles needing attention", description = "Retrieves motorcycles that need maintenance or service")
    public ResponseEntity<ApiResponse<List<MotorcycleResponse>>> getMotorcyclesNeedingAttention() {
        List<MotorcycleResponse> response = motorcycleService.getMotorcyclesNeedingAttention();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recently added motorcycles", description = "Retrieves recently added motorcycles")
    public ResponseEntity<ApiResponse<List<MotorcycleResponse>>> getRecentlyAddedMotorcycles(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") int limit) {
        List<MotorcycleResponse> response = motorcycleService.getRecentlyAddedMotorcycles(limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get motorcycles by price range", description = "Retrieves motorcycles within a price range")
    public ResponseEntity<ApiResponse<PageResponse<MotorcycleResponse>>> getMotorcyclesByPriceRange(
            @Parameter(description = "Minimum price") @RequestParam BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam BigDecimal maxPrice,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<MotorcycleResponse> response = motorcycleService.getMotorcyclesByPriceRange(minPrice, maxPrice, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/move")
    @Operation(summary = "Move motorcycle to location", description = "Moves a motorcycle to a different storage location")
    public ResponseEntity<ApiResponse<MotorcycleResponse>> moveMotorcycleToLocation(
            @Parameter(description = "Motorcycle ID") @PathVariable Long id,
            @Parameter(description = "Target Storage Location ID") @RequestParam Long locationId) {
        MotorcycleResponse response = motorcycleService.moveMotorcycleToLocation(id, locationId);
        return ResponseEntity.ok(ApiResponse.success("Motorcycle moved successfully", response));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update motorcycle status", description = "Updates the status of a motorcycle")
    public ResponseEntity<ApiResponse<MotorcycleResponse>> updateMotorcycleStatus(
            @Parameter(description = "Motorcycle ID") @PathVariable Long id,
            @Parameter(description = "New motorcycle status") @RequestParam MotorcycleStatus status) {
        MotorcycleResponse response = motorcycleService.updateMotorcycleStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Motorcycle status updated successfully", response));
    }

    @GetMapping("/inventory-value")
    @Operation(summary = "Calculate inventory value", description = "Calculates total value of motorcycle inventory")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateInventoryValue() {
        BigDecimal value = motorcycleService.getTotalInventoryValue();
        return ResponseEntity.ok(ApiResponse.success(value));
    }

    @GetMapping("/average-price")
    @Operation(summary = "Get average selling price", description = "Retrieves average selling price of motorcycles")
    public ResponseEntity<ApiResponse<BigDecimal>> getAverageSellingPrice() {
        BigDecimal average = motorcycleService.getAverageSellingPrice();
        return ResponseEntity.ok(ApiResponse.success(average));
    }

    @GetMapping("/count/{status}")
    @Operation(summary = "Count motorcycles by status", description = "Returns the count of motorcycles with specific status")
    public ResponseEntity<ApiResponse<Long>> countMotorcyclesByStatus(
            @Parameter(description = "Motorcycle status") @PathVariable MotorcycleStatus status) {
        Long count = motorcycleService.countMotorcyclesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/exists/vin/{vinNumber}")
    @Operation(summary = "Check if VIN exists", description = "Checks if a VIN number exists in the system")
    public ResponseEntity<ApiResponse<Boolean>> existsByVin(
            @Parameter(description = "VIN number") @PathVariable String vinNumber) {
        boolean exists = motorcycleService.existsByVin(vinNumber);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @GetMapping("/exists/registration/{registrationNumber}")
    @Operation(summary = "Check if registration exists", description = "Checks if a registration number exists in the system")
    public ResponseEntity<ApiResponse<Boolean>> existsByRegistration(
            @Parameter(description = "Registration number") @PathVariable String registrationNumber) {
        boolean exists = motorcycleService.existsByRegistration(registrationNumber);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}
