package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.client.AIServiceClient;
import com.wheelshiftpro.dto.ai.SimilarVehicleDto;
import com.wheelshiftpro.dto.ai.SimilarVehiclesResponseDto;
import com.wheelshiftpro.dto.response.SimilarCarDto;
import com.wheelshiftpro.dto.response.SimilarCarsResponse;
import com.wheelshiftpro.dto.response.SimilarMotorcycleDto;
import com.wheelshiftpro.dto.response.SimilarMotorcyclesResponse;
import com.wheelshiftpro.entity.Car;
import com.wheelshiftpro.entity.Motorcycle;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.repository.CarRepository;
import com.wheelshiftpro.repository.MotorcycleRepository;
import com.wheelshiftpro.service.RecommendationService;
import com.wheelshiftpro.utils.FileUrlBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final CarRepository carRepository;
    private final MotorcycleRepository motorcycleRepository;
    private final AIServiceClient aiServiceClient;

    @Override
    @Transactional(readOnly = true)
    public SimilarCarsResponse getSimilarCars(Long carId, int limit) {
        carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", carId));

        Optional<SimilarVehiclesResponseDto> aiResult =
                aiServiceClient.getSimilarVehicles(carId, "car", limit);

        if (aiResult.isEmpty() || aiResult.get().suggestions() == null || aiResult.get().suggestions().isEmpty()) {
            return SimilarCarsResponse.builder()
                    .sourceCarId(carId)
                    .similarCars(Collections.emptyList())
                    .similaritiesAvailable(false)
                    .method(null)
                    .build();
        }

        SimilarVehiclesResponseDto aiResponse = aiResult.get();
        List<Long> suggestedIds = aiResponse.suggestions().stream()
                .map(SimilarVehicleDto::vehicleId)
                .collect(Collectors.toList());

        Map<Long, Car> carsById = carRepository.findAllById(suggestedIds).stream()
                .collect(Collectors.toMap(Car::getId, Function.identity()));

        Map<Long, SimilarVehicleDto> scoreMap = aiResponse.suggestions().stream()
                .collect(Collectors.toMap(SimilarVehicleDto::vehicleId, Function.identity()));

        List<SimilarCarDto> enriched = suggestedIds.stream()
                .filter(carsById::containsKey)
                .map(id -> {
                    Car car = carsById.get(id);
                    SimilarVehicleDto aiItem = scoreMap.get(id);
                    return SimilarCarDto.builder()
                            .id(car.getId())
                            .make(car.getCarModel().getMake())
                            .model(car.getCarModel().getModel())
                            .variant(car.getCarModel().getVariant())
                            .year(car.getYear())
                            .color(car.getColor())
                            .mileageKm(car.getMileageKm())
                            .sellingPrice(car.getSellingPrice())
                            .status(car.getStatus())
                            .storageLocationName(
                                    car.getStorageLocation() != null ? car.getStorageLocation().getName() : null)
                            .primaryImageUrl(FileUrlBuilder.buildFileUrl(car.getPrimaryImageId()))
                            .score(aiItem.score())
                            .reason(aiItem.reason())
                            .build();
                })
                .collect(Collectors.toList());

        return SimilarCarsResponse.builder()
                .sourceCarId(carId)
                .similarCars(enriched)
                .similaritiesAvailable(true)
                .method(aiResponse.method())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SimilarMotorcyclesResponse getSimilarMotorcycles(Long motorcycleId, int limit) {
        motorcycleRepository.findById(motorcycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Motorcycle", "id", motorcycleId));

        Optional<SimilarVehiclesResponseDto> aiResult =
                aiServiceClient.getSimilarVehicles(motorcycleId, "motorcycle", limit);

        if (aiResult.isEmpty() || aiResult.get().suggestions() == null || aiResult.get().suggestions().isEmpty()) {
            return SimilarMotorcyclesResponse.builder()
                    .sourceMotorcycleId(motorcycleId)
                    .similarMotorcycles(Collections.emptyList())
                    .similaritiesAvailable(false)
                    .method(null)
                    .build();
        }

        SimilarVehiclesResponseDto aiResponse = aiResult.get();
        List<Long> suggestedIds = aiResponse.suggestions().stream()
                .map(SimilarVehicleDto::vehicleId)
                .collect(Collectors.toList());

        Map<Long, Motorcycle> motorcyclesById = motorcycleRepository.findAllById(suggestedIds).stream()
                .collect(Collectors.toMap(Motorcycle::getId, Function.identity()));

        Map<Long, SimilarVehicleDto> scoreMap = aiResponse.suggestions().stream()
                .collect(Collectors.toMap(SimilarVehicleDto::vehicleId, Function.identity()));

        List<SimilarMotorcycleDto> enriched = suggestedIds.stream()
                .filter(motorcyclesById::containsKey)
                .map(id -> {
                    Motorcycle motorcycle = motorcyclesById.get(id);
                    SimilarVehicleDto aiItem = scoreMap.get(id);
                    return SimilarMotorcycleDto.builder()
                            .id(motorcycle.getId())
                            .make(motorcycle.getMotorcycleModel().getMake())
                            .model(motorcycle.getMotorcycleModel().getModel())
                            .variant(motorcycle.getMotorcycleModel().getVariant())
                            .year(motorcycle.getManufactureYear())
                            .color(motorcycle.getColor())
                            .mileageKm(motorcycle.getMileageKm())
                            .sellingPrice(motorcycle.getSellingPrice())
                            .status(motorcycle.getStatus())
                            .storageLocationName(
                                    motorcycle.getStorageLocation() != null
                                            ? motorcycle.getStorageLocation().getName()
                                            : null)
                            .primaryImageUrl(FileUrlBuilder.buildFileUrl(motorcycle.getPrimaryImageId()))
                            .score(aiItem.score())
                            .reason(aiItem.reason())
                            .build();
                })
                .collect(Collectors.toList());

        return SimilarMotorcyclesResponse.builder()
                .sourceMotorcycleId(motorcycleId)
                .similarMotorcycles(enriched)
                .similaritiesAvailable(true)
                .method(aiResponse.method())
                .build();
    }
}
