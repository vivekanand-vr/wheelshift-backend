package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.TaskRequest;
import com.wheelshiftpro.dto.response.TaskResponse;
import com.wheelshiftpro.entity.Task;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Task entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {

    /**
     * Converts Task entity to TaskResponse DTO.
     */
    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "assignee.name", target = "assigneeName")
    TaskResponse toResponse(Task task);

    /**
     * Converts TaskRequest DTO to Task entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "assigneeId", target = "assignee.id")
    Task toEntity(TaskRequest request);

    /**
     * Converts list of Task entities to list of TaskResponse DTOs.
     */
    List<TaskResponse> toResponseList(List<Task> tasks);

    /**
     * Updates Task entity from TaskRequest DTO.
     * Ignores null values in the request.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "assigneeId", target = "assignee.id")
    void updateEntityFromRequest(TaskRequest request, @MappingTarget Task task);
}
