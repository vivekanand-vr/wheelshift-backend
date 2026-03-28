package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.FinancialTransactionRequest;
import com.wheelshiftpro.dto.response.FinancialTransactionResponse;
import com.wheelshiftpro.entity.Car;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.FinancialTransaction;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.TransactionType;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.FinancialTransactionMapper;
import com.wheelshiftpro.repository.CarRepository;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.FinancialTransactionRepository;
import com.wheelshiftpro.repository.SaleRepository;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.AuditService;
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
@DisplayName("FinancialTransactionServiceImpl")
class FinancialTransactionServiceImplTest {

    @Mock
    private FinancialTransactionRepository financialTransactionRepository;

    @Mock
    private FinancialTransactionMapper financialTransactionMapper;

    @Mock
    private CarRepository carRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private FinancialTransactionServiceImpl financialTransactionService;

    private Car car;
    private FinancialTransaction transaction;
    private FinancialTransactionRequest request;

    @BeforeEach
    void setUp() {
        car = new Car();
        car.setId(1L);
        car.setVinNumber("TEST123VIN");

        transaction = new FinancialTransaction();
        transaction.setId(1L);
        transaction.setCar(car);
        transaction.setTransactionType(TransactionType.PURCHASE);
        transaction.setAmount(new BigDecimal("20000.00"));
        transaction.setTransactionDate(LocalDateTime.now());

        request = new FinancialTransactionRequest();
        request.setCarId(1L);
        request.setTransactionType(TransactionType.PURCHASE);
        request.setAmount(new BigDecimal("20000.00"));
        request.setTransactionDate(LocalDateTime.now());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("createTransaction")
    class CreateTransaction {

        @Test
        @DisplayName("should create transaction successfully")
        void happyPath() {
            setUpAuthenticatedEmployee(1L);
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(financialTransactionMapper.toEntity(request)).thenReturn(transaction);
            when(financialTransactionRepository.save(any(FinancialTransaction.class))).thenReturn(transaction);
            when(financialTransactionMapper.toResponse(transaction)).thenReturn(new FinancialTransactionResponse());

            FinancialTransactionResponse result = financialTransactionService.createTransaction(request);

            assertThat(result).isNotNull();
            verify(financialTransactionRepository).save(any(FinancialTransaction.class));
            verify(auditService).log(eq(AuditCategory.FINANCIAL_TRANSACTION), eq(1L), eq("CREATE"), eq(AuditLevel.CRITICAL),
                    any(Employee.class), anyString());
        }

        @Test
        @DisplayName("should throw exception when amount is zero")
        void amountZero_throws() {
            request.setAmount(BigDecimal.ZERO);

            assertThatThrownBy(() -> financialTransactionService.createTransaction(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Amount must be greater than 0");
        }

        @Test
        @DisplayName("should throw exception when amount is negative")
        void amountNegative_throws() {
            request.setAmount(new BigDecimal("-1000.00"));

            assertThatThrownBy(() -> financialTransactionService.createTransaction(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Amount must be greater than 0");
        }

        @Test
        @DisplayName("should throw exception when amount is null")
        void amountNull_throws() {
            request.setAmount(null);

            assertThatThrownBy(() -> financialTransactionService.createTransaction(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Amount must be greater than 0");
        }

        @Test
        @DisplayName("should throw exception when transaction date is in the future")
        void transactionDateInFuture_throws() {
            request.setTransactionDate(LocalDateTime.now().plusDays(1));

            assertThatThrownBy(() -> financialTransactionService.createTransaction(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Transaction date cannot be in the future");
        }

        @Test
        @DisplayName("should throw exception when car not found")
        void carNotFound_throws() {
            request.setAmount(new BigDecimal("1000.00"));
            request.setTransactionDate(LocalDateTime.now());
            when(carRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> financialTransactionService.createTransaction(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Car");
        }

        @Test
        @DisplayName("should wire car relationship after mapping")
        void carRelationshipWired() {
            setUpAuthenticatedEmployee(1L);
            FinancialTransaction unmappedTransaction = new FinancialTransaction();
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(financialTransactionMapper.toEntity(request)).thenReturn(unmappedTransaction);
            when(financialTransactionRepository.save(any(FinancialTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(financialTransactionMapper.toResponse(any())).thenReturn(new FinancialTransactionResponse());

            financialTransactionService.createTransaction(request);

            ArgumentCaptor<FinancialTransaction> captor = ArgumentCaptor.forClass(FinancialTransaction.class);
            verify(financialTransactionRepository).save(captor.capture());
            FinancialTransaction saved = captor.getValue();
            assertThat(saved.getCar()).isEqualTo(car);
        }

        @Test
        @DisplayName("should create transaction without car for overhead expenses")
        void overheadTransaction_noCar() {
            setUpAuthenticatedEmployee(1L);
            request.setCarId(null);
            FinancialTransaction overheadTransaction = new FinancialTransaction();
            overheadTransaction.setId(1L);
            overheadTransaction.setTransactionType(TransactionType.PURCHASE);
            overheadTransaction.setAmount(new BigDecimal("500.00"));
            overheadTransaction.setTransactionDate(LocalDateTime.now());

            when(financialTransactionMapper.toEntity(request)).thenReturn(overheadTransaction);
            when(financialTransactionRepository.save(any(FinancialTransaction.class))).thenReturn(overheadTransaction);
            when(financialTransactionMapper.toResponse(overheadTransaction)).thenReturn(new FinancialTransactionResponse());

            FinancialTransactionResponse result = financialTransactionService.createTransaction(request);

            assertThat(result).isNotNull();
            verify(carRepository, never()).findById(anyLong());
            verify(financialTransactionRepository).save(any(FinancialTransaction.class));
        }
    }

    @Nested
    @DisplayName("updateTransaction")
    class UpdateTransaction {

        @Test
        @DisplayName("should update transaction successfully")
        void happyPath() {
            setUpAuthenticatedEmployee(1L);
            transaction.setTransactionType(TransactionType.REPAIR);
            when(financialTransactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
            when(financialTransactionRepository.save(transaction)).thenReturn(transaction);
            when(financialTransactionMapper.toResponse(transaction)).thenReturn(new FinancialTransactionResponse());

            FinancialTransactionResponse result = financialTransactionService.updateTransaction(1L, request);

            assertThat(result).isNotNull();
            verify(financialTransactionMapper).updateEntityFromRequest(request, transaction);
            verify(financialTransactionRepository).save(transaction);
            verify(auditService).log(eq(AuditCategory.FINANCIAL_TRANSACTION), eq(1L), eq("UPDATE"), eq(AuditLevel.CRITICAL),
                    any(Employee.class), anyString());
        }

        @Test
        @DisplayName("should throw exception when updating SALE type transaction")
        void saleTransactionUpdate_throws() {
            transaction.setTransactionType(TransactionType.SALE);
            when(financialTransactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

            assertThatThrownBy(() -> financialTransactionService.updateTransaction(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("SUPER_ADMIN authorization");
        }

        @Test
        @DisplayName("should throw exception when transaction not found")
        void transactionNotFound_throws() {
            when(financialTransactionRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> financialTransactionService.updateTransaction(1L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("FinancialTransaction");
        }
    }

    @Nested
    @DisplayName("deleteTransaction")
    class DeleteTransaction {

        @Test
        @DisplayName("should delete transaction successfully")
        void happyPath() {
            setUpAuthenticatedEmployee(1L);
            when(financialTransactionRepository.existsById(1L)).thenReturn(true);

            financialTransactionService.deleteTransaction(1L);

            verify(financialTransactionRepository).deleteById(1L);
            verify(auditService).log(eq(AuditCategory.FINANCIAL_TRANSACTION), eq(1L), eq("DELETE"), eq(AuditLevel.CRITICAL),
                    any(Employee.class), anyString());
        }

        @Test
        @DisplayName("should throw exception when transaction not found")
        void transactionNotFound_throws() {
            when(financialTransactionRepository.existsById(1L)).thenReturn(false);

            assertThatThrownBy(() -> financialTransactionService.deleteTransaction(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("FinancialTransaction");
        }

        @Test
        @DisplayName("should log audit entry before deletion")
        void auditLoggedBeforeDeletion() {
            setUpAuthenticatedEmployee(1L);
            when(financialTransactionRepository.existsById(1L)).thenReturn(true);

            financialTransactionService.deleteTransaction(1L);

            verify(auditService).log(any(), anyLong(), anyString(), any(), any(), anyString());
            verify(financialTransactionRepository).deleteById(1L);
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
