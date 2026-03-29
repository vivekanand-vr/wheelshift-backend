package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.MotorcycleModelRequest;
import com.wheelshiftpro.dto.response.MotorcycleModelResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.Motorcycle;
import com.wheelshiftpro.entity.MotorcycleModel;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.MotorcycleModelMapper;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.MotorcycleModelRepository;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.AuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("MotorcycleModelServiceImpl")
class MotorcycleModelServiceImplTest {

    @Mock MotorcycleModelRepository motorcycleModelRepository;
    @Mock MotorcycleModelMapper motorcycleModelMapper;
    @Mock AuditService auditService;
    @Mock EmployeeRepository employeeRepository;

    @InjectMocks
    MotorcycleModelServiceImpl motorcycleModelService;

    // ── helpers ──────────────────────────────────────────────────────────────

    private MotorcycleModel model(Long id, String make, String model, String variant) {
        MotorcycleModel m = MotorcycleModel.builder().make(make).model(model).variant(variant).build();
        m.setId(id);
        return m;
    }

    private MotorcycleModel modelWithMotorcycles(Long id) {
        MotorcycleModel m = model(id, "Honda", "CBR", "500R");
        Motorcycle motorcycle = new Motorcycle();
        m.getMotorcycles().add(motorcycle);
        return m;
    }

    private MotorcycleModelRequest request(String make, String model, String variant) {
        return MotorcycleModelRequest.builder().make(make).model(model).variant(variant).build();
    }

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

    // ── createMotorcycleModel ────────────────────────────────────────────────

    @Nested
    @DisplayName("createMotorcycleModel")
    class CreateMotorcycleModel {

