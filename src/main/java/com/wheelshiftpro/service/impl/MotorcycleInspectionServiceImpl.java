package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.MotorcycleInspectionRequest;
import com.wheelshiftpro.dto.response.MotorcycleInspectionResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.entity.Employee;
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
import com.wheelshiftpro.service.MotorcycleInspectionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class MotorcycleInspectionServiceImpl implements MotorcycleInspectionService {

    private final MotorcycleInspectionRepository motorcycleInspectionRepository;
    private final MotorcycleInspectionMapper motorcycleInspectionMapper;
    private final MotorcycleRepository motorcycleRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    @Override
    public MotorcycleInspectionResponse createInspection(MotorcycleInspectionRequest request) {
        log.debug("Creating inspection for motorcycle ID: {}", request.getMotorcycleId());

        if (!motorcycleRepository.existsById(request.getMotorcycleId())) {
            throw new ResourceNotFoundException("Motorcycle", "id", request.getMotorcycleId());
        }

        if (request.getInspectorId() != null && !employeeRepository.existsById(request.getInspectorId())) {
            throw new ResourceNotFoundException("Employee", "id", request.getInspectorId());
        }

        if (request.getInspectionDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Inspection date cannot be in the future", "FUTURE_INSPECTION_DATE");
        }

        MotorcycleInspection inspection = motorcycleInspectionMapper.toEntity(request);
        inspection.setMotorcycle(motorcycleRepository.getReferenceById(request.getMotorcycleId()));
        if (request.getInspectorId() != null) {
            inspection.setInspector(employeeRepository.getReferenceById(request.getInspectorId()));
        }
        MotorcycleInspection saved = motorcycleInspectionRepository.save(inspection);

        auditService.log(AuditCategory.INSPECTION, saved.getId(), "CREATE",
                AuditLevel.REGULAR, resolveCurrentEmployee(),
                "Motorcycle ID: " + request.getMotorcycleId() + ", Date: " + saved.getInspectionDate());
        log.info("Created motorcycle inspection with ID: {}", saved.getId());
        return motorcycleInspectionMapper.toResponse(saved);
    }

    @Override
    public MotorcycleInspectionResponse updateInspection(Long id, MotorcycleInspectionRequest request) {
        log.debug("Updating motorcycle inspection ID: {}", id);

        MotorcycleInspection inspection = motorcycleInspectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MotorcycleInspection", "id", id));

        if (request.getInspectionDate() != null && request.getInspectionDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Inspection date cannot be in the future", "FUTURE_INSPECTION_DATE");
        }

        if (request.getInspectorId() != null && !employeeRepository.existsById(request.getInspectorId())) {
            throw new ResourceNotFoundException("Employee", "id", request.getInspectorId());
        }

        motorcycleInspectionMapper.updateEntityFromRequest(request, inspection);
        if (request.getInspectorId() != null) {
            inspection.setInspector(employeeRepository.getReferenceById(request.getInspectorId()));
        }
        MotorcycleInspection updated = motorcycleInspectionRepository.save(inspection);

        auditService.log(AuditCategory.INSPECTION, id, "UPDATE",
                AuditLevel.REGULAR, resolveCurrentEmployee(), "Motorcycle inspection updated");
        log.info("Updated motorcycle inspection ID: {}", id);
        return motorcycleInspectionMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public MotorcycleInspectionResponse getInspectionById(Long id) {
        log.debug("Fetching motorcycle inspection ID: {}", id);

        MotorcycleInspection inspection = motorcycleInspectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MotorcycleInspection", "id", id));

        return motorcycleInspectionMapper.toResponse(inspection);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MotorcycleInspectionResponse> getAllInspections(int page, int size) {
        log.debug("Fetching all motorcycle inspections - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("inspectionDate").descending());
        Page<MotorcycleInspection> inspectionsPage = motorcycleInspectionRepository.findAll(pageable);

        return buildPageResponse(inspectionsPage);
    }

    @Override
    public void deleteInspection(Long id) {
        log.debug("Deleting motorcycle inspection ID: {}", id);

        if (!motorcycleInspectionRepository.existsById(id)) {
            throw new ResourceNotFoundException("MotorcycleInspection", "id", id);
        }

        motorcycleInspectionRepository.deleteById(id);
        auditService.log(AuditCategory.INSPECTION, id, "DELETE",
                AuditLevel.HIGH, resolveCurrentEmployee(), "Motorcycle inspection deleted");
        log.info("Deleted motorcycle inspection ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MotorcycleInspectionResponse> getInspectionsByMotorcycleId(Long motorcycleId, int page, int size) {
        log.debug("Fetching inspections for motorcycle ID: {}", motorcycleId);

        if (!motorcycleRepository.existsById(motorcycleId)) {
            throw new ResourceNotFoundException("Motorcycle", "id", motorcycleId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("inspectionDate").descending());
        List<MotorcycleInspection> inspections = motorcycleInspectionRepository.findByMotorcycleIdOrderByInspectionDateDesc(motorcycleId);
        
        // Convert list to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), inspections.size());
        List<MotorcycleInspection> pageContent = inspections.subList(start, end);
        Page<MotorcycleInspection> inspectionsPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, inspections.size());

        return buildPageResponse(inspectionsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public MotorcycleInspectionResponse getLatestInspectionByMotorcycleId(Long motorcycleId) {
        log.debug("Fetching latest inspection for motorcycle ID: {}", motorcycleId);

        if (!motorcycleRepository.existsById(motorcycleId)) {
            throw new ResourceNotFoundException("Motorcycle", "id", motorcycleId);
        }

        MotorcycleInspection inspection = motorcycleInspectionRepository.findLatestInspectionForMotorcycle(motorcycleId)
                .orElseThrow(() -> new ResourceNotFoundException("MotorcycleInspection", "motorcycleId", motorcycleId));

        return motorcycleInspectionMapper.toResponse(inspection);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MotorcycleInspectionResponse> getInspectionsByInspectorId(Long inspectorId, int page, int size) {
        log.debug("Fetching inspections by inspector ID: {}", inspectorId);

        if (!employeeRepository.existsById(inspectorId)) {
            throw new ResourceNotFoundException("Employee", "id", inspectorId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("inspectionDate").descending());
        List<MotorcycleInspection> inspections = motorcycleInspectionRepository.findByInspectorId(inspectorId);
        
        // Convert list to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), inspections.size());
        List<MotorcycleInspection> pageContent = inspections.subList(start, end);
        Page<MotorcycleInspection> inspectionsPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, inspections.size());

        return buildPageResponse(inspectionsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MotorcycleInspectionResponse> getInspectionsByDateRange(LocalDate startDate, LocalDate endDate,
                                                                                 int page, int size) {
        log.debug("Fetching motorcycle inspections between {} and {}", startDate, endDate);

        Pageable pageable = PageRequest.of(page, size, Sort.by("inspectionDate").descending());
        List<MotorcycleInspection> inspections = motorcycleInspectionRepository.findByInspectionDateBetween(startDate, endDate);
        
        // Convert list to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), inspections.size());
        List<MotorcycleInspection> pageContent = inspections.subList(start, end);
        Page<MotorcycleInspection> inspectionsPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, inspections.size());

        return buildPageResponse(inspectionsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MotorcycleInspectionResponse> getFailedInspections(int page, int size) {
        log.debug("Fetching failed motorcycle inspections");

        Pageable pageable = PageRequest.of(page, size, Sort.by("inspectionDate").descending());
        List<MotorcycleInspection> inspections = motorcycleInspectionRepository.findByPassedFalse();
        
        // Convert list to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), inspections.size());
        List<MotorcycleInspection> pageContent = inspections.subList(start, end);
        Page<MotorcycleInspection> inspectionsPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, inspections.size());

        return buildPageResponse(inspectionsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MotorcycleInspectionResponse> getInspectionsRequiringRepair(int page, int size) {
        log.debug("Fetching motorcycle inspections requiring repair");

        Pageable pageable = PageRequest.of(page, size, Sort.by("inspectionDate").descending());
        List<MotorcycleInspection> inspections = motorcycleInspectionRepository.findByRequiresRepairTrue();
        
        // Convert list to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), inspections.size());
        List<MotorcycleInspection> pageContent = inspections.subList(start, end);
        Page<MotorcycleInspection> inspectionsPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, inspections.size());

        return buildPageResponse(inspectionsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MotorcycleInspectionResponse> getInspectionsWithAccidentHistory(int page, int size) {
        log.debug("Fetching motorcycle inspections with accident history");

        Pageable pageable = PageRequest.of(page, size, Sort.by("inspectionDate").descending());
        List<MotorcycleInspection> inspections = motorcycleInspectionRepository.findByHasAccidentHistoryTrue();
        
        // Convert list to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), inspections.size());
        List<MotorcycleInspection> pageContent = inspections.subList(start, end);
        Page<MotorcycleInspection> inspectionsPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, inspections.size());

        return buildPageResponse(inspectionsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MotorcycleInspectionResponse> getInspectionsByCondition(String condition, int page, int size) {
        log.debug("Fetching motorcycle inspections with condition: {}", condition);

        Pageable pageable = PageRequest.of(page, size, Sort.by("inspectionDate").descending());
        Page<MotorcycleInspection> inspectionsPage = motorcycleInspectionRepository.findByOverallCondition(condition, pageable);

        return buildPageResponse(inspectionsPage);
    }

    private Employee resolveCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof EmployeeUserDetails u) {
            return employeeRepository.getReferenceById(u.getId());
        }
        return null;
    }

    private PageResponse<MotorcycleInspectionResponse> buildPageResponse(Page<MotorcycleInspection> page) {
        List<MotorcycleInspectionResponse> content = motorcycleInspectionMapper.toResponseList(page.getContent());

        return PageResponse.<MotorcycleInspectionResponse>builder()
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
