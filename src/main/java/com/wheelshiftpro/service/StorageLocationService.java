package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.StorageLocationRequest;
import com.wheelshiftpro.dto.response.CarResponse;
import com.wheelshiftpro.dto.response.MotorcycleResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.dto.response.StorageLocationResponse;

import java.util.List;

/**
 * Service interface for StorageLocation operations.
 * Manages storage facilities and capacity tracking.
 */
public interface StorageLocationService {

    /**
     * Creates a new storage location.
     *
     * @param request the storage location data
     * @return created storage location response
     * @throws com.wheelshiftpro.exception.DuplicateResourceException if name already exists
     */
    StorageLocationResponse createStorageLocation(StorageLocationRequest request);

    /**
     * Updates an existing storage location.
     * Cannot update capacity to less than current vehicle count.
     *
     * @param id the storage location ID
     * @param request updated storage location data
     * @return updated storage location response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if location not found
     * @throws com.wheelshiftpro.exception.BusinessException if capacity validation fails
     */
    StorageLocationResponse updateStorageLocation(Long id, StorageLocationRequest request);

    /**
     * Retrieves a storage location by ID.
     *
     * @param id the storage location ID
     * @return storage location response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if location not found
     */
    StorageLocationResponse getStorageLocationById(Long id);

    /**
     * Retrieves all storage locations with pagination.
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @return paginated storage location responses
     */
    PageResponse<StorageLocationResponse> getAllStorageLocations(int page, int size);

    /**
     * Retrieves all storage locations with available capacity.
     *
     * @return list of available storage locations
     */
    List<StorageLocationResponse> getAvailableStorageLocations();

    /**
     * Deletes a storage location by ID.
     * Cannot delete if vehicles are assigned.
     *
     * @param id the storage location ID
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if location not found
     * @throws com.wheelshiftpro.exception.BusinessException if vehicles are assigned
     */
    void deleteStorageLocation(Long id);

    /**
     * Retrieves cars at a specific storage location with pagination.
     *
     * @param locationId the storage location ID
     * @param page page number
     * @param size page size
     * @return paginated list of cars
     */
    PageResponse<CarResponse> getCarsAtLocation(Long locationId, int page, int size);

    /**
     * Retrieves motorcycles at a specific storage location with pagination.
     *
     * @param locationId the storage location ID
     * @param page page number
     * @param size page size
     * @return paginated list of motorcycles
     */
    PageResponse<MotorcycleResponse> getMotorcyclesAtLocation(Long locationId, int page, int size);

    /**
     * Checks if a location has available capacity.
     *
     * @param locationId the storage location ID
     * @return true if capacity available
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if location not found
     */
    boolean hasAvailableCapacity(Long locationId);
}
