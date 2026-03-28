package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.FinancialTransactionRequest;
import com.wheelshiftpro.dto.response.FinancialTransactionResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.entity.Car;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.FinancialTransaction;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.PaymentMethod;
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
import com.wheelshiftpro.service.FinancialTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class FinancialTransactionServiceImpl implements FinancialTransactionService {

    private final FinancialTransactionRepository financialTransactionRepository;
    private final FinancialTransactionMapper financialTransactionMapper;
    private final CarRepository carRepository;
    private final EmployeeRepository employeeRepository;
    private final SaleRepository saleRepository;
    private final AuditService auditService;

    @Override
    public FinancialTransactionResponse createTransaction(FinancialTransactionRequest request) {
        log.debug("Creating financial transaction for car ID: {}", request.getCarId());

        // Validate amount
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than 0", "INVALID_AMOUNT");
        }

        // Validate transaction date
        if (request.getTransactionDate() != null && request.getTransactionDate().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Transaction date cannot be in the future", "INVALID_TRANSACTION_DATE");
        }

        Car car = null;
        if (request.getCarId() != null) {
            car = carRepository.findById(request.getCarId())
                    .orElseThrow(() -> new ResourceNotFoundException("Car", "id", request.getCarId()));
        }

        FinancialTransaction transaction = financialTransactionMapper.toEntity(request);
        if (car != null) {
            transaction.setCar(car);
        }

        FinancialTransaction saved = financialTransactionRepository.save(transaction);

        auditService.log(AuditCategory.FINANCIAL_TRANSACTION, saved.getId(), "CREATE", AuditLevel.CRITICAL,
                resolveCurrentEmployee(), "Type: " + saved.getTransactionType() + ", Amount: " + saved.getAmount());

        log.info("Created transaction with ID: {}", saved.getId());
        return financialTransactionMapper.toResponse(saved);
    }

    @Override
    public FinancialTransactionResponse updateTransaction(Long id, FinancialTransactionRequest request) {
        log.debug("Updating transaction ID: {}", id);

        FinancialTransaction transaction = financialTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialTransaction", "id", id));

        // Block auto-created SALE transactions from being freely edited
        if (transaction.getTransactionType() == TransactionType.SALE) {
            throw new BusinessException("Auto-created SALE transactions require SUPER_ADMIN authorization", "IMMUTABLE_TRANSACTION");
        }

        financialTransactionMapper.updateEntityFromRequest(request, transaction);
        FinancialTransaction updated = financialTransactionRepository.save(transaction);

        auditService.log(AuditCategory.FINANCIAL_TRANSACTION, id, "UPDATE", AuditLevel.CRITICAL,
                resolveCurrentEmployee(), "Transaction updated");

        log.info("Updated transaction ID: {}", id);
        return financialTransactionMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialTransactionResponse getTransactionById(Long id) {
        log.debug("Fetching transaction ID: {}", id);

        FinancialTransaction transaction = financialTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialTransaction", "id", id));

        return financialTransactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FinancialTransactionResponse> getAllTransactions(int page, int size) {
        log.debug("Fetching all transactions - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<FinancialTransaction> transactionsPage = financialTransactionRepository.findAll(pageable);

        return buildPageResponse(transactionsPage);
    }

    @Override
    public void deleteTransaction(Long id) {
        log.debug("Deleting transaction ID: {}", id);

        if (!financialTransactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("FinancialTransaction", "id", id);
        }

        auditService.log(AuditCategory.FINANCIAL_TRANSACTION, id, "DELETE", AuditLevel.CRITICAL,
                resolveCurrentEmployee(), "Transaction deleted");

        financialTransactionRepository.deleteById(id);
        log.info("Deleted transaction ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FinancialTransactionResponse> searchTransactions(Long saleId, TransactionType transactionType,
                                                                          PaymentMethod paymentMethod, LocalDate startDate,
                                                                          LocalDate endDate, int page, int size) {
        log.debug("Searching transactions with filters - saleId: {}, type: {}, method: {}, startDate: {}, endDate: {}",
                saleId, transactionType, paymentMethod, startDate, endDate);

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());

        // Build specification for filtering
        Specification<FinancialTransaction> spec = (root, query, cb) -> cb.conjunction();

        if (saleId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sale").get("id"), saleId));
        }

        if (transactionType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("transactionType"), transactionType));
        }

        if (paymentMethod != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("paymentMethod"), paymentMethod));
        }

        if (startDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("transactionDate"), startDateTime));
        }

        if (endDate != null) {
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("transactionDate"), endDateTime));
        }

        Page<FinancialTransaction> transactionsPage = financialTransactionRepository.findAll(spec, pageable);

        return buildPageResponse(transactionsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FinancialTransactionResponse> getTransactionsBySaleId(Long saleId, int page, int size) {
        log.debug("Fetching transactions for sale ID: {}", saleId);

        if (!saleRepository.existsById(saleId)) {
            throw new ResourceNotFoundException("Sale", "id", saleId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Specification<FinancialTransaction> spec = (root, query, cb) -> cb.equal(root.get("sale").get("id"), saleId);
        Page<FinancialTransaction> transactionsPage = financialTransactionRepository.findAll(spec, pageable);

        return buildPageResponse(transactionsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FinancialTransactionResponse> getTransactionsByType(TransactionType transactionType, int page, int size) {
        log.debug("Fetching transactions by type: {}", transactionType);

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<FinancialTransaction> transactionsPage = financialTransactionRepository.findByTransactionType(
                transactionType, pageable);

        return buildPageResponse(transactionsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FinancialTransactionResponse> getTransactionsByPaymentMethod(PaymentMethod paymentMethod, int page, int size) {
        log.debug("Fetching transactions by payment method: {}", paymentMethod);

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Specification<FinancialTransaction> spec = (root, query, cb) -> cb.equal(root.get("paymentMethod"), paymentMethod);
        Page<FinancialTransaction> transactionsPage = financialTransactionRepository.findAll(spec, pageable);

        return buildPageResponse(transactionsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FinancialTransactionResponse> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate,
                                                                                  int page, int size) {
        log.debug("Fetching transactions between {} and {}", startDate, endDate);

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        Page<FinancialTransaction> transactionsPage = financialTransactionRepository.findByTransactionDateBetween(
                startDateTime, endDateTime, pageable);

        return buildPageResponse(transactionsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateTotalByType(TransactionType transactionType, LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating total for type: {} between {} and {}", transactionType, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        Specification<FinancialTransaction> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("transactionType"), transactionType),
                cb.between(root.get("transactionDate"), startDateTime, endDateTime)
        );
        List<FinancialTransaction> transactions = financialTransactionRepository.findAll(spec);

        double total = transactions.stream()
                .map(FinancialTransaction::getAmount)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        log.info("Total amount for type {}: {}", transactionType, total);
        return total;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getFinancialSummary(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching financial summary between {} and {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        Map<String, Object> summary = new HashMap<>();

        // Get totals by transaction type
        Map<String, Double> totalsByType = new HashMap<>();
        for (TransactionType type : TransactionType.values()) {
            List<FinancialTransaction> transactions = financialTransactionRepository.findAll(
                    (root, query, cb) -> cb.and(
                            cb.equal(root.get("transactionType"), type),
                            cb.between(root.get("transactionDate"), startDateTime, endDateTime)
                    )
            );

            double total = transactions.stream()
                    .map(FinancialTransaction::getAmount)
                    .mapToDouble(BigDecimal::doubleValue)
                    .sum();

            totalsByType.put(type.name(), total);
        }

        // Get totals by payment method
        Map<String, Double> totalsByPaymentMethod = new HashMap<>();
        for (PaymentMethod method : PaymentMethod.values()) {
            List<FinancialTransaction> transactions = financialTransactionRepository.findAll(
                    (root, query, cb) -> cb.and(
                            cb.equal(root.get("paymentMethod"), method),
                            cb.between(root.get("transactionDate"), startDateTime, endDateTime)
                    )
            );

            double total = transactions.stream()
                    .map(FinancialTransaction::getAmount)
                    .mapToDouble(BigDecimal::doubleValue)
                    .sum();

            totalsByPaymentMethod.put(method.name(), total);
        }

        summary.put("totalsByType", totalsByType);
        summary.put("totalsByPaymentMethod", totalsByPaymentMethod);
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);

        log.info("Retrieved financial summary");
        return summary;
    }

    private PageResponse<FinancialTransactionResponse> buildPageResponse(Page<FinancialTransaction> page) {
        List<FinancialTransactionResponse> content = financialTransactionMapper.toResponseList(page.getContent());

        return PageResponse.<FinancialTransactionResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }

    private Employee resolveCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof EmployeeUserDetails u) {
            return employeeRepository.getReferenceById(u.getId());
        }
        return null;
    }
}
