package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.CarModelRequest;
import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.CarModelResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.enums.FuelType;
import com.wheelshiftpro.service.CarModelService;
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
@RequestMapping("/api/v1/car-models")
@RequiredArgsConstructor
@Tag(name = "Car Model Management", description = "APIs for managing car models, makes, models, and variants")
public class CarModelController {

    private final CarModelService carModelService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a new car model", description = "Creates a new car model with specified details")
    public ResponseEntity<ApiResponse<CarModelResponse>> createCarModel(
            @Valid @RequestBody CarModelRequest request) {
        CarModelResponse response = carModelService.createCarModel(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Car model created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a car model", description = "Updates an existing car model by ID")
    public ResponseEntity<ApiResponse<CarModelResponse>> updateCarModel(
            @Parameter(description = "Car Model ID") @PathVariable Long id,
            @Valid @RequestBody CarModelRequest request) {
        CarModelResponse response = carModelService.updateCarModel(id, request);
        return ResponseEntity.ok(ApiResponse.success("Car model updated successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get car model by ID", description = "Retrieves a specific car model by its ID")
    public ResponseEntity<ApiResponse<CarModelResponse>> getCarModelById(
            @Parameter(description = "Car Model ID") @PathVariable Long id) {
        CarModelResponse response = carModelService.getCarModelById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all car models", description = "Retrieves all car models with pagination")
    public ResponseEntity<ApiResponse<PageResponse<CarModelResponse>>> getAllCarModels(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<CarModelResponse> response = carModelService.getAllCarModels(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Delete a car model", description = "Deletes a car model by ID if no cars are associated")
    public ResponseEntity<ApiResponse<Void>> deleteCarModel(
            @Parameter(description = "Car Model ID") @PathVariable Long id) {
        carModelService.deleteCarModel(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Car model deleted successfully", null));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search car models", description = "Search car models with multiple filter criteria")
    public ResponseEntity<ApiResponse<PageResponse<CarModelResponse>>> searchCarModels(
            @Parameter(description = "Make filter") @RequestParam(required = false) String make,
            @Parameter(description = "Model filter") @RequestParam(required = false) String model,
            @Parameter(description = "Fuel type filter") @RequestParam(required = false) FuelType fuelType,
            @Parameter(description = "Body type filter") @RequestParam(required = false) String bodyType,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<CarModelResponse> response = carModelService.searchCarModels(
                make, model, fuelType, bodyType, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/makes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all makes", description = "Retrieves all distinct car makes")
    public ResponseEntity<ApiResponse<List<String>>> getAllMakes() {
        List<String> makes = carModelService.getAllMakes();
        return ResponseEntity.ok(ApiResponse.success(makes));
    }

    @GetMapping("/models")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get models by make", description = "Retrieves all models for a specific make")
    public ResponseEntity<ApiResponse<List<String>>> getModelsByMake(
            @Parameter(description = "Car make") @RequestParam String make) {
        List<String> models = carModelService.getModelsByMake(make);
        return ResponseEntity.ok(ApiResponse.success(models));
    }

    @GetMapping("/variants")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get variants", description = "Retrieves all variants for a specific make and model")
    public ResponseEntity<ApiResponse<List<String>>> getVariantsByMakeAndModel(
            @Parameter(description = "Car make") @RequestParam String make,
            @Parameter(description = "Car model") @RequestParam String model) {
        List<String> variants = carModelService.getVariantsByMakeAndModel(make, model);
        return ResponseEntity.ok(ApiResponse.success(variants));
    }

    @GetMapping("/fuel-types")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all fuel types", description = "Retrieves all available fuel types")
    public ResponseEntity<ApiResponse<List<FuelType>>> getAllFuelTypes() {
        List<FuelType> fuelTypes = carModelService.getAllFuelTypes();
        return ResponseEntity.ok(ApiResponse.success(fuelTypes));
    }

    @GetMapping("/body-types")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all body types", description = "Retrieves all available body types")
    public ResponseEntity<ApiResponse<List<String>>> getAllBodyTypes() {
        List<String> bodyTypes = carModelService.getAllBodyTypes();
        return ResponseEntity.ok(ApiResponse.success(bodyTypes));
    }
}
