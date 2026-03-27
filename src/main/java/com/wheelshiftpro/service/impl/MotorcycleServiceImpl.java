package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.MotorcycleRequest;
import com.wheelshiftpro.dto.response.MotorcycleResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.Motorcycle;
import com.wheelshiftpro.entity.MotorcycleModel;
import com.wheelshiftpro.entity.MotorcycleMovement;
import com.wheelshiftpro.entity.StorageLocation;
import com.wheelshiftpro.enums.MotorcycleStatus;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.MotorcycleMapper;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.FinancialTransactionRepository;
import com.wheelshiftpro.repository.MotorcycleModelRepository;
import com.wheelshiftpro.repository.MotorcycleMovementRepository;
import com.wheelshiftpro.repository.MotorcycleRepository;
import com.wheelshiftpro.repository.StorageLocationRepository;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.MotorcycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final FinancialTransactionRepository financialTransactionRepository;
    private final EmployeeRepository employeeRepository;
    private final MotorcycleMapper motorcycleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MotorcycleResponse createMotorcycle(MotorcycleRequest request) {
        log.debug("Creating motorcycle with VIN: {}", request.getVinNumber());

        if (motorcycleRepository.existsByVinNumber(request.getVinNumber())) {
            throw new DuplicateResourceException("Motorcycle", "VIN", request.getVinNumber());
        }

        if (request.getRegistrationNumber() != null &&
                motorcycleRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateResourceException("Motorcycle", "Registration Number", request.getRegistrationNumber());
        }

        MotorcycleModel motorcycleModel = motorcycleModelRepository.findById(request.getMotorcycleModelId())
                .orElseThrow(() -> new ResourceNotFoundException("MotorcycleModel", "id", request.getMotorcycleModelId()));

        StorageLocation location = null;
        if (request.getStorageLocationId() != null) {
            location = storageLocationRepository.findById(request.getStorageLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("StorageLocation", "id",
                            request.getStorageLocationId()));

            if (!location.hasCapacity()) {
                throw new BusinessException("Storage location has reached maximum capacity", "LOCATION_FULL");
            }
        }

        Motorcycle motorcycle = motorcycleMapper.toEntity(request);
        motorcycle.setMotorcycleModel(motorcycleModel);
        motorcycle.setStorageLocation(location);

        Motorcycle saved = motorcycleRepository.save(motorcycle);

        if (saved.getStorageLocation() != null) {
            location.setCurrentMotorcycleCount(location.getCurrentMotorcycleCount() + 1);
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
    @Transactional(rollbackFor = Exception.class)
    public MotorcycleResponse updateMotorcycle(Long id, MotorcycleRequest request) {
        log.debug("Updating motorcycle ID: {}", id);

        Motorcycle motorcycle = motorcycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Motorcycle", "id", id));

        // Guard VIN uniqueness when it is being changed
        if (request.getVinNumber() != null && !request.getVinNumber().equals(motorcycle.getVinNumber()) &&
                motorcycleRepository.existsByVinNumber(request.getVinNumber())) {
            throw new DuplicateResourceException("Motorcycle", "VIN", request.getVinNumber());
        }

        // Guard registration-number uniqueness when it is being changed
        if (request.getRegistrationNumber() != null &&
                !request.getRegistrationNumber().equals(motorcycle.getRegistrationNumber()) &&
                motorcycleRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateResourceException("Motorcycle", "Registration Number", request.getRegistrationNumber());
        }

        // Update model if provided
        if (request.getMotorcycleModelId() != null) {
            MotorcycleModel motorcycleModel = motorcycleModelRepository.findById(request.getMotorcycleModelId())
                    .orElseThrow(() -> new ResourceNotFoundException("MotorcycleModel", "id",
                            request.getMotorcycleModelId()));
            motorcycle.setMotorcycleModel(motorcycleModel);
        }

        // If the storage location is changing, use full move logic (count updates + movement record)
        if (request.getStorageLocationId() != null) {
            Long currentLocationId = motorcycle.getStorageLocation() != null
                    ? motorcycle.getStorageLocation().getId() : null;
            if (!request.getStorageLocationId().equals(currentLocationId)) {
                applyMotorcycleMove(motorcycle, request.getStorageLocationId());
            }
        }

        motorcycleMapper.updateEntityFromRequest(request, motorcycle);
        Motorcycle updated = motorcycleRepository.save(motorcycle);

        log.info("Updated motorcycle ID: {}", id);
        return motorcycleMapper.toResponse(updated);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
    public MotorcycleResponse moveMotorcycleToLocation(Long motorcycleId, Long locationId) {
        log.debug("Moving motorcycle ID: {} to location ID: {}", motorcycleId, locationId);

        Motorcycle motorcycle = motorcycleRepository.findById(motorcycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Motorcycle", "id", motorcycleId));

        applyMotorcycleMove(motorcycle, locationId);

        Motorcycle updated = motorcycleRepository.save(motorcycle);
        log.info("Moved motorcycle ID: {} to location ID: {}", motorcycleId, locationId);
        return motorcycleMapper.toResponse(updated);
    }

    /**
     * Internal helper: move a motorcycle to a new location, update counts and create an audit movement record.
     * The motorcycle entity is NOT saved here — caller must save() after.
     */
    private void applyMotorcycleMove(Motorcycle motorcycle, Long newLocationId) {
        StorageLocation newLocation = storageLocationRepository.findById(newLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("StorageLocation", "id", newLocationId));

        if (!newLocation.hasCapacity()) {
            throw new BusinessException("Storage location has reached maximum capacity", "LOCATION_FULL");
        }

        StorageLocation oldLocation = motorcycle.getStorageLocation();

        if (oldLocation != null) {
            oldLocation.setCurrentMotorcycleCount(oldLocation.getCurrentMotorcycleCount() - 1);
            storageLocationRepository.save(oldLocation);
        }

        newLocation.setCurrentMotorcycleCount(newLocation.getCurrentMotorcycleCount() + 1);
        storageLocationRepository.save(newLocation);

        Employee movedBy = resolveCurrentEmployee();

        MotorcycleMovement movement = MotorcycleMovement.builder()
                .motorcycle(motorcycle)
                .fromLocation(oldLocation)
                .toLocation(newLocation)
                .movedAt(LocalDateTime.now())
                .movedBy(movedBy)
                .build();
        motorcycleMovementRepository.save(movement);

        motorcycle.setStorageLocation(newLocation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMotorcycle(Long id) {
        log.debug("Deleting motorcycle ID: {}", id);

        Motorcycle motorcycle = motorcycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Motorcycle", "id", id));

        if (motorcycle.getStatus() == MotorcycleStatus.RESERVED ||
                motorcycle.getStatus() == MotorcycleStatus.SOLD) {
            throw new BusinessException("Cannot delete motorcycle with status: " + motorcycle.getStatus(),
                    "INVALID_MOTORCYCLE_STATUS");
        }

        if (financialTransactionRepository.existsByMotorcycleId(id)) {
            throw new BusinessException(
                    "Cannot delete motorcycle with existing financial transactions",
                    "MOTORCYCLE_HAS_TRANSACTIONS");
        }

        if (motorcycle.getStorageLocation() != null) {
            StorageLocation location = motorcycle.getStorageLocation();
            location.setCurrentMotorcycleCount(location.getCurrentMotorcycleCount() - 1);
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
        if (currentStatus == MotorcycleStatus.SOLD) {
            throw new BusinessException("Cannot change status of a sold motorcycle", "INVALID_STATUS_TRANSITION");
        }

        if (newStatus == MotorcycleStatus.SOLD) {
            throw new BusinessException(
                    "Status cannot be set to SOLD directly; create a sale transaction instead",
                    "INVALID_STATUS_TRANSITION");
        }

        if (currentStatus == MotorcycleStatus.MAINTENANCE && newStatus == MotorcycleStatus.RESERVED) {
            throw new BusinessException("Cannot reserve motorcycle in maintenance", "INVALID_STATUS_TRANSITION");
        }

        if (currentStatus == MotorcycleStatus.RESERVED && newStatus == MotorcycleStatus.AVAILABLE) {
            log.info("Reverting reservation status to available for motorcycle");
        }
    }

    /** Returns the Employee proxy for the currently authenticated user, or null for system actions. */
    private Employee resolveCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof EmployeeUserDetails userDetails) {
            return employeeRepository.getReferenceById(userDetails.getId());
        }
        return null;
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
