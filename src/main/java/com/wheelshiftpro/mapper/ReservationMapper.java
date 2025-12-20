package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.ReservationRequest;
import com.wheelshiftpro.dto.response.ReservationResponse;
import com.wheelshiftpro.entity.Reservation;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Reservation entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReservationMapper {

    @Mapping(target = "carId", source = "car.id")
    @Mapping(target = "carVin", source = "car.vinNumber")
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", source = "client.name")
    @Mapping(target = "clientEmail", source = "client.email")
    ReservationResponse toResponse(Reservation entity);

    List<ReservationResponse> toResponseList(List<Reservation> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "client", ignore = true)
    Reservation toEntity(ReservationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "client", ignore = true)
    void updateEntityFromRequest(ReservationRequest request, @MappingTarget Reservation entity);
}
