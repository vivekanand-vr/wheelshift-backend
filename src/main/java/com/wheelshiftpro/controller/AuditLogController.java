package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.AuditLogResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Read-only access to the system audit trail")
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(
            summary = "Get audit logs",
            description = "Returns a paginated, filterable list of all audit records. "
                    + "Filter by category (CAR, MOTORCYCLE, FINANCIAL_TRANSACTION, …), "
                    + "severity level (INFO, REGULAR, HIGH, CRITICAL), action, entity ID, "
                    + "or the employee who performed the action. "
                    + "Results are ordered newest-first. Accessible by SUPER_ADMIN and ADMIN only."
    )
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogs(
            @Parameter(description = "Filter by domain category (e.g. CAR, MOTORCYCLE, FINANCIAL_TRANSACTION)")
            @RequestParam(required = false) AuditCategory category,

            @Parameter(description = "Filter by severity level (INFO, REGULAR, HIGH, CRITICAL)")
            @RequestParam(required = false) AuditLevel level,

            @Parameter(description = "Filter by action (e.g. CREATE, UPDATE, DELETE, MOVE, STATUS_CHANGE)")
            @RequestParam(required = false) String action,

            @Parameter(description = "Filter by the ID of the affected entity")
            @RequestParam(required = false) Long entityId,

            @Parameter(description = "Filter by the ID of the employee who performed the action")
            @RequestParam(required = false) Long performedById,

            @Parameter(description = "Zero-based page number")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of records per page")
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<AuditLogResponse> result = auditService.getAuditLogs(
                category, level, action, entityId, performedById, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