        @Test
        @DisplayName("happy path — persists model and logs audit")
        void happyPath() {
            MotorcycleModelRequest req = request("Honda", "CBR", "500R");
            MotorcycleModel entity = model(1L, "Honda", "CBR", "500R");
            MotorcycleModelResponse response = new MotorcycleModelResponse();

            when(motorcycleModelRepository.existsByMakeAndModelAndVariant("Honda", "CBR", "500R"))
                    .thenReturn(false);
            when(motorcycleModelMapper.toEntity(req)).thenReturn(entity);
            when(motorcycleModelRepository.save(entity)).thenReturn(entity);
            when(motorcycleModelMapper.toResponse(entity)).thenReturn(response);

            MotorcycleModelResponse result = motorcycleModelService.createMotorcycleModel(req);

            assertThat(result).isSameAs(response);
            verify(motorcycleModelRepository).save(entity);
            verify(auditService).log(eq(AuditCategory.MOTORCYCLE), eq(1L), eq("CREATE"),
                    eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("duplicate make+model+variant — throws DuplicateResourceException")
        void duplicateMakeModelVariant_throws() {
            MotorcycleModelRequest req = request("Honda", "CBR", "500R");
            when(motorcycleModelRepository.existsByMakeAndModelAndVariant("Honda", "CBR", "500R"))
                    .thenReturn(true);

            assertThatThrownBy(() -> motorcycleModelService.createMotorcycleModel(req))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(motorcycleModelRepository, never()).save(any());
            verify(auditService, never()).log(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("audit level is REGULAR")
        void auditLevelIsRegular() {
            MotorcycleModelRequest req = request("Yamaha", "R3", "Base");
            MotorcycleModel entity = model(2L, "Yamaha", "R3", "Base");

            when(motorcycleModelRepository.existsByMakeAndModelAndVariant(any(), any(), any()))
                    .thenReturn(false);
            when(motorcycleModelMapper.toEntity(req)).thenReturn(entity);
            when(motorcycleModelRepository.save(entity)).thenReturn(entity);
            when(motorcycleModelMapper.toResponse(entity)).thenReturn(new MotorcycleModelResponse());

            motorcycleModelService.createMotorcycleModel(req);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.REGULAR), any(), any());
        }
    }

    // ── updateMotorcycleModel ────────────────────────────────────────────────

    @Nested
    @DisplayName("updateMotorcycleModel")
    class UpdateMotorcycleModel {

        @Test
        @DisplayName("happy path — updates and logs audit")
        void happyPath() {
            MotorcycleModelRequest req = request("Honda", "CBR", "600RR");
            MotorcycleModel existing = model(1L, "Honda", "CBR", "500R");
            MotorcycleModelResponse response = new MotorcycleModelResponse();

            when(motorcycleModelRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(motorcycleModelRepository.existsByMakeAndModelAndVariantAndIdNot("Honda", "CBR", "600RR", 1L))
                    .thenReturn(false);
            when(motorcycleModelRepository.save(existing)).thenReturn(existing);
            when(motorcycleModelMapper.toResponse(existing)).thenReturn(response);

            MotorcycleModelResponse result = motorcycleModelService.updateMotorcycleModel(1L, req);

            assertThat(result).isSameAs(response);
            verify(motorcycleModelMapper).updateEntityFromRequest(req, existing);
            verify(auditService).log(eq(AuditCategory.MOTORCYCLE), eq(1L), eq("UPDATE"),
                    eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("model not found — throws ResourceNotFoundException")
        void modelNotFound_throws() {
            when(motorcycleModelRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> motorcycleModelService.updateMotorcycleModel(99L, request("A", "B", "C")))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("duplicate make+model+variant on update — throws DuplicateResourceException")
        void duplicateOnUpdate_throws() {
            MotorcycleModelRequest req = request("Honda", "CBR", "600RR");
            MotorcycleModel existing = model(1L, "Honda", "CBR", "500R");

            when(motorcycleModelRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(motorcycleModelRepository.existsByMakeAndModelAndVariantAndIdNot("Honda", "CBR", "600RR", 1L))
                    .thenReturn(true);

            assertThatThrownBy(() -> motorcycleModelService.updateMotorcycleModel(1L, req))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(motorcycleModelRepository, never()).save(any());
        }
    }

    // ── deleteMotorcycleModel ────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteMotorcycleModel")
    class DeleteMotorcycleModel {

        @Test
        @DisplayName("happy path — deletes and logs audit at HIGH level")
        void happyPath() {
            MotorcycleModel existing = model(1L, "Honda", "CBR", "500R");
            when(motorcycleModelRepository.findById(1L)).thenReturn(Optional.of(existing));

            motorcycleModelService.deleteMotorcycleModel(1L);

            verify(motorcycleModelRepository).delete(existing);
            verify(auditService).log(eq(AuditCategory.MOTORCYCLE), eq(1L), eq("DELETE"),
                    eq(AuditLevel.HIGH), any(), any());
        }

        @Test
        @DisplayName("model not found — throws ResourceNotFoundException")
        void modelNotFound_throws() {
            when(motorcycleModelRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> motorcycleModelService.deleteMotorcycleModel(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("model has associated motorcycles — throws BusinessException with MODEL_HAS_VEHICLES")
        void modelHasMotorcycles_throws() {
            MotorcycleModel existing = modelWithMotorcycles(1L);
            when(motorcycleModelRepository.findById(1L)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> motorcycleModelService.deleteMotorcycleModel(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("vehicle")
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo("MODEL_HAS_VEHICLES"));

            verify(motorcycleModelRepository, never()).delete(any(MotorcycleModel.class));
        }

        @Test
        @DisplayName("audit level is HIGH on delete")
        void auditLevelIsHigh() {
            MotorcycleModel existing = model(1L, "Honda", "CBR", "500R");
            when(motorcycleModelRepository.findById(1L)).thenReturn(Optional.of(existing));

            motorcycleModelService.deleteMotorcycleModel(1L);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.HIGH), any(), any());
        }

        @Test
        @DisplayName("resolveCurrentEmployee populated when authenticated")
        void resolveCurrentEmployee_populatedWhenAuthenticated() {
            setUpAuthenticatedEmployee(7L);
            MotorcycleModel existing = model(1L, "Honda", "CBR", "500R");
            when(motorcycleModelRepository.findById(1L)).thenReturn(Optional.of(existing));

            motorcycleModelService.deleteMotorcycleModel(1L);

            verify(employeeRepository).getReferenceById(7L);
        }

        @Test
        @DisplayName("resolveCurrentEmployee is null when no authenticated user")
        void resolveCurrentEmployee_nullWhenUnauthenticated() {
            SecurityContextHolder.clearContext();
            MotorcycleModel existing = model(1L, "Honda", "CBR", "500R");
            when(motorcycleModelRepository.findById(1L)).thenReturn(Optional.of(existing));

            motorcycleModelService.deleteMotorcycleModel(1L);

            verify(employeeRepository, never()).getReferenceById(any());
        }
    }
}
