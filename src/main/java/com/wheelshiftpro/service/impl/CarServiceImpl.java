package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.CarRequest;
import com.wheelshiftpro.dto.response.CarResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.entity.Car;
import com.wheelshiftpro.entity.CarModel;
import com.wheelshiftpro.entity.CarMovement;
import com.wheelshiftpro.entity.StorageLocation;
import com.wheelshiftpro.enums.CarStatus;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.CarMapper;
import com.wheelshiftpro.repository.CarModelRepository;
import com.wheelshiftpro.repository.CarMovementRepository;
import com.wheelshiftpro.repository.CarRepository;
import com.wheelshiftpro.repository.StorageLocationRepository;
import com.wheelshiftpro.service.CarService;
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
    private final CarMapper carMapper;

    @Override
    public CarResponse createCar(CarRequest request) {
        log.debug("Creating car with VIN: {}", request.getVinNumber());

        if (carRepository.existsByVinNumber(request.getVinNumber())) {
            throw new DuplicateResourceException("Car", "VIN", request.getVinNumber());
        }

        // if (!carModelRepository.existsById(request.getCarModelId())) {
        // throw new ResourceNotFoundException("CarModel", "id",
        // request.getCarModelId());
        // }

        // if (request.getStorageLocationId() != null) {
        // StorageLocation location =
        // storageLocationRepository.findById(request.getStorageLocationId())
        // .orElseThrow(() -> new ResourceNotFoundException("StorageLocation", "id",
        // request.getStorageLocationId()));

        // if (!location.hasCapacity()) {
        // throw new BusinessException("Storage location has reached maximum capacity",
        // "LOCATION_FULL");
        // }
        // }

        // Fetch CarModel and set it on the entity after mapping
        CarModel carModel = carModelRepository.findById(request.getCarModelId())
                .orElseThrow(() -> new ResourceNotFoundException("CarModel", "id", request.getCarModelId()));

        // Fetch StorageLocation and set it on the entity after mapping
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
        car.setCarModel(carModel); // ✅ Set CarModel on entity
        car.setStorageLocation(location); // ✅ Set StorageLocation on entity

        Car saved = carRepository.save(car);

        if (saved.getStorageLocation() != null) {
            location.setCurrentCarCount(location.getCurrentCarCount() + 1);
            storageLocationRepository.save(location);
        }

        log.info("Created car with ID: {} and VIN: {}", saved.getId(), saved.getVinNumber());
        return carMapper.toResponse(saved);
    }

    @Override
    public CarResponse updateCar(Long id, CarRequest request) {
        log.debug("Updating car ID: {}", id);

        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", id));

        // Update carModel only if a new one is provided in the request
        if (request.getCarModelId() != null) {
            CarModel carModel = carModelRepository.findById(request.getCarModelId())
                    .orElseThrow(() -> new ResourceNotFoundException("CarModel", "id", request.getCarModelId()));
            car.setCarModel(carModel);
        }

        // Update storageLocation only if provided
        if (request.getStorageLocationId() != null) {
            StorageLocation location = storageLocationRepository.findById(request.getStorageLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("StorageLocation", "id",
                            request.getStorageLocationId()));
            car.setStorageLocation(location);
        }

        carMapper.updateEntityFromRequest(request, car);
        Car updated = carRepository.save(car);

        log.info("Updated car ID: {}", id);
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
    public void deleteCar(Long id) {
        log.debug("Deleting car ID: {}", id);

        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", id));

        if (car.getStatus() == CarStatus.RESERVED || car.getStatus() == CarStatus.SOLD) {
            throw new BusinessException("Cannot delete car with status: " + car.getStatus(), "INVALID_CAR_STATUS");
        }

        if (car.getStorageLocation() != null) {
            StorageLocation location = car.getStorageLocation();
            location.setCurrentCarCount(location.getCurrentCarCount() - 1);
            storageLocationRepository.save(location);
        }

        carRepository.delete(car);
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
    public CarResponse moveCarToLocation(Long carId, Long locationId) {
        log.debug("Moving car ID: {} to location ID: {}", carId, locationId);

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", carId));

        StorageLocation newLocation = storageLocationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("StorageLocation", "id", locationId));

        if (!newLocation.hasCapacity()) {
            throw new BusinessException("Storage location has reached maximum capacity", "LOCATION_FULL");
        }

        StorageLocation oldLocation = car.getStorageLocation();

        // Update old location
        if (oldLocation != null) {
            oldLocation.setCurrentCarCount(oldLocation.getCurrentCarCount() - 1);
            storageLocationRepository.save(oldLocation);
        }

        // Update new location
        newLocation.setCurrentCarCount(newLocation.getCurrentCarCount() + 1);
        storageLocationRepository.save(newLocation);

        // Create movement record
        CarMovement movement = CarMovement.builder()
                .car(car)
                .fromLocation(oldLocation)
                .toLocation(newLocation)
                .movedAt(LocalDateTime.now())
                .build();
        carMovementRepository.save(movement);

        // Update car location
        car.setStorageLocation(newLocation);
        Car updated = carRepository.save(car);

        log.info("Moved car ID: {} to location ID: {}", carId, locationId);
        return carMapper.toResponse(updated);
    }

    @Override
    public CarResponse updateCarStatus(Long carId, CarStatus newStatus) {
        log.debug("Updating car ID: {} status to: {}", carId, newStatus);

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", carId));

        // Validate status transitions
        validateStatusTransition(car.getStatus(), newStatus);

        car.setStatus(newStatus);
        Car updated = carRepository.save(car);

        log.info("Updated car ID: {} status to: {}", carId, newStatus);
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
        // Define valid transitions
        if (currentStatus == CarStatus.SOLD && newStatus != CarStatus.SOLD) {
            throw new BusinessException("Cannot change status of a sold car", "INVALID_STATUS_TRANSITION");
        }

        if (currentStatus == CarStatus.RESERVED && newStatus == CarStatus.AVAILABLE) {
            // Allow only when reservation is cancelled
            log.info("Reverting reservation status to available");
        }
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
