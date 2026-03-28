package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.CarModelRequest;
import com.wheelshiftpro.dto.response.CarModelResponse;
import com.wheelshiftpro.entity.Car;
import com.wheelshiftpro.entity.CarModel;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.CarModelMapper;
import com.wheelshiftpro.repository.CarModelRepository;
import com.wheelshiftpro.repository.EmployeeRepository;
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
@DisplayName("CarModelServiceImpl")
class CarModelServiceImplTest {

    @Mock CarModelRepository carModelRepository;
    @Mock CarModelMapper carModelMapper;
    @Mock AuditService auditService;
    @Mock EmployeeRepository employeeRepository;

    @InjectMocks
    CarModelServiceImpl carModelService;

    // ── helpers ──────────────────────────────────────────────────────────────

    private CarModel model(Long id, String make, String model, String variant) {
        CarModel m = CarModel.builder().make(make).model(model).variant(variant).build();
        m.setId(id);
        return m;
    }

    private CarModel modelWithCars(Long id) {
        CarModel m = model(id, "Toyota", "Camry", "XLE");
        Car car = new Car();
        m.getCars().add(car);
        return m;
    }

    private CarModelRequest request(String make, String model, String variant) {
        return CarModelRequest.builder().make(make).model(model).variant(variant).build();
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

    // ── createCarModel ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("createCarModel")
    class CreateCarModel {

        @Test
        @DisplayName("happy path — persists model and logs audit")
        void happyPath() {
            CarModelRequest req = request("Toyota", "Camry", "XLE");
            CarModel entity = model(1L, "Toyota", "Camry", "XLE");
            CarModelResponse response = new CarModelResponse();

            when(carModelRepository.existsByMakeAndModelAndVariant("Toyota", "Camry", "XLE")).thenReturn(false);
            when(carModelMapper.toEntity(req)).thenReturn(entity);
            when(carModelRepository.save(entity)).thenReturn(entity);
            when(carModelMapper.toResponse(entity)).thenReturn(response);

            CarModelResponse result = carModelService.createCarModel(req);

            assertThat(result).isSameAs(response);
            verify(carModelRepository).save(entity);
            verify(auditService).log(eq(AuditCategory.CAR), eq(1L), eq("CREATE"),
                    eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("duplicate make+model+variant — throws DuplicateResourceException")
        void duplicateMakeModelVariant_throws() {
            CarModelRequest req = request("Toyota", "Camry", "XLE");
            when(carModelRepository.existsByMakeAndModelAndVariant("Toyota", "Camry", "XLE")).thenReturn(true);

            assertThatThrownBy(() -> carModelService.createCarModel(req))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(carModelRepository, never()).save(any());
            verify(auditService, never()).log(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("audit level is REGULAR")
        void auditLevelIsRegular() {
            CarModelRequest req = request("Honda", "Civic", "EX");
            CarModel entity = model(2L, "Honda", "Civic", "EX");

            when(carModelRepository.existsByMakeAndModelAndVariant(any(), any(), any())).thenReturn(false);
            when(carModelMapper.toEntity(req)).thenReturn(entity);
            when(carModelRepository.save(entity)).thenReturn(entity);
            when(carModelMapper.toResponse(entity)).thenReturn(new CarModelResponse());

            carModelService.createCarModel(req);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.REGULAR), any(), any());
        }
    }

    // ── updateCarModel ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateCarModel")
    class UpdateCarModel {

        @Test
        @DisplayName("happy path — updates and logs audit")
        void happyPath() {
            CarModelRequest req = request("Toyota", "Camry", "SE");
            CarModel existing = model(1L, "Toyota", "Camry", "XLE");
            CarModelResponse response = new CarModelResponse();

            when(carModelRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(carModelRepository.existsByMakeAndModelAndVariantAndIdNot("Toyota", "Camry", "SE", 1L))
                    .thenReturn(false);
            when(carModelRepository.save(existing)).thenReturn(existing);
            when(carModelMapper.toResponse(existing)).thenReturn(response);

            CarModelResponse result = carModelService.updateCarModel(1L, req);

            assertThat(result).isSameAs(response);
            verify(carModelMapper).updateEntityFromRequest(req, existing);
            verify(auditService).log(eq(AuditCategory.CAR), eq(1L), eq("UPDATE"),
                    eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("model not found — throws ResourceNotFoundException")
        void modelNotFound_throws() {
            when(carModelRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> carModelService.updateCarModel(99L, request("A", "B", "C")))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("duplicate make+model+variant on update — throws DuplicateResourceException")
        void duplicateOnUpdate_throws() {
            CarModelRequest req = request("Toyota", "Camry", "SE");
            CarModel existing = model(1L, "Toyota", "Camry", "XLE");

            when(carModelRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(carModelRepository.existsByMakeAndModelAndVariantAndIdNot("Toyota", "Camry", "SE", 1L))
                    .thenReturn(true);

            assertThatThrownBy(() -> carModelService.updateCarModel(1L, req))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(carModelRepository, never()).save(any());
        }
    }

    // ── deleteCarModel ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteCarModel")
    class DeleteCarModel {

        @Test
        @DisplayName("happy path — deletes and logs audit at HIGH level")
        void happyPath() {
            CarModel existing = model(1L, "Toyota", "Camry", "XLE");

            when(carModelRepository.findById(1L)).thenReturn(Optional.of(existing));

            carModelService.deleteCarModel(1L);

            verify(carModelRepository).delete(existing);
            verify(auditService).log(eq(AuditCategory.CAR), eq(1L), eq("DELETE"),
                    eq(AuditLevel.HIGH), any(), any());
        }

        @Test
        @DisplayName("model not found — throws ResourceNotFoundException")
        void modelNotFound_throws() {
            when(carModelRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> carModelService.deleteCarModel(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("model has associated cars — throws BusinessException with MODEL_HAS_VEHICLES")
        void modelHasCars_throws() {
            CarModel existing = modelWithCars(1L);
            when(carModelRepository.findById(1L)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> carModelService.deleteCarModel(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("vehicle")
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo("MODEL_HAS_VEHICLES"));

            verify(carModelRepository, never()).delete(any());
        }

        @Test
        @DisplayName("audit level is HIGH on delete")
        void auditLevelIsHigh() {
            CarModel existing = model(1L, "Toyota", "Camry", "XLE");
            when(carModelRepository.findById(1L)).thenReturn(Optional.of(existing));

            carModelService.deleteCarModel(1L);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.HIGH), any(), any());
        }

        @Test
        @DisplayName("resolveCurrentEmployee populated when authenticated")
        void resolveCurrentEmployee_populatedWhenAuthenticated() {
            setUpAuthenticatedEmployee(5L);
            CarModel existing = model(1L, "Toyota", "Camry", "XLE");
            when(carModelRepository.findById(1L)).thenReturn(Optional.of(existing));

            carModelService.deleteCarModel(1L);

            verify(employeeRepository).getReferenceById(5L);
        }

        @Test
        @DisplayName("resolveCurrentEmployee is null when no authenticated user")
        void resolveCurrentEmployee_nullWhenUnauthenticated() {
            SecurityContextHolder.clearContext();
            CarModel existing = model(1L, "Toyota", "Camry", "XLE");
            when(carModelRepository.findById(1L)).thenReturn(Optional.of(existing));

            carModelService.deleteCarModel(1L);

            verify(employeeRepository, never()).getReferenceById(any());
        }
    }
}
