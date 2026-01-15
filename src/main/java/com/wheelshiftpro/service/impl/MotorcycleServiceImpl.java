package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.MotorcycleRequest;
import com.wheelshiftpro.dto.response.MotorcycleResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.entity.Motorcycle;
import com.wheelshiftpro.entity.MotorcycleMovement;
import com.wheelshiftpro.entity.StorageLocation;
import com.wheelshiftpro.enums.MotorcycleStatus;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.MotorcycleMapper;
import com.wheelshiftpro.repository.MotorcycleModelRepository;
import com.wheelshiftpro.repository.MotorcycleMovementRepository;
import com.wheelshiftpro.repository.MotorcycleRepository;
import com.wheelshiftpro.repository.StorageLocationRepository;
import com.wheelshiftpro.service.MotorcycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MotorcycleServiceImpl implements MotorcycleService {

    private final MotorcycleRepository motorcycleRepository;
    private final MotorcycleModelRepository motorcycleModelRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final MotorcycleMovementRepository motorcycleMovementRepository;
    private final MotorcycleMapper motorcycleMapper;

    @Override
    public MotorcycleResponse createMotorcycle(MotorcycleRequest request) {
        log.debug("Creating motorcycle with VIN: {}", request.getVinNumber());

        // Validate VIN uniqueness
        if (motorcycleRepository.existsByVinNumber(request.getVinNumber())) {
            throw new DuplicateResourceException("Motorcycle", "VIN", request.getVinNumber());
        }

        // Validate registration uniqueness if provided
        if (request.getRegistrationNumber() != null && 
            motorcycleRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateResourceException("Motorcycle", "Registration Number", request.getRegistrationNumber());
        }

        // Validate motorcycle model exists
        if (!motorcycleModelRepository.existsById(request.getMotorcycleModelId())) {
            throw new ResourceNotFoundException("MotorcycleModel", "id", request.getMotorcycleModelId());
        }

        // Validate storage location if provided
        if (request.getStorageLocationId() != null) {
            StorageLocation location = storageLocationRepository.findById(request.getStorageLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("StorageLocation", "id", request.getStorageLocationId()));

            if (!location.hasCapacity()) {
                throw new BusinessException("Storage location has reached maximum capacity", "LOCATION_FULL");
            }
        }

        Motorcycle motorcycle = motorcycleMapper.toEntity(request);
        Motorcycle saved = motorcycleRepository.save(motorcycle);

        // Update storage location count
        if (saved.getStorageLocation() != null) {
            StorageLocation location = saved.getStorageLocation();
            location.setCurrentVehicleCount(location.getCurrentVehicleCount() + 1);
            storageLocationRepository.save(location);
        }

        log.info("Created motorcycle with ID: {} and VIN: {}", saved.getId(), saved.getVinNumber());
        return motorcycleMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MotorcycleResponse getMotorcycleById(Long id) {
        log.debug("Fetching motorcycle ID: {}", id);

        Motorcycle motorcycle = motorcycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Motorcycle", "id", id));

        return motorcycleMapper.toResponse(motorcycle);
    }

    @Override
    @Transactional(readOnly = true)
    public MotorcycleResponse getMotorcycleByVin(String vinNumber) {
        log.debug("Fetching motorcycle by VIN: {}", vinNumber);

        Motorcycle motorcycle = motorcycleRepository.findByVinNumber(vinNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Motorcycle", "vinNumber", vinNumber));

        return motorcycleMapper.toResponse(motorcycle);
    }

    @Override
    @Transactional(readOnly = true)
    public MotorcycleResponse getMotorcycleByRegistration(String registrationNumber) {
        log.debug("Fetching motorcycle by registration: {}", registrationNumber);

        Motorcycle motorcycle = motorcycleRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Motorcycle", "registrationNumber", registrationNumber));

        return motorcycleMapper.toResponse(motorcycle);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MotorcycleResponse> getAllMotorcycles(int page, int size, String sortBy, String sortDir) {
        log.debug("Fetching all motorcycles - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy != null ? sortBy : "createdAt"));
        Page<Motorcycle> motorcyclesPage = motorcycleRepository.findAll(pageable);

        return buildPageResponse(motorcyclesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MotorcycleResponse> getMotorcyclesByStatus(MotorcycleStatus status) {
        log.debug("Fetching motorcycles with status: {}", status);

        List<Motorcycle> motorcycles = motorcycleRepository.findByStatus(status);
        return motorcycleMapper.toResponseList(motorcycles);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MotorcycleResponse> getMotorcyclesByStorageLocation(Long storageLocationId) {
        log.debug("Fetching motorcycles at location ID: {}", storageLocationId);

        List<Motorcycle> motorcycles = motorcycleRepository.findByStorageLocationId(storageLocationId);
        return motorcycleMapper.toResponseList(motorcycles);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MotorcycleResponse> getMotorcyclesByModel(Long motorcycleModelId) {
        log.debug("Fetching motorcycles by model ID: {}", motorcycleModelId);

        List<Motorcycle> motorcycles = motorcycleRepository.findByMotorcycleModelId(motorcycleModelId);
        return motorcycleMapper.toResponseList(motorcycles);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MotorcycleResponse> searchMotorcycles(String searchTerm, int page, int size) {
        log.debug("Searching motorcycles with term: {}", searchTerm);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Motorcycle> motorcyclesPage = motorcycleRepository.searchMotorcycles(searchTerm, pageable);

        return buildPageResponse(motorcyclesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MotorcycleResponse> getMotorcyclesByPriceRange(
            BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        log.debug("Fetching motorcycles in price range: {} - {}", minPrice, maxPrice);

        Pageable pageable = PageRequest.of(page, size, Sort.by("sellingPrice").ascending());
        Page<Motorcycle> motorcyclesPage = motorcycleRepository.findByPriceRange(minPrice, maxPrice, pageable);

        return buildPageResponse(motorcyclesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MotorcycleResponse> getAvailableMotorcycles() {
        log.debug("Fetching available motorcycles");

        List<Motorcycle> motorcycles = motorcycleRepository.findByStatus(MotorcycleStatus.AVAILABLE);
        return motorcycleMapper.toResponseList(motorcycles);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MotorcycleResponse> getMotorcyclesNeedingAttention() {
        log.debug("Fetching motorcycles needing attention");

        List<Motorcycle> motorcycles = motorcycleRepository.findMotorcyclesNeedingAttention();
        return motorcycleMapper.toResponseList(motorcycles);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MotorcycleResponse> getRecentlyAddedMotorcycles(int limit) {
        log.debug("Fetching recently added motorcycles, limit: {}", limit);

        Pageable pageable = PageRequest.of(0, limit);
        Page<Motorcycle> motorcyclesPage = motorcycleRepository.findRecentlyAdded(pageable);
        
        return motorcycleMapper.toResponseList(motorcyclesPage.getContent());
    }

    @Override
    public MotorcycleResponse updateMotorcycle(Long id, MotorcycleRequest request) {
        log.debug("Updating motorcycle ID: {}", id);

        Motorcycle motorcycle = motorcycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Motorcycle", "id", id));

        // Check for VIN conflicts (excluding current motorcycle)
        if (!motorcycle.getVinNumber().equals(request.getVinNumber()) && 
            motorcycleRepository.existsByVinNumber(request.getVinNumber())) {
            throw new DuplicateResourceException("Motorcycle", "VIN", request.getVinNumber());
        }

        // Check for registration conflicts (excluding current motorcycle)
        if (request.getRegistrationNumber() != null && 
            !request.getRegistrationNumber().equals(motorcycle.getRegistrationNumber()) &&
            motorcycleRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateResourceException("Motorcycle", "Registration Number", request.getRegistrationNumber());
        }

        motorcycleMapper.updateEntityFromRequest(request, motorcycle);
        Motorcycle updated = motorcycleRepository.save(motorcycle);

        log.info("Updated motorcycle ID: {}", id);
        return motorcycleMapper.toResponse(updated);
    }

    @Override
    public MotorcycleResponse updateMotorcycleStatus(Long id, MotorcycleStatus status) {
        log.debug("Updating motorcycle ID: {} status to: {}", id, status);

        Motorcycle motorcycle = motorcycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Motorcycle", "id", id));

        // Validate status transitions
        validateStatusTransition(motorcycle.getStatus(), status);

        motorcycle.setStatus(status);
        Motorcycle updated = motorcycleRepository.save(motorcycle);

        log.info("Updated motorcycle ID: {} status to: {}", id, status);
        return motorcycleMapper.toResponse(updated);
    }

    @Override
    public MotorcycleResponse moveMotorcycleToLocation(Long motorcycleId, Long locationId) {
        log.debug("Moving motorcycle ID: {} to location ID: {}", motorcycleId, locationId);

        Motorcycle motorcycle = motorcycleRepository.findById(motorcycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Motorcycle", "id", motorcycleId));

        StorageLocation newLocation = storageLocationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("StorageLocation", "id", locationId));

        if (!newLocation.hasCapacity()) {
            throw new BusinessException("Storage location has reached maximum capacity", "LOCATION_FULL");
        }

        StorageLocation oldLocation = motorcycle.getStorageLocation();

        // Update old location count
        if (oldLocation != null) {
            oldLocation.setCurrentVehicleCount(oldLocation.getCurrentVehicleCount() - 1);
            storageLocationRepository.save(oldLocation);
        }

        // Update new location count
        newLocation.setCurrentVehicleCount(newLocation.getCurrentVehicleCount() + 1);
        storageLocationRepository.save(newLocation);

        // Create movement record
        MotorcycleMovement movement = MotorcycleMovement.builder()
                .motorcycle(motorcycle)
                .fromLocation(oldLocation)
                .toLocation(newLocation)
                .movedAt(LocalDateTime.now())
                .build();
        motorcycleMovementRepository.save(movement);

        // Update motorcycle location
        motorcycle.setStorageLocation(newLocation);
        Motorcycle updated = motorcycleRepository.save(motorcycle);

        log.info("Moved motorcycle ID: {} to location ID: {}", motorcycleId, locationId);
        return motorcycleMapper.toResponse(updated);
    }

    @Override
    public void deleteMotorcycle(Long id) {
        log.debug("Deleting motorcycle ID: {}", id);

        Motorcycle motorcycle = motorcycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Motorcycle", "id", id));

        // Business rule: Cannot delete reserved or sold motorcycles
        if (motorcycle.getStatus() == MotorcycleStatus.RESERVED || 
            motorcycle.getStatus() == MotorcycleStatus.SOLD) {
            throw new BusinessException("Cannot delete motorcycle with status: " + motorcycle.getStatus(), 
                    "INVALID_MOTORCYCLE_STATUS");
        }

        // Update storage location count
        if (motorcycle.getStorageLocation() != null) {
            StorageLocation location = motorcycle.getStorageLocation();
            location.setCurrentVehicleCount(location.getCurrentVehicleCount() - 1);
            storageLocationRepository.save(location);
        }

        motorcycleRepository.delete(motorcycle);
        log.info("Deleted motorcycle ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countMotorcyclesByStatus(MotorcycleStatus status) {
        log.debug("Counting motorcycles with status: {}", status);
        return motorcycleRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalInventoryValue() {
        log.debug("Calculating total motorcycle inventory value");

        BigDecimal total = motorcycleRepository.getTotalInventoryValue();
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAverageSellingPrice() {
        log.debug("Calculating average motorcycle selling price");

        BigDecimal average = motorcycleRepository.getAverageSellingPrice();
        return average != null ? average : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByVin(String vinNumber) {
        return motorcycleRepository.existsByVinNumber(vinNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRegistration(String registrationNumber) {
        return motorcycleRepository.existsByRegistrationNumber(registrationNumber);
    }

    private void validateStatusTransition(MotorcycleStatus currentStatus, MotorcycleStatus newStatus) {
        // Business rules for status transitions
        if (currentStatus == MotorcycleStatus.SOLD && newStatus != MotorcycleStatus.SOLD) {
            throw new BusinessException("Cannot change status of a sold motorcycle", "INVALID_STATUS_TRANSITION");
        }

        if (currentStatus == MotorcycleStatus.RESERVED && newStatus == MotorcycleStatus.AVAILABLE) {
            // Allow only when reservation is cancelled
            log.info("Reverting reservation status to available");
        }

        if (currentStatus == MotorcycleStatus.MAINTENANCE && newStatus == MotorcycleStatus.RESERVED) {
            throw new BusinessException("Cannot reserve motorcycle in maintenance", "INVALID_STATUS_TRANSITION");
        }
    }

    private PageResponse<MotorcycleResponse> buildPageResponse(Page<Motorcycle> page) {
        List<MotorcycleResponse> content = motorcycleMapper.toResponseList(page.getContent());

        return PageResponse.<MotorcycleResponse>builder()
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
