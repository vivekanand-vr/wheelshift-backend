package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.MotorcycleInspectionRequest;
import com.wheelshiftpro.dto.response.MotorcycleInspectionResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.Motorcycle;
import com.wheelshiftpro.entity.MotorcycleInspection;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.MotorcycleInspectionMapper;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.MotorcycleInspectionRepository;
import com.wheelshiftpro.repository.MotorcycleRepository;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MotorcycleInspectionServiceImplTest {

    @Mock MotorcycleInspectionRepository motorcycleInspectionRepository;
    @Mock MotorcycleInspectionMapper motorcycleInspectionMapper;
    @Mock MotorcycleRepository motorcycleRepository;
    @Mock EmployeeRepository employeeRepository;
    @Mock AuditService auditService;

    @InjectMocks
    MotorcycleInspectionServiceImpl motorcycleInspectionService;

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
    // createInspection
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createInspection")
    class CreateInspection {

        @Test
        void happyPath_savesAndAudits() {
            MotorcycleInspectionRequest request = validRequest(1L, LocalDate.now(), null);

            MotorcycleInspection entity = new MotorcycleInspection();
            entity.setId(20L);
            entity.setInspectionDate(request.getInspectionDate());
            MotorcycleInspectionResponse response = new MotorcycleInspectionResponse();
            response.setId(20L);

            when(motorcycleRepository.existsById(1L)).thenReturn(true);
            when(motorcycleInspectionMapper.toEntity(request)).thenReturn(entity);
            when(motorcycleRepository.getReferenceById(1L)).thenReturn(new Motorcycle());
            when(motorcycleInspectionRepository.save(entity)).thenReturn(entity);
            when(motorcycleInspectionMapper.toResponse(entity)).thenReturn(response);

            MotorcycleInspectionResponse result = motorcycleInspectionService.createInspection(request);

            assertThat(result.getId()).isEqualTo(20L);
            verify(auditService).log(eq(AuditCategory.INSPECTION), eq(20L), eq("CREATE"),
                    eq(AuditLevel.REGULAR), isNull(), anyString());
        }

        @Test
        void withInspector_wiresInspectorReference() {
            MotorcycleInspectionRequest request = validRequest(1L, LocalDate.now(), 7L);

            MotorcycleInspection entity = new MotorcycleInspection();
            entity.setId(20L);
            entity.setInspectionDate(request.getInspectionDate());
            Employee inspector = new Employee();
            inspector.setId(7L);

            when(motorcycleRepository.existsById(1L)).thenReturn(true);
            when(employeeRepository.existsById(7L)).thenReturn(true);
            when(motorcycleInspectionMapper.toEntity(request)).thenReturn(entity);
            when(motorcycleRepository.getReferenceById(1L)).thenReturn(new Motorcycle());
            when(employeeRepository.getReferenceById(7L)).thenReturn(inspector);
            when(motorcycleInspectionRepository.save(entity)).thenReturn(entity);
            when(motorcycleInspectionMapper.toResponse(entity)).thenReturn(new MotorcycleInspectionResponse());

            motorcycleInspectionService.createInspection(request);

            assertThat(entity.getInspector()).isNotNull();
            assertThat(entity.getInspector().getId()).isEqualTo(7L);
        }

        @Test
        void motorcycleNotFound_throwsResourceNotFoundException() {
            MotorcycleInspectionRequest request = validRequest(99L, LocalDate.now(), null);
            when(motorcycleRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> motorcycleInspectionService.createInspection(request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(auditService, never()).log(any(), any(), any(), any(), any(), any());
        }

        @Test
        void inspectorNotFound_throwsResourceNotFoundException() {
            MotorcycleInspectionRequest request = validRequest(1L, LocalDate.now(), 99L);
            when(motorcycleRepository.existsById(1L)).thenReturn(true);
            when(employeeRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> motorcycleInspectionService.createInspection(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void futureDate_throwsBusinessException() {
            MotorcycleInspectionRequest request = validRequest(1L, LocalDate.now().plusDays(1), null);
            when(motorcycleRepository.existsById(1L)).thenReturn(true);

            assertThatThrownBy(() -> motorcycleInspectionService.createInspection(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("future");
        }

        @Test
        void auditLevel_isRegular() {
            MotorcycleInspectionRequest request = validRequest(1L, LocalDate.now(), null);
            MotorcycleInspection entity = new MotorcycleInspection();
            entity.setId(20L);
            entity.setInspectionDate(request.getInspectionDate());

            when(motorcycleRepository.existsById(1L)).thenReturn(true);
            when(motorcycleInspectionMapper.toEntity(request)).thenReturn(entity);
            when(motorcycleRepository.getReferenceById(1L)).thenReturn(new Motorcycle());
            when(motorcycleInspectionRepository.save(entity)).thenReturn(entity);
            when(motorcycleInspectionMapper.toResponse(entity)).thenReturn(new MotorcycleInspectionResponse());

            motorcycleInspectionService.createInspection(request);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        void auditField_populatedWhenAuthenticated() {
            setUpAuthenticatedEmployee(8L);

            MotorcycleInspectionRequest request = validRequest(1L, LocalDate.now(), null);
            MotorcycleInspection entity = new MotorcycleInspection();
            entity.setId(20L);
            entity.setInspectionDate(request.getInspectionDate());

            when(motorcycleRepository.existsById(1L)).thenReturn(true);
            when(motorcycleInspectionMapper.toEntity(request)).thenReturn(entity);
            when(motorcycleRepository.getReferenceById(1L)).thenReturn(new Motorcycle());
            when(motorcycleInspectionRepository.save(entity)).thenReturn(entity);
            when(motorcycleInspectionMapper.toResponse(entity)).thenReturn(new MotorcycleInspectionResponse());

            motorcycleInspectionService.createInspection(request);

            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(auditService).log(any(), any(), any(), any(), captor.capture(), any());
            assertThat(captor.getValue()).isNotNull();
            assertThat(captor.getValue().getId()).isEqualTo(8L);
        }

        @Test
        void auditField_nullWhenNotAuthenticated() {
            MotorcycleInspectionRequest request = validRequest(1L, LocalDate.now(), null);
            MotorcycleInspection entity = new MotorcycleInspection();
            entity.setId(20L);
            entity.setInspectionDate(request.getInspectionDate());

            when(motorcycleRepository.existsById(1L)).thenReturn(true);
            when(motorcycleInspectionMapper.toEntity(request)).thenReturn(entity);
            when(motorcycleRepository.getReferenceById(1L)).thenReturn(new Motorcycle());
            when(motorcycleInspectionRepository.save(entity)).thenReturn(entity);
            when(motorcycleInspectionMapper.toResponse(entity)).thenReturn(new MotorcycleInspectionResponse());

            motorcycleInspectionService.createInspection(request);

            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(auditService).log(any(), any(), any(), any(), captor.capture(), any());
            assertThat(captor.getValue()).isNull();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateInspection
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateInspection")
    class UpdateInspection {

        @Test
        void happyPath_updatesAndAudits() {
            MotorcycleInspectionRequest request = validRequest(1L, LocalDate.now(), null);
            MotorcycleInspection entity = new MotorcycleInspection();
            entity.setId(20L);

            when(motorcycleInspectionRepository.findById(20L)).thenReturn(Optional.of(entity));
            when(motorcycleInspectionRepository.save(entity)).thenReturn(entity);
            when(motorcycleInspectionMapper.toResponse(entity)).thenReturn(new MotorcycleInspectionResponse());

            motorcycleInspectionService.updateInspection(20L, request);

            verify(motorcycleInspectionMapper).updateEntityFromRequest(request, entity);
            verify(auditService).log(eq(AuditCategory.INSPECTION), eq(20L), eq("UPDATE"),
                    eq(AuditLevel.REGULAR), isNull(), anyString());
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(motorcycleInspectionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> motorcycleInspectionService.updateInspection(99L, validRequest(1L, LocalDate.now(), null)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void futureDateOnUpdate_throwsBusinessException() {
            MotorcycleInspectionRequest request = validRequest(1L, LocalDate.now().plusDays(3), null);
            MotorcycleInspection entity = new MotorcycleInspection();
            entity.setId(20L);
            when(motorcycleInspectionRepository.findById(20L)).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> motorcycleInspectionService.updateInspection(20L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("future");
        }

        @Test
        void nullDateOnUpdate_doesNotThrow() {
            MotorcycleInspectionRequest request = validRequest(1L, null, null);
            MotorcycleInspection entity = new MotorcycleInspection();
            entity.setId(20L);

            when(motorcycleInspectionRepository.findById(20L)).thenReturn(Optional.of(entity));
            when(motorcycleInspectionRepository.save(entity)).thenReturn(entity);
            when(motorcycleInspectionMapper.toResponse(entity)).thenReturn(new MotorcycleInspectionResponse());

            motorcycleInspectionService.updateInspection(20L, request);

            verify(motorcycleInspectionRepository).save(entity);
        }

        @Test
        void inspectorNotFoundOnUpdate_throwsResourceNotFoundException() {
            MotorcycleInspectionRequest request = validRequest(1L, LocalDate.now(), 99L);
            MotorcycleInspection entity = new MotorcycleInspection();
            entity.setId(20L);

            when(motorcycleInspectionRepository.findById(20L)).thenReturn(Optional.of(entity));
            when(employeeRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> motorcycleInspectionService.updateInspection(20L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void newInspectorOnUpdate_wiresInspectorReference() {
            MotorcycleInspectionRequest request = validRequest(1L, LocalDate.now(), 7L);
            MotorcycleInspection entity = new MotorcycleInspection();
            entity.setId(20L);
            Employee inspector = new Employee();
            inspector.setId(7L);

            when(motorcycleInspectionRepository.findById(20L)).thenReturn(Optional.of(entity));
            when(employeeRepository.existsById(7L)).thenReturn(true);
            when(employeeRepository.getReferenceById(7L)).thenReturn(inspector);
            when(motorcycleInspectionRepository.save(entity)).thenReturn(entity);
            when(motorcycleInspectionMapper.toResponse(entity)).thenReturn(new MotorcycleInspectionResponse());

            motorcycleInspectionService.updateInspection(20L, request);

            assertThat(entity.getInspector()).isNotNull();
            assertThat(entity.getInspector().getId()).isEqualTo(7L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteInspection
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteInspection")
    class DeleteInspection {

        @Test
        void happyPath_deletesAndAudits() {
            when(motorcycleInspectionRepository.existsById(20L)).thenReturn(true);

            motorcycleInspectionService.deleteInspection(20L);

            verify(motorcycleInspectionRepository).deleteById(20L);
            verify(auditService).log(eq(AuditCategory.INSPECTION), eq(20L), eq("DELETE"),
                    eq(AuditLevel.HIGH), isNull(), anyString());
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(motorcycleInspectionRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> motorcycleInspectionService.deleteInspection(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(motorcycleInspectionRepository, never()).deleteById(any());
        }

        @Test
        void auditLevel_isHigh() {
            when(motorcycleInspectionRepository.existsById(20L)).thenReturn(true);

            motorcycleInspectionService.deleteInspection(20L);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.HIGH), any(), any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getInspectionById
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getInspectionById")
    class GetInspectionById {

        @Test
        void happyPath_returnsResponse() {
            MotorcycleInspection entity = new MotorcycleInspection();
            entity.setId(20L);
            MotorcycleInspectionResponse response = new MotorcycleInspectionResponse();
            response.setId(20L);

            when(motorcycleInspectionRepository.findById(20L)).thenReturn(Optional.of(entity));
            when(motorcycleInspectionMapper.toResponse(entity)).thenReturn(response);

            MotorcycleInspectionResponse result = motorcycleInspectionService.getInspectionById(20L);

            assertThat(result.getId()).isEqualTo(20L);
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(motorcycleInspectionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> motorcycleInspectionService.getInspectionById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    private MotorcycleInspectionRequest validRequest(Long motorcycleId, LocalDate date, Long inspectorId) {
        MotorcycleInspectionRequest r = new MotorcycleInspectionRequest();
        r.setMotorcycleId(motorcycleId);
        r.setInspectionDate(date);
        r.setInspectorId(inspectorId);
        r.setOverallCondition("GOOD");
        return r;
    }
}
