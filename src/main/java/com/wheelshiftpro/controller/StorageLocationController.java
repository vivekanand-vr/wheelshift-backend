package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.StorageLocationRequest;
import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.dto.response.StorageLocationResponse;
import com.wheelshiftpro.service.StorageLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/storage-locations")
@RequiredArgsConstructor
@Tag(name = "Storage Location Management", description = "APIs for managing storage facilities and vehicle locations")
public class StorageLocationController {

    private final StorageLocationService storageLocationService;

    @PostMapping
    @Operation(summary = "Create a new storage location", description = "Creates a new storage facility")
    public ResponseEntity<ApiResponse<StorageLocationResponse>> createStorageLocation(
            @Valid @RequestBody StorageLocationRequest request) {
        StorageLocationResponse response = storageLocationService.createStorageLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Storage location created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a storage location", description = "Updates an existing storage location")
    public ResponseEntity<ApiResponse<StorageLocationResponse>> updateStorageLocation(
            @Parameter(description = "Storage Location ID") @PathVariable Long id,
            @Valid @RequestBody StorageLocationRequest request) {
        StorageLocationResponse response = storageLocationService.updateStorageLocation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Storage location updated successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get storage location by ID", description = "Retrieves a specific storage location")
    public ResponseEntity<ApiResponse<StorageLocationResponse>> getStorageLocationById(
            @Parameter(description = "Storage Location ID") @PathVariable Long id) {
        StorageLocationResponse response = storageLocationService.getStorageLocationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all storage locations", description = "Retrieves all storage locations with pagination")
    public ResponseEntity<ApiResponse<PageResponse<StorageLocationResponse>>> getAllStorageLocations(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<StorageLocationResponse> response = storageLocationService.getAllStorageLocations(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available storage locations", description = "Retrieves storage locations with available capacity")
    public ResponseEntity<ApiResponse<List<StorageLocationResponse>>> getAvailableStorageLocations() {
        List<StorageLocationResponse> response = storageLocationService.getAvailableStorageLocations();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a storage location", description = "Deletes a storage location if no vehicles are assigned")
    public ResponseEntity<ApiResponse<Void>> deleteStorageLocation(
            @Parameter(description = "Storage Location ID") @PathVariable Long id) {
        storageLocationService.deleteStorageLocation(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Storage location deleted successfully", null));
    }

    @GetMapping("/{id}/cars")
    @Operation(summary = "Get cars at location", description = "Retrieves all cars at a specific storage location")
    public ResponseEntity<ApiResponse<PageResponse<?>>> getCarsAtLocation(
            @Parameter(description = "Storage Location ID") @PathVariable Long id,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<?> response = storageLocationService.getCarsAtLocation(id, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/capacity-check")
    @Operation(summary = "Check capacity availability", description = "Checks if a storage location has available capacity")
    public ResponseEntity<ApiResponse<Boolean>> hasAvailableCapacity(
            @Parameter(description = "Storage Location ID") @PathVariable Long id) {
        boolean hasCapacity = storageLocationService.hasAvailableCapacity(id);
        return ResponseEntity.ok(ApiResponse.success(hasCapacity));
    }
}
