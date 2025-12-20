package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.ClientRequest;
import com.wheelshiftpro.dto.response.ClientResponse;
import com.wheelshiftpro.entity.Client;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Client entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClientMapper {

    ClientResponse toResponse(Client entity);

    List<ClientResponse> toResponseList(List<Client> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "totalPurchases", constant = "0")
    @Mapping(target = "inquiries", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "sales", ignore = true)
    Client toEntity(ClientRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "totalPurchases", ignore = true)
    @Mapping(target = "lastPurchase", ignore = true)
    @Mapping(target = "inquiries", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "sales", ignore = true)
    void updateEntityFromRequest(ClientRequest request, @MappingTarget Client entity);
}
