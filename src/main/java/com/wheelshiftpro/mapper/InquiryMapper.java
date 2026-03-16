package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.InquiryRequest;
import com.wheelshiftpro.dto.response.InquiryResponse;
import com.wheelshiftpro.entity.Inquiry;
import com.wheelshiftpro.utils.FileUrlBuilder;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Inquiry entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, imports = { FileUrlBuilder.class })
public interface InquiryMapper {

    @Mapping(target = "carId", source = "car.id")
    @Mapping(target = "carVin", source = "car.vinNumber")
    @Mapping(target = "motorcycleId", source = "motorcycle.id")
    @Mapping(target = "motorcycleVin", source = "motorcycle.vinNumber")
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", source = "client.name")
    @Mapping(target = "clientEmail", source = "client.email")
    @Mapping(target = "assignedEmployeeId", source = "assignedEmployee.id")
    @Mapping(target = "assignedEmployeeName", source = "assignedEmployee.name")
    @Mapping(target = "attachmentFileIds", expression = "java(FileUrlBuilder.splitToList(entity.getAttachmentFileIds()))")
    @Mapping(target = "attachmentFileUrls", expression = "java(FileUrlBuilder.buildFileUrls(entity.getAttachmentFileIds()))")
    InquiryResponse toResponse(Inquiry entity);

    List<InquiryResponse> toResponseList(List<Inquiry> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "motorcycle", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "assignedEmployee", ignore = true)
    @Mapping(target = "responseDate", ignore = true)
    @Mapping(target = "attachmentFileIds", expression = "java(FileUrlBuilder.joinList(request.getAttachmentFileIds()))")
    Inquiry toEntity(InquiryRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "motorcycle", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "assignedEmployee", ignore = true)
    @Mapping(target = "attachmentFileIds", expression = "java(FileUrlBuilder.joinList(request.getAttachmentFileIds()))")
    void updateEntityFromRequest(InquiryRequest request, @MappingTarget Inquiry entity);
}
