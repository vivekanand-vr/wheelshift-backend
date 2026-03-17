package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.EventRequest;
import com.wheelshiftpro.dto.response.EventResponse;
import com.wheelshiftpro.entity.Event;
import com.wheelshiftpro.utils.FileUrlBuilder;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Event entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = { FileUrlBuilder.class })
public interface EventMapper {

    /**
     * Converts Event entity to EventResponse DTO.
     */
    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "car.vinNumber", target = "carVin")
    @Mapping(source = "motorcycle.id", target = "motorcycleId")
    @Mapping(source = "motorcycle.vinNumber", target = "motorcycleVin")
    @Mapping(target = "attachmentFileIds", expression = "java(FileUrlBuilder.splitToList(event.getAttachmentFileIds()))")
    @Mapping(target = "attachmentFileUrls", expression = "java(FileUrlBuilder.buildFileUrls(event.getAttachmentFileIds()))")
    EventResponse toResponse(Event event);

    /**
     * Converts EventRequest DTO to Event entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "carId", target = "car.id")
    @Mapping(source = "motorcycleId", target = "motorcycle.id")
    @Mapping(target = "attachmentFileIds", expression = "java(FileUrlBuilder.joinList(request.getAttachmentFileIds()))")
    Event toEntity(EventRequest request);

    /**
     * Converts list of Event entities to list of EventResponse DTOs.
     */
    List<EventResponse> toResponseList(List<Event> events);

    /**
     * Updates Event entity from EventRequest DTO.
     * Ignores null values in the request.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "carId", target = "car.id")
    @Mapping(source = "motorcycleId", target = "motorcycle.id")
    @Mapping(target = "attachmentFileIds", expression = "java(FileUrlBuilder.joinList(request.getAttachmentFileIds()))")
    void updateEntityFromRequest(EventRequest request, @MappingTarget Event event);
}
