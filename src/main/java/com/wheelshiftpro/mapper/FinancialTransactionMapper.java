package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.FinancialTransactionRequest;
import com.wheelshiftpro.dto.response.FinancialTransactionResponse;
import com.wheelshiftpro.entity.FinancialTransaction;
import com.wheelshiftpro.utils.FileUrlBuilder;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for FinancialTransaction entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = { FileUrlBuilder.class })
public interface FinancialTransactionMapper {

    /**
     * Converts FinancialTransaction entity to FinancialTransactionResponse DTO.
     */
    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "car.vinNumber", target = "carVin")
    @Mapping(source = "motorcycle.id", target = "motorcycleId")
    @Mapping(source = "motorcycle.vinNumber", target = "motorcycleVin")
    @Mapping(target = "transactionFileIds", expression = "java(FileUrlBuilder.splitToList(transaction.getTransactionFileIds()))")
    @Mapping(target = "transactionFileUrls", expression = "java(FileUrlBuilder.buildFileUrls(transaction.getTransactionFileIds()))")
    FinancialTransactionResponse toResponse(FinancialTransaction transaction);

    /**
     * Converts FinancialTransactionRequest DTO to FinancialTransaction entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "carId", target = "car.id")
    @Mapping(source = "motorcycleId", target = "motorcycle.id")
    @Mapping(target = "transactionFileIds", expression = "java(FileUrlBuilder.joinList(request.getTransactionFileIds()))")
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
    @Mapping(source = "motorcycleId", target = "motorcycle.id")
    @Mapping(target = "transactionFileIds", expression = "java(FileUrlBuilder.joinList(request.getTransactionFileIds()))")
    void updateEntityFromRequest(FinancialTransactionRequest request, @MappingTarget FinancialTransaction transaction);
}
