package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.InquiryRequest;
import com.wheelshiftpro.dto.response.InquiryResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.entity.Client;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.Inquiry;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.ClientStatus;
import com.wheelshiftpro.enums.InquiryStatus;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.InquiryMapper;
import com.wheelshiftpro.repository.CarRepository;
import com.wheelshiftpro.repository.ClientRepository;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.InquiryRepository;
import com.wheelshiftpro.repository.SaleRepository;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.AuditService;
import com.wheelshiftpro.service.InquiryService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryMapper inquiryMapper;
    private final ClientRepository clientRepository;
    private final CarRepository carRepository;
    private final EmployeeRepository employeeRepository;
    private final SaleRepository saleRepository;
    private final AuditService auditService;

    @Override
    public InquiryResponse createInquiry(InquiryRequest request) {
        log.debug("Creating inquiry for client ID: {}", request.getClientId());

        // Validate client exists and is active
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", request.getClientId()));
        
        if (client.getStatus() != ClientStatus.ACTIVE) {
            throw new BusinessException("Client must be active to create inquiry", "CLIENT_NOT_ACTIVE");
        }

        // Validate single-vehicle discriminator
        if (request.getCarId() != null && request.getMotorcycleId() != null) {
            throw new BusinessException("Inquiry can only reference one vehicle (car or motorcycle)", "MULTIPLE_VEHICLES");
        }

        if (request.getCarId() != null && !carRepository.existsById(request.getCarId())) {
            throw new ResourceNotFoundException("Car", "id", request.getCarId());
        }

        Inquiry inquiry = inquiryMapper.toEntity(request);
        inquiry.setClient(client);
        inquiry.setStatus(InquiryStatus.OPEN);

        Inquiry saved = inquiryRepository.save(inquiry);

        auditService.log(AuditCategory.INQUIRY, saved.getId(), "CREATE", AuditLevel.REGULAR,
                resolveCurrentEmployee(), "Client: " + client.getName());

        log.info("Created inquiry with ID: {}", saved.getId());
        return inquiryMapper.toResponse(saved);
    }

    @Override
    public InquiryResponse updateInquiry(Long id, InquiryRequest request) {
        log.debug("Updating inquiry ID: {}", id);

        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry", "id", id));

        inquiryMapper.updateEntityFromRequest(request, inquiry);
        Inquiry updated = inquiryRepository.save(inquiry);

        auditService.log(AuditCategory.INQUIRY, id, "UPDATE", AuditLevel.REGULAR,
                resolveCurrentEmployee(), "Inquiry updated");

        log.info("Updated inquiry ID: {}", id);
        return inquiryMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public InquiryResponse getInquiryById(Long id) {
        log.debug("Fetching inquiry ID: {}", id);

        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry", "id", id));

        return inquiryMapper.toResponse(inquiry);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InquiryResponse> getAllInquiries(int page, int size) {
        log.debug("Fetching all inquiries - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Inquiry> inquiriesPage = inquiryRepository.findAll(pageable);

        return buildPageResponse(inquiriesPage);
    }

    @Override
    public void deleteInquiry(Long id) {
        log.debug("Deleting inquiry ID: {}", id);

        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry", "id", id));

        // Block deletion of CLOSED inquiries that led to a sale
        if (inquiry.getStatus() == InquiryStatus.CLOSED) {
            if (inquiry.getCar() != null && saleRepository.existsByCarId(inquiry.getCar().getId())) {
                throw new BusinessException("Cannot delete closed inquiry that led to a sale", "INQUIRY_HAS_SALE");
            }
        }

        auditService.log(AuditCategory.INQUIRY, id, "DELETE", AuditLevel.HIGH,
                resolveCurrentEmployee(), "Inquiry deleted");

        inquiryRepository.delete(inquiry);
        log.info("Deleted inquiry ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InquiryResponse> searchInquiries(Long clientId, InquiryStatus status,
                                                         LocalDate startDate, LocalDate endDate,
                                                         int page, int size) {
        log.debug("Searching inquiries with filters - clientId: {}, status: {}, startDate: {}, endDate: {}",
                clientId, status, startDate, endDate);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Build specification for filtering
        Specification<Inquiry> spec = (root, query, cb) -> cb.conjunction();

        if (clientId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("client").get("id"), clientId));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (startDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
        }

        if (endDate != null) {
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("createdAt"), endDateTime));
        }

        Page<Inquiry> inquiriesPage = inquiryRepository.findAll(spec, pageable);

        return buildPageResponse(inquiriesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InquiryResponse> getInquiriesByStatus(InquiryStatus status, int page, int size) {
        log.debug("Fetching inquiries by status: {}", status);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Inquiry> inquiriesPage = inquiryRepository.findByStatus(status, pageable);

        return buildPageResponse(inquiriesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InquiryResponse> getInquiriesByClient(Long clientId, int page, int size) {
        log.debug("Fetching inquiries for client ID: {}", clientId);

        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", "id", clientId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Inquiry> inquiriesPage = inquiryRepository.findByClientId(clientId, pageable);

        return buildPageResponse(inquiriesPage);
    }

    @Override
    public InquiryResponse updateInquiryStatus(Long id, InquiryStatus status) {
        log.debug("Updating inquiry ID: {} to status: {}", id, status);

        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry", "id", id));

        // Validate status transitions
        InquiryStatus currentStatus = inquiry.getStatus();
        validateInquiryStatusTransition(currentStatus, status);

        // If transitioning to RESPONDED, response text must be present
        if (status == InquiryStatus.RESPONDED && (inquiry.getResponse() == null || inquiry.getResponse().trim().isEmpty())) {
            throw new BusinessException("Response text is required when status is RESPONDED", "RESPONSE_REQUIRED");
        }

        InquiryStatus previousStatus = inquiry.getStatus();
        inquiry.setStatus(status);
        Inquiry updated = inquiryRepository.save(inquiry);

        auditService.log(AuditCategory.INQUIRY, id, "STATUS_CHANGE", AuditLevel.HIGH,
                resolveCurrentEmployee(), "From " + previousStatus + " to " + status);

        log.info("Updated inquiry ID: {} to status: {}", id, status);
        return inquiryMapper.toResponse(updated);
    }

    @Override
    public Object convertToReservation(Long inquiryId, Long carId, Double depositAmount) {
        log.debug("Converting inquiry ID: {} to reservation", inquiryId);

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry", "id", inquiryId));

        // Validate inquiry status
        if (inquiry.getStatus() != InquiryStatus.OPEN && inquiry.getStatus() != InquiryStatus.IN_PROGRESS) {
            throw new BusinessException("Only OPEN or IN_PROGRESS inquiries can be converted to reservation", "INVALID_INQUIRY_STATUS");
        }

        // Validate vehicle exists
        if (!carRepository.existsById(carId)) {
            throw new ResourceNotFoundException("Car", "id", carId);
        }

        // Deposit amount is required
        if (depositAmount == null || depositAmount <= 0) {
            throw new BusinessException("Valid deposit amount is required", "INVALID_DEPOSIT_AMOUNT");
        }

        // This will be implemented when ReservationService integration is complete
        throw new BusinessException("Conversion to reservation not yet implemented", "NOT_IMPLEMENTED");
    }

    @Override
    @Transactional(readOnly = true)
    public Object getInquiryStatistics() {
        log.debug("Fetching inquiry statistics");

        Map<String, Object> statistics = new HashMap<>();

        // Get counts by status
        Map<String, Long> statusCounts = new HashMap<>();
        for (InquiryStatus status : InquiryStatus.values()) {
            Specification<Inquiry> spec = (root, query, cb) -> cb.equal(root.get("status"), status);
            long count = inquiryRepository.count(spec);
            statusCounts.put(status.name(), count);
        }

        statistics.put("statusCounts", statusCounts);
        statistics.put("totalInquiries", inquiryRepository.count());

        log.info("Retrieved inquiry statistics");
        return statistics;
    }

    private void validateInquiryStatusTransition(InquiryStatus from, InquiryStatus to) {
        // Allowed transitions: OPEN → IN_PROGRESS → RESPONDED → CLOSED, OPEN → CLOSED
        boolean valid = switch (from) {
            case OPEN -> to == InquiryStatus.IN_PROGRESS || to == InquiryStatus.CLOSED;
            case IN_PROGRESS -> to == InquiryStatus.RESPONDED || to == InquiryStatus.CLOSED;
            case RESPONDED -> to == InquiryStatus.CLOSED;
            case CLOSED -> false; // Terminal state
        };

        if (!valid) {
            throw new BusinessException("Invalid status transition from " + from + " to " + to, "INVALID_STATUS_TRANSITION");
        }
    }

    private Employee resolveCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof EmployeeUserDetails u) {
            return employeeRepository.getReferenceById(u.getId());
        }
        return null;
    }

    private PageResponse<InquiryResponse> buildPageResponse(Page<Inquiry> page) {
        List<InquiryResponse> content = inquiryMapper.toResponseList(page.getContent());

        return PageResponse.<InquiryResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}
