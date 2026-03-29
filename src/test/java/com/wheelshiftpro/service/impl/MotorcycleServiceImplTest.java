package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.MotorcycleRequest;
import com.wheelshiftpro.dto.response.MotorcycleResponse;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MotorcycleServiceImpl")
class MotorcycleServiceImplTest {

    @Mock MotorcycleRepository motorcycleRepository;
    @Mock MotorcycleModelRepository motorcycleModelRepository;
    @Mock StorageLocationRepository storageLocationRepository;
    @Mock MotorcycleMovementRepository motorcycleMovementRepository;
    @Mock FinancialTransactionRepository financialTransactionRepository;
    @Mock EmployeeRepository employeeRepository;
    @Mock MotorcycleMapper motorcycleMapper;
    @Mock AuditService auditService;

    @InjectMocks
    MotorcycleServiceImpl motorcycleService;

    // ── helpers ─────────────────────────────────────────────────────────────

    private Motorcycle motoWithStatus(Long id, String vin, MotorcycleStatus status) {
        Motorcycle m = Motorcycle.builder().vinNumber(vin).status(status).build();
        m.setId(id);
        return m;
    }

    private StorageLocation locationWithCapacity(int total, int currentCar, int currentMoto) {
        return StorageLocation.builder()
                .totalCapacity(total)
                .currentCarCount(currentCar)
                .currentMotorcycleCount(currentMoto)
                .build();
    }

