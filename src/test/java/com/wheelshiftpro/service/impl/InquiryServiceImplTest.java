package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.InquiryRequest;
import com.wheelshiftpro.dto.response.InquiryResponse;
import com.wheelshiftpro.dto.response.ReservationResponse;
import com.wheelshiftpro.entity.Car;
import com.wheelshiftpro.entity.Client;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.Inquiry;
import com.wheelshiftpro.entity.Motorcycle;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.CarStatus;
import com.wheelshiftpro.enums.ClientStatus;
import com.wheelshiftpro.enums.InquiryStatus;
import com.wheelshiftpro.enums.MotorcycleStatus;
import com.wheelshiftpro.enums.VehicleType;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.InquiryMapper;
import com.wheelshiftpro.repository.CarRepository;
import com.wheelshiftpro.repository.ClientRepository;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.InquiryRepository;
import com.wheelshiftpro.repository.MotorcycleRepository;
import com.wheelshiftpro.repository.SaleRepository;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.AuditService;
import com.wheelshiftpro.service.ReservationService;
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
@DisplayName("InquiryServiceImpl Tests")
class InquiryServiceImplTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private MotorcycleRepository motorcycleRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private InquiryMapper inquiryMapper;

    @Mock
    private AuditService auditService;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private InquiryServiceImpl inquiryService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void setUpAuthenticatedEmployee(Long employeeId) {
        Employee e = new Employee();
        e.setId(employeeId);
        EmployeeUserDetails principal = mock(EmployeeUserDetails.class);
        when(principal.getId()).thenReturn(employeeId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
        when(employeeRepository.getReferenceById(employeeId)).thenReturn(e);
    }

    @Nested
    @DisplayName("createInquiry")
    class CreateInquiry {

        @Test
        @DisplayName("should create inquiry successfully with all validations passed")
        void happyPath() {
            Long clientId = 1L;
            Long carId = 10L;
            InquiryRequest request = InquiryRequest.builder()
                    .clientId(clientId)
                    .carId(carId)
                    .message("Interested in this car")
                    .build();

            Client client = new Client();
            client.setId(clientId);
            client.setStatus(ClientStatus.ACTIVE);

            Car car = new Car();
            car.setId(carId);

            Inquiry inquiry = new Inquiry();
            inquiry.setStatus(InquiryStatus.OPEN);

            Inquiry saved = new Inquiry();
            saved.setId(100L);

            InquiryResponse response = InquiryResponse.builder()
                    .id(100L)
                    .build();

            when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
            when(inquiryMapper.toEntity(request)).thenReturn(inquiry);
            when(carRepository.findById(carId)).thenReturn(Optional.of(car));
            when(inquiryRepository.save(inquiry)).thenReturn(saved);
            when(inquiryMapper.toResponse(saved)).thenReturn(response);

            setUpAuthenticatedEmployee(50L);

            InquiryResponse result = inquiryService.createInquiry(request);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(100L);
            verify(clientRepository).findById(clientId);
            verify(carRepository).findById(carId);
            assertThat(inquiry.getCar()).isEqualTo(car);
            verify(inquiryRepository).save(inquiry);
            verify(auditService).log(eq(AuditCategory.INQUIRY), eq(100L), eq("CREATE"), 
                    eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("should throw exception when client is not found")
        void clientNotFound() {
            InquiryRequest request = InquiryRequest.builder()
                    .clientId(999L)
                    .carId(10L)
                    .build();

            when(clientRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inquiryService.createInquiry(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client")
                    .hasMessageContaining("999");

            verify(inquiryRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when client is not ACTIVE")
        void clientNotActive() {
            Long clientId = 1L;
            InquiryRequest request = InquiryRequest.builder()
                    .clientId(clientId)
                    .carId(10L)
                    .build();

            Client client = new Client();
            client.setId(clientId);
            client.setStatus(ClientStatus.INACTIVE);

            when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

            assertThatThrownBy(() -> inquiryService.createInquiry(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ACTIVE");

            verify(inquiryRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when car is not found")
        void carNotFound() {
            Long clientId = 1L;
            InquiryRequest request = InquiryRequest.builder()
                    .clientId(clientId)
                    .carId(999L)
                    .build();

            Client client = new Client();
            client.setId(clientId);
            client.setStatus(ClientStatus.ACTIVE);

            Inquiry inquiry = new Inquiry();

            when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
            when(inquiryMapper.toEntity(request)).thenReturn(inquiry);
            when(carRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inquiryService.createInquiry(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Car")
                    .hasMessageContaining("999");

            verify(inquiryRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when both carId and motorcycleId are provided")
        void bothVehiclesProvided() {
            Long clientId = 1L;
            InquiryRequest request = InquiryRequest.builder()
                    .clientId(clientId)
                    .carId(10L)
                    .motorcycleId(20L)
                    .build();

            Client client = new Client();
            client.setId(clientId);
            client.setStatus(ClientStatus.ACTIVE);

            when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

            assertThatThrownBy(() -> inquiryService.createInquiry(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("one vehicle");

            verify(inquiryRepository, never()).save(any());
        }

        @Test
        @DisplayName("should set car relationship after mapping")
        void carRelationshipWired() {
            Long clientId = 1L;
            Long carId = 10L;
            InquiryRequest request = InquiryRequest.builder()
                    .clientId(clientId)
                    .carId(carId)
                    .build();

            Client client = new Client();
            client.setId(clientId);
            client.setStatus(ClientStatus.ACTIVE);

            Car car = new Car();
            car.setId(carId);

            Inquiry inquiry = new Inquiry();
            Inquiry saved = new Inquiry();
            saved.setId(100L);

            when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
            when(inquiryMapper.toEntity(request)).thenReturn(inquiry);
            when(carRepository.findById(carId)).thenReturn(Optional.of(car));
            when(inquiryRepository.save(inquiry)).thenReturn(saved);
            when(inquiryMapper.toResponse(saved)).thenReturn(new InquiryResponse());

            inquiryService.createInquiry(request);

            // Verify car was set on the inquiry
            assertThat(inquiry.getCar()).isEqualTo(car);
        }
    }

    @Nested
    @DisplayName("updateInquiry")
    class UpdateInquiry {

        @Test
        @DisplayName("should update inquiry successfully")
        void happyPath() {
            Long inquiryId = 1L;
            InquiryRequest request = InquiryRequest.builder()
                    .message("Updated text")
                    .build();

            Inquiry inquiry = new Inquiry();
            inquiry.setId(inquiryId);

            InquiryResponse response = InquiryResponse.builder()
                    .id(inquiryId)
                    .build();

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));
            when(inquiryRepository.save(inquiry)).thenReturn(inquiry);
            when(inquiryMapper.toResponse(inquiry)).thenReturn(response);

            setUpAuthenticatedEmployee(50L);

            InquiryResponse result = inquiryService.updateInquiry(inquiryId, request);

            assertThat(result).isNotNull();
            verify(inquiryMapper).updateEntityFromRequest(request, inquiry);
            verify(auditService).log(eq(AuditCategory.INQUIRY), eq(inquiryId), eq("UPDATE"), 
                    eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("should throw exception when inquiry not found")
        void inquiryNotFound() {
            InquiryRequest request = InquiryRequest.builder().build();

            when(inquiryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inquiryService.updateInquiry(999L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Inquiry");

            verify(inquiryRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when assigned employee does not exist")
        void assignedEmployeeNotFound_throws() {
            Long inquiryId = 1L;
            Long nonExistentEmployeeId = 999L;

            InquiryRequest request = InquiryRequest.builder()
                    .assignedEmployeeId(nonExistentEmployeeId)
                    .message("Test inquiry")
                    .build();

            Inquiry inquiry = new Inquiry();
            inquiry.setId(inquiryId);

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));
            when(employeeRepository.existsById(nonExistentEmployeeId)).thenReturn(false);

            assertThatThrownBy(() -> inquiryService.updateInquiry(inquiryId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Employee");

            verify(inquiryRepository, never()).save(any());
            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("should update inquiry with valid assigned employee")
        void validAssignedEmployee_updates() {
            Long inquiryId = 1L;
            Long validEmployeeId = 10L;

            InquiryRequest request = InquiryRequest.builder()
                    .assignedEmployeeId(validEmployeeId)
                    .message("Updated inquiry")
                    .build();

            Inquiry inquiry = new Inquiry();
            inquiry.setId(inquiryId);

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));
            when(employeeRepository.existsById(validEmployeeId)).thenReturn(true);
            when(inquiryRepository.save(inquiry)).thenReturn(inquiry);
            when(inquiryMapper.toResponse(inquiry)).thenReturn(new InquiryResponse());

            InquiryResponse result = inquiryService.updateInquiry(inquiryId, request);

            assertThat(result).isNotNull();
            verify(inquiryRepository).save(inquiry);
            verify(auditService).log(any(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("deleteInquiry")
    class DeleteInquiry {

        @Test
        @DisplayName("should delete inquiry successfully when not CLOSED with sale")
        void happyPath() {
            Long inquiryId = 1L;
            Inquiry inquiry = new Inquiry();
            inquiry.setId(inquiryId);
            inquiry.setStatus(InquiryStatus.OPEN);

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));

            setUpAuthenticatedEmployee(50L);

            inquiryService.deleteInquiry(inquiryId);

            verify(auditService).log(eq(AuditCategory.INQUIRY), eq(inquiryId), eq("DELETE"), 
                    eq(AuditLevel.HIGH), any(), any());
            verify(inquiryRepository).delete(inquiry);
        }

        @Test
        @DisplayName("should throw exception when inquiry not found")
        void inquiryNotFound() {
            when(inquiryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inquiryService.deleteInquiry(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Inquiry");

            verify(inquiryRepository, never()).delete(any(Inquiry.class));
        }

        @Test
        @DisplayName("should throw exception when CLOSED inquiry has sale")
        void closedInquiryWithSale() {
            Long inquiryId = 1L;
            Long carId = 10L;
            Inquiry inquiry = new Inquiry();
            inquiry.setId(inquiryId);
            inquiry.setStatus(InquiryStatus.CLOSED);
            
            Car car = new Car();
            car.setId(carId);
            inquiry.setCar(car);

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));
            when(saleRepository.existsByCarId(carId)).thenReturn(true);

            assertThatThrownBy(() -> inquiryService.deleteInquiry(inquiryId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("sale");

            verify(inquiryRepository, never()).delete(any(Inquiry.class));
        }
    }

    @Nested
    @DisplayName("updateInquiryStatus")
    class UpdateInquiryStatus {

        @Test
        @DisplayName("should update status from OPEN to IN_PROGRESS")
        void openToInProgress() {
            Long inquiryId = 1L;
            Inquiry inquiry = new Inquiry();
            inquiry.setId(inquiryId);
            inquiry.setStatus(InquiryStatus.OPEN);

            InquiryResponse response = InquiryResponse.builder()
                    .id(inquiryId)
                    .status(InquiryStatus.IN_PROGRESS)
                    .build();

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));
            when(inquiryRepository.save(inquiry)).thenReturn(inquiry);
            when(inquiryMapper.toResponse(inquiry)).thenReturn(response);

            setUpAuthenticatedEmployee(50L);

            InquiryResponse result = inquiryService.updateInquiryStatus(inquiryId, InquiryStatus.IN_PROGRESS);

            assertThat(result.getStatus()).isEqualTo(InquiryStatus.IN_PROGRESS);
            verify(auditService).log(eq(AuditCategory.INQUIRY), eq(inquiryId), eq("STATUS_CHANGE"), 
                    eq(AuditLevel.HIGH), any(), any());
        }

        @Test
        @DisplayName("should update status from IN_PROGRESS to RESPONDED with response text")
        void inProgressToResponded() {
            Long inquiryId = 1L;
            Inquiry inquiry = new Inquiry();
            inquiry.setId(inquiryId);
            inquiry.setStatus(InquiryStatus.IN_PROGRESS);
            inquiry.setResponse("Here is our response");

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));
            when(inquiryRepository.save(inquiry)).thenReturn(inquiry);
            when(inquiryMapper.toResponse(inquiry)).thenReturn(new InquiryResponse());

            inquiryService.updateInquiryStatus(inquiryId, InquiryStatus.RESPONDED);

            assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.RESPONDED);
        }

        @Test
        @DisplayName("should throw exception when transitioning to RESPONDED without response text")
        void respondedWithoutText() {
            Long inquiryId = 1L;
            Inquiry inquiry = new Inquiry();
            inquiry.setId(inquiryId);
            inquiry.setStatus(InquiryStatus.IN_PROGRESS);

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));

            assertThatThrownBy(() -> inquiryService.updateInquiryStatus(inquiryId, InquiryStatus.RESPONDED))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("response text");

            verify(inquiryRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception for invalid transition from RESPONDED to IN_PROGRESS")
        void invalidTransitionBackward() {
            Long inquiryId = 1L;
            Inquiry inquiry = new Inquiry();
            inquiry.setId(inquiryId);
            inquiry.setStatus(InquiryStatus.RESPONDED);

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));

            assertThatThrownBy(() -> inquiryService.updateInquiryStatus(inquiryId, InquiryStatus.IN_PROGRESS))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Invalid status transition");

            verify(inquiryRepository, never()).save(any());
        }

        @Test
        @DisplayName("should allow direct transition from OPEN to CLOSED")
        void openToClosed() {
            Long inquiryId = 1L;
            Inquiry inquiry = new Inquiry();
            inquiry.setId(inquiryId);
            inquiry.setStatus(InquiryStatus.OPEN);

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));
            when(inquiryRepository.save(inquiry)).thenReturn(inquiry);
            when(inquiryMapper.toResponse(inquiry)).thenReturn(new InquiryResponse());

            inquiryService.updateInquiryStatus(inquiryId, InquiryStatus.CLOSED);

            assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.CLOSED);
        }

        @Test
        @DisplayName("should update status from RESPONDED to CLOSED")
        void respondedToClosed() {
            Long inquiryId = 1L;
            Inquiry inquiry = new Inquiry();
            inquiry.setId(inquiryId);
            inquiry.setStatus(InquiryStatus.RESPONDED);

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));
            when(inquiryRepository.save(inquiry)).thenReturn(inquiry);
            when(inquiryMapper.toResponse(inquiry)).thenReturn(new InquiryResponse());

            inquiryService.updateInquiryStatus(inquiryId, InquiryStatus.CLOSED);

            assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.CLOSED);
        }
    }

    @Nested
    @DisplayName("assignInquiry")
    class AssignInquiry {

        @Test
        @DisplayName("should assign inquiry to employee successfully")
        void happyPath() {
            Long inquiryId = 1L;
            Long employeeId = 10L;

            Inquiry inquiry = new Inquiry();
            inquiry.setId(inquiryId);
            inquiry.setStatus(InquiryStatus.OPEN);

            Employee newAssignee = new Employee();
            newAssignee.setId(employeeId);
            newAssignee.setName("John Doe");

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(newAssignee));
            when(inquiryRepository.save(inquiry)).thenReturn(inquiry);

            InquiryResponse mockResponse = new InquiryResponse();
            when(inquiryMapper.toResponse(inquiry)).thenReturn(mockResponse);

            InquiryResponse result = inquiryService.assignInquiry(inquiryId, employeeId);

            assertThat(result).isNotNull();
            assertThat(inquiry.getAssignedEmployee()).isEqualTo(newAssignee);

            verify(inquiryRepository).save(inquiry);
            verify(auditService).log(eq(AuditCategory.INQUIRY), eq(inquiryId), eq("ASSIGN"),
                    eq(AuditLevel.HIGH), any(), any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when inquiry not found")
        void inquiryNotFound_throws() {
            when(inquiryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inquiryService.assignInquiry(99L, 10L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Inquiry");

            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when employee not found")
        void employeeNotFound_throws() {
            Inquiry inquiry = new Inquiry();
            inquiry.setId(1L);

            when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inquiryService.assignInquiry(1L, 99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Employee");

            verify(inquiryRepository, never()).save(any());
            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("should capture previous assignee in audit log")
        void previousAssignee_capturedInAudit() {
            Long inquiryId = 1L;
            Long newEmployeeId = 20L;

            Employee previousAssignee = new Employee();
            previousAssignee.setId(10L);
            previousAssignee.setName("Alice");

            Employee newAssignee = new Employee();
            newAssignee.setId(newEmployeeId);
            newAssignee.setName("Bob");

            Inquiry inquiry = new Inquiry();
            inquiry.setId(inquiryId);
            inquiry.setAssignedEmployee(previousAssignee);

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));
            when(employeeRepository.findById(newEmployeeId)).thenReturn(Optional.of(newAssignee));
            when(inquiryRepository.save(inquiry)).thenReturn(inquiry);
            when(inquiryMapper.toResponse(inquiry)).thenReturn(new InquiryResponse());

            inquiryService.assignInquiry(inquiryId, newEmployeeId);

            verify(auditService).log(eq(AuditCategory.INQUIRY), eq(inquiryId), eq("ASSIGN"),
                    eq(AuditLevel.HIGH), any(), argThat(details ->
                            details.contains("Bob") && details.contains("Alice")));
        }

        @Test
        @DisplayName("audit level should be HIGH for assignment")
        void auditLevelHigh() {
            Inquiry inquiry = new Inquiry();
            inquiry.setId(1L);

            Employee employee = new Employee();
            employee.setId(10L);
            employee.setName("Test Employee");

            when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));
            when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
            when(inquiryRepository.save(inquiry)).thenReturn(inquiry);
            when(inquiryMapper.toResponse(inquiry)).thenReturn(new InquiryResponse());

            inquiryService.assignInquiry(1L, 10L);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.HIGH), any(), any());
        }
    }

    @Nested
    @DisplayName("convertToReservation")
    class ConvertToReservation {

        @Test
        @DisplayName("should convert car inquiry to reservation successfully")
        void happyPath_car() {
            Long inquiryId = 1L;
            Double depositAmount = 5000.0;

            Client client = new Client();
            client.setId(10L);

            Car car = new Car();
            car.setId(100L);
            car.setStatus(CarStatus.AVAILABLE);

            Inquiry inquiry = new Inquiry();
            inquiry.setId(inquiryId);
            inquiry.setStatus(InquiryStatus.OPEN);
            inquiry.setVehicleType(VehicleType.CAR);
            inquiry.setCar(car);
            inquiry.setClient(client);

            ReservationResponse reservationResponse = new ReservationResponse();
            reservationResponse.setId(200L);

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));
            when(reservationService.createReservation(any())).thenReturn(reservationResponse);
            when(inquiryRepository.save(inquiry)).thenReturn(inquiry);

            Object result = inquiryService.convertToReservation(inquiryId, 100L, depositAmount);

            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(ReservationResponse.class);
            assertThat(((ReservationResponse) result).getId()).isEqualTo(200L);
            assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.IN_PROGRESS);

            verify(reservationService).createReservation(any());
            verify(inquiryRepository).save(inquiry);
            verify(auditService).log(eq(AuditCategory.INQUIRY), eq(inquiryId), 
                    eq("CONVERT_TO_RESERVATION"), eq(AuditLevel.HIGH), any(), any());
        }

        @Test
        @DisplayName("should convert motorcycle inquiry to reservation successfully")
        void happyPath_motorcycle() {
            Long inquiryId = 1L;
            Double depositAmount = 3000.0;

            Client client = new Client();
            client.setId(10L);

            Motorcycle motorcycle = new Motorcycle();
            motorcycle.setId(100L);
            motorcycle.setStatus(MotorcycleStatus.AVAILABLE);

            Inquiry inquiry = new Inquiry();
            inquiry.setId(inquiryId);
            inquiry.setStatus(InquiryStatus.IN_PROGRESS);
            inquiry.setVehicleType(VehicleType.MOTORCYCLE);
            inquiry.setMotorcycle(motorcycle);
            inquiry.setClient(client);

            ReservationResponse reservationResponse = new ReservationResponse();
            reservationResponse.setId(200L);

            when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));
            when(reservationService.createReservation(any())).thenReturn(reservationResponse);
            when(inquiryRepository.save(inquiry)).thenReturn(inquiry);

            Object result = inquiryService.convertToReservation(inquiryId, 100L, depositAmount);

            assertThat(result).isNotNull();
            assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.IN_PROGRESS);

            verify(reservationService).createReservation(any());
            verify(auditService).log(any(), any(), eq("CONVERT_TO_RESERVATION"), eq(AuditLevel.HIGH), any(), any());
        }

        @Test
        @DisplayName("should throw exception when inquiry not found")
        void inquiryNotFound_throws() {
            when(inquiryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inquiryService.convertToReservation(99L, 100L, 5000.0))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Inquiry");

            verifyNoInteractions(reservationService);
        }

        @Test
        @DisplayName("should throw exception when inquiry status is CLOSED")
        void closedInquiry_throws() {
            Inquiry inquiry = new Inquiry();
            inquiry.setId(1L);
            inquiry.setStatus(InquiryStatus.CLOSED);

            when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

            assertThatThrownBy(() -> inquiryService.convertToReservation(1L, 100L, 5000.0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("OPEN or IN_PROGRESS");

            verifyNoInteractions(reservationService);
        }

        @Test
        @DisplayName("should throw exception when deposit amount is zero")
        void zeroDeposit_throws() {
            Inquiry inquiry = new Inquiry();
            inquiry.setId(1L);
            inquiry.setStatus(InquiryStatus.OPEN);

            when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

            assertThatThrownBy(() -> inquiryService.convertToReservation(1L, 100L, 0.0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Valid deposit amount is required");

            verifyNoInteractions(reservationService);
        }

        @Test
        @DisplayName("should throw exception when deposit amount is negative")
        void negativeDeposit_throws() {
            Inquiry inquiry = new Inquiry();
            inquiry.setId(1L);
            inquiry.setStatus(InquiryStatus.OPEN);

            when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

            assertThatThrownBy(() -> inquiryService.convertToReservation(1L, 100L, -1000.0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("deposit amount");

            verifyNoInteractions(reservationService);
        }

        @Test
        @DisplayName("should throw exception when car is not AVAILABLE")
        void carNotAvailable_throws() {
            Client client = new Client();
            client.setId(10L);

            Car car = new Car();
            car.setId(100L);
            car.setStatus(CarStatus.RESERVED);

            Inquiry inquiry = new Inquiry();
            inquiry.setId(1L);
            inquiry.setStatus(InquiryStatus.OPEN);
            inquiry.setVehicleType(VehicleType.CAR);
            inquiry.setCar(car);
            inquiry.setClient(client);

            when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

            assertThatThrownBy(() -> inquiryService.convertToReservation(1L, 100L, 5000.0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("AVAILABLE");

            verifyNoInteractions(reservationService);
        }

        @Test
        @DisplayName("should throw exception when motorcycle is not AVAILABLE")
        void motorcycleNotAvailable_throws() {
            Client client = new Client();
            client.setId(10L);

            Motorcycle motorcycle = new Motorcycle();
            motorcycle.setId(100L);
            motorcycle.setStatus(MotorcycleStatus.SOLD);

            Inquiry inquiry = new Inquiry();
            inquiry.setId(1L);
            inquiry.setStatus(InquiryStatus.OPEN);
            inquiry.setVehicleType(VehicleType.MOTORCYCLE);
            inquiry.setMotorcycle(motorcycle);
            inquiry.setClient(client);

            when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

            assertThatThrownBy(() -> inquiryService.convertToReservation(1L, 100L, 3000.0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("AVAILABLE");

            verifyNoInteractions(reservationService);
        }

        @Test
        @DisplayName("should throw exception when inquiry has no vehicle")
        void noVehicle_throws() {
            Client client = new Client();
            client.setId(10L);

            Inquiry inquiry = new Inquiry();
            inquiry.setId(1L);
            inquiry.setStatus(InquiryStatus.OPEN);
            inquiry.setVehicleType(VehicleType.CAR);
            inquiry.setCar(null);
            inquiry.setClient(client);

            when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

            assertThatThrownBy(() -> inquiryService.convertToReservation(1L, 100L, 5000.0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("associated");

            verifyNoInteractions(reservationService);
        }

        @Test
        @DisplayName("audit level should be HIGH for conversion")
        void auditLevelHigh() {
            Client client = new Client();
            client.setId(10L);

            Car car = new Car();
            car.setId(100L);
            car.setStatus(CarStatus.AVAILABLE);

            Inquiry inquiry = new Inquiry();
            inquiry.setId(1L);
            inquiry.setStatus(InquiryStatus.OPEN);
            inquiry.setVehicleType(VehicleType.CAR);
            inquiry.setCar(car);
            inquiry.setClient(client);

            ReservationResponse reservationResponse = new ReservationResponse();
            reservationResponse.setId(200L);

            when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));
            when(reservationService.createReservation(any())).thenReturn(reservationResponse);
            when(inquiryRepository.save(inquiry)).thenReturn(inquiry);

            inquiryService.convertToReservation(1L, 100L, 5000.0);

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.HIGH), any(), any());
        }
    }
}
