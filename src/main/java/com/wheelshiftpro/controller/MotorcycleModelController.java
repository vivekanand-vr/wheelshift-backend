package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.MotorcycleModelRequest;
import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.MotorcycleModelResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.enums.FuelType;
import com.wheelshiftpro.enums.MotorcycleVehicleType;
import com.wheelshiftpro.service.MotorcycleModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/motorcycle-models")
@RequiredArgsConstructor
@Tag(name = "Motorcycle Model Management", description = "APIs for managing motorcycle models, makes, models, and variants")
public class MotorcycleModelController {

    private final MotorcycleModelService motorcycleModelService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a new motorcycle model", description = "Creates a new motorcycle model with specified details")
    public ResponseEntity<ApiResponse<MotorcycleModelResponse>> createMotorcycleModel(
            @Valid @RequestBody MotorcycleModelRequest request) {
        MotorcycleModelResponse response = motorcycleModelService.createMotorcycleModel(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Motorcycle model created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a motorcycle model", description = "Updates an existing motorcycle model by ID")
    public ResponseEntity<ApiResponse<MotorcycleModelResponse>> updateMotorcycleModel(
            @Parameter(description = "Motorcycle Model ID") @PathVariable Long id,
            @Valid @RequestBody MotorcycleModelRequest request) {
        MotorcycleModelResponse response = motorcycleModelService.updateMotorcycleModel(id, request);
        return ResponseEntity.ok(ApiResponse.success("Motorcycle model updated successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get motorcycle model by ID", description = "Retrieves a specific motorcycle model by its ID")
    public ResponseEntity<ApiResponse<MotorcycleModelResponse>> getMotorcycleModelById(
            @Parameter(description = "Motorcycle Model ID") @PathVariable Long id) {
        MotorcycleModelResponse response = motorcycleModelService.getMotorcycleModelById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all motorcycle models", description = "Retrieves all motorcycle models with pagination")
    public ResponseEntity<ApiResponse<PageResponse<MotorcycleModelResponse>>> getAllMotorcycleModels(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<MotorcycleModelResponse> response = motorcycleModelService.getAllMotorcycleModels(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Delete a motorcycle model", description = "Deletes a motorcycle model by ID if no motorcycles are associated")
    public ResponseEntity<ApiResponse<Void>> deleteMotorcycleModel(
            @Parameter(description = "Motorcycle Model ID") @PathVariable Long id) {
        motorcycleModelService.deleteMotorcycleModel(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Motorcycle model deleted successfully", null));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search motorcycle models", description = "Search motorcycle models with multiple filter criteria")
    public ResponseEntity<ApiResponse<PageResponse<MotorcycleModelResponse>>> searchMotorcycleModels(
            @Parameter(description = "Make filter") @RequestParam(required = false) String make,
            @Parameter(description = "Model filter") @RequestParam(required = false) String model,
            @Parameter(description = "Fuel type filter") @RequestParam(required = false) FuelType fuelType,
            @Parameter(description = "Vehicle type filter") @RequestParam(required = false) MotorcycleVehicleType vehicleType,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<MotorcycleModelResponse> response = motorcycleModelService.searchMotorcycleModels(
                make, model, fuelType, vehicleType, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/makes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all makes", description = "Retrieves all distinct motorcycle makes")
    public ResponseEntity<ApiResponse<List<String>>> getAllMakes() {
        List<String> makes = motorcycleModelService.getAllMakes();
        return ResponseEntity.ok(ApiResponse.success(makes));
    }

    @GetMapping("/models")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get models by make", description = "Retrieves all models for a specific make")
    public ResponseEntity<ApiResponse<List<String>>> getModelsByMake(
            @Parameter(description = "Motorcycle make") @RequestParam String make) {
        List<String> models = motorcycleModelService.getModelsByMake(make);
        return ResponseEntity.ok(ApiResponse.success(models));
    }

    @GetMapping("/variants")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get variants", description = "Retrieves all variants for a specific make and model")
    public ResponseEntity<ApiResponse<List<String>>> getVariantsByMakeAndModel(
            @Parameter(description = "Motorcycle make") @RequestParam String make,
            @Parameter(description = "Motorcycle model") @RequestParam String model) {
        List<String> variants = motorcycleModelService.getVariantsByMakeAndModel(make, model);
        return ResponseEntity.ok(ApiResponse.success(variants));
    }

    @GetMapping("/fuel-types")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all fuel types", description = "Retrieves all available fuel types")
    public ResponseEntity<ApiResponse<List<FuelType>>> getAllFuelTypes() {
        List<FuelType> fuelTypes = motorcycleModelService.getAllFuelTypes();
        return ResponseEntity.ok(ApiResponse.success(fuelTypes));
    }

    @GetMapping("/vehicle-types")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all vehicle types", description = "Retrieves all available motorcycle vehicle types")
    public ResponseEntity<ApiResponse<List<MotorcycleVehicleType>>> getAllVehicleTypes() {
        List<MotorcycleVehicleType> vehicleTypes = motorcycleModelService.getAllVehicleTypes();
        return ResponseEntity.ok(ApiResponse.success(vehicleTypes));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get active models", description = "Retrieves all active motorcycle models")
    public ResponseEntity<ApiResponse<List<MotorcycleModelResponse>>> getActiveModels() {
        List<MotorcycleModelResponse> activeModels = motorcycleModelService.getActiveModels();
        return ResponseEntity.ok(ApiResponse.success(activeModels));
    }

    @GetMapping("/exists/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check if model exists", description = "Checks if a motorcycle model exists by ID")
    public ResponseEntity<ApiResponse<Boolean>> existsById(
            @Parameter(description = "Motorcycle Model ID") @PathVariable Long id) {
        boolean exists = motorcycleModelService.existsById(id);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}
