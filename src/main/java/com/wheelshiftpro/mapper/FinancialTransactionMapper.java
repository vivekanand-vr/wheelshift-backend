package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.FinancialTransactionRequest;
import com.wheelshiftpro.dto.response.FinancialTransactionResponse;
import com.wheelshiftpro.entity.FinancialTransaction;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for FinancialTransaction entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FinancialTransactionMapper {

    /**
     * Converts FinancialTransaction entity to FinancialTransactionResponse DTO.
     */
    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "car.vinNumber", target = "carVin")
    FinancialTransactionResponse toResponse(FinancialTransaction transaction);

    /**
     * Converts FinancialTransactionRequest DTO to FinancialTransaction entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "carId", target = "car.id")
    FinancialTransaction toEntity(FinancialTransactionRequest request);

    /**
     * Converts list of FinancialTransaction entities to list of FinancialTransactionResponse DTOs.
     */
    List<FinancialTransactionResponse> toResponseList(List<FinancialTransaction> transactions);

    /**
     * Updates FinancialTransaction entity from FinancialTransactionRequest DTO.
     * Ignores null values in the request.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "carId", target = "car.id")
    void updateEntityFromRequest(FinancialTransactionRequest request, @MappingTarget FinancialTransaction transaction);
}
