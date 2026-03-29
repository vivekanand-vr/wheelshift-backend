package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.response.AuditLogResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;

/**
 * Service for recording and querying audit events on domain entities.
 *
 * <p>All write operations (create, update, delete, move, status change) should call
 * {@link #log} so that a tamper-evident trail is maintained in {@code audit_logs}.
 */
public interface AuditService {

    /**
     * Persist one audit record.
     *
     * @param category    domain area of the affected entity (e.g. {@code CAR}, {@code MOTORCYCLE})
     * @param entityId    primary key of the affected entity; {@code null} for system events
     * @param action      operation performed, e.g. {@code "CREATE"}, {@code "UPDATE"},
     *                    {@code "DELETE"}, {@code "MOVE"}, {@code "STATUS_CHANGE"}
     * @param level       severity of the event
     * @param performedBy employee who triggered the action; {@code null} for system actions
     * @param details     optional human-readable description of what changed
     */
    void log(AuditCategory category, Long entityId, String action, AuditLevel level,
             Employee performedBy, String details);

    /**
     * Retrieve a paginated, filterable list of audit records.
     * All parameters are optional; omitting one removes that filter.
     *
     * @param category      filter by domain category
     * @param level         filter by severity level
     * @param action        filter by exact action string (case-insensitive)
     * @param entityId      filter by specific entity ID
     * @param performedById filter by the employee who performed the action
     * @param page          zero-based page number
     * @param size          page size
     */
    PageResponse<AuditLogResponse> getAuditLogs(AuditCategory category, AuditLevel level,
                                                String action, Long entityId,
                                                Long performedById, int page, int size);
}
