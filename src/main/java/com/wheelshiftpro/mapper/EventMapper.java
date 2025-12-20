package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.EventRequest;
import com.wheelshiftpro.dto.response.EventResponse;
import com.wheelshiftpro.entity.Event;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Event entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {

    /**
     * Converts Event entity to EventResponse DTO.
     */
    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "car.vinNumber", target = "carVin")
    EventResponse toResponse(Event event);

    /**
     * Converts EventRequest DTO to Event entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "carId", target = "car.id")
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
    void updateEntityFromRequest(EventRequest request, @MappingTarget Event event);
}