    private MotorcycleRequest requestForVin(String vin, Long modelId) {
        return MotorcycleRequest.builder().vinNumber(vin).motorcycleModelId(modelId).build();
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

    // ── createMotorcycle ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("createMotorcycle")
    class CreateMotorcycle {

        @Test
        @DisplayName("should create motorcycle, set model and location, increment count")
        void happyPath() {
            MotorcycleRequest request = requestForVin("12345678901234567", 10L);
            request.setStorageLocationId(5L);

            MotorcycleModel model = new MotorcycleModel();
            StorageLocation location = locationWithCapacity(5, 0, 0);

            Motorcycle entity = motoWithStatus(null, request.getVinNumber(), MotorcycleStatus.AVAILABLE);
            Motorcycle saved = motoWithStatus(200L, request.getVinNumber(), MotorcycleStatus.AVAILABLE);
            saved.setStorageLocation(location);

            when(motorcycleRepository.existsByVinNumber(request.getVinNumber())).thenReturn(false);
            when(motorcycleModelRepository.findById(10L)).thenReturn(Optional.of(model));
            when(storageLocationRepository.findById(5L)).thenReturn(Optional.of(location));
            when(motorcycleMapper.toEntity(request)).thenReturn(entity);
            when(motorcycleRepository.save(any())).thenReturn(saved);
            when(motorcycleMapper.toResponse(saved)).thenReturn(new MotorcycleResponse());

            motorcycleService.createMotorcycle(request);

            assertThat(location.getCurrentMotorcycleCount()).isEqualTo(1);
            verify(storageLocationRepository).save(location);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException for duplicate VIN")
        void duplicateVin() {
            MotorcycleRequest request = requestForVin("12345678901234567", 1L);
            when(motorcycleRepository.existsByVinNumber(request.getVinNumber())).thenReturn(true);

            assertThatThrownBy(() -> motorcycleService.createMotorcycle(request))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException for duplicate registration")
        void duplicateRegistration() {
            MotorcycleRequest request = requestForVin("12345678901234567", 1L);
            request.setRegistrationNumber("MH02XY9999");

            when(motorcycleRepository.existsByVinNumber(any())).thenReturn(false);
            when(motorcycleRepository.existsByRegistrationNumber("MH02XY9999")).thenReturn(true);

            assertThatThrownBy(() -> motorcycleService.createMotorcycle(request))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when model does not exist")
        void modelNotFound() {
            MotorcycleRequest request = requestForVin("12345678901234567", 99L);
            when(motorcycleRepository.existsByVinNumber(any())).thenReturn(false);
            when(motorcycleModelRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> motorcycleService.createMotorcycle(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should throw BusinessException when storage location is at full capacity")
        void locationFull() {
            MotorcycleRequest request = requestForVin("12345678901234567", 1L);
            request.setStorageLocationId(5L);

            StorageLocation fullLoc = locationWithCapacity(2, 1, 1); // total=2, used=2

            when(motorcycleRepository.existsByVinNumber(any())).thenReturn(false);
            when(motorcycleModelRepository.findById(1L)).thenReturn(Optional.of(new MotorcycleModel()));
            when(storageLocationRepository.findById(5L)).thenReturn(Optional.of(fullLoc));

            assertThatThrownBy(() -> motorcycleService.createMotorcycle(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("maximum capacity");
        }
    }

    // ── updateMotorcycle ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateMotorcycle")
    class UpdateMotorcycle {

        @Test
        @DisplayName("should throw DuplicateResourceException when VIN changed to an already-used VIN")
        void vinChangedToDuplicate() {
            Motorcycle existing = motoWithStatus(1L, "AAAAAAAAAAAAAAAAA", MotorcycleStatus.AVAILABLE);
            MotorcycleRequest request = requestForVin("BBBBBBBBBBBBBBBBB", null);

            when(motorcycleRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(motorcycleRepository.existsByVinNumber("BBBBBBBBBBBBBBBBB")).thenReturn(true);

            assertThatThrownBy(() -> motorcycleService.updateMotorcycle(1L, request))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        @DisplayName("should update motorcycle model when model ID provided")
        void updatesModel() {
            Motorcycle existing = motoWithStatus(1L, "AAAAAAAAAAAAAAAAA", MotorcycleStatus.AVAILABLE);
            MotorcycleModel newModel = new MotorcycleModel();
            MotorcycleRequest request = requestForVin("AAAAAAAAAAAAAAAAA", 20L);

            when(motorcycleRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(motorcycleModelRepository.findById(20L)).thenReturn(Optional.of(newModel));
            when(motorcycleRepository.save(any())).thenReturn(existing);
            when(motorcycleMapper.toResponse(any())).thenReturn(new MotorcycleResponse());

            motorcycleService.updateMotorcycle(1L, request);

            assertThat(existing.getMotorcycleModel()).isEqualTo(newModel);
        }

        @Test
        @DisplayName("should trigger move logic when storage location changes")
        void locationChangeTriggersMoveLogic() {
            StorageLocation oldLoc = locationWithCapacity(5, 0, 1);
            StorageLocation newLoc = locationWithCapacity(5, 0, 0);

            Motorcycle existing = motoWithStatus(1L, "AAAAAAAAAAAAAAAAA", MotorcycleStatus.AVAILABLE);
            existing.setStorageLocation(oldLoc);

            MotorcycleRequest request = requestForVin("AAAAAAAAAAAAAAAAA", null);
            request.setStorageLocationId(7L);

            when(motorcycleRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(storageLocationRepository.findById(7L)).thenReturn(Optional.of(newLoc));
            when(motorcycleRepository.save(any())).thenReturn(existing);
            when(motorcycleMapper.toResponse(any())).thenReturn(new MotorcycleResponse());

            motorcycleService.updateMotorcycle(1L, request);

            assertThat(oldLoc.getCurrentMotorcycleCount()).isEqualTo(0);
            assertThat(newLoc.getCurrentMotorcycleCount()).isEqualTo(1);
            verify(motorcycleMovementRepository).save(any(MotorcycleMovement.class));
        }

        @Test
        @DisplayName("should NOT create movement record when location is unchanged")
        void locationUnchangedSkipsMoveLogic() {
            StorageLocation location = locationWithCapacity(5, 0, 1);
            location.setId(3L);

            Motorcycle existing = motoWithStatus(1L, "AAAAAAAAAAAAAAAAA", MotorcycleStatus.AVAILABLE);
            existing.setStorageLocation(location);

            MotorcycleRequest request = requestForVin("AAAAAAAAAAAAAAAAA", null);
            request.setStorageLocationId(3L); // same location id

            when(motorcycleRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(motorcycleRepository.save(any())).thenReturn(existing);
            when(motorcycleMapper.toResponse(any())).thenReturn(new MotorcycleResponse());

            motorcycleService.updateMotorcycle(1L, request);

            verify(motorcycleMovementRepository, never()).save(any());
        }
    }

    // ── deleteMotorcycle ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteMotorcycle")
    class DeleteMotorcycle {

        @Test
        @DisplayName("should delete AVAILABLE motorcycle and decrement location count")
        void happyPath() {
            StorageLocation location = locationWithCapacity(5, 0, 1);
            Motorcycle moto = motoWithStatus(1L, "AAAAAAAAAAAAAAAAA", MotorcycleStatus.AVAILABLE);
            moto.setStorageLocation(location);

            when(motorcycleRepository.findById(1L)).thenReturn(Optional.of(moto));
            when(financialTransactionRepository.existsByMotorcycleId(1L)).thenReturn(false);

            motorcycleService.deleteMotorcycle(1L);

            assertThat(location.getCurrentMotorcycleCount()).isEqualTo(0);
            verify(motorcycleRepository).delete(moto);
        }

        @Test
        @DisplayName("should throw BusinessException when deleting RESERVED motorcycle")
        void cannotDeleteReserved() {
            Motorcycle moto = motoWithStatus(1L, "AAAAAAAAAAAAAAAAA", MotorcycleStatus.RESERVED);
            when(motorcycleRepository.findById(1L)).thenReturn(Optional.of(moto));

            assertThatThrownBy(() -> motorcycleService.deleteMotorcycle(1L))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw BusinessException when deleting SOLD motorcycle")
        void cannotDeleteSold() {
            Motorcycle moto = motoWithStatus(1L, "AAAAAAAAAAAAAAAAA", MotorcycleStatus.SOLD);
            when(motorcycleRepository.findById(1L)).thenReturn(Optional.of(moto));

            assertThatThrownBy(() -> motorcycleService.deleteMotorcycle(1L))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw BusinessException when motorcycle has financial transactions")
        void cannotDeleteWithTransactions() {
            Motorcycle moto = motoWithStatus(1L, "AAAAAAAAAAAAAAAAA", MotorcycleStatus.AVAILABLE);
            when(motorcycleRepository.findById(1L)).thenReturn(Optional.of(moto));
            when(financialTransactionRepository.existsByMotorcycleId(1L)).thenReturn(true);

            assertThatThrownBy(() -> motorcycleService.deleteMotorcycle(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("financial transactions");
        }
    }

    // ── moveMotorcycleToLocation ──────────────────────────────────────────────

    @Nested
    @DisplayName("moveMotorcycleToLocation")
    class MoveMotorcycleToLocation {

        @Test
        @DisplayName("should move motorcycle, update counts, create movement record with movedBy")
        void happyPath() {
            setUpAuthenticatedEmployee(7L);

            StorageLocation oldLoc = locationWithCapacity(5, 0, 1);
            StorageLocation newLoc = locationWithCapacity(5, 0, 0);
            Motorcycle moto = motoWithStatus(1L, "AAAAAAAAAAAAAAAAA", MotorcycleStatus.AVAILABLE);
            moto.setStorageLocation(oldLoc);

            when(motorcycleRepository.findById(1L)).thenReturn(Optional.of(moto));
            when(storageLocationRepository.findById(3L)).thenReturn(Optional.of(newLoc));
            when(motorcycleRepository.save(any())).thenReturn(moto);
            when(motorcycleMapper.toResponse(any())).thenReturn(new MotorcycleResponse());

            motorcycleService.moveMotorcycleToLocation(1L, 3L);

            assertThat(oldLoc.getCurrentMotorcycleCount()).isEqualTo(0);
            assertThat(newLoc.getCurrentMotorcycleCount()).isEqualTo(1);
            assertThat(moto.getStorageLocation()).isEqualTo(newLoc);

            ArgumentCaptor<MotorcycleMovement> captor = ArgumentCaptor.forClass(MotorcycleMovement.class);
            verify(motorcycleMovementRepository).save(captor.capture());
            MotorcycleMovement saved = captor.getValue();
            assertThat(saved.getFromLocation()).isEqualTo(oldLoc);
            assertThat(saved.getToLocation()).isEqualTo(newLoc);
            assertThat(saved.getMovedBy()).isNotNull();
            assertThat(saved.getMovedBy().getId()).isEqualTo(7L);
        }

        @Test
        @DisplayName("should throw BusinessException when target location is full")
        void targetLocationFull() {
            Motorcycle moto = motoWithStatus(1L, "AAAAAAAAAAAAAAAAA", MotorcycleStatus.AVAILABLE);
            StorageLocation fullLoc = locationWithCapacity(2, 1, 1); // total=2, used=2

            when(motorcycleRepository.findById(1L)).thenReturn(Optional.of(moto));
            when(storageLocationRepository.findById(3L)).thenReturn(Optional.of(fullLoc));

            assertThatThrownBy(() -> motorcycleService.moveMotorcycleToLocation(1L, 3L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("maximum capacity");
        }

        @Test
        @DisplayName("should set movedBy to null when no authenticated user")
        void noAuthenticatedUser() {
            StorageLocation newLoc = locationWithCapacity(5, 0, 0);
            Motorcycle moto = motoWithStatus(1L, "AAAAAAAAAAAAAAAAA", MotorcycleStatus.AVAILABLE);

            when(motorcycleRepository.findById(1L)).thenReturn(Optional.of(moto));
            when(storageLocationRepository.findById(3L)).thenReturn(Optional.of(newLoc));
            when(motorcycleRepository.save(any())).thenReturn(moto);
            when(motorcycleMapper.toResponse(any())).thenReturn(new MotorcycleResponse());

            motorcycleService.moveMotorcycleToLocation(1L, 3L);

            ArgumentCaptor<MotorcycleMovement> captor = ArgumentCaptor.forClass(MotorcycleMovement.class);
            verify(motorcycleMovementRepository).save(captor.capture());
            assertThat(captor.getValue().getMovedBy()).isNull();
        }
    }

    // ── updateMotorcycleStatus ────────────────────────────────────────────────

    @Nested
    @DisplayName("updateMotorcycleStatus - status transition rules")
    class UpdateMotorcycleStatus {

        private Motorcycle moto(MotorcycleStatus status) {
            return motoWithStatus(1L, "AAAAAAAAAAAAAAAAA", status);
        }

        @Test
        @DisplayName("AVAILABLE → RESERVED should succeed")
        void availableToReserved() {
            Motorcycle m = moto(MotorcycleStatus.AVAILABLE);
            when(motorcycleRepository.findById(1L)).thenReturn(Optional.of(m));
            when(motorcycleRepository.save(any())).thenReturn(m);
            when(motorcycleMapper.toResponse(any())).thenReturn(new MotorcycleResponse());

            motorcycleService.updateMotorcycleStatus(1L, MotorcycleStatus.RESERVED);

            assertThat(m.getStatus()).isEqualTo(MotorcycleStatus.RESERVED);
        }

        @Test
        @DisplayName("AVAILABLE → MAINTENANCE should succeed")
        void availableToMaintenance() {
            Motorcycle m = moto(MotorcycleStatus.AVAILABLE);
            when(motorcycleRepository.findById(1L)).thenReturn(Optional.of(m));
            when(motorcycleRepository.save(any())).thenReturn(m);
            when(motorcycleMapper.toResponse(any())).thenReturn(new MotorcycleResponse());

            motorcycleService.updateMotorcycleStatus(1L, MotorcycleStatus.MAINTENANCE);

            assertThat(m.getStatus()).isEqualTo(MotorcycleStatus.MAINTENANCE);
        }

        @Test
        @DisplayName("RESERVED → AVAILABLE should succeed (reservation cancelled)")
        void reservedToAvailable() {
            Motorcycle m = moto(MotorcycleStatus.RESERVED);
            when(motorcycleRepository.findById(1L)).thenReturn(Optional.of(m));
            when(motorcycleRepository.save(any())).thenReturn(m);
            when(motorcycleMapper.toResponse(any())).thenReturn(new MotorcycleResponse());

            motorcycleService.updateMotorcycleStatus(1L, MotorcycleStatus.AVAILABLE);

            assertThat(m.getStatus()).isEqualTo(MotorcycleStatus.AVAILABLE);
        }

        @Test
        @DisplayName("SOLD → any status should throw BusinessException")
        void soldToAnyStatusBlocked() {
            Motorcycle m = moto(MotorcycleStatus.SOLD);
            when(motorcycleRepository.findById(1L)).thenReturn(Optional.of(m));

            assertThatThrownBy(() -> motorcycleService.updateMotorcycleStatus(1L, MotorcycleStatus.AVAILABLE))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("ANY → SOLD direct transition should be blocked")
        void anyToSoldIsBlocked() {
            Motorcycle m = moto(MotorcycleStatus.AVAILABLE);
            when(motorcycleRepository.findById(1L)).thenReturn(Optional.of(m));

            assertThatThrownBy(() -> motorcycleService.updateMotorcycleStatus(1L, MotorcycleStatus.SOLD))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("sale transaction");
        }

        @Test
        @DisplayName("RESERVED → SOLD direct transition should be blocked")
        void reservedToSoldIsBlocked() {
            Motorcycle m = moto(MotorcycleStatus.RESERVED);
            when(motorcycleRepository.findById(1L)).thenReturn(Optional.of(m));

            assertThatThrownBy(() -> motorcycleService.updateMotorcycleStatus(1L, MotorcycleStatus.SOLD))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("sale transaction");
        }

        @Test
        @DisplayName("MAINTENANCE → RESERVED should throw BusinessException")
        void maintenanceToReservedIsBlocked() {
            Motorcycle m = moto(MotorcycleStatus.MAINTENANCE);
            when(motorcycleRepository.findById(1L)).thenReturn(Optional.of(m));

            assertThatThrownBy(() -> motorcycleService.updateMotorcycleStatus(1L, MotorcycleStatus.RESERVED))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("maintenance");
        }
    }
}
