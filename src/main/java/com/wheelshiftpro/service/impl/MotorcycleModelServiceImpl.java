package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.MotorcycleModelRequest;
import com.wheelshiftpro.dto.response.MotorcycleModelResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.MotorcycleModel;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.FuelType;
import com.wheelshiftpro.enums.MotorcycleVehicleType;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.MotorcycleModelMapper;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.MotorcycleModelRepository;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.AuditService;
import com.wheelshiftpro.service.MotorcycleModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MotorcycleModelServiceImpl implements MotorcycleModelService {

    private final MotorcycleModelRepository motorcycleModelRepository;
    private final MotorcycleModelMapper motorcycleModelMapper;
    private final AuditService auditService;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MotorcycleModelResponse createMotorcycleModel(MotorcycleModelRequest request) {
        log.debug("Creating motorcycle model: {} {} {}", request.getMake(), request.getModel(), request.getVariant());
        
        if (motorcycleModelRepository.existsByMakeAndModelAndVariant(
                request.getMake(), request.getModel(), request.getVariant())) {
            throw new DuplicateResourceException("MotorcycleModel", "make-model-variant", 
                    request.getMake() + " " + request.getModel() + " " + request.getVariant());
        }

        MotorcycleModel motorcycleModel = motorcycleModelMapper.toEntity(request);
        MotorcycleModel saved = motorcycleModelRepository.save(motorcycleModel);
        
        log.info("Created motorcycle model with ID: {}", saved.getId());
        auditService.log(AuditCategory.MOTORCYCLE, saved.getId(), "CREATE", AuditLevel.REGULAR,
                resolveCurrentEmployee(), saved.getMake() + " " + saved.getModel() + " " + saved.getVariant());
        return motorcycleModelMapper.toResponse(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MotorcycleModelResponse updateMotorcycleModel(Long id, MotorcycleModelRequest request) {
        log.debug("Updating motorcycle model ID: {}", id);
        
        MotorcycleModel motorcycleModel = motorcycleModelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MotorcycleModel", "id", id));

        if (request.getMake() != null && request.getModel() != null && request.getVariant() != null) {
            if (motorcycleModelRepository.existsByMakeAndModelAndVariantAndIdNot(
                    request.getMake(), request.getModel(), request.getVariant(), id)) {
                throw new DuplicateResourceException("MotorcycleModel", "make-model-variant",
                        request.getMake() + " " + request.getModel() + " " + request.getVariant());
            }
        }

        motorcycleModelMapper.updateEntityFromRequest(request, motorcycleModel);
        MotorcycleModel updated = motorcycleModelRepository.save(motorcycleModel);
        
        log.info("Updated motorcycle model ID: {}", id);
        auditService.log(AuditCategory.MOTORCYCLE, updated.getId(), "UPDATE", AuditLevel.REGULAR,
                resolveCurrentEmployee(), null);
        return motorcycleModelMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public MotorcycleModelResponse getMotorcycleModelById(Long id) {
        log.debug("Fetching motorcycle model ID: {}", id);
        
        MotorcycleModel motorcycleModel = motorcycleModelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MotorcycleModel", "id", id));
        
        return motorcycleModelMapper.toResponse(motorcycleModel);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MotorcycleModelResponse> getAllMotorcycleModels(int page, int size) {
        log.debug("Fetching all motorcycle models - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("make", "model", "variant"));
        Page<MotorcycleModel> motorcycleModelsPage = motorcycleModelRepository.findAll(pageable);
        
        return buildPageResponse(motorcycleModelsPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMotorcycleModel(Long id) {
        log.debug("Deleting motorcycle model ID: {}", id);
        
        MotorcycleModel motorcycleModel = motorcycleModelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MotorcycleModel", "id", id));

        if (!motorcycleModel.getMotorcycles().isEmpty()) {
            throw new BusinessException(
                    "Cannot delete motorcycle model with associated vehicles.",
                    "MODEL_HAS_VEHICLES");
        }

        String label = motorcycleModel.getMake() + " " + motorcycleModel.getModel() + " " + motorcycleModel.getVariant();
        motorcycleModelRepository.delete(motorcycleModel);
        log.info("Deleted motorcycle model ID: {}", id);
        auditService.log(AuditCategory.MOTORCYCLE, id, "DELETE", AuditLevel.HIGH,
                resolveCurrentEmployee(), label);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MotorcycleModelResponse> searchMotorcycleModels(String make, String model, 
                                                                         FuelType fuelType, MotorcycleVehicleType vehicleType, 
                                                                         int page, int size) {
        log.debug("Searching motorcycle models with filters");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("make", "model"));
        Page<MotorcycleModel> motorcycleModelsPage = motorcycleModelRepository.searchMotorcycleModels(
                make, model, fuelType, vehicleType, pageable);
        
        return buildPageResponse(motorcycleModelsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllMakes() {
        log.debug("Fetching all makes");
        return motorcycleModelRepository.findDistinctMakes();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getModelsByMake(String make) {
        log.debug("Fetching models for make: {}", make);
        return motorcycleModelRepository.findDistinctModelsByMake(make);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getVariantsByMakeAndModel(String make, String model) {
        log.debug("Fetching variants for make: {}, model: {}", make, model);
        return motorcycleModelRepository.findDistinctVariantsByMakeAndModel(make, model);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuelType> getAllFuelTypes() {
        return Arrays.asList(FuelType.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MotorcycleVehicleType> getAllVehicleTypes() {
        return Arrays.asList(MotorcycleVehicleType.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MotorcycleModelResponse> getActiveModels() {
        log.debug("Fetching active motorcycle models");
        List<MotorcycleModel> activeModels = motorcycleModelRepository.findByIsActiveTrue();
        return motorcycleModelMapper.toResponseList(activeModels);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return motorcycleModelRepository.existsById(id);
    }

    private PageResponse<MotorcycleModelResponse> buildPageResponse(Page<MotorcycleModel> page) {
        List<MotorcycleModelResponse> content = motorcycleModelMapper.toResponseList(page.getContent());
        
        return PageResponse.<MotorcycleModelResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }

    /** Returns the Employee proxy for the currently authenticated user, or null for system actions. */
    private Employee resolveCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof EmployeeUserDetails userDetails) {
            return employeeRepository.getReferenceById(userDetails.getId());
        }
        return null;
    }
}
