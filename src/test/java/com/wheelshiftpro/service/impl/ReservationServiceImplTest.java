package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.ReservationRequest;
import com.wheelshiftpro.dto.request.SaleRequest;
import com.wheelshiftpro.dto.response.ReservationResponse;
import com.wheelshiftpro.dto.response.SaleResponse;
import com.wheelshiftpro.entity.Car;
import com.wheelshiftpro.entity.Client;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.Motorcycle;
import com.wheelshiftpro.entity.Reservation;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.CarStatus;
import com.wheelshiftpro.enums.MotorcycleStatus;
import com.wheelshiftpro.enums.ReservationStatus;
import com.wheelshiftpro.enums.VehicleType;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.ReservationMapper;
import com.wheelshiftpro.repository.CarRepository;
import com.wheelshiftpro.repository.ClientRepository;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.ReservationRepository;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.AuditService;
import com.wheelshiftpro.service.SaleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationServiceImpl")
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private AuditService auditService;

    @Mock
    private SaleService saleService;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Car car;
    private Client client;
    private Reservation reservation;
    private ReservationRequest request;

    @BeforeEach
    void setUp() {
        car = new Car();
        car.setId(1L);
        car.setStatus(CarStatus.AVAILABLE);
        car.setVinNumber("TEST123VIN");

        client = new Client();
        client.setId(1L);
        client.setName("John Doe");

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setCar(car);
        reservation.setClient(client);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setReservationDate(LocalDateTime.now());
        reservation.setExpiryDate(LocalDateTime.now().plusDays(7));

        request = new ReservationRequest();
        request.setCarId(1L);
        request.setClientId(1L);
        request.setExpiryDate(LocalDateTime.now().plusDays(7));
        request.setDepositAmount(new BigDecimal("1000.00"));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("createReservation")
    class CreateReservation {

        @Test
        @DisplayName("should create reservation successfully")
        void happyPath() {
            setUpAuthenticatedEmployee(1L);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(reservationRepository.existsByCarId(1L)).thenReturn(false);
            when(reservationMapper.toEntity(request)).thenReturn(reservation);
            when(carRepository.save(car)).thenReturn(car);
            when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
            when(reservationMapper.toResponse(reservation)).thenReturn(new ReservationResponse());

            ReservationResponse result = reservationService.createReservation(request);

            assertThat(result).isNotNull();
            verify(carRepository).save(car);
            assertThat(car.getStatus()).isEqualTo(CarStatus.RESERVED);
            verify(reservationRepository).save(any(Reservation.class));
            verify(auditService).log(eq(AuditCategory.RESERVATION), eq(1L), eq("CREATE"), eq(AuditLevel.REGULAR),
                    any(Employee.class), anyString());
        }

        @Test
        @DisplayName("should throw exception when expiry date is in the past")
        void expiryDateInPast_throws() {
            request.setExpiryDate(LocalDateTime.now().minusDays(1));

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Expiry date must be in the future");
        }

        @Test
        @DisplayName("should throw exception when deposit amount is negative")
        void depositAmountNegative_throws() {
            request.setDepositAmount(new BigDecimal("-100.00"));

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Deposit amount cannot be negative");
        }

        @Test
        @DisplayName("should throw exception when car not found")
        void carNotFound_throws() {
            when(carRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Car");
        }

        @Test
        @DisplayName("should throw exception when car is not available")
        void carNotAvailable_throws() {
            car.setStatus(CarStatus.SOLD);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("not available for reservation");
        }

        @Test
        @DisplayName("should throw exception when client not found")
        void clientNotFound_throws() {
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(clientRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client");
        }

        @Test
        @DisplayName("should throw exception when car already has active reservation")
        void duplicateReservation_throws() {
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(reservationRepository.existsByCarId(1L)).thenReturn(true);

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already has an active reservation");
        }

        @Test
        @DisplayName("should populate relationships after mapping")
        void relationshipsPopulated() {
            setUpAuthenticatedEmployee(1L);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(reservationRepository.existsByCarId(1L)).thenReturn(false);
            when(reservationMapper.toEntity(request)).thenReturn(new Reservation());
            when(carRepository.save(car)).thenReturn(car);
            when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(reservationMapper.toResponse(any())).thenReturn(new ReservationResponse());

            reservationService.createReservation(request);

            ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
            verify(reservationRepository).save(captor.capture());
            Reservation saved = captor.getValue();
            assertThat(saved.getCar()).isEqualTo(car);
            assertThat(saved.getClient()).isEqualTo(client);
            assertThat(saved.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        }
    }

    @Nested
    @DisplayName("updateReservation")
    class UpdateReservation {

        @Test
        @DisplayName("should update reservation successfully")
        void happyPath() {
            setUpAuthenticatedEmployee(1L);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.save(reservation)).thenReturn(reservation);
            when(reservationMapper.toResponse(reservation)).thenReturn(new ReservationResponse());

            ReservationResponse result = reservationService.updateReservation(1L, request);

            assertThat(result).isNotNull();
            verify(reservationMapper).updateEntityFromRequest(request, reservation);
            verify(reservationRepository).save(reservation);
            verify(auditService).log(eq(AuditCategory.RESERVATION), eq(1L), eq("UPDATE"), eq(AuditLevel.REGULAR),
                    any(Employee.class), anyString());
        }

        @Test
        @DisplayName("should throw exception when reservation not found")
        void reservationNotFound_throws() {
            when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.updateReservation(1L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Reservation");
        }
    }

    @Nested
    @DisplayName("deleteReservation")
    class DeleteReservation {

        @Test
        @DisplayName("should delete active reservation and revert car status")
        void activeReservation_revertsCarStatus() {
            setUpAuthenticatedEmployee(1L);
            reservation.setStatus(ReservationStatus.CONFIRMED);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

            reservationService.deleteReservation(1L);

            verify(carRepository).save(car);
            assertThat(car.getStatus()).isEqualTo(CarStatus.AVAILABLE);
            verify(reservationRepository).delete(reservation);
            verify(auditService).log(eq(AuditCategory.RESERVATION), eq(1L), eq("DELETE"), eq(AuditLevel.HIGH),
                    any(Employee.class), anyString());
        }

        @Test
        @DisplayName("should delete cancelled reservation without changing car status")
        void cancelledReservation_noCarStatusChange() {
            setUpAuthenticatedEmployee(1L);
            reservation.setStatus(ReservationStatus.CANCELLED);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

            reservationService.deleteReservation(1L);

            verify(carRepository, never()).save(car);
            verify(reservationRepository).delete(reservation);
        }

        @Test
        @DisplayName("should throw exception when reservation not found")
        void reservationNotFound_throws() {
            when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.deleteReservation(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Reservation");
        }
    }

    @Nested
    @DisplayName("cancelReservation")
    class CancelReservation {

        @Test
        @DisplayName("should cancel confirmed reservation successfully")
        void confirmedReservation_cancelled() {
            setUpAuthenticatedEmployee(1L);
            reservation.setStatus(ReservationStatus.CONFIRMED);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.save(reservation)).thenReturn(reservation);
            when(reservationMapper.toResponse(reservation)).thenReturn(new ReservationResponse());

            ReservationResponse result = reservationService.cancelReservation(1L);

            assertThat(result).isNotNull();
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
            verify(carRepository).save(car);
            assertThat(car.getStatus()).isEqualTo(CarStatus.AVAILABLE);
            verify(auditService).log(eq(AuditCategory.RESERVATION), eq(1L), eq("STATUS_CHANGE"), eq(AuditLevel.HIGH),
                    any(Employee.class), anyString());
        }

        @Test
        @DisplayName("should cancel pending reservation successfully")
        void pendingReservation_cancelled() {
            setUpAuthenticatedEmployee(1L);
            reservation.setStatus(ReservationStatus.PENDING);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.save(reservation)).thenReturn(reservation);
            when(reservationMapper.toResponse(reservation)).thenReturn(new ReservationResponse());

            reservationService.cancelReservation(1L);

            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
            verify(carRepository).save(car);
        }

        @Test
        @DisplayName("should throw exception when trying to cancel expired reservation")
        void expiredReservation_throws() {
            reservation.setStatus(ReservationStatus.EXPIRED);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

            assertThatThrownBy(() -> reservationService.cancelReservation(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("confirmed or pending");
        }

        @Test
        @DisplayName("should throw exception when trying to cancel already cancelled reservation")
        void alreadyCancelled_throws() {
            reservation.setStatus(ReservationStatus.CANCELLED);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

           assertThatThrownBy(() -> reservationService.cancelReservation(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("confirmed or pending");
        }

        @Test
        @DisplayName("should throw exception when reservation not found")
        void reservationNotFound_throws() {
            when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.cancelReservation(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Reservation");
        }
    }

    @Nested
    @DisplayName("updateReservationStatus")
    class UpdateReservationStatus {

        @Test
        @DisplayName("should update status and revert car status when cancelled")
        void statusChangeToCancel_revertsCarStatus() {
            setUpAuthenticatedEmployee(1L);
            reservation.setStatus(ReservationStatus.CONFIRMED);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.save(reservation)).thenReturn(reservation);
            when(reservationMapper.toResponse(reservation)).thenReturn(new ReservationResponse());

            reservationService.updateReservationStatus(1L, ReservationStatus.CANCELLED);

            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
            verify(carRepository).save(car);
            assertThat(car.getStatus()).isEqualTo(CarStatus.AVAILABLE);
            verify(auditService).log(eq(AuditCategory.RESERVATION), eq(1L), eq("STATUS_CHANGE"), eq(AuditLevel.HIGH),
                    any(Employee.class), anyString());
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(auditService).log(any(), anyLong(), anyString(), any(), any(), captor.capture());
            assertThat(captor.getValue()).contains("CONFIRMED", "CANCELLED");
        }

        @Test
        @DisplayName("should update status and revert car status when expired")
        void statusChangeToExpired_revertsCarStatus() {
            setUpAuthenticatedEmployee(1L);
            reservation.setStatus(ReservationStatus.CONFIRMED);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.save(reservation)).thenReturn(reservation);
            when(reservationMapper.toResponse(reservation)).thenReturn(new ReservationResponse());

            reservationService.updateReservationStatus(1L, ReservationStatus.EXPIRED);

            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
            verify(carRepository).save(car);
            assertThat(car.getStatus()).isEqualTo(CarStatus.AVAILABLE);
        }

        @Test
        @DisplayName("should not change car status when transitioning from cancelled")
        void fromCancelledStatus_noCarStatusChange() {
            setUpAuthenticatedEmployee(1L);
            reservation.setStatus(ReservationStatus.CANCELLED);
            car.setStatus(CarStatus.AVAILABLE);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.save(reservation)).thenReturn(reservation);
            when(reservationMapper.toResponse(reservation)).thenReturn(new ReservationResponse());

            reservationService.updateReservationStatus(1L, ReservationStatus.EXPIRED);

            verify(carRepository, never()).save(car);
        }

        @Test
        @DisplayName("should throw exception when reservation not found")
        void reservationNotFound_throws() {
            when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.updateReservationStatus(1L, ReservationStatus.CANCELLED))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Reservation");
        }
    }

    @Nested
    @DisplayName("updateDepositStatus")
    class UpdateDepositStatus {

        @Test
        @DisplayName("should update deposit status successfully")
        void happyPath() {
            setUpAuthenticatedEmployee(1L);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.save(reservation)).thenReturn(reservation);
            when(reservationMapper.toResponse(reservation)).thenReturn(new ReservationResponse());

            ReservationResponse result = reservationService.updateDepositStatus(1L, true);

            assertThat(result).isNotNull();
            assertThat(reservation.getDepositPaid()).isTrue();
            verify(reservationRepository).save(reservation);
            verify(auditService).log(eq(AuditCategory.RESERVATION), eq(1L), eq("UPDATE"), eq(AuditLevel.REGULAR),
                    any(Employee.class), contains("Deposit status"));
        }

        @Test
        @DisplayName("should throw exception when reservation not found")
        void reservationNotFound_throws() {
            when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.updateDepositStatus(1L, true))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Reservation");
        }
    }

    @Nested
    @DisplayName("expireReservations")
    class ExpireReservations {

        @Test
        @DisplayName("should expire past-due reservations and revert car status")
        void happyPath() {
            Car car2 = new Car();
            car2.setId(2L);
            car2.setStatus(CarStatus.RESERVED);

            Reservation reservation2 = new Reservation();
            reservation2.setId(2L);
            reservation2.setCar(car2);
            reservation2.setStatus(ReservationStatus.CONFIRMED);
            car2.setStatus(CarStatus.RESERVED);

            when(reservationRepository.findExpiredReservations(any(LocalDateTime.class)))
                    .thenReturn(List.of(reservation, reservation2));
            when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(carRepository.save(any(Car.class))).thenReturn(car); // Both cars get saved

            reservationService.expireReservations();

            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
            assertThat(reservation2.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
            // Only car2 should be saved because car1 is not RESERVED
            verify(carRepository).save(car2);
            verify(reservationRepository, times(2)).save(any(Reservation.class));
            verify(auditService, times(2)).log(eq(AuditCategory.RESERVATION), anyLong(), eq("STATUS_CHANGE"), 
                    eq(AuditLevel.HIGH), isNull(), contains("expired"));
        }

        @Test
        @DisplayName("should not change car status when car is not reserved")
        void carNotReserved_noStatusChange() {
            car.setStatus(CarStatus.AVAILABLE);
            when(reservationRepository.findExpiredReservations(any(LocalDateTime.class)))
                    .thenReturn(List.of(reservation));
            when(reservationRepository.save(reservation)).thenReturn(reservation);

            reservationService.expireReservations();

            verify(carRepository, never()).save(car);
        }
    }

    @Nested
    @DisplayName("convertToSale")
    class ConvertToSale {

        @Test
        @DisplayName("should convert car reservation to sale successfully")
        void happyPath_car() {
            // Arrange
            car.setSellingPrice(new BigDecimal("25000.00"));
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservation.setDepositPaid(true);
            reservation.setVehicleType(VehicleType.CAR);
            reservation.setCar(car);
            
            SaleResponse saleResponse = new SaleResponse();
            saleResponse.setId(100L);

            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(employeeRepository.existsById(1L)).thenReturn(true);
            when(saleService.createSale(any(SaleRequest.class))).thenReturn(saleResponse);
            when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

            setUpAuthenticatedEmployee(50L);

            // Act
            SaleResponse result = reservationService.convertToSale(1L, 1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(100L);
            
            verify(saleService).createSale(argThat(request -> 
                request.getCarId().equals(1L) &&
                request.getClientId().equals(1L) &&
                request.getEmployeeId().equals(1L) &&
                request.getSalePrice().equals(new BigDecimal("25000.00"))
            ));
            verify(reservationRepository).save(reservation);
            verify(auditService).log(eq(AuditCategory.RESERVATION), eq(1L), eq("CONVERT_TO_SALE"), 
                    eq(AuditLevel.CRITICAL), any(), anyString());
        }

        @Test
        @DisplayName("should convert motorcycle reservation to sale successfully")
        void happyPath_motorcycle() {
            // Arrange
            Motorcycle motorcycle = new Motorcycle();
            motorcycle.setId(2L);
            motorcycle.setSellingPrice(new BigDecimal("15000.00"));
            motorcycle.setStatus(MotorcycleStatus.AVAILABLE);
            
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservation.setDepositPaid(true);
            reservation.setVehicleType(VehicleType.MOTORCYCLE);
            reservation.setCar(null);
            reservation.setMotorcycle(motorcycle);
            
            SaleResponse saleResponse = new SaleResponse();
            saleResponse.setId(101L);

            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(employeeRepository.existsById(1L)).thenReturn(true);
            when(saleService.createSale(any(SaleRequest.class))).thenReturn(saleResponse);
            when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

            setUpAuthenticatedEmployee(50L);

            // Act
            SaleResponse result = reservationService.convertToSale(1L, 1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(101L);
            
            verify(saleService).createSale(argThat(request -> 
                request.getMotorcycleId().equals(2L) &&
                request.getClientId().equals(1L) &&
                request.getEmployeeId().equals(1L) &&
                request.getSalePrice().equals(new BigDecimal("15000.00"))
            ));
            verify(reservationRepository).save(reservation);
            verify(auditService).log(eq(AuditCategory.RESERVATION), eq(1L), eq("CONVERT_TO_SALE"), 
                    eq(AuditLevel.CRITICAL), any(), anyString());
        }

        @Test
        @DisplayName("should throw exception when no vehicle associated")
        void noVehicle_throws() {
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservation.setDepositPaid(true);
            reservation.setCar(null);
            reservation.setMotorcycle(null);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(employeeRepository.existsById(1L)).thenReturn(true);

            assertThatThrownBy(() -> reservationService.convertToSale(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("no valid vehicle");
        }

        @Test
        @DisplayName("should throw exception when reservation status is not confirmed")
        void invalidStatus_throws() {
            reservation.setStatus(ReservationStatus.PENDING);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

            assertThatThrownBy(() -> reservationService.convertToSale(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("confirmed reservations");
        }

        @Test
        @DisplayName("should throw exception when deposit not paid")
        void depositNotPaid_throws() {
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservation.setDepositPaid(false);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

            assertThatThrownBy(() -> reservationService.convertToSale(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Deposit must be paid");
        }

        @Test
        @DisplayName("should throw exception when employee not found")
        void employeeNotFound_throws() {
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservation.setDepositPaid(true);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(employeeRepository.existsById(1L)).thenReturn(false);

            assertThatThrownBy(() -> reservationService.convertToSale(1L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Employee");
        }

        @Test
        @DisplayName("should throw exception when reservation not found")
        void reservationNotFound_throws() {
            when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.convertToSale(1L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Reservation");
        }
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
}
