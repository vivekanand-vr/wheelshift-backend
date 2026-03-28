package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.StorageLocationRequest;
import com.wheelshiftpro.dto.response.StorageLocationResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.StorageLocation;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.StorageLocationMapper;
import com.wheelshiftpro.repository.EmployeeRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageLocationServiceImplTest {

    @Mock StorageLocationRepository storageLocationRepository;
    @Mock StorageLocationMapper storageLocationMapper;
    @Mock EmployeeRepository employeeRepository;
    @Mock AuditService auditService;

    @InjectMocks
    StorageLocationServiceImpl storageLocationService;

    private void setUpAuthenticatedEmployee(Long employeeId) {
        Employee employee = new Employee();
        employee.setId(employeeId);
        EmployeeUserDetails principal = mock(EmployeeUserDetails.class);
        when(principal.getId()).thenReturn(employeeId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
        when(employeeRepository.getReferenceById(employeeId)).thenReturn(employee);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createStorageLocation
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createStorageLocation")
    class CreateStorageLocation {

        @Test
        void happyPath_savesAndAudits() {
            StorageLocationRequest request = new StorageLocationRequest();
            request.setName("Warehouse A");

            StorageLocation entity = new StorageLocation();
            entity.setId(1L);
            entity.setName("Warehouse A");
            StorageLocationResponse response = new StorageLocationResponse();
            response.setId(1L);

            when(storageLocationRepository.existsByName("Warehouse A")).thenReturn(false);
            when(storageLocationMapper.toEntity(request)).thenReturn(entity);
            when(storageLocationRepository.save(entity)).thenReturn(entity);
            when(storageLocationMapper.toResponse(entity)).thenReturn(response);

            StorageLocationResponse result = storageLocationService.createStorageLocation(request);

            assertThat(result.getId()).isEqualTo(1L);
            verify(auditService).log(eq(AuditCategory.STORAGE_LOCATION), eq(1L), eq("CREATE"),
                    eq(AuditLevel.REGULAR), isNull(), anyString());
        }

        @Test
        void duplicateName_throwsDuplicateResourceException() {
            StorageLocationRequest request = new StorageLocationRequest();
            request.setName("Warehouse A");
            when(storageLocationRepository.existsByName("Warehouse A")).thenReturn(true);

            assertThatThrownBy(() -> storageLocationService.createStorageLocation(request))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(auditService, never()).log(any(), any(), any(), any(), any(), any());
        }

        @Test
        void auditLevel_isRegular() {
            StorageLocationRequest request = new StorageLocationRequest();
            request.setName("Lot B");
            StorageLocation entity = new StorageLocation();
            entity.setId(2L);
            entity.setName("Lot B");

            when(storageLocationRepository.existsByName("Lot B")).thenReturn(false);
            when(storageLocationMapper.toEntity(request)).thenReturn(entity);
            when(storageLocationRepository.save(entity)).thenReturn(entity);
            when(storageLocationMapper.toResponse(entity)).thenReturn(new StorageLocationResponse());

            storageLocationService.createStorageLocation(request);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        void auditField_populatedWhenAuthenticated() {
            setUpAuthenticatedEmployee(42L);

            StorageLocationRequest request = new StorageLocationRequest();
            request.setName("Authenticated Lot");
            StorageLocation entity = new StorageLocation();
            entity.setId(3L);
            entity.setName("Authenticated Lot");

            when(storageLocationRepository.existsByName("Authenticated Lot")).thenReturn(false);
            when(storageLocationMapper.toEntity(request)).thenReturn(entity);
            when(storageLocationRepository.save(entity)).thenReturn(entity);
            when(storageLocationMapper.toResponse(entity)).thenReturn(new StorageLocationResponse());

            storageLocationService.createStorageLocation(request);

            ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
            verify(auditService).log(any(), any(), any(), any(), employeeCaptor.capture(), any());
            assertThat(employeeCaptor.getValue()).isNotNull();
            assertThat(employeeCaptor.getValue().getId()).isEqualTo(42L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateStorageLocation
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateStorageLocation")
    class UpdateStorageLocation {

        @Test
        void happyPath_updatesAndAudits() {
            StorageLocationRequest request = new StorageLocationRequest();
            request.setName("Updated Name");
            request.setTotalCapacity(50);

            StorageLocation entity = storageLocationWithCounts(0, 0);
            when(storageLocationRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(storageLocationRepository.existsByNameAndIdNot("Updated Name", 1L)).thenReturn(false);
            when(storageLocationRepository.save(entity)).thenReturn(entity);
            when(storageLocationMapper.toResponse(entity)).thenReturn(new StorageLocationResponse());

            storageLocationService.updateStorageLocation(1L, request);

            verify(auditService).log(eq(AuditCategory.STORAGE_LOCATION), eq(1L), eq("UPDATE"),
                    eq(AuditLevel.REGULAR), isNull(), anyString());
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(storageLocationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> storageLocationService.updateStorageLocation(99L, new StorageLocationRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void duplicateNameOnUpdate_throwsDuplicateResourceException() {
            StorageLocationRequest request = new StorageLocationRequest();
            request.setName("Taken Name");

            StorageLocation entity = storageLocationWithCounts(0, 0);
            when(storageLocationRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(storageLocationRepository.existsByNameAndIdNot("Taken Name", 1L)).thenReturn(true);

            assertThatThrownBy(() -> storageLocationService.updateStorageLocation(1L, request))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        void capacityBelowCurrentCount_throwsBusinessException() {
            StorageLocationRequest request = new StorageLocationRequest();
            request.setTotalCapacity(5);

            StorageLocation entity = storageLocationWithCounts(4, 3); // total = 7
            when(storageLocationRepository.findById(1L)).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> storageLocationService.updateStorageLocation(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("capacity");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteStorageLocation
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteStorageLocation")
    class DeleteStorageLocation {

        @Test
        void happyPath_deletesAndAudits() {
            StorageLocation entity = storageLocationWithCounts(0, 0);
            entity.setName("Empty Lot");
            when(storageLocationRepository.findById(1L)).thenReturn(Optional.of(entity));

            storageLocationService.deleteStorageLocation(1L);

            verify(storageLocationRepository).delete(entity);
            verify(auditService).log(eq(AuditCategory.STORAGE_LOCATION), eq(1L), eq("DELETE"),
                    eq(AuditLevel.HIGH), isNull(), anyString());
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(storageLocationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> storageLocationService.deleteStorageLocation(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void locationHasVehicles_throwsBusinessException() {
            StorageLocation entity = storageLocationWithCounts(3, 0);
            when(storageLocationRepository.findById(1L)).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> storageLocationService.deleteStorageLocation(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Vehicles are currently assigned");
        }

        @Test
        void auditLevel_isHigh() {
            StorageLocation entity = storageLocationWithCounts(0, 0);
            entity.setName("Empty");
            when(storageLocationRepository.findById(1L)).thenReturn(Optional.of(entity));

            storageLocationService.deleteStorageLocation(1L);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.HIGH), any(), any());
        }

        @Test
        void auditField_nullWhenNotAuthenticated() {
            StorageLocation entity = storageLocationWithCounts(0, 0);
            entity.setName("Empty");
            when(storageLocationRepository.findById(1L)).thenReturn(Optional.of(entity));

            storageLocationService.deleteStorageLocation(1L);

            ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
            verify(auditService).log(any(), any(), any(), any(), employeeCaptor.capture(), any());
            assertThat(employeeCaptor.getValue()).isNull();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getStorageLocationById
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getStorageLocationById")
    class GetStorageLocationById {

        @Test
        void happyPath_returnsResponse() {
            StorageLocation entity = new StorageLocation();
            entity.setId(1L);
            StorageLocationResponse response = new StorageLocationResponse();
            response.setId(1L);

            when(storageLocationRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(storageLocationMapper.toResponse(entity)).thenReturn(response);

            StorageLocationResponse result = storageLocationService.getStorageLocationById(1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(storageLocationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> storageLocationService.getStorageLocationById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllStorageLocations
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllStorageLocations")
    class GetAllStorageLocations {

        @Test
        void happyPath_returnsPaginatedResponse() {
            Page<StorageLocation> page = new PageImpl<>(List.of());
            when(storageLocationRepository.findAll(any(Pageable.class))).thenReturn(page);
            when(storageLocationMapper.toResponseList(List.of())).thenReturn(List.of());

            var result = storageLocationService.getAllStorageLocations(0, 20);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // hasAvailableCapacity
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("hasAvailableCapacity")
    class HasAvailableCapacity {

        @Test
        void returnsTrue_whenSpaceAvailable() {
            StorageLocation entity = storageLocationWithCounts(3, 2);
            entity.setTotalCapacity(10);
            when(storageLocationRepository.findById(1L)).thenReturn(Optional.of(entity));

            boolean result = storageLocationService.hasAvailableCapacity(1L);

            assertThat(result).isTrue();
        }

        @Test
        void returnsFalse_whenFull() {
            StorageLocation entity = storageLocationWithCounts(5, 5);
            entity.setTotalCapacity(10);
            when(storageLocationRepository.findById(1L)).thenReturn(Optional.of(entity));

            boolean result = storageLocationService.hasAvailableCapacity(1L);

            assertThat(result).isFalse();
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(storageLocationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> storageLocationService.hasAvailableCapacity(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    private StorageLocation storageLocationWithCounts(int cars, int motorcycles) {
        StorageLocation loc = new StorageLocation();
        loc.setId(1L);
        loc.setCurrentCarCount(cars);
        loc.setCurrentMotorcycleCount(motorcycles);
        return loc;
    }
}
