package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.CarInspectionRequest;
import com.wheelshiftpro.dto.response.CarInspectionResponse;
import com.wheelshiftpro.dto.response.PageResponse;
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
import com.wheelshiftpro.service.CarInspectionService;
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
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class CarInspectionServiceImpl implements CarInspectionService {

    private final CarInspectionRepository carInspectionRepository;
    private final CarInspectionMapper carInspectionMapper;
    private final CarRepository carRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    @Override
    public CarInspectionResponse createInspection(CarInspectionRequest request) {
        log.debug("Creating inspection for car ID: {}", request.getCarId());

        if (!carRepository.existsById(request.getCarId())) {
            throw new ResourceNotFoundException("Car", "id", request.getCarId());
        }

        if (request.getInspectionDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Inspection date cannot be in the future", "FUTURE_INSPECTION_DATE");
        }

        CarInspection inspection = carInspectionMapper.toEntity(request);
        inspection.setCar(carRepository.getReferenceById(request.getCarId()));
        CarInspection saved = carInspectionRepository.save(inspection);

        auditService.log(AuditCategory.INSPECTION, saved.getId(), "CREATE",
                AuditLevel.REGULAR, resolveCurrentEmployee(),
                "Car ID: " + request.getCarId() + ", Date: " + saved.getInspectionDate());
        log.info("Created inspection with ID: {}", saved.getId());
        return carInspectionMapper.toResponse(saved);
    }

    @Override
    public CarInspectionResponse updateInspection(Long id, CarInspectionRequest request) {
        log.debug("Updating inspection ID: {}", id);

        CarInspection inspection = carInspectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CarInspection", "id", id));

        if (request.getInspectionDate() != null && request.getInspectionDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Inspection date cannot be in the future", "FUTURE_INSPECTION_DATE");
        }

        carInspectionMapper.updateEntityFromRequest(request, inspection);
        CarInspection updated = carInspectionRepository.save(inspection);

        auditService.log(AuditCategory.INSPECTION, id, "UPDATE",
                AuditLevel.REGULAR, resolveCurrentEmployee(), "Car inspection updated");
        log.info("Updated inspection ID: {}", id);
        return carInspectionMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CarInspectionResponse getInspectionById(Long id) {
        log.debug("Fetching inspection ID: {}", id);

        CarInspection inspection = carInspectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CarInspection", "id", id));

        return carInspectionMapper.toResponse(inspection);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CarInspectionResponse> getAllInspections(int page, int size) {
        log.debug("Fetching all inspections - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("inspectionDate").descending());
        Page<CarInspection> inspectionsPage = carInspectionRepository.findAll(pageable);

        return buildPageResponse(inspectionsPage);
    }

    @Override
    public void deleteInspection(Long id) {
        log.debug("Deleting inspection ID: {}", id);

        if (!carInspectionRepository.existsById(id)) {
            throw new ResourceNotFoundException("CarInspection", "id", id);
        }

        carInspectionRepository.deleteById(id);
        auditService.log(AuditCategory.INSPECTION, id, "DELETE",
                AuditLevel.HIGH, resolveCurrentEmployee(), "Car inspection deleted");
        log.info("Deleted inspection ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CarInspectionResponse> getInspectionsByCarId(Long carId, int page, int size) {
        log.debug("Fetching inspections for car ID: {}", carId);

        if (!carRepository.existsById(carId)) {
            throw new ResourceNotFoundException("Car", "id", carId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("inspectionDate").descending());
        Page<CarInspection> inspectionsPage = carInspectionRepository.findByCarId(carId, pageable);

        return buildPageResponse(inspectionsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CarInspectionResponse getLatestInspectionByCarId(Long carId) {
        log.debug("Fetching latest inspection for car ID: {}", carId);

        if (!carRepository.existsById(carId)) {
            throw new ResourceNotFoundException("Car", "id", carId);
        }

        CarInspection inspection = carInspectionRepository.findLatestByCarId(carId)
                .orElseThrow(() -> new ResourceNotFoundException("CarInspection", "carId", carId));

        return carInspectionMapper.toResponse(inspection);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CarInspectionResponse> getInspectionsByEmployeeId(Long employeeId, int page, int size) {
        log.debug("Fetching inspections by employee ID: {}", employeeId);

        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("inspectionDate").descending());
        // Note: CarInspection entity uses inspectorName field, not employee relationship
        // This method returns empty results as there's no employee relationship
        Page<CarInspection> inspectionsPage = Page.empty(pageable);

        return buildPageResponse(inspectionsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CarInspectionResponse> getInspectionsByDateRange(LocalDate startDate, LocalDate endDate,
                                                                          int page, int size) {
        log.debug("Fetching inspections between {} and {}", startDate, endDate);

        Pageable pageable = PageRequest.of(page, size, Sort.by("inspectionDate").descending());
        Page<CarInspection> inspectionsPage = carInspectionRepository.findByInspectionDateBetween(
                startDate, endDate, pageable);

        return buildPageResponse(inspectionsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<?> getCarsRequiringInspection(int daysSinceLastInspection, int page, int size) {
        log.debug("Fetching cars requiring inspection (last inspection > {} days ago)", daysSinceLastInspection);

        // Return empty page response for now - implementation requires complex query
        return PageResponse.builder()
                .content(Collections.emptyList())
                .pageNumber(page)
                .pageSize(size)
                .totalElements(0L)
                .totalPages(0)
                .last(true)
                .first(true)
                .build();
    }

    private Employee resolveCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof EmployeeUserDetails u) {
            return employeeRepository.getReferenceById(u.getId());
        }
        return null;
    }

    private PageResponse<CarInspectionResponse> buildPageResponse(Page<CarInspection> page) {
        List<CarInspectionResponse> content = carInspectionMapper.toResponseList(page.getContent());

        return PageResponse.<CarInspectionResponse>builder()
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
