package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.CarInspectionRequest;
import com.wheelshiftpro.dto.response.CarInspectionResponse;
import com.wheelshiftpro.entity.Car;
import com.wheelshiftpro.entity.CarInspection;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.CarInspectionMapper;
import com.wheelshiftpro.repository.CarInspectionRepository;
import com.wheelshiftpro.repository.CarRepository;
import com.wheelshiftpro.repository.EmployeeRepository;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarInspectionServiceImplTest {

    @Mock CarInspectionRepository carInspectionRepository;
    @Mock CarInspectionMapper carInspectionMapper;
    @Mock CarRepository carRepository;
    @Mock EmployeeRepository employeeRepository;
    @Mock AuditService auditService;

    @InjectMocks
    CarInspectionServiceImpl carInspectionService;

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
            CarInspectionRequest request = validRequest(1L, LocalDate.now());

            Car car = new Car();
            car.setId(1L);
            CarInspection entity = new CarInspection();
            entity.setId(10L);
            entity.setInspectionDate(request.getInspectionDate());
            CarInspectionResponse response = new CarInspectionResponse();
            response.setId(10L);

            when(carRepository.existsById(1L)).thenReturn(true);
            when(carInspectionMapper.toEntity(request)).thenReturn(entity);
            when(carRepository.getReferenceById(1L)).thenReturn(car);
            when(carInspectionRepository.save(entity)).thenReturn(entity);
            when(carInspectionMapper.toResponse(entity)).thenReturn(response);

            CarInspectionResponse result = carInspectionService.createInspection(request);

            assertThat(result.getId()).isEqualTo(10L);
            verify(carInspectionMapper).toEntity(request);
            verify(auditService).log(eq(AuditCategory.INSPECTION), eq(10L), eq("CREATE"),
                    eq(AuditLevel.REGULAR), isNull(), anyString());
        }

        @Test
        void carNotFound_throwsResourceNotFoundException() {
            CarInspectionRequest request = validRequest(99L, LocalDate.now());
            when(carRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> carInspectionService.createInspection(request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(auditService, never()).log(any(), any(), any(), any(), any(), any());
        }

        @Test
        void futureDate_throwsBusinessException() {
            CarInspectionRequest request = validRequest(1L, LocalDate.now().plusDays(1));
            when(carRepository.existsById(1L)).thenReturn(true);

            assertThatThrownBy(() -> carInspectionService.createInspection(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("future");
        }

        @Test
        void auditLevel_isRegular() {
            CarInspectionRequest request = validRequest(1L, LocalDate.now());
            CarInspection entity = new CarInspection();
            entity.setId(10L);
            entity.setInspectionDate(request.getInspectionDate());

            when(carRepository.existsById(1L)).thenReturn(true);
            when(carInspectionMapper.toEntity(request)).thenReturn(entity);
            when(carRepository.getReferenceById(1L)).thenReturn(new Car());
            when(carInspectionRepository.save(entity)).thenReturn(entity);
            when(carInspectionMapper.toResponse(entity)).thenReturn(new CarInspectionResponse());

            carInspectionService.createInspection(request);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        void auditField_populatedWhenAuthenticated() {
            setUpAuthenticatedEmployee(5L);

            CarInspectionRequest request = validRequest(1L, LocalDate.now());
            CarInspection entity = new CarInspection();
            entity.setId(10L);
            entity.setInspectionDate(request.getInspectionDate());

            when(carRepository.existsById(1L)).thenReturn(true);
            when(carInspectionMapper.toEntity(request)).thenReturn(entity);
            when(carRepository.getReferenceById(1L)).thenReturn(new Car());
            when(carInspectionRepository.save(entity)).thenReturn(entity);
            when(carInspectionMapper.toResponse(entity)).thenReturn(new CarInspectionResponse());

            carInspectionService.createInspection(request);

            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(auditService).log(any(), any(), any(), any(), captor.capture(), any());
            assertThat(captor.getValue()).isNotNull();
            assertThat(captor.getValue().getId()).isEqualTo(5L);
        }

        @Test
        void auditField_nullWhenNotAuthenticated() {
            CarInspectionRequest request = validRequest(1L, LocalDate.now());
            CarInspection entity = new CarInspection();
            entity.setId(10L);
            entity.setInspectionDate(request.getInspectionDate());

            when(carRepository.existsById(1L)).thenReturn(true);
            when(carInspectionMapper.toEntity(request)).thenReturn(entity);
            when(carRepository.getReferenceById(1L)).thenReturn(new Car());
            when(carInspectionRepository.save(entity)).thenReturn(entity);
            when(carInspectionMapper.toResponse(entity)).thenReturn(new CarInspectionResponse());

            carInspectionService.createInspection(request);

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
            CarInspectionRequest request = validRequest(1L, LocalDate.now());
            CarInspection entity = new CarInspection();
            entity.setId(10L);

            when(carInspectionRepository.findById(10L)).thenReturn(Optional.of(entity));
            when(carInspectionRepository.save(entity)).thenReturn(entity);
            when(carInspectionMapper.toResponse(entity)).thenReturn(new CarInspectionResponse());

            carInspectionService.updateInspection(10L, request);

            verify(carInspectionMapper).updateEntityFromRequest(request, entity);
            verify(auditService).log(eq(AuditCategory.INSPECTION), eq(10L), eq("UPDATE"),
                    eq(AuditLevel.REGULAR), isNull(), anyString());
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(carInspectionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> carInspectionService.updateInspection(99L, validRequest(1L, LocalDate.now())))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void futureDateOnUpdate_throwsBusinessException() {
            CarInspectionRequest request = validRequest(1L, LocalDate.now().plusDays(2));
            CarInspection entity = new CarInspection();
            entity.setId(10L);
            when(carInspectionRepository.findById(10L)).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> carInspectionService.updateInspection(10L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("future");
        }

        @Test
        void nullInspectionDate_doesNotThrow() {
            CarInspectionRequest request = validRequest(1L, null);
            CarInspection entity = new CarInspection();
            entity.setId(10L);

            when(carInspectionRepository.findById(10L)).thenReturn(Optional.of(entity));
            when(carInspectionRepository.save(entity)).thenReturn(entity);
            when(carInspectionMapper.toResponse(entity)).thenReturn(new CarInspectionResponse());

            carInspectionService.updateInspection(10L, request);

            // null date must not throw FUTURE_INSPECTION_DATE
            verify(carInspectionRepository).save(entity);
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
            when(carInspectionRepository.existsById(10L)).thenReturn(true);

            carInspectionService.deleteInspection(10L);

            verify(carInspectionRepository).deleteById(10L);
            verify(auditService).log(eq(AuditCategory.INSPECTION), eq(10L), eq("DELETE"),
                    eq(AuditLevel.HIGH), isNull(), anyString());
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(carInspectionRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> carInspectionService.deleteInspection(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(carInspectionRepository, never()).deleteById(any());
        }

        @Test
        void auditLevel_isHigh() {
            when(carInspectionRepository.existsById(10L)).thenReturn(true);

            carInspectionService.deleteInspection(10L);

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
            CarInspection entity = new CarInspection();
            entity.setId(10L);
            CarInspectionResponse response = new CarInspectionResponse();
            response.setId(10L);

            when(carInspectionRepository.findById(10L)).thenReturn(Optional.of(entity));
            when(carInspectionMapper.toResponse(entity)).thenReturn(response);

            CarInspectionResponse result = carInspectionService.getInspectionById(10L);

            assertThat(result.getId()).isEqualTo(10L);
        }

        @Test
        void notFound_throwsResourceNotFoundException() {
            when(carInspectionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> carInspectionService.getInspectionById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getInspectionsByCarId
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getInspectionsByCarId")
    class GetInspectionsByCarId {

        @Test
        void happyPath_returnsPaginatedResponse() {
            Page<CarInspection> page = new PageImpl<>(List.of());
            when(carRepository.existsById(1L)).thenReturn(true);
            when(carInspectionRepository.findByCarId(eq(1L), any(Pageable.class))).thenReturn(page);
            when(carInspectionMapper.toResponseList(List.of())).thenReturn(List.of());

            var result = carInspectionService.getInspectionsByCarId(1L, 0, 20);

            assertThat(result).isNotNull();
        }

        @Test
        void carNotFound_throwsResourceNotFoundException() {
            when(carRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> carInspectionService.getInspectionsByCarId(99L, 0, 20))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    private CarInspectionRequest validRequest(Long carId, LocalDate date) {
        CarInspectionRequest r = new CarInspectionRequest();
        r.setCarId(carId);
        r.setInspectionDate(date);
        return r;
    }
}
