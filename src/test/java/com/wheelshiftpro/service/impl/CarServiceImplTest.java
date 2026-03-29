package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.CarRequest;
import com.wheelshiftpro.dto.response.CarResponse;
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
import com.wheelshiftpro.service.AuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CarServiceImpl")
class CarServiceImplTest {

    @Mock CarRepository carRepository;
    @Mock CarModelRepository carModelRepository;
    @Mock StorageLocationRepository storageLocationRepository;
    @Mock CarMovementRepository carMovementRepository;
    @Mock FinancialTransactionRepository financialTransactionRepository;
    @Mock EmployeeRepository employeeRepository;
    @Mock CarMapper carMapper;
    @Mock AuditService auditService;

    @InjectMocks
    CarServiceImpl carService;

    // ── helpers ─────────────────────────────────────────────────────────────

    private Car availableCar(Long id, String vin) {
        Car car = Car.builder().vinNumber(vin).status(CarStatus.AVAILABLE).build();
        car.setId(id);
        return car;
    }

    private StorageLocation locationWithCapacity(Long id, int total, int current) {
        return StorageLocation.builder()
                .totalCapacity(total)
                .currentCarCount(current)
                .currentMotorcycleCount(0)
                .build();
    }

    private CarRequest requestForVin(String vin, Long modelId) {
        return CarRequest.builder().vinNumber(vin).carModelId(modelId).build();
    }

