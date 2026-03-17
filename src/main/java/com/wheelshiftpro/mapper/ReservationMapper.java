package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.ReservationRequest;
import com.wheelshiftpro.dto.response.ReservationResponse;
import com.wheelshiftpro.entity.Reservation;
import com.wheelshiftpro.utils.FileUrlBuilder;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Reservation entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, imports = { FileUrlBuilder.class })
public interface ReservationMapper {

    @Mapping(target = "carId", source = "car.id")
    @Mapping(target = "carVin", source = "car.vinNumber")
    @Mapping(target = "motorcycleId", source = "motorcycle.id")
    @Mapping(target = "motorcycleVin", source = "motorcycle.vinNumber")
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", source = "client.name")
    @Mapping(target = "clientEmail", source = "client.email")
    @Mapping(target = "reservationDocumentIds", expression = "java(FileUrlBuilder.splitToList(entity.getReservationDocumentIds()))")
    @Mapping(target = "reservationDocumentUrls", expression = "java(FileUrlBuilder.buildFileUrls(entity.getReservationDocumentIds()))")
    ReservationResponse toResponse(Reservation entity);

    List<ReservationResponse> toResponseList(List<Reservation> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "motorcycle", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "reservationDocumentIds", expression = "java(FileUrlBuilder.joinList(request.getReservationDocumentIds()))")
    Reservation toEntity(ReservationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "motorcycle", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "reservationDocumentIds", expression = "java(FileUrlBuilder.joinList(request.getReservationDocumentIds()))")
    void updateEntityFromRequest(ReservationRequest request, @MappingTarget Reservation entity);
}
