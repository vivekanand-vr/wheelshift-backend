package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.CarRequest;
import com.wheelshiftpro.dto.response.CarResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.entity.Car;
import com.wheelshiftpro.entity.CarModel;
import com.wheelshiftpro.entity.CarMovement;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.StorageLocation;
import com.wheelshiftpro.enums.CarStatus;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.CarMapper;
import com.wheelshiftpro.repository.CarModelRepository;
import com.wheelshiftpro.repository.CarMovementRepository;
import com.wheelshiftpro.repository.CarRepository;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.FinancialTransactionRepository;
import com.wheelshiftpro.repository.StorageLocationRepository;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.service.AuditService;
import com.wheelshiftpro.service.CarService;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarModelRepository carModelRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final CarMovementRepository carMovementRepository;
    private final FinancialTransactionRepository financialTransactionRepository;
    private final EmployeeRepository employeeRepository;
    private final CarMapper carMapper;
    private final AuditService auditService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CarResponse createCar(CarRequest request) {
        log.debug("Creating car with VIN: {}", request.getVinNumber());

        if (carRepository.existsByVinNumber(request.getVinNumber())) {
            throw new DuplicateResourceException("Car", "VIN", request.getVinNumber());
        }

        if (request.getRegistrationNumber() != null &&
                carRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateResourceException("Car", "Registration Number", request.getRegistrationNumber());
        }

        CarModel carModel = carModelRepository.findById(request.getCarModelId())
                .orElseThrow(() -> new ResourceNotFoundException("CarModel", "id", request.getCarModelId()));

        StorageLocation location = null;
        if (request.getStorageLocationId() != null) {
            location = storageLocationRepository.findById(request.getStorageLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("StorageLocation", "id",
                            request.getStorageLocationId()));

            if (!location.hasCapacity()) {
                throw new BusinessException("Storage location has reached maximum capacity", "LOCATION_FULL");
            }
        }

        Car car = carMapper.toEntity(request);
        car.setCarModel(carModel);
        car.setStorageLocation(location);

        Car saved = carRepository.save(car);

        if (saved.getStorageLocation() != null) {
            location.setCurrentCarCount(location.getCurrentCarCount() + 1);
            storageLocationRepository.save(location);
        }

        log.info("Created car with ID: {} and VIN: {}", saved.getId(), saved.getVinNumber());
        auditService.log(AuditCategory.CAR, saved.getId(), "CREATE", AuditLevel.REGULAR,
                resolveCurrentEmployee(), "VIN: " + saved.getVinNumber());
        return carMapper.toResponse(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CarResponse updateCar(Long id, CarRequest request) {
        log.debug("Updating car ID: {}", id);

        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", id));

        // Guard VIN uniqueness when it is being changed
        if (request.getVinNumber() != null && !request.getVinNumber().equals(car.getVinNumber())) {
            if (carRepository.existsByVinNumberAndIdNot(request.getVinNumber(), id)) {
                throw new DuplicateResourceException("Car", "VIN", request.getVinNumber());
            }
        }

        // Guard registration-number uniqueness when it is being changed
        if (request.getRegistrationNumber() != null &&
                !request.getRegistrationNumber().equals(car.getRegistrationNumber())) {
            if (carRepository.existsByRegistrationNumberAndIdNot(request.getRegistrationNumber(), id)) {
                throw new DuplicateResourceException("Car", "Registration Number", request.getRegistrationNumber());
            }
        }

        // Update car model if provided
        if (request.getCarModelId() != null) {
            CarModel carModel = carModelRepository.findById(request.getCarModelId())
                    .orElseThrow(() -> new ResourceNotFoundException("CarModel", "id", request.getCarModelId()));
            car.setCarModel(carModel);
        }

        // If the storage location is changing, use full move logic (count updates + movement record)
        if (request.getStorageLocationId() != null) {
            Long currentLocationId = car.getStorageLocation() != null ? car.getStorageLocation().getId() : null;
            if (!request.getStorageLocationId().equals(currentLocationId)) {
                applyCarMove(car, request.getStorageLocationId());
            }
        }

        carMapper.updateEntityFromRequest(request, car);
        Car updated = carRepository.save(car);

        log.info("Updated car ID: {}", id);
        auditService.log(AuditCategory.CAR, updated.getId(), "UPDATE", AuditLevel.REGULAR,
                resolveCurrentEmployee(), null);
        return carMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CarResponse getCarById(Long id) {
        log.debug("Fetching car ID: {}", id);

        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", id));

        return carMapper.toResponse(car);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CarResponse> getAllCars(int page, int size) {
        log.debug("Fetching all cars - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Car> carsPage = carRepository.findAll(pageable);

        return buildPageResponse(carsPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCar(Long id) {
        log.debug("Deleting car ID: {}", id);

        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", id));

        if (car.getStatus() == CarStatus.RESERVED || car.getStatus() == CarStatus.SOLD) {
            throw new BusinessException("Cannot delete car with status: " + car.getStatus(), "INVALID_CAR_STATUS");
        }

        if (financialTransactionRepository.existsByCarId(id)) {
            throw new BusinessException(
                    "Cannot delete car with existing financial transactions", "CAR_HAS_TRANSACTIONS");
        }

        if (car.getStorageLocation() != null) {
            StorageLocation location = car.getStorageLocation();
            location.setCurrentCarCount(location.getCurrentCarCount() - 1);
            storageLocationRepository.save(location);
        }

        String vinNumber = car.getVinNumber();
        carRepository.delete(car);
        auditService.log(AuditCategory.CAR, id, "DELETE", AuditLevel.HIGH,
                resolveCurrentEmployee(), "VIN: " + vinNumber);
        log.info("Deleted car ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CarResponse> searchCars(CarStatus status, Long locationId, String make,
            String model, Integer minYear, Integer maxYear,
            BigDecimal minPrice, BigDecimal maxPrice,
            int page, int size) {
        log.debug("Searching cars with filters");

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Car> carsPage = carRepository.searchCars(status, locationId, make, model, minYear, maxYear,
                minPrice, maxPrice, pageable);

        return buildPageResponse(carsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CarResponse getCarByVin(String vinNumber) {
        log.debug("Fetching car by VIN: {}", vinNumber);

        Car car = carRepository.findByVinNumber(vinNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "vinNumber", vinNumber));

        return carMapper.toResponse(car);
    }

    @Override
    @Transactional(readOnly = true)
    public CarResponse getCarByRegistration(String registrationNumber) {
        log.debug("Fetching car by registration: {}", registrationNumber);

        Car car = carRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "registrationNumber", registrationNumber));

        return carMapper.toResponse(car);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CarResponse moveCarToLocation(Long carId, Long locationId) {
        log.debug("Moving car ID: {} to location ID: {}", carId, locationId);

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", carId));

        applyCarMove(car, locationId);

        Car updated = carRepository.save(car);
        log.info("Moved car ID: {} to location ID: {}", carId, locationId);
        return carMapper.toResponse(updated);
    }

    /**
     * Internal helper: move a car to a new location, update counts and create an audit movement record.
     * The car entity is NOT saved here — caller must save() after.
     */
    private void applyCarMove(Car car, Long newLocationId) {
        StorageLocation newLocation = storageLocationRepository.findById(newLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("StorageLocation", "id", newLocationId));

        if (!newLocation.hasCapacity()) {
            throw new BusinessException("Storage location has reached maximum capacity", "LOCATION_FULL");
        }

        StorageLocation oldLocation = car.getStorageLocation();

        if (oldLocation != null) {
            oldLocation.setCurrentCarCount(oldLocation.getCurrentCarCount() - 1);
            storageLocationRepository.save(oldLocation);
        }

        newLocation.setCurrentCarCount(newLocation.getCurrentCarCount() + 1);
        storageLocationRepository.save(newLocation);

        Employee movedBy = resolveCurrentEmployee();

        CarMovement movement = CarMovement.builder()
                .car(car)
                .fromLocation(oldLocation)
                .toLocation(newLocation)
                .movedAt(LocalDateTime.now())
                .movedBy(movedBy)
                .build();
        carMovementRepository.save(movement);
        auditService.log(AuditCategory.CAR, car.getId(), "MOVE", AuditLevel.REGULAR, movedBy,
                "From location " + (oldLocation != null ? oldLocation.getId() : "none")
                        + " to " + newLocation.getId());

        car.setStorageLocation(newLocation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CarResponse updateCarStatus(Long carId, CarStatus newStatus) {
        log.debug("Updating car ID: {} status to: {}", carId, newStatus);

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", carId));

        // Capture old status before mutation for audit detail
        CarStatus previousStatus = car.getStatus();

        // Validate status transitions
        validateStatusTransition(previousStatus, newStatus);

        car.setStatus(newStatus);
        Car updated = carRepository.save(car);

        log.info("Updated car ID: {} status to: {}", carId, newStatus);
        auditService.log(AuditCategory.CAR, carId, "STATUS_CHANGE", AuditLevel.HIGH,
                resolveCurrentEmployee(), "From " + previousStatus + " to " + newStatus);
        return carMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getInventoryStatistics() {
        log.debug("Calculating inventory statistics");

        long totalCars = carRepository.count();
        long availableCars = carRepository.countByStatus(CarStatus.AVAILABLE);

        return Map.of(
                "totalCars", totalCars,
                "availableCars", availableCars);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateInventoryValue() {
        log.debug("Calculating total inventory value");

        BigDecimal total = carRepository.calculateTotalInventoryValue();
        return total != null ? total : BigDecimal.ZERO;
    }

    private void validateStatusTransition(CarStatus currentStatus, CarStatus newStatus) {
        if (currentStatus == CarStatus.SOLD) {
            throw new BusinessException("Cannot change status of a sold car", "INVALID_STATUS_TRANSITION");
        }

        if (newStatus == CarStatus.SOLD) {
            throw new BusinessException(
                    "Status cannot be set to SOLD directly; create a sale transaction instead",
                    "INVALID_STATUS_TRANSITION");
        }

        if (currentStatus == CarStatus.MAINTENANCE && newStatus == CarStatus.RESERVED) {
            throw new BusinessException("Cannot reserve a car that is under maintenance",
                    "INVALID_STATUS_TRANSITION");
        }

        if (currentStatus == CarStatus.RESERVED && newStatus == CarStatus.AVAILABLE) {
            log.info("Reverting reservation status to available for car");
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

    private PageResponse<CarResponse> buildPageResponse(Page<Car> page) {
        List<CarResponse> content = carMapper.toResponseList(page.getContent());

        return PageResponse.<CarResponse>builder()
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
