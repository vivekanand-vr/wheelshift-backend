package com.wheelshiftpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for event response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {

    private Long id;
    private String type;
    private String name;
    private Long carId;
    private String carVin;
    private Long motorcycleId;
    private String motorcycleVin;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<String> attachmentFileIds;
    private List<String> attachmentFileUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
