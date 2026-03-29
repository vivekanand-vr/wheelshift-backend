package com.wheelshiftpro.dto.response;

import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {

    private Long id;
    private AuditCategory category;
    private Long entityId;
    private String action;
    private AuditLevel level;
    private Long performedById;
    private String performedByName;
    private String details;
    private LocalDateTime createdAt;
}
