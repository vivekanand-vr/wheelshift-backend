package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.CarModelRequest;
import com.wheelshiftpro.dto.response.CarModelResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.entity.CarModel;
import com.wheelshiftpro.enums.FuelType;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.CarModelMapper;
import com.wheelshiftpro.repository.CarModelRepository;
import com.wheelshiftpro.service.CarModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CarModelServiceImpl implements CarModelService {

    private final CarModelRepository carModelRepository;
    private final CarModelMapper carModelMapper;

    @Override
    public CarModelResponse createCarModel(CarModelRequest request) {
        log.debug("Creating car model: {} {} {}", request.getMake(), request.getModel(), request.getVariant());
        
        if (carModelRepository.existsByMakeAndModelAndVariant(
                request.getMake(), request.getModel(), request.getVariant())) {
            throw new DuplicateResourceException("CarModel", "make-model-variant", 
                    request.getMake() + " " + request.getModel() + " " + request.getVariant());
        }

        CarModel carModel = carModelMapper.toEntity(request);
        CarModel saved = carModelRepository.save(carModel);
        
        log.info("Created car model with ID: {}", saved.getId());
        return carModelMapper.toResponse(saved);
    }

    @Override
    public CarModelResponse updateCarModel(Long id, CarModelRequest request) {
        log.debug("Updating car model ID: {}", id);
        
        CarModel carModel = carModelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CarModel", "id", id));

        carModelMapper.updateEntityFromRequest(request, carModel);
        CarModel updated = carModelRepository.save(carModel);
        
        log.info("Updated car model ID: {}", id);
        return carModelMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CarModelResponse getCarModelById(Long id) {
        log.debug("Fetching car model ID: {}", id);
        
        CarModel carModel = carModelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CarModel", "id", id));
        
        return carModelMapper.toResponse(carModel);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CarModelResponse> getAllCarModels(int page, int size) {
        log.debug("Fetching all car models - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("make", "model", "variant"));
        Page<CarModel> carModelsPage = carModelRepository.findAll(pageable);
        
        return buildPageResponse(carModelsPage);
    }

    @Override
    public void deleteCarModel(Long id) {
        log.debug("Deleting car model ID: {}", id);
        
        CarModel carModel = carModelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CarModel", "id", id));

        if (!carModel.getCars().isEmpty()) {
            throw new BusinessException(
                    "Cannot delete car model. Cars are associated with this model.", 
                    "CAR_MODEL_HAS_CARS");
        }

        carModelRepository.delete(carModel);
        log.info("Deleted car model ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CarModelResponse> searchCarModels(String make, String model, 
                                                          FuelType fuelType, String bodyType, 
                                                          int page, int size) {
        log.debug("Searching car models with filters");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("make", "model"));
        Page<CarModel> carModelsPage = carModelRepository.searchCarModels(
                make, model, fuelType, bodyType, pageable);
        
        return buildPageResponse(carModelsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllMakes() {
        log.debug("Fetching all makes");
        return carModelRepository.findDistinctMakes();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getModelsByMake(String make) {
        log.debug("Fetching models for make: {}", make);
        return carModelRepository.findDistinctModelsByMake(make);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getVariantsByMakeAndModel(String make, String model) {
        log.debug("Fetching variants for make: {}, model: {}", make, model);
        return carModelRepository.findDistinctVariantsByMakeAndModel(make, model);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuelType> getAllFuelTypes() {
        return Arrays.asList(FuelType.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllBodyTypes() {
        log.debug("Fetching all body types");
        return List.of("Sedan", "SUV", "Hatchback", "Coupe", "Convertible", "Wagon", "Truck", "Van");
    }

    private PageResponse<CarModelResponse> buildPageResponse(Page<CarModel> page) {
        List<CarModelResponse> content = carModelMapper.toResponseList(page.getContent());
        
        return PageResponse.<CarModelResponse>builder()
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
