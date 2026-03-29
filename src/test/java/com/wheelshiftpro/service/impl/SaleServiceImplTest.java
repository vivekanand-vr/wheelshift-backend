package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.SaleRequest;
import com.wheelshiftpro.dto.response.SaleResponse;
import com.wheelshiftpro.entity.*;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.CarStatus;
import com.wheelshiftpro.enums.ReservationStatus;
import com.wheelshiftpro.enums.TransactionType;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.SaleMapper;
import com.wheelshiftpro.repository.*;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.AuditService;
import com.wheelshiftpro.service.ClientService;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleServiceImpl")
class SaleServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private StorageLocationRepository storageLocationRepository;

    @Mock
    private FinancialTransactionRepository financialTransactionRepository;

    @Mock
    private SaleMapper saleMapper;

    @Mock
    private ClientService clientService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private SaleServiceImpl saleService;

    private Car car;
    private Client client;
    private Employee employee;
    private Sale sale;
    private SaleRequest request;
    private StorageLocation location;

    @BeforeEach
    void setUp() {
        location = new StorageLocation();
        location.setId(1L);
        location.setCurrentCarCount(5);

        car = new Car();
        car.setId(1L);
        car.setStatus(CarStatus.AVAILABLE);
        car.setVinNumber("TEST123VIN");
        car.setStorageLocation(location);

        client = new Client();
        client.setId(1L);
        client.setName("John Doe");
        client.setTotalPurchases(0);

        employee = new Employee();
        employee.setId(1L);
        employee.setName("Jane Smith");

        sale = new Sale();
        sale.setId(1L);
        sale.setCar(car);
        sale.setClient(client);
        sale.setEmployee(employee);
        sale.setSalePrice(new BigDecimal("25000.00"));
        sale.setCommissionRate(new BigDecimal("5.0"));
        sale.setSaleDate(LocalDateTime.now());

        request = new SaleRequest();
        request.setCarId(1L);
        request.setClientId(1L);
        request.setEmployeeId(1L);
        request.setSalePrice(new BigDecimal("25000.00"));
        request.setCommissionRate(new BigDecimal("5.0"));
        request.setSaleDate(LocalDateTime.now());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("createSale")
    class CreateSale {

        @Test
        @DisplayName("should create sale successfully with all side effects")
        void happyPath() {
            setUpAuthenticatedEmployee(1L);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(saleMapper.toEntity(request)).thenReturn(sale);
            when(carRepository.save(car)).thenReturn(car);
            when(storageLocationRepository.save(location)).thenReturn(location);
            when(reservationRepository.findByCarId(1L)).thenReturn(Optional.empty());
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);
            when(financialTransactionRepository.save(any(FinancialTransaction.class))).thenReturn(new FinancialTransaction());
            when(saleMapper.toResponse(sale)).thenReturn(new SaleResponse());

            SaleResponse result = saleService.createSale(request);

            assertThat(result).isNotNull();
            verify(carRepository).save(car);
            assertThat(car.getStatus()).isEqualTo(CarStatus.SOLD);
            verify(storageLocationRepository).save(location);
            assertThat(location.getCurrentCarCount()).isEqualTo(4);
            verify(clientService).incrementPurchaseCount(1L);
            verify(financialTransactionRepository).save(any(FinancialTransaction.class));
            verify(auditService).log(eq(AuditCategory.SALE), eq(1L), eq("CREATE"), eq(AuditLevel.CRITICAL),
                    any(Employee.class), anyString());
        }

        @Test
        @DisplayName("should throw exception when sale price is zero")
        void salePriceZero_throws() {
            request.setSalePrice(BigDecimal.ZERO);

            assertThatThrownBy(() -> saleService.createSale(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Sale price must be greater than 0");
        }

        @Test
        @DisplayName("should throw exception when sale price is negative")
        void salePriceNegative_throws() {
            request.setSalePrice(new BigDecimal("-1000.00"));

            assertThatThrownBy(() -> saleService.createSale(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Sale price must be greater than 0");
        }

        @Test
        @DisplayName("should throw exception when sale price is null")
        void salePriceNull_throws() {
            request.setSalePrice(null);

            assertThatThrownBy(() -> saleService.createSale(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Sale price must be greater than 0");
        }

        @Test
        @DisplayName("should throw exception when sale date is in the future")
        void saleDateInFuture_throws() {
            request.setSaleDate(LocalDateTime.now().plusDays(1));

            assertThatThrownBy(() -> saleService.createSale(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Sale date cannot be in the future");
        }

        @Test
        @DisplayName("should throw exception when car not found")
        void carNotFound_throws() {
            when(carRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> saleService.createSale(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Car");
        }

        @Test
        @DisplayName("should throw exception when car is already sold")
        void carAlreadySold_throws() {
            car.setStatus(CarStatus.SOLD);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));

            assertThatThrownBy(() -> saleService.createSale(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already sold");
        }

        @Test
        @DisplayName("should throw exception when client not found")
        void clientNotFound_throws() {
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(clientRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> saleService.createSale(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client");
        }

        @Test
        @DisplayName("should throw exception when employee not found")
        void employeeNotFound_throws() {
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> saleService.createSale(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Employee");
        }

        @Test
        @DisplayName("should calculate commission correctly")
        void commissionCalculated() {
            setUpAuthenticatedEmployee(1L);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(saleMapper.toEntity(request)).thenReturn(sale);
            when(carRepository.save(car)).thenReturn(car);
            when(storageLocationRepository.save(location)).thenReturn(location);
            when(reservationRepository.findByCarId(1L)).thenReturn(Optional.empty());
            when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(financialTransactionRepository.save(any(FinancialTransaction.class))).thenReturn(new FinancialTransaction());
            when(saleMapper.toResponse(any())).thenReturn(new SaleResponse());

            saleService.createSale(request);

            ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
            verify(saleRepository).save(captor.capture());
            Sale saved = captor.getValue();
            assertThat(saved.getTotalCommission()).isNotNull();
            assertThat(saved.getTotalCommission()).isEqualByComparingTo(new BigDecimal("1250.00"));
        }

        @Test
        @DisplayName("should update active reservation status to confirmed")
        void activeReservationUpdated() {
            setUpAuthenticatedEmployee(1L);
            Reservation reservation = new Reservation();
            reservation.setId(1L);
            reservation.setStatus(ReservationStatus.PENDING);

            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(saleMapper.toEntity(request)).thenReturn(sale);
            when(carRepository.save(car)).thenReturn(car);
            when(storageLocationRepository.save(location)).thenReturn(location);
            when(reservationRepository.findByCarId(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.save(reservation)).thenReturn(reservation);
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);
            when(financialTransactionRepository.save(any(FinancialTransaction.class))).thenReturn(new FinancialTransaction());
            when(saleMapper.toResponse(sale)).thenReturn(new SaleResponse());

            saleService.createSale(request);

            verify(reservationRepository).save(reservation);
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        }

        @Test
        @DisplayName("should create financial transaction with correct details")
        void financialTransactionCreated() {
            setUpAuthenticatedEmployee(1L);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(saleMapper.toEntity(request)).thenReturn(sale);
            when(carRepository.save(car)).thenReturn(car);
            when(storageLocationRepository.save(location)).thenReturn(location);
            when(reservationRepository.findByCarId(1L)).thenReturn(Optional.empty());
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);
            when(financialTransactionRepository.save(any(FinancialTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(saleMapper.toResponse(sale)).thenReturn(new SaleResponse());

            saleService.createSale(request);

            ArgumentCaptor<FinancialTransaction> captor = ArgumentCaptor.forClass(FinancialTransaction.class);
            verify(financialTransactionRepository).save(captor.capture());
            FinancialTransaction transaction = captor.getValue();
            assertThat(transaction.getCar()).isEqualTo(car);
            assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.SALE);
            assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("25000.00"));
        }

        @Test
        @DisplayName("should handle sale with no storage location")
        void noStorageLocation_noError() {
            setUpAuthenticatedEmployee(1L);
            car.setStorageLocation(null);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(saleMapper.toEntity(request)).thenReturn(sale);
            when(carRepository.save(car)).thenReturn(car);
            when(reservationRepository.findByCarId(1L)).thenReturn(Optional.empty());
            when(saleRepository.save(any(Sale.class))).thenReturn(sale);
            when(financialTransactionRepository.save(any(FinancialTransaction.class))).thenReturn(new FinancialTransaction());
            when(saleMapper.toResponse(sale)).thenReturn(new SaleResponse());

            saleService.createSale(request);

            verify(storageLocationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateSale")
    class UpdateSale {

        @Test
        @DisplayName("should update sale successfully")
        void happyPath() {
            setUpAuthenticatedEmployee(1L);
            request.setCarId(1L); // Same car ID
            when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
            when(saleRepository.save(sale)).thenReturn(sale);
            when(saleMapper.toResponse(sale)).thenReturn(new SaleResponse());

            SaleResponse result = saleService.updateSale(1L, request);

            assertThat(result).isNotNull();
            verify(saleMapper).updateEntityFromRequest(request, sale);
            verify(saleRepository).save(sale);
            verify(auditService).log(eq(AuditCategory.SALE), eq(1L), eq("UPDATE"), eq(AuditLevel.HIGH),
                    any(Employee.class), anyString());
        }

        @Test
        @DisplayName("should recalculate commission when price changes")
        void priceChanged_commissionRecalculated() {
            setUpAuthenticatedEmployee(1L);
            request.setCarId(1L);
            request.setSalePrice(new BigDecimal("30000.00"));
            when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
            when(saleRepository.save(sale)).thenAnswer(invocation -> invocation.getArgument(0));
            when(saleMapper.toResponse(any())).thenReturn(new SaleResponse());

            saleService.updateSale(1L, request);

            verify(saleRepository).save(sale);
        }

        @Test
        @DisplayName("should throw exception when trying to change car")
        void changingCar_throws() {
            request.setCarId(2L);
            when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

            assertThatThrownBy(() -> saleService.updateSale(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot change car after sale is recorded");
        }

        @Test
        @DisplayName("should throw exception when sale not found")
        void saleNotFound_throws() {
            when(saleRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> saleService.updateSale(1L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Sale");
        }
    }

    @Nested
    @DisplayName("deleteSale")
    class DeleteSale {

        @Test
        @DisplayName("should delete sale and revert car status")
        void happyPath() {
            setUpAuthenticatedEmployee(1L);
            client.setTotalPurchases(1);
            when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
            when(financialTransactionRepository.existsBySaleCarId(1L)).thenReturn(false);
            when(carRepository.save(car)).thenReturn(car);
            when(clientRepository.save(client)).thenReturn(client);

            saleService.deleteSale(1L);

            verify(carRepository).save(car);
            assertThat(car.getStatus()).isEqualTo(CarStatus.AVAILABLE);
            verify(clientRepository).save(client);
            assertThat(client.getTotalPurchases()).isEqualTo(0);
            verify(saleRepository).delete(sale);
            verify(auditService).log(eq(AuditCategory.SALE), eq(1L), eq("DELETE"), eq(AuditLevel.CRITICAL),
                    any(Employee.class), anyString());
        }

        @Test
        @DisplayName("should throw exception when financial transactions exist")
        void hasFinancialTransactions_throws() {
            when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
            when(financialTransactionRepository.existsBySaleCarId(1L)).thenReturn(true);

            assertThatThrownBy(() -> saleService.deleteSale(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("financial transactions");
        }

        @Test
        @DisplayName("should throw exception when sale not found")
        void saleNotFound_throws() {
            when(saleRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> saleService.deleteSale(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Sale");
        }

        @Test
        @DisplayName("should handle client with zero purchases gracefully")
        void zeroPurchases_noDecrement() {
            setUpAuthenticatedEmployee(1L);
            client.setTotalPurchases(0);
            when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
            when(financialTransactionRepository.existsBySaleCarId(1L)).thenReturn(false);
            when(carRepository.save(car)).thenReturn(car);

            saleService.deleteSale(1L);

            verify(clientRepository, never()).save(client);
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
