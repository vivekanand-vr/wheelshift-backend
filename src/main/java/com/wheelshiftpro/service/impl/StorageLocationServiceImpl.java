package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.StorageLocationRequest;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.dto.response.StorageLocationResponse;
import com.wheelshiftpro.entity.StorageLocation;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.StorageLocationMapper;
import com.wheelshiftpro.repository.StorageLocationRepository;
import com.wheelshiftpro.service.StorageLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StorageLocationServiceImpl implements StorageLocationService {

    private final StorageLocationRepository storageLocationRepository;
    private final StorageLocationMapper storageLocationMapper;

    @Override
    public StorageLocationResponse createStorageLocation(StorageLocationRequest request) {
        log.debug("Creating storage location: {}", request.getName());
        
        if (storageLocationRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("StorageLocation", "name", request.getName());
        }

        StorageLocation location = storageLocationMapper.toEntity(request);
        StorageLocation saved = storageLocationRepository.save(location);
        
        log.info("Created storage location with ID: {}", saved.getId());
        return storageLocationMapper.toResponse(saved);
    }

    @Override
    public StorageLocationResponse updateStorageLocation(Long id, StorageLocationRequest request) {
        log.debug("Updating storage location ID: {}", id);
        
        StorageLocation location = storageLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StorageLocation", "id", id));

        if (request.getTotalCapacity() != null && 
            request.getTotalCapacity() < location.getCurrentVehicleCount()) {
            throw new BusinessException(
                    "Cannot reduce capacity below current vehicle count: " + location.getCurrentVehicleCount(),
                    "CAPACITY_BELOW_CURRENT_COUNT");
        }

        storageLocationMapper.updateEntityFromRequest(request, location);
        StorageLocation updated = storageLocationRepository.save(location);
        
        log.info("Updated storage location ID: {}", id);
        return storageLocationMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public StorageLocationResponse getStorageLocationById(Long id) {
        log.debug("Fetching storage location ID: {}", id);
        
        StorageLocation location = storageLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StorageLocation", "id", id));
        
        return storageLocationMapper.toResponse(location);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StorageLocationResponse> getAllStorageLocations(int page, int size) {
        log.debug("Fetching all storage locations - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<StorageLocation> locationsPage = storageLocationRepository.findAll(pageable);
        
        return buildPageResponse(locationsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StorageLocationResponse> getAvailableStorageLocations() {
        log.debug("Fetching available storage locations");
        
        List<StorageLocation> locations = storageLocationRepository.findAvailableLocations();
        return storageLocationMapper.toResponseList(locations);
    }

    @Override
    public void deleteStorageLocation(Long id) {
        log.debug("Deleting storage location ID: {}", id);
        
        StorageLocation location = storageLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StorageLocation", "id", id));

        if (location.getCurrentVehicleCount() > 0) {
            throw new BusinessException(
                    "Cannot delete storage location. Vehicles are currently assigned.",
                    "LOCATION_HAS_VEHICLES");
        }

        storageLocationRepository.delete(location);
        log.info("Deleted storage location ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<?> getCarsAtLocation(Long locationId, int page, int size) {
        log.debug("Fetching cars at location ID: {}", locationId);
        
        if (!storageLocationRepository.existsById(locationId)) {
            throw new ResourceNotFoundException("StorageLocation", "id", locationId);
        }

        return PageResponse.builder()
                .content(List.of()) // Will be implemented when CarService is available
                .pageNumber(page)
                .pageSize(size)
                .totalElements(0)
                .totalPages(0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAvailableCapacity(Long locationId) {
        log.debug("Checking capacity for location ID: {}", locationId);
        
        StorageLocation location = storageLocationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("StorageLocation", "id", locationId));
        
        return location.hasCapacity();
    }

    private PageResponse<StorageLocationResponse> buildPageResponse(Page<StorageLocation> page) {
        List<StorageLocationResponse> content = storageLocationMapper.toResponseList(page.getContent());
        
        return PageResponse.<StorageLocationResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}
