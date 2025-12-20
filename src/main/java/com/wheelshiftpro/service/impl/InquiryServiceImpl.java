package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.InquiryRequest;
import com.wheelshiftpro.dto.response.InquiryResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.entity.Inquiry;
import com.wheelshiftpro.enums.InquiryStatus;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.InquiryMapper;
import com.wheelshiftpro.repository.CarRepository;
import com.wheelshiftpro.repository.ClientRepository;
import com.wheelshiftpro.repository.InquiryRepository;
import com.wheelshiftpro.service.InquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
@Transactional
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryMapper inquiryMapper;
    private final ClientRepository clientRepository;
    private final CarRepository carRepository;

    @Override
    public InquiryResponse createInquiry(InquiryRequest request) {
        log.debug("Creating inquiry for client ID: {}", request.getClientId());

        if (!clientRepository.existsById(request.getClientId())) {
            throw new ResourceNotFoundException("Client", "id", request.getClientId());
        }

        if (request.getCarId() != null && !carRepository.existsById(request.getCarId())) {
            throw new ResourceNotFoundException("Car", "id", request.getCarId());
        }

        Inquiry inquiry = inquiryMapper.toEntity(request);
        inquiry.setStatus(InquiryStatus.OPEN);

        Inquiry saved = inquiryRepository.save(inquiry);

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

        if (!inquiryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Inquiry", "id", id);
        }

        inquiryRepository.deleteById(id);
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

        inquiry.setStatus(status);
        Inquiry updated = inquiryRepository.save(inquiry);

        log.info("Updated inquiry ID: {} to status: {}", id, status);
        return inquiryMapper.toResponse(updated);
    }

    @Override
    public Object convertToReservation(Long inquiryId, Long carId, Double depositAmount) {
        log.debug("Converting inquiry ID: {} to reservation", inquiryId);

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry", "id", inquiryId));

        if (inquiry.getStatus() == InquiryStatus.CLOSED) {
            throw new BusinessException("Inquiry has already been closed", "INQUIRY_ALREADY_CLOSED");
        }

        if (!carRepository.existsById(carId)) {
            throw new ResourceNotFoundException("Car", "id", carId);
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