    private void setUpAuthenticatedEmployee(Long employeeId) {
        Employee employee = new Employee();
        employee.setId(employeeId);
        EmployeeUserDetails principal = mock(EmployeeUserDetails.class);
        when(principal.getId()).thenReturn(employeeId);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(employeeRepository.getReferenceById(employeeId)).thenReturn(employee);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ── createCar ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createCar")
    class CreateCar {

        @Test
        @DisplayName("should create car and increment location count")
        void happyPath() {
            CarRequest request = requestForVin("12345678901234567", 1L);
            request.setStorageLocationId(10L);

            CarModel model = new CarModel();
            StorageLocation location = locationWithCapacity(10L, 5, 0);

            Car entity = availableCar(null, request.getVinNumber());
            Car saved = availableCar(100L, request.getVinNumber());
            saved.setStorageLocation(location);

            when(carRepository.existsByVinNumber(request.getVinNumber())).thenReturn(false);
            when(carModelRepository.findById(1L)).thenReturn(Optional.of(model));
            when(storageLocationRepository.findById(10L)).thenReturn(Optional.of(location));
            when(carMapper.toEntity(request)).thenReturn(entity);
            when(carRepository.save(any())).thenReturn(saved);
            when(carMapper.toResponse(saved)).thenReturn(new CarResponse());

            carService.createCar(request);

            assertThat(location.getCurrentCarCount()).isEqualTo(1);
            verify(storageLocationRepository).save(location);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException for duplicate VIN")
        void duplicateVin() {
            CarRequest request = requestForVin("12345678901234567", 1L);
            when(carRepository.existsByVinNumber(request.getVinNumber())).thenReturn(true);

            assertThatThrownBy(() -> carService.createCar(request))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException for duplicate registration number")
        void duplicateRegistration() {
            CarRequest request = requestForVin("12345678901234567", 1L);
            request.setRegistrationNumber("MH01AB1234");

            when(carRepository.existsByVinNumber(request.getVinNumber())).thenReturn(false);
            when(carRepository.existsByRegistrationNumber("MH01AB1234")).thenReturn(true);

            assertThatThrownBy(() -> carService.createCar(request))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when model does not exist")
        void modelNotFound() {
            CarRequest request = requestForVin("12345678901234567", 99L);
            when(carRepository.existsByVinNumber(request.getVinNumber())).thenReturn(false);
            when(carModelRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> carService.createCar(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when storage location does not exist")
        void locationNotFound() {
            CarRequest request = requestForVin("12345678901234567", 1L);
            request.setStorageLocationId(999L);

            when(carRepository.existsByVinNumber(request.getVinNumber())).thenReturn(false);
            when(carModelRepository.findById(1L)).thenReturn(Optional.of(new CarModel()));
            when(storageLocationRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> carService.createCar(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should throw BusinessException when storage location is at full capacity")
        void locationFull() {
            CarRequest request = requestForVin("12345678901234567", 1L);
            request.setStorageLocationId(10L);

            StorageLocation fullLocation = locationWithCapacity(10L, 2, 2); // total=2, car+moto=2 → full

            when(carRepository.existsByVinNumber(request.getVinNumber())).thenReturn(false);
            when(carModelRepository.findById(1L)).thenReturn(Optional.of(new CarModel()));
            when(storageLocationRepository.findById(10L)).thenReturn(Optional.of(fullLocation));

            assertThatThrownBy(() -> carService.createCar(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("maximum capacity");
        }
    }

    // ── updateCar ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateCar")
    class UpdateCar {

        @Test
        @DisplayName("should throw DuplicateResourceException when VIN is changed to an already-used VIN")
        void vinChangedToDuplicate() {
            Car existing = availableCar(1L, "AAAAAAAAAAAAAAAAA");
            CarRequest request = requestForVin("BBBBBBBBBBBBBBBBB", null);

            when(carRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(carRepository.existsByVinNumberAndIdNot("BBBBBBBBBBBBBBBBB", 1L)).thenReturn(true);

            assertThatThrownBy(() -> carService.updateCar(1L, request))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        @DisplayName("should allow VIN update when number is unique")
        void vinChangedToUnique() {
            Car existing = availableCar(1L, "AAAAAAAAAAAAAAAAA");
            CarRequest request = requestForVin("BBBBBBBBBBBBBBBBB", null);
            Car updated = availableCar(1L, "BBBBBBBBBBBBBBBBB");

            when(carRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(carRepository.existsByVinNumberAndIdNot("BBBBBBBBBBBBBBBBB", 1L)).thenReturn(false);
            when(carRepository.save(any())).thenReturn(updated);
            when(carMapper.toResponse(any())).thenReturn(new CarResponse());

            carService.updateCar(1L, request);

            verify(carMapper).updateEntityFromRequest(eq(request), eq(existing));
        }

        @Test
        @DisplayName("should trigger move logic when storage location changes")
        void locationChangeTriggersMoveLogic() {
            StorageLocation oldLocation = locationWithCapacity(1L, 5, 1);
            StorageLocation newLocation = locationWithCapacity(2L, 5, 0);

            Car existing = availableCar(1L, "AAAAAAAAAAAAAAAAA");
            existing.setStorageLocation(oldLocation);

            CarRequest request = requestForVin("AAAAAAAAAAAAAAAAA", null);
            request.setStorageLocationId(2L);

            when(carRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(storageLocationRepository.findById(2L)).thenReturn(Optional.of(newLocation));
            when(carRepository.save(any())).thenReturn(existing);
            when(carMapper.toResponse(any())).thenReturn(new CarResponse());

            carService.updateCar(1L, request);

            // Old location count decremented, new location count incremented
            assertThat(oldLocation.getCurrentCarCount()).isEqualTo(0);
            assertThat(newLocation.getCurrentCarCount()).isEqualTo(1);
            verify(carMovementRepository).save(any(CarMovement.class));
        }

        @Test
        @DisplayName("should NOT create movement record when storage location is unchanged")
        void locationUnchangedSkipsMoveLogic() {
            StorageLocation location = locationWithCapacity(1L, 5, 1);
            location.setId(1L);

            Car existing = availableCar(1L, "AAAAAAAAAAAAAAAAA");
            existing.setStorageLocation(location);

            CarRequest request = requestForVin("AAAAAAAAAAAAAAAAA", null);
            request.setStorageLocationId(1L); // same location

            when(carRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(carRepository.save(any())).thenReturn(existing);
            when(carMapper.toResponse(any())).thenReturn(new CarResponse());

            carService.updateCar(1L, request);

            verify(carMovementRepository, never()).save(any());
        }
    }

    // ── deleteCar ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteCar")
    class DeleteCar {

        @Test
        @DisplayName("should delete AVAILABLE car and decrement location count")
        void happyPath() {
            StorageLocation location = locationWithCapacity(1L, 5, 1);
            Car car = availableCar(1L, "AAAAAAAAAAAAAAAAA");
            car.setStorageLocation(location);

            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(financialTransactionRepository.existsByCarId(1L)).thenReturn(false);

            carService.deleteCar(1L);

            assertThat(location.getCurrentCarCount()).isEqualTo(0);
            verify(carRepository).delete(car);
        }

        @Test
        @DisplayName("should throw BusinessException when deleting RESERVED car")
        void cannotDeleteReserved() {
            Car car = availableCar(1L, "AAAAAAAAAAAAAAAAA");
            car.setStatus(CarStatus.RESERVED);

            when(carRepository.findById(1L)).thenReturn(Optional.of(car));

            assertThatThrownBy(() -> carService.deleteCar(1L))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw BusinessException when deleting SOLD car")
        void cannotDeleteSold() {
            Car car = availableCar(1L, "AAAAAAAAAAAAAAAAA");
            car.setStatus(CarStatus.SOLD);

            when(carRepository.findById(1L)).thenReturn(Optional.of(car));

            assertThatThrownBy(() -> carService.deleteCar(1L))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw BusinessException when car has financial transactions")
        void cannotDeleteWithTransactions() {
            Car car = availableCar(1L, "AAAAAAAAAAAAAAAAA");

            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(financialTransactionRepository.existsByCarId(1L)).thenReturn(true);

            assertThatThrownBy(() -> carService.deleteCar(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("financial transactions");
        }
    }

    // ── moveCarToLocation ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("moveCarToLocation")
    class MoveCarToLocation {

        @Test
        @DisplayName("should move car, update counts, create movement record with movedBy")
        void happyPath() {
            setUpAuthenticatedEmployee(5L);

            StorageLocation oldLoc = locationWithCapacity(1L, 5, 1);
            StorageLocation newLoc = locationWithCapacity(2L, 5, 0);
            Car car = availableCar(1L, "AAAAAAAAAAAAAAAAA");
            car.setStorageLocation(oldLoc);

            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(storageLocationRepository.findById(2L)).thenReturn(Optional.of(newLoc));
            when(carRepository.save(any())).thenReturn(car);
            when(carMapper.toResponse(any())).thenReturn(new CarResponse());

            carService.moveCarToLocation(1L, 2L);

            assertThat(oldLoc.getCurrentCarCount()).isEqualTo(0);
            assertThat(newLoc.getCurrentCarCount()).isEqualTo(1);
            assertThat(car.getStorageLocation()).isEqualTo(newLoc);

            ArgumentCaptor<CarMovement> movementCaptor = ArgumentCaptor.forClass(CarMovement.class);
            verify(carMovementRepository).save(movementCaptor.capture());
            CarMovement saved = movementCaptor.getValue();
            assertThat(saved.getFromLocation()).isEqualTo(oldLoc);
            assertThat(saved.getToLocation()).isEqualTo(newLoc);
            assertThat(saved.getMovedBy()).isNotNull();
            assertThat(saved.getMovedBy().getId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("should throw BusinessException when target location is full")
        void targetLocationFull() {
            Car car = availableCar(1L, "AAAAAAAAAAAAAAAAA");
            StorageLocation fullLoc = locationWithCapacity(2L, 2, 2);

            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(storageLocationRepository.findById(2L)).thenReturn(Optional.of(fullLoc));

            assertThatThrownBy(() -> carService.moveCarToLocation(1L, 2L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("maximum capacity");
        }

        @Test
        @DisplayName("should set movedBy to null when no authenticated user")
        void noAuthenticatedUser() {
            // SecurityContext is empty (cleared in @AfterEach sets this up)
            StorageLocation newLoc = locationWithCapacity(2L, 5, 0);
            Car car = availableCar(1L, "AAAAAAAAAAAAAAAAA");

            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(storageLocationRepository.findById(2L)).thenReturn(Optional.of(newLoc));
            when(carRepository.save(any())).thenReturn(car);
            when(carMapper.toResponse(any())).thenReturn(new CarResponse());

            carService.moveCarToLocation(1L, 2L);

            ArgumentCaptor<CarMovement> captor = ArgumentCaptor.forClass(CarMovement.class);
            verify(carMovementRepository).save(captor.capture());
            assertThat(captor.getValue().getMovedBy()).isNull();
        }
    }

    // ── updateCarStatus ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateCarStatus - status transition rules")
    class UpdateCarStatus {

        private Car carWithStatus(CarStatus status) {
            Car car = availableCar(1L, "AAAAAAAAAAAAAAAAA");
            car.setStatus(status);
            return car;
        }

        @Test
        @DisplayName("AVAILABLE → RESERVED should succeed")
        void availableToReserved() {
            Car car = carWithStatus(CarStatus.AVAILABLE);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(carRepository.save(any())).thenReturn(car);
            when(carMapper.toResponse(any())).thenReturn(new CarResponse());

            carService.updateCarStatus(1L, CarStatus.RESERVED);

            assertThat(car.getStatus()).isEqualTo(CarStatus.RESERVED);
        }

        @Test
        @DisplayName("AVAILABLE → MAINTENANCE should succeed")
        void availableToMaintenance() {
            Car car = carWithStatus(CarStatus.AVAILABLE);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(carRepository.save(any())).thenReturn(car);
            when(carMapper.toResponse(any())).thenReturn(new CarResponse());

            carService.updateCarStatus(1L, CarStatus.MAINTENANCE);

            assertThat(car.getStatus()).isEqualTo(CarStatus.MAINTENANCE);
        }

        @Test
        @DisplayName("RESERVED → AVAILABLE should succeed (reservation cancelled)")
        void reservedToAvailable() {
            Car car = carWithStatus(CarStatus.RESERVED);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(carRepository.save(any())).thenReturn(car);
            when(carMapper.toResponse(any())).thenReturn(new CarResponse());

            carService.updateCarStatus(1L, CarStatus.AVAILABLE);

            assertThat(car.getStatus()).isEqualTo(CarStatus.AVAILABLE);
        }

        @Test
        @DisplayName("SOLD → any status should throw BusinessException")
        void soldToAnyStatus() {
            Car car = carWithStatus(CarStatus.SOLD);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));

            assertThatThrownBy(() -> carService.updateCarStatus(1L, CarStatus.AVAILABLE))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("ANY → SOLD direct transition should be blocked")
        void anyToSoldIsBlocked() {
            Car car = carWithStatus(CarStatus.AVAILABLE);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));

            assertThatThrownBy(() -> carService.updateCarStatus(1L, CarStatus.SOLD))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("sale transaction");
        }

        @Test
        @DisplayName("RESERVED → SOLD direct transition should be blocked")
        void reservedToSoldIsBlocked() {
            Car car = carWithStatus(CarStatus.RESERVED);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));

            assertThatThrownBy(() -> carService.updateCarStatus(1L, CarStatus.SOLD))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("sale transaction");
        }

        @Test
        @DisplayName("MAINTENANCE → RESERVED should throw BusinessException")
        void maintenanceToReservedIsBlocked() {
            Car car = carWithStatus(CarStatus.MAINTENANCE);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));

            assertThatThrownBy(() -> carService.updateCarStatus(1L, CarStatus.RESERVED))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("maintenance");
        }
    }
}
